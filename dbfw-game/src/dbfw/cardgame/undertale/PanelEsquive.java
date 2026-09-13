package dbfw.cardgame.undertale;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
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
 */
public class PanelEsquive extends JPanel {
    private static final int ANCHO = 380;
    private static final int ALTO = 260;
    private static final int RADIO_CORAZON = 8;
    private static final double VELOCIDAD_CORAZON = 4.0;
    private static final int MS_POR_TICK = 16;
    private static final long INVULNERABILIDAD_MS = 700;

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
    private double hy = ALTO / 2.0;
    private boolean arriba, abajo, izquierda, derecha;

    private long tiempoTranscurridoMs = 0;
    private long proximoSpawnMs = 0;
    private long invulnerableHastaMs = 0;
    private int escudosRestantes;
    private int golpesRecibidos = 0;
    private boolean terminado = false;
    private Runnable alTerminar;

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
        setPreferredSize(new java.awt.Dimension(ANCHO, ALTO));
        setBackground(Color.BLACK);
        setFocusable(true);
        configurarControles();
        timerJuego = new Timer(MS_POR_TICK, e -> tick());
    }

    /** Registra las flechas del teclado (WHEN_IN_FOCUSED_WINDOW: funcionan aunque el foco lo tenga otro componente de la ventana). */
    private void configurarControles() {
        registrarTecla(KeyEvent.VK_UP, "arriba", true);
        registrarTecla(KeyEvent.VK_DOWN, "abajo", true);
        registrarTecla(KeyEvent.VK_LEFT, "izquierda", true);
        registrarTecla(KeyEvent.VK_RIGHT, "derecha", true);
    }

    private void registrarTecla(int codigo, String nombre, boolean flechas) {
        JComponent panel = this;
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(codigo, 0, false), "presiona-" + nombre);
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(codigo, 0, true), "suelta-" + nombre);
        panel.getActionMap().put("presiona-" + nombre, new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                setDireccion(nombre, true);
            }
        });
        panel.getActionMap().put("suelta-" + nombre, new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
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

    /** Un paso del bucle de juego: mover corazon y balas, revisar colisiones, generar balas nuevas. */
    private void tick() {
        if (terminado) {
            return;
        }
        tiempoTranscurridoMs += MS_POR_TICK;
        moverCorazon();

        if (tiempoTranscurridoMs >= proximoSpawnMs) {
            generarBala();
            int rango = Math.max(1, intervaloSpawnMaxMs - intervaloSpawnMinMs);
            proximoSpawnMs = tiempoTranscurridoMs + intervaloSpawnMinMs + random.nextInt(rango);
        }

        for (Bala b : balas) {
            b.mover();
        }
        balas.removeIf(b -> b.fueraDeArea(ANCHO, ALTO));
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
        hy = clamp(hy + dy * VELOCIDAD_CORAZON, RADIO_CORAZON, ALTO - RADIO_CORAZON);
    }

    private double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    /** Genera una bala nueva desde un borde aleatorio, apuntando hacia una zona cercana al centro de la caja. */
    private void generarBala() {
        double velocidad = velocidadBalaMin + random.nextDouble() * (velocidadBalaMax - velocidadBalaMin);
        int lado = random.nextInt(4); // 0=arriba, 1=abajo, 2=izquierda, 3=derecha
        double origenX, origenY;
        switch (lado) {
            case 0: origenX = random.nextInt(ANCHO); origenY = -10; break;
            case 1: origenX = random.nextInt(ANCHO); origenY = ALTO + 10; break;
            case 2: origenX = -10; origenY = random.nextInt(ALTO); break;
            default: origenX = ANCHO + 10; origenY = random.nextInt(ALTO); break;
        }
        // Apunta hacia un punto aleatorio dentro de la caja (no siempre el centro exacto), para
        // que las trayectorias varien y no sean todas paralelas.
        double destinoX = ANCHO * 0.2 + random.nextDouble() * ANCHO * 0.6;
        double destinoY = ALTO * 0.2 + random.nextDouble() * ALTO * 0.6;
        double dx = destinoX - origenX;
        double dy = destinoY - origenY;
        double distancia = Math.max(1, Math.sqrt(dx * dx + dy * dy));
        double vx = dx / distancia * velocidad;
        double vy = dy / distancia * velocidad;
        balas.add(new Bala(origenX, origenY, vx, vy, 6));
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

        g.setColor(Color.WHITE);
        g.drawRect(1, 1, ANCHO - 3, ALTO - 3);

        g.setColor(new Color(255, 140, 60));
        for (Bala b : balas) {
            int r = b.getRadio();
            g.fillOval((int) (b.getX() - r), (int) (b.getY() - r), r * 2, r * 2);
        }

        boolean parpadeoInvulnerable = tiempoTranscurridoMs < invulnerableHastaMs && (tiempoTranscurridoMs / 100) % 2 == 0;
        g.setColor(parpadeoInvulnerable ? Color.WHITE : Color.RED);
        g.fill(formaCorazon(hx, hy, RADIO_CORAZON));

        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 13));
        int segundosRestantes = Math.max(0, (int) Math.ceil((duracionMs - tiempoTranscurridoMs) / 1000.0));
        g.drawString("Tiempo: " + segundosRestantes + "s", 8, ALTO - 26);
        g.drawString("Golpes recibidos: " + golpesRecibidos, 8, ALTO - 10);
        if (escudosIniciales > 0) {
            g.drawString("Escudos: " + escudosRestantes, ANCHO - 90, ALTO - 10);
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
