package dbfw.cardgame.undertale;

import dbfw.cardgame.TemaUndertale;
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
 * Minijuego de esquive en tiempo real al estilo Undertale:
 * el jugador mueve un corazon para esquivar balas.
 * Representa el ataque del lider de la CPU en su turno.
 * Los patrones rotan entre lluvia, lanzas, ondas, cruz y espiral.
 * El jefe cambia de pose y animacion segun el ataque,
 * inspirado en Undyne the Undying.
 * En las lanzas, se dibuja un indicador rojo/verde por carril
 * para avisar por donde van a salir.
 */
public class PanelEsquive extends JPanel {
    private static final int ANCHO = 380;
    /** Alto de la arena donde se mueve el corazon y pasan las balas. */
    private static final int ALTO_ARENA = 260;
    /** Alto de la franja superior donde se dibuja el jefe. */
    private static final int ALTO_JEFE = 90;
    private static final int ALTO_TOTAL = ALTO_JEFE + ALTO_ARENA;

    private static final int RADIO_CORAZON = 8;
    private static final double VELOCIDAD_CORAZON = 4.0;
    private static final int MS_POR_TICK = 16;
    private static final long INVULNERABILIDAD_MS = 700;

    /** Tiempo entre cambios de patron especial. */
    private static final long CICLO_PATRON_MS = 2600;
    /** Tiempo de aviso antes de disparar lanzas. */
    private static final long TELEGRAFO_LANZAS_MS = 550;
    /** Tiempo de carga antes de ondas o espiral. */
    private static final long CARGA_GENERICA_MS = 380;
    /** Cantidad de carriles usados por las lanzas. */
    private static final int CANTIDAD_LANZAS = 7;
    /** Ancho relativo del hueco seguro en la pared de lanzas. */
    private static final double HUECO_LANZAS_ANCHO = 0.16;

    /** Cinco patrones de ataque del jefe. */
    private enum PatronJefe { LLUVIA, LANZAS, ONDAS, CRUZ, ESPIRAL }

    private final int duracionMs;
    private final int intervaloSpawnMinMs;
    private final int intervaloSpawnMaxMs;
    private final double velocidadBalaMin;
    private final double velocidadBalaMax;
    private final int escudosIniciales;

    private final List<Bala> balas = new ArrayList<>();
    private final Random random = new Random();
    /** Desfase aleatorio para no empezar siempre con el mismo patron. */
    private final int desfaseCiclo = random.nextInt(5);
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

    // Estado visual y del patron actual del jefe.
    private long ultimoCicloIniciado = -1;
    private PatronJefe patronActual = PatronJefe.LLUVIA;
    private boolean jefeAtacando = false;
    private long jefeLungeHastaMs = 0;
    private boolean lanzasTelegrafiando = false;
    private int[] lanzasLadosActivos = new int[0];
    private double lanzasHuecoCentro = 0.5;
    private long lanzasDispararEnMs = -1;
    /** Momento en que se libera el ataque cargado, o -1 si no hay carga. */
    private long cargaDispararEnMs = -1;
    private Runnable cargaAccion;

    /**
     * Crea el panel de esquive con los valores de esta fase.
     *
     * @param duracionMs duracion total de la fase, en milisegundos
     * @param intervaloSpawnMinMs tiempo minimo entre balas nuevas
     * @param intervaloSpawnMaxMs tiempo maximo entre balas nuevas
     * @param velocidadBalaMin velocidad minima de una bala
     * @param velocidadBalaMax velocidad maxima de una bala
     * @param escudosIniciales golpes que se absorben sin quitar vida
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
        setBackground(TemaUndertale.FONDO);
        setFocusable(true);
        configurarControles();
        timerJuego = new Timer(MS_POR_TICK, e -> tick());
    }

    /** Registra las flechas del teclado aunque el foco este en otro componente. */
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

    /** Inicia la fase, toma el foco y arranca el bucle. Al terminar, ejecuta el callback. */
    public void iniciar(Runnable alTerminar) {
        this.alTerminar = alTerminar;
        requestFocusInWindow();
        proximoSpawnMs = intervaloSpawnMinMs;
        timerJuego.start();
    }

    /** Ejecuta un paso del juego: mueve, genera balas, revisa colisiones y cierra la fase si toca. */
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
     * Cambia el patron del jefe por ciclos.
     * Tambien lanza los ataques telegrafiados o cargados cuando llega su momento.
     */
    private void actualizarPatronDelJefe() {
        long ciclo = tiempoTranscurridoMs / CICLO_PATRON_MS;
        if (ciclo != ultimoCicloIniciado) {
            ultimoCicloIniciado = ciclo;
            int patron = (int) ((ciclo + desfaseCiclo) % 5);
            switch (patron) {
                case 1:
                    iniciarTelegrafoLanzas(new int[]{random.nextInt(4)});
                    break;
                case 2:
                    patronActual = PatronJefe.ONDAS;
                    jefeAtacando = true;
                    cargaDispararEnMs = tiempoTranscurridoMs + CARGA_GENERICA_MS;
                    cargaAccion = this::generarOleadaOndas;
                    break;
                case 3:
                    iniciarTelegrafoLanzas(new int[]{0, 1, 2, 3});
                    break;
                case 4:
                    patronActual = PatronJefe.ESPIRAL;
                    jefeAtacando = true;
                    cargaDispararEnMs = tiempoTranscurridoMs + CARGA_GENERICA_MS;
                    cargaAccion = this::generarRafagaCircular;
                    break;
                default:
                    patronActual = PatronJefe.LLUVIA;
                    jefeAtacando = false;
                    break;
            }
        }
        if (lanzasDispararEnMs >= 0 && tiempoTranscurridoMs >= lanzasDispararEnMs) {
            dispararLanzas();
            lanzasDispararEnMs = -1;
        }
        if (cargaDispararEnMs >= 0 && tiempoTranscurridoMs >= cargaDispararEnMs) {
            Runnable accion = cargaAccion;
            cargaDispararEnMs = -1;
            cargaAccion = null;
            if (accion != null) {
                accion.run();
            }
        }
    }

    /** Genera una bala desde un borde, apuntando a una zona cercana al centro. */
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
        // Apunta a una zona cercana al centro para variar las trayectorias.
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
     * Activa el aviso previo de lanzas, fija el hueco seguro
     * y programa el disparo real.
     *
     * @param lados lados que van a disparar lanzas
     */
    private void iniciarTelegrafoLanzas(int[] lados) {
        lanzasLadosActivos = lados;
        lanzasHuecoCentro = 0.15 + random.nextDouble() * 0.7;
        lanzasTelegrafiando = true;
        jefeAtacando = true;
        patronActual = lados.length > 1 ? PatronJefe.CRUZ : PatronJefe.LANZAS;
        lanzasDispararEnMs = tiempoTranscurridoMs + TELEGRAFO_LANZAS_MS;
    }

    /** Dispara lanzas desde todos los lados que estaban avisados. */
    private void dispararLanzas() {
        lanzasTelegrafiando = false;
        for (int lado : lanzasLadosActivos) {
            dispararLanzasEnLado(lado);
        }
        jefeLungeHastaMs = tiempoTranscurridoMs + 260;
    }

    /** Dispara una pared de lanzas desde un lado, dejando un hueco seguro. */
    private void dispararLanzasEnLado(int lado) {
        double velocidad = velocidadBalaMax * 1.3;
        for (int i = 0; i < CANTIDAD_LANZAS; i++) {
            double frac = (i + 0.5) / CANTIDAD_LANZAS;
            if (Math.abs(frac - lanzasHuecoCentro) < HUECO_LANZAS_ANCHO) {
                continue; // hueco seguro: aqui no sale lanza
            }
            double vx2, vy2, x0, y0;
            switch (lado) {
                case 0: x0 = frac * ANCHO; y0 = ALTO_JEFE - 20; vx2 = 0; vy2 = velocidad; break;
                case 1: x0 = frac * ANCHO; y0 = ALTO_TOTAL + 20; vx2 = 0; vy2 = -velocidad; break;
                case 2: x0 = -20; y0 = ALTO_JEFE + frac * ALTO_ARENA; vx2 = velocidad; vy2 = 0; break;
                default: x0 = ANCHO + 20; y0 = ALTO_JEFE + frac * ALTO_ARENA; vx2 = -velocidad; vy2 = 0; break;
            }
            balas.add(new Bala(x0, y0, vx2, vy2, 6, true));
        }
    }

    /** Genera una oleada de 3 balas onduladas que cruza la arena de lado a lado. */
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

    /** Genera una rafaga circular desde el jefe hacia todas las direcciones. */
    private void generarRafagaCircular() {
        int cantidad = 16;
        double velocidad = (velocidadBalaMin + velocidadBalaMax) / 2.0 * 1.1;
        double cx = ANCHO / 2.0;
        double cy = ALTO_JEFE + 15;
        double faseBase = random.nextDouble() * Math.PI * 2;
        for (int i = 0; i < cantidad; i++) {
            double angulo = faseBase + (2 * Math.PI * i / cantidad);
            double vx = Math.cos(angulo) * velocidad;
            double vy = Math.sin(angulo) * velocidad;
            balas.add(new Bala(cx, cy, vx, vy, 5));
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

        g.setColor(TemaUndertale.VERDE);
        g.drawRect(1, ALTO_JEFE + 1, ANCHO - 3, ALTO_ARENA - 3);

        dibujarIndicadoresLanzas(g);

        for (Bala b : balas) {
            dibujarBala(g, b);
        }

        boolean parpadeoInvulnerable = tiempoTranscurridoMs < invulnerableHastaMs && (tiempoTranscurridoMs / 100) % 2 == 0;
        g.setColor(parpadeoInvulnerable ? Color.WHITE : TemaUndertale.ROJO_ALERTA);
        g.fill(formaCorazon(hx, hy, RADIO_CORAZON));

        g.setColor(TemaUndertale.VERDE);
        g.setFont(new Font("Consolas", Font.BOLD, 13));
        int segundosRestantes = Math.max(0, (int) Math.ceil((duracionMs - tiempoTranscurridoMs) / 1000.0));
        g.drawString("Tiempo: " + segundosRestantes + "s", 8, ALTO_TOTAL - 26);
        g.drawString("Golpes recibidos: " + golpesRecibidos, 8, ALTO_TOTAL - 10);
        if (escudosIniciales > 0) {
            g.drawString("Escudos: " + escudosRestantes, ANCHO - 90, ALTO_TOTAL - 10);
        }
    }

    /**
     * Dibuja el aviso rojo/verde de cada carril
     * mientras las lanzas se estan cargando.
     */
    private void dibujarIndicadoresLanzas(Graphics2D g) {
        if (!lanzasTelegrafiando) {
            return;
        }
        for (int lado : lanzasLadosActivos) {
            boolean horizontal = lado == 0 || lado == 1; // arriba y abajo usan carriles en X
            for (int i = 0; i < CANTIDAD_LANZAS; i++) {
                double frac0 = (double) i / CANTIDAD_LANZAS;
                double frac1 = (double) (i + 1) / CANTIDAD_LANZAS;
                double fracCentro = (i + 0.5) / CANTIDAD_LANZAS;
                boolean esHuecoSeguro = Math.abs(fracCentro - lanzasHuecoCentro) < HUECO_LANZAS_ANCHO;
                Color color = esHuecoSeguro ? new Color(60, 230, 110, 100) : new Color(220, 50, 50, 100);
                g.setColor(color);
                if (horizontal) {
                    int x0 = (int) (frac0 * ANCHO);
                    int x1 = (int) (frac1 * ANCHO);
                    int y = lado == 0 ? ALTO_JEFE : ALTO_TOTAL - 18;
                    g.fillRect(x0, y, Math.max(1, x1 - x0), 18);
                } else {
                    int y0 = (int) (ALTO_JEFE + frac0 * ALTO_ARENA);
                    int y1 = (int) (ALTO_JEFE + frac1 * ALTO_ARENA);
                    int x = lado == 2 ? 0 : ANCHO - 18;
                    g.fillRect(x, y0, 18, Math.max(1, y1 - y0));
                }
            }
        }
    }

    /** Dibuja una bala normal o una lanza girada segun su direccion. */
    private void dibujarBala(Graphics2D g, Bala b) {
        if (b.isLanza()) {
            g.setColor(TemaUndertale.HUESO);
            double angulo = Math.atan2(b.getVy(), b.getVx());
            AffineTransform anterior = g.getTransform();
            g.translate(b.getX(), b.getY());
            g.rotate(angulo);
            g.fillRoundRect(-22, -5, 44, 10, 6, 6);
            g.setTransform(anterior);
        } else {
            int r = b.getRadio();
            g.setColor(TemaUndertale.HUESO);
            g.fillOval((int) (b.getX() - r), (int) (b.getY() - r), r * 2, r * 2);
        }
    }

    /**
     * Dibuja al jefe con su pose, aura y frase segun el patron actual.
     * Si hay lanzas en carga, tambien marca los bordes de salida.
     */
    private void dibujarJefe(Graphics2D g) {
        g.setColor(new Color(6, 16, 10));
        g.fillRect(0, 0, ANCHO, ALTO_JEFE);
        g.setColor(TemaUndertale.VERDE_OSCURO);
        g.drawLine(0, ALTO_JEFE - 1, ANCHO, ALTO_JEFE - 1);

        boolean cargando = cargaDispararEnMs >= 0;
        double bob = Math.sin(tiempoTranscurridoMs / 180.0) * 4;
        // En reposo se mueve un poco; al atacar se queda quieto para verse mas claro.
        double deriva = jefeAtacando ? 0 : Math.sin(tiempoTranscurridoMs / 900.0) * 14;
        boolean lungeando = tiempoTranscurridoMs < jefeLungeHastaMs;
        double cx = ANCHO / 2.0 + deriva;
        double cy = ALTO_JEFE / 2.0 + bob + (lungeando ? 6 : 0) - (cargando ? 4 : 0);

        boolean resplandor = lanzasTelegrafiando || jefeAtacando || cargando;
        if (resplandor) {
            Color colorAura;
            int radioAura;
            switch (patronActual) {
                case ESPIRAL: colorAura = new Color(200, 60, 220); radioAura = 44; break;
                case CRUZ: colorAura = new Color(220, 50, 50); radioAura = 42; break;
                case ONDAS: colorAura = new Color(255, 140, 40); radioAura = 34; break;
                default: colorAura = new Color(220, 50, 50); radioAura = 30; break;
            }
            float alpha = (float) (0.35 + 0.35 * Math.abs(Math.sin(tiempoTranscurridoMs / 90.0)));
            g.setColor(new Color(colorAura.getRed(), colorAura.getGreen(), colorAura.getBlue(), (int) (alpha * 255)));
            g.fillOval((int) (cx - radioAura), (int) (cy - radioAura), radioAura * 2, radioAura * 2);
        }

        g.setStroke(new BasicStroke(3f));
        g.setColor(Color.WHITE);
        g.fillOval((int) (cx - 10), (int) (cy - 26), 20, 20);
        g.drawLine((int) cx, (int) (cy - 6), (int) cx, (int) (cy + 18));

        switch (patronActual) {
            case CRUZ:
                // Brazos abiertos con una lanza a cada lado.
                g.drawLine((int) cx, (int) cy, (int) (cx - 26), (int) (cy - 4));
                g.drawLine((int) cx, (int) cy, (int) (cx + 26), (int) (cy - 4));
                g.setColor(new Color(255, 210, 120));
                g.drawLine((int) (cx - 26), (int) (cy - 4), (int) (cx - 44), (int) (cy - 4));
                g.drawLine((int) (cx + 26), (int) (cy - 4), (int) (cx + 44), (int) (cy - 4));
                break;
            case ESPIRAL:
                // Brazos arriba antes de la rafaga.
                g.drawLine((int) cx, (int) cy, (int) (cx - 18), (int) (cy - 20));
                g.drawLine((int) cx, (int) cy, (int) (cx + 18), (int) (cy - 20));
                break;
            case ONDAS:
                // Brazos al frente como cargando la ola.
                g.drawLine((int) cx, (int) (cy + 2), (int) (cx - 16), (int) (cy + 12));
                g.drawLine((int) cx, (int) (cy + 2), (int) (cx + 16), (int) (cy + 12));
                break;
            default:
                // Pose base con una lanza en alto.
                g.drawLine((int) cx, (int) cy, (int) (cx - 14), (int) (cy + 8));
                g.drawLine((int) cx, (int) cy, (int) (cx + 16), (int) (cy - 14));
                g.setColor(new Color(255, 210, 120));
                g.drawLine((int) (cx + 16), (int) (cy - 14), (int) (cx + 34), (int) (cy - 30));
        }

        g.setColor(Color.WHITE);
        g.drawLine((int) cx, (int) (cy + 18), (int) (cx - 10), (int) (cy + 34));
        g.drawLine((int) cx, (int) (cy + 18), (int) (cx + 10), (int) (cy + 34));

        g.setFont(new Font("Consolas", Font.BOLD, 12));
        g.setColor(TemaUndertale.VERDE_BRILLANTE);
        g.drawString(fraseDelJefe(), 10, 16);

        if (lanzasTelegrafiando) {
            g.setColor(TemaUndertale.ROJO_ALERTA);
            g.setStroke(new BasicStroke(3f));
            for (int lado : lanzasLadosActivos) {
                switch (lado) {
                    case 0: g.drawLine(0, ALTO_JEFE, ANCHO, ALTO_JEFE); break;
                    case 1: g.drawLine(0, ALTO_TOTAL - 1, ANCHO, ALTO_TOTAL - 1); break;
                    case 2: g.drawLine(1, ALTO_JEFE, 1, ALTO_TOTAL); break;
                    default: g.drawLine(ANCHO - 1, ALTO_JEFE, ANCHO - 1, ALTO_TOTAL); break;
                }
            }
        }
    }

    /** @return la frase de dialogo del jefe segun lo que este haciendo en este instante. */
    private String fraseDelJefe() {
        if (lanzasTelegrafiando) {
            return patronActual == PatronJefe.CRUZ ? "¡No hay escapatoria!" : "¡Prepárate!";
        }
        if (cargaDispararEnMs >= 0) {
            return patronActual == PatronJefe.ESPIRAL ? "¡Siente mi furia!" : "¡Aquí viene!";
        }
        if (jefeAtacando) {
            return "¡Esquiva esto!";
        }
        return "¡Nadie escapa de mi ataque!";
    }

    /** Dibuja un corazon simple centrado en (cx, cy). */
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
