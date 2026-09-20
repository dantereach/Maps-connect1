package dbfw.cardgame.undertale;

import dbfw.cardgame.TemaUndertale;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Animacion de victoria al estilo Undertale: el corazon del jugador brilla y crece
 * con un resplandor dorado/verde, mientras estrellas se expanden a su alrededor.
 * Al terminar, avisa con un callback para que la pantalla de fin se muestre despues.
 */
public class PanelVictoria extends JPanel {
    private static final int RADIO_BASE = 50;
    private static final long DURACION_MS = 2200;
    private static final int MS_POR_TICK = 16;
    /** Cuantas puntas de estrella se dibujan alrededor del corazon. */
    private static final int CANTIDAD_ESTRELLAS = 10;

    private Timer timer;
    private long transcurridoMs;
    private Runnable alTerminar;

    public PanelVictoria() {
        setPreferredSize(new Dimension(380, 350));
        setBackground(Color.BLACK);
        setOpaque(true);
    }

    /** Arranca la animacion; llama a {@code alTerminar} una sola vez cuando termina. */
    public void iniciar(Runnable alTerminar) {
        this.alTerminar = alTerminar;
        transcurridoMs = 0;
        timer = new Timer(MS_POR_TICK, e -> {
            transcurridoMs += MS_POR_TICK;
            repaint();
            if (transcurridoMs >= DURACION_MS) {
                timer.stop();
                if (this.alTerminar != null) {
                    Runnable callback = this.alTerminar;
                    this.alTerminar = null;
                    callback.run();
                }
            }
        });
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());

        double progreso = Math.min(1.0, transcurridoMs / (double) DURACION_MS);
        double cx = getWidth() / 2.0;
        double cy = getHeight() / 2.0;

        // El corazon late (pulso) y crece un poco mientras avanza la animacion.
        double pulso = 1.0 + 0.08 * Math.sin(transcurridoMs / 90.0);
        double radio = (RADIO_BASE * (0.75 + 0.25 * progreso)) * pulso;

        // Resplandor: anillos que se expanden y se desvanecen, en verde/dorado.
        for (int anillo = 0; anillo < 3; anillo++) {
            double faseAnillo = (transcurridoMs / 500.0 + anillo / 3.0) % 1.0;
            float alphaAnillo = (float) Math.max(0.0, 0.5 * (1.0 - faseAnillo));
            int radioAnillo = (int) (radio + faseAnillo * 120);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alphaAnillo));
            g.setColor(new Color(255, 221, 120));
            g.setStroke(new BasicStroke(3f));
            g.drawOval((int) (cx - radioAnillo), (int) (cy - radioAnillo), radioAnillo * 2, radioAnillo * 2);
        }

        // Estrellas girando alrededor del corazon.
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        double distanciaEstrellas = 90 + 20 * Math.sin(transcurridoMs / 260.0);
        double giro = transcurridoMs / 400.0;
        for (int i = 0; i < CANTIDAD_ESTRELLAS; i++) {
            double angulo = giro + (2 * Math.PI * i / CANTIDAD_ESTRELLAS);
            double ex = cx + Math.cos(angulo) * distanciaEstrellas;
            double ey = cy + Math.sin(angulo) * distanciaEstrellas * 0.6;
            g.setColor(new Color(255, 240, 160));
            g.fill(formaEstrella(ex, ey, 7));
        }

        // El corazon brillante en verde claro, como el alma del jugador contenta.
        g.setColor(TemaUndertale.VERDE_BRILLANTE);
        g.fill(formaCorazon(cx, cy, radio));

        if (progreso > 0.25) {
            g.setColor(new Color(255, 230, 130));
            g.setFont(new Font(TemaUndertale.FAMILIA_FUENTE, Font.BOLD, 26));
            String texto = "¡VICTORIA!";
            int anchoTexto = g.getFontMetrics().stringWidth(texto);
            float alphaTexto = (float) Math.min(1.0, (progreso - 0.25) / 0.25);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alphaTexto));
            g.drawString(texto, (float) (cx - anchoTexto / 2.0), (float) (cy + radio + 46));
        }
    }

    /** Misma forma de corazon que {@link PanelEsquive}, centrada en (cx, cy). */
    private static Polygon formaCorazon(double cx, double cy, double r) {
        Polygon p = new Polygon();
        p.addPoint((int) cx, (int) (cy + r));
        p.addPoint((int) (cx - r), (int) (cy - r * 0.2));
        p.addPoint((int) (cx - r * 0.5), (int) (cy - r));
        p.addPoint((int) cx, (int) (cy - r * 0.4));
        p.addPoint((int) (cx + r * 0.5), (int) (cy - r));
        p.addPoint((int) (cx + r), (int) (cy - r * 0.2));
        return p;
    }

    /** Estrella de 4 puntas simple, centrada en (cx, cy). */
    private static Polygon formaEstrella(double cx, double cy, double r) {
        Polygon p = new Polygon();
        p.addPoint((int) cx, (int) (cy - r));
        p.addPoint((int) (cx + r * 0.28), (int) (cy - r * 0.28));
        p.addPoint((int) (cx + r), (int) cy);
        p.addPoint((int) (cx + r * 0.28), (int) (cy + r * 0.28));
        p.addPoint((int) cx, (int) (cy + r));
        p.addPoint((int) (cx - r * 0.28), (int) (cy + r * 0.28));
        p.addPoint((int) (cx - r), (int) cy);
        p.addPoint((int) (cx - r * 0.28), (int) (cy - r * 0.28));
        return p;
    }
}
