package dbfw.cardgame.undertale;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;

/**
 * Mini-juego de esquive en tiempo real, al estilo de las peleas de Undertale: el jugador mueve
 * un corazon con las flechas del teclado dentro de una caja mientras "balas" (proyectiles)
 * cruzan la pantalla; cada bala que toca al corazon quita una vida de esta fase (o consume un
 * escudo, si el jugador tiene alguno preparado por combo defensivo).
 * <p>
 * Representa, en el hibrido Undertale/Slay the Spire de este juego, el ataque del Lider CPU en
 * su propio turno: en vez de resolverse por comparacion de poder, el jugador humano lo esquiva
 * (o no) en tiempo real. Se usa desde {@code CardBattleFrame} dentro de un {@code JDialog}
 * modal: como los dialogos modales de Swing siguen despachando eventos (incluyendo los del
 * {@link Timer} de este panel) mientras estan visibles, el metodo que los abre puede esperar
 * de forma sincrona a que la fase termine y despues leer {@link #getGolpesRecibidos()}.
 * <p>
 * Sobre el area de esquive se dibuja una "zona del jefe" con un stickman animado, inspirada en
 * la pelea de Undyne the Undying: se mueve con un ligero vaiven constante, se ilumina en rojo y
 * muestra una linea de advertencia antes de disparar su ataque mas fuerte (patron de "lanzas"),
 * y se dibuja un letrero con una frase corta segun lo que esta haciendo. Los patrones de balas
 * rotan entre tres variantes (ver {@link #tick()}): la lluvia normal de fondo, paredes de lanzas
 * telegrafiadas con un hueco para esquivar, y oleadas de balas con movimiento ondulado.
 */
public class PanelEsquive extends JPanel {
    private static final int ANCHO = 380;
    /** Alto de la zona de esquive (balas y corazon); coincide con el area de juego original. */
    private static final int ALTO_ARENA = 260;
    /** Alto de la franja superior donde se anima el jefe (el Lider CPU). */
    private static final int ALTO_JEFE = 90;
    private static final int ALTO_TOTAL = ALTO_JEFE + ALTO_ARENA;

    private static final int RADIO_CORAZON = 8;
    private static final double VELOCIDAD_CORAZON = 4.0;
    private static final int MS_POR_TICK = 16;
    private static final long INVULNERABILIDAD_MS = 700;

    /** Cada cuanto se decide el siguiente patron especial (lanzas u ondas), en milisegundos. */
    private static final long CICLO_PATRON_MS = 2600;
    /** Cuanto dura la advertencia antes de que disparen las lanzas, en milisegundos. */
    private static final long TELEGRAFO_MS = 550;

    private final int duracionMs;
    private final int intervaloSpawnMinMs;
    private final int intervaloSpawnMaxMs;
    private final double velocidadBalaMin;
    private final double velocidadBalaMax;
    private final int escudosIniciales;

    private final List<Bala> balas = new ArrayList<>();
    private final Random random = new Random();
    private final Timer timerJuego;

    private double hx = ANCHO / 2.0;
    private double hy = ALTO_JEFE + ALTO_ARENA / 2.0;
    private boolean arriba, abajo, izquierda, derecha;

    private long tiempoTranscurridoMs = 0;
    private long proximoSpawnMs = 0;
    private long invulnerableHastaMs = 0;
    private int escudosRestantes;
    private int golpesRecibidos = 0;
    private boolean terminado = false;
    private Runnable alTerminar;

    // Estado de la animacion y los patrones del jefe (ver dibujarJefe() y tick()).
    private long ultimoCicloIniciado = -1;
    private boolean jefeAtacando = false;
    private long jefeLungeHastaMs = 0;
    private boolean lanzasTelegrafiando = false;
    private int lanzasLado = 0;
    private double lanzasHuecoCentro = 0.5;
    private long lanzasDispararEnMs = -1;

    /**
     * Crea el panel de esquive con los parametros de dificultad de esta fase.
     *
     * @param duracionMs          duracion total de la fase, en milisegundos
     * @param intervaloSpawnMinMs tiempo minimo entre balas nuevas, en milisegundos
     * @param intervaloSpawnMaxMs tiempo maximo entre balas nuevas, en milisegundos
     * @param velocidadBalaMin    velocidad minima de una bala (pixeles por tick de 16ms)
     * @param velocidadBalaMax    velocidad maxima de una bala (pixeles por tick de 16ms)
     * @param escudosIniciales    golpes que se absorben sin quitar vida (por combo defensivo)
     */
    public PanelEsquive(int duracionMs, int intervaloSpawnMinMs, int intervaloSpawnMaxMs,
                         double velocidadBalaMin, double velocidadBalaMax, int escudosIniciales) {
        this.duracionMs = duracionMs;
        this.intervaloSpawnMinMs = intervaloSpawnMinMs;
        this.intervaloSpawnMaxMs = intervaloSpawnMaxMs;
        this.velocidadBalaMin = velocidadBalaMin;
        this.velocidadBalaMax = velocidadBalaMax;
        this.escudosIniciales = escudosIniciales;
        this.escudosRestantes = escudosIniciales;
        setPreferredSize(new Dimension(ANCHO, ALTO_TOTAL));
        setBackground(Color.BLACK);
        setFocusable(true);
        configurarControles();
        timerJuego = new Timer(MS_POR_TICK, e -> tick());
    }

    /** Registra las flechas del teclado (WHEN_IN_FOCUSED_WINDOW: funcionan aunque el foco lo tenga otro componente de la ventana). */
    private void configurarControles() {
        registrarTecla(KeyEvent.VK_UP, "arriba");
        registrarTecla(KeyEvent.VK_DOWN, "abajo");
        registrarTecla(KeyEvent.VK_LEFT, "izquierda");
        registrarTecla(KeyEvent.VK_RIGHT, "derecha");
    }

    private void registrarTecla(int codigo, String nombre) {
        JComponent panel = this;
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(codigo, 0, false), "presiona-" + nombre);
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(codigo, 0, true), "suelta-" + nombre);
        panel.getActionMap().put("presiona-" + nombre, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                setDireccion(nombre, true);
            }
        });
        panel.getActionMap().put("suelta-" + nombre, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                setDireccion(nombre, false);
            }
        });
    }

    private void setDireccion(String nombre, boolean valor) {
        switch (nombre) {
            case "arriba": arriba = valor; break;
            case "abajo": abajo = valor; break;
            case "izquierda": izquierda = valor; break;
            case "derecha": derecha = valor; break;
            default: break;
        }
    }

    /**
     * Arranca la fase: pide el foco de teclado y comienza el bucle de juego (60 fps aprox.).
     * Cuando la fase termina (se acaba el tiempo), se ejecuta el callback indicado; en ese
     * punto {@link #getGolpesRecibidos()} ya tiene el resultado final.
     */
    public void iniciar(Runnable alTerminar) {
        this.alTerminar = alTerminar;
        requestFocusInWindow();
        proximoSpawnMs = intervaloSpawnMinMs;
        timerJuego.start();
    }

    /**
     * Un paso del bucle de juego: mover corazon y balas, decidir el patron especial del ciclo
     * actual (lanzas u ondas, estilo Undyne), generar la lluvia de balas de fondo, revisar
     * colisiones y comprobar si la fase ya termino.
     */
    private void tick() {
        if (terminado) {
            return;
        }
        tiempoTranscurridoMs += MS_POR_TICK;
        moverCorazon();
        actualizarPatronDelJefe();

        if (tiempoTranscurridoMs >= proximoSpawnMs) {
            generarBala();
            int rango = Math.max(1, intervaloSpawnMaxMs - intervaloSpawnMinMs);
            proximoSpawnMs = tiempoTranscurridoMs + intervaloSpawnMinMs + random.nextInt(rango);
        }

        for (Bala b : balas) {
            b.mover();
        }
        balas.removeIf(b -> b.fueraDeArea(ANCHO, ALTO_TOTAL));
        revisarColisiones();

        if (tiempoTranscurridoMs >= duracionMs) {
            finalizar();
        }
        repaint();
    }

    private void moverCorazon() {
        double dx = (derecha ? 1 : 0) - (izquierda ? 1 : 0);
        double dy = (abajo ? 1 : 0) - (arriba ? 1 : 0);
        hx = clamp(hx + dx * VELOCIDAD_CORAZON, RADIO_CORAZON, ANCHO - RADIO_CORAZON);
        hy = clamp(hy + dy * VELOCIDAD_CORAZON, ALTO_JEFE + RADIO_CORAZON, ALTO_TOTAL - RADIO_CORAZON);
    }

    private double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    /**
     * Decide, cada {@link #CICLO_PATRON_MS}, si el jefe lanza uno de sus dos ataques especiales
     * (0 = solo sigue la lluvia normal, 1 = telegrafia y dispara una pared de lanzas con un
     * hueco, 2 = lanza una oleada de balas onduladas), y dispara las lanzas cuando termina su
     * tiempo de advertencia.
     */
    private void actualizarPatronDelJefe() {
        long ciclo = tiempoTranscurridoMs / CICLO_PATRON_MS;
        if (ciclo != ultimoCicloIniciado) {
            ultimoCicloIniciado = ciclo;
            int patron = (int) (ciclo % 3);
            if (patron == 1) {
                iniciarTelegrafoLanzas();
            } else if (patron == 2) {
                generarOleadaOndas();
                jefeAtacando = true;
            } else {
                jefeAtacando = false;
            }
        }
        if (lanzasDispararEnMs >= 0 && tiempoTranscurridoMs >= lanzasDispararEnMs) {
            dispararLanzas();
            lanzasDispararEnMs = -1;
        }
    }

    /** Genera una bala nueva desde un borde aleatorio, apuntando hacia una zona cercana al centro de la arena. */
    private void generarBala() {
        double velocidad = velocidadBalaMin + random.nextDouble() * (velocidadBalaMax - velocidadBalaMin);
        int lado = random.nextInt(4); // 0=arriba, 1=abajo, 2=izquierda, 3=derecha
        double origenX, origenY;
        switch (lado) {
            case 0: origenX = random.nextInt(ANCHO); origenY = ALTO_JEFE - 10; break;
            case 1: origenX = random.nextInt(ANCHO); origenY = ALTO_TOTAL + 10; break;
            case 2: origenX = -10; origenY = ALTO_JEFE + random.nextInt(ALTO_ARENA); break;
            default: origenX = ANCHO + 10; origenY = ALTO_JEFE + random.nextInt(ALTO_ARENA); break;
        }
        // Apunta hacia un punto aleatorio dentro de la arena (no siempre el centro exacto), para
        // que las trayectorias varien y no sean todas paralelas.
        double destinoX = ANCHO * 0.2 + random.nextDouble() * ANCHO * 0.6;
        double destinoY = ALTO_JEFE + ALTO_ARENA * 0.2 + random.nextDouble() * ALTO_ARENA * 0.6;
        double dx = destinoX - origenX;
        double dy = destinoY - origenY;
        double distancia = Math.max(1, Math.sqrt(dx * dx + dy * dy));
        double vx = dx / distancia * velocidad;
        double vy = dy / distancia * velocidad;
        balas.add(new Bala(origenX, origenY, vx, vy, 6));
    }

    /**
     * Empieza la advertencia del ataque de lanzas: elige un borde y un hueco seguro al azar, y
     * programa el disparo real para {@link #TELEGRAFO_MS} despues (ver {@link #dispararLanzas()}).
     */
    private void iniciarTelegrafoLanzas() {
        lanzasLado = random.nextInt(4);
        lanzasHuecoCentro = 0.15 + random.nextDouble() * 0.7;
        lanzasTelegrafiando = true;
        jefeAtacando = true;
        lanzasDispararEnMs = tiempoTranscurridoMs + TELEGRAFO_MS;
    }

    /**
     * Dispara una pared de lanzas desde el borde telegrafiado, dejando un hueco seguro cerca de
     * {@link #lanzasHuecoCentro} (estilo las paredes de lanzas de Undyne the Undying).
     */
    private void dispararLanzas() {
        lanzasTelegrafiando = false;
        double velocidad = velocidadBalaMax * 1.3;
        int cantidad = 7;
        double huecoAncho = 0.16;
        for (int i = 0; i < cantidad; i++) {
            double frac = (i + 0.5) / cantidad;
            if (Math.abs(frac - lanzasHuecoCentro) < huecoAncho) {
                continue; // hueco seguro: aqui no sale ninguna lanza
            }
            double vx2, vy2, x0, y0;
            switch (lanzasLado) {
                case 0: x0 = frac * ANCHO; y0 = ALTO_JEFE - 20; vx2 = 0; vy2 = velocidad; break;
                case 1: x0 = frac * ANCHO; y0 = ALTO_TOTAL + 20; vx2 = 0; vy2 = -velocidad; break;
                case 2: x0 = -20; y0 = ALTO_JEFE + frac * ALTO_ARENA; vx2 = velocidad; vy2 = 0; break;
                default: x0 = ANCHO + 20; y0 = ALTO_JEFE + frac * ALTO_ARENA; vx2 = -velocidad; vy2 = 0; break;
            }
            balas.add(new Bala(x0, y0, vx2, vy2, 6, true));
        }
        jefeLungeHastaMs = tiempoTranscurridoMs + 260;
    }

    /**
     * Genera una oleada de 3 balas con movimiento ondulado que cruzan la arena de lado a lado
     * (estilo el ataque de onda de Undyne), escalonadas en altura y en fase para que su
     * oscilacion no coincida.
     */
    private void generarOleadaOndas() {
        boolean desdeIzquierda = random.nextBoolean();
        double velocidad = (velocidadBalaMin + velocidadBalaMax) / 2.0;
        double vx = desdeIzquierda ? velocidad : -velocidad;
        double x0 = desdeIzquierda ? -20 : ANCHO + 20;
        for (int i = 0; i < 3; i++) {
            double y0 = ALTO_JEFE + ALTO_ARENA * (0.25 + i * 0.25);
            double fase = i * (Math.PI / 2);
            balas.add(new Bala(x0, y0, vx, 0, 6, 26, 0.12, fase));
        }
        jefeLungeHastaMs = tiempoTranscurridoMs + 260;
    }

    private void revisarColisiones() {
        if (tiempoTranscurridoMs < invulnerableHastaMs) {
            return;
        }
        for (Bala b : balas) {
            if (b.chocaCon(hx, hy, RADIO_CORAZON)) {
                if (escudosRestantes > 0) {
                    escudosRestantes--;
                } else {
                    golpesRecibidos++;
                }
                invulnerableHastaMs = tiempoTranscurridoMs + INVULNERABILIDAD_MS;
                break;
            }
        }
    }

    private void finalizar() {
        terminado = true;
        timerJuego.stop();
        if (alTerminar != null) {
            alTerminar.run();
        }
    }

    /** @return los golpes que el corazon recibio en esta fase (ya sin contar los absorbidos por escudos). */
    public int getGolpesRecibidos() {
        return golpesRecibidos;
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        dibujarJefe(g);

        g.setColor(Color.WHITE);
        g.drawRect(1, ALTO_JEFE + 1, ANCHO - 3, ALTO_ARENA - 3);

        for (Bala b : balas) {
            dibujarBala(g, b);
        }

        boolean parpadeoInvulnerable = tiempoTranscurridoMs < invulnerableHastaMs && (tiempoTranscurridoMs / 100) % 2 == 0;
        g.setColor(parpadeoInvulnerable ? Color.WHITE : Color.RED);
        g.fill(formaCorazon(hx, hy, RADIO_CORAZON));

        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 13));
        int segundosRestantes = Math.max(0, (int) Math.ceil((duracionMs - tiempoTranscurridoMs) / 1000.0));
        g.drawString("Tiempo: " + segundosRestantes + "s", 8, ALTO_TOTAL - 26);
        g.drawString("Golpes recibidos: " + golpesRecibidos, 8, ALTO_TOTAL - 10);
        if (escudosIniciales > 0) {
            g.drawString("Escudos: " + escudosRestantes, ANCHO - 90, ALTO_TOTAL - 10);
        }
    }

    /** Dibuja una bala: un circulo normal, o una lanza alargada y girada segun su direccion si {@code isLanza()}. */
    private void dibujarBala(Graphics2D g, Bala b) {
        if (b.isLanza()) {
            g.setColor(new Color(255, 160, 60));
            double angulo = Math.atan2(b.getVy(), b.getVx());
            AffineTransform anterior = g.getTransform();
            g.translate(b.getX(), b.getY());
            g.rotate(angulo);
            g.fillRoundRect(-22, -5, 44, 10, 6, 6);
            g.setTransform(anterior);
        } else {
            int r = b.getRadio();
            g.setColor(new Color(255, 140, 60));
            g.fillOval((int) (b.getX() - r), (int) (b.getY() - r), r * 2, r * 2);
        }
    }

    /**
     * Dibuja la zona del jefe (el Lider CPU): un stickman rojo con una lanza en la mano que se
     * balancea constantemente, se ilumina y "embiste" cuando dispara un ataque especial, y
     * muestra un letrero de dialogo corto segun lo que esta haciendo (estilo Undyne the Undying).
     */
    private void dibujarJefe(Graphics2D g) {
        g.setColor(new Color(40, 5, 5));
        g.fillRect(0, 0, ANCHO, ALTO_JEFE);
        g.setColor(new Color(90, 20, 20));
        g.drawLine(0, ALTO_JEFE - 1, ANCHO, ALTO_JEFE - 1);

        double bob = Math.sin(tiempoTranscurridoMs / 180.0) * 4;
        boolean lungeando = tiempoTranscurridoMs < jefeLungeHastaMs;
        double cx = ANCHO / 2.0;
        double cy = ALTO_JEFE / 2.0 + bob + (lungeando ? 6 : 0);

        boolean resplandor = lanzasTelegrafiando || jefeAtacando;
        if (resplandor) {
            float alpha = (float) (0.35 + 0.35 * Math.abs(Math.sin(tiempoTranscurridoMs / 90.0)));
            g.setColor(new Color(255, 60, 60, (int) (alpha * 255)));
            g.fillOval((int) (cx - 30), (int) (cy - 30), 60, 60);
        }

        g.setStroke(new BasicStroke(3f));
        g.setColor(Color.WHITE);
        g.fillOval((int) (cx - 10), (int) (cy - 26), 20, 20);
        g.drawLine((int) cx, (int) (cy - 6), (int) cx, (int) (cy + 18));
        g.drawLine((int) cx, (int) cy, (int) (cx - 14), (int) (cy + 8));
        g.drawLine((int) cx, (int) cy, (int) (cx + 16), (int) (cy - 14));
        g.drawLine((int) cx, (int) (cy + 18), (int) (cx - 10), (int) (cy + 34));
        g.drawLine((int) cx, (int) (cy + 18), (int) (cx + 10), (int) (cy + 34));
        g.setColor(new Color(255, 210, 120));
        g.drawLine((int) (cx + 16), (int) (cy - 14), (int) (cx + 34), (int) (cy - 30));

        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        g.setColor(Color.WHITE);
        String texto = lanzasTelegrafiando ? "¡Prepárate!" : (jefeAtacando ? "¡Esquiva esto!" : "¡Nadie escapa de mi ataque!");
        g.drawString(texto, 10, 16);

        if (lanzasTelegrafiando) {
            g.setColor(new Color(255, 60, 60));
            g.setStroke(new BasicStroke(3f));
            switch (lanzasLado) {
                case 0: g.drawLine(0, ALTO_JEFE, ANCHO, ALTO_JEFE); break;
                case 1: g.drawLine(0, ALTO_TOTAL - 1, ANCHO, ALTO_TOTAL - 1); break;
                case 2: g.drawLine(1, ALTO_JEFE, 1, ALTO_TOTAL); break;
                default: g.drawLine(ANCHO - 1, ALTO_JEFE, ANCHO - 1, ALTO_TOTAL); break;
            }
        }
    }

    /** Dibuja una forma de corazon simple (dos lobulos + una punta) centrada en (cx, cy). */
    private Polygon formaCorazon(double cx, double cy, int r) {
        Polygon p = new Polygon();
        p.addPoint((int) cx, (int) (cy + r));
        p.addPoint((int) (cx - r), (int) (cy - r * 0.2));
        p.addPoint((int) (cx - r * 0.5), (int) (cy - r));
        p.addPoint((int) cx, (int) (cy - r * 0.4));
        p.addPoint((int) (cx + r * 0.5), (int) (cy - r));
        p.addPoint((int) (cx + r), (int) (cy - r * 0.2));
        return p;
    }
}
