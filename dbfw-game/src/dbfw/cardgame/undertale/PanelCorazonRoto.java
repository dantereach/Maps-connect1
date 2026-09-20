package dbfw.cardgame.undertale;

import dbfw.cardgame.TemaUndertale;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Animacion de Game Over al estilo Undertale: pantalla negra con el corazon del jugador
 * rompiendose primero en 2 mitades y luego en varios fragmentos que caen y se desvanecen.
 * Al terminar, avisa con un callback para que la pantalla de fin se muestre despues.
 */
public class PanelCorazonRoto extends JPanel {
    /** Radio del corazon grande centrado en la pantalla. */
    private static final int RADIO = 70;
    private static final long DURACION_INTACTO_MS = 500;
    private static final long DURACION_PARTIDO_MS = 350;
    private static final long DURACION_ESTALLIDO_MS = 1300;
    private static final long DURACION_PAUSA_NEGRA_MS = 500;
    private static final long DURACION_TOTAL_MS =
            DURACION_INTACTO_MS + DURACION_PARTIDO_MS + DURACION_ESTALLIDO_MS + DURACION_PAUSA_NEGRA_MS;
    private static final int MS_POR_TICK = 16;
    /** Distancia horizontal a la que se separan las 2 mitades antes de estallar. */
    private static final double SEPARACION_MITADES = 22;
    /** Distancia maxima que recorren los fragmentos al estallar. */
    private static final double DISTANCIA_ESTALLIDO = 160;

    /** Un pedazo del corazon: su forma (en coordenadas locales, origen en el centro del corazon). */
    private static final class Fragmento {
        final Area forma;
        final double centroX;
        final double centroY;
        final boolean mitadIzquierda;
        final double dirX;
        final double dirY;
        final double velocidadRotacion;

        Fragmento(Area forma, double centroX, double centroY, boolean mitadIzquierda,
                  double dirX, double dirY, double velocidadRotacion) {
            this.forma = forma;
            this.centroX = centroX;
            this.centroY = centroY;
            this.mitadIzquierda = mitadIzquierda;
            this.dirX = dirX;
            this.dirY = dirY;
            this.velocidadRotacion = velocidadRotacion;
        }
    }

    private final List<Fragmento> fragmentos = new ArrayList<>();
    private Timer timer;
    private long transcurridoMs;
    private Runnable alTerminar;

    public PanelCorazonRoto() {
        setPreferredSize(new Dimension(380, 350));
        setBackground(Color.BLACK);
        setOpaque(true);
        generarFragmentos();
    }

    /** Arranca la animacion; llama a {@code alTerminar} una sola vez cuando termina. */
    public void iniciar(Runnable alTerminar) {
        this.alTerminar = alTerminar;
        transcurridoMs = 0;
        timer = new Timer(MS_POR_TICK, e -> {
            transcurridoMs += MS_POR_TICK;
            repaint();
            if (transcurridoMs >= DURACION_TOTAL_MS) {
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

    /** Divide el corazon en una cuadricula de pedazos, cada uno recortado a la forma real del corazon. */
    private void generarFragmentos() {
        Polygon corazon = formaCorazon(RADIO);
        Random azar = new Random(1);
        int celdas = 6;
        double paso = (2.0 * RADIO) / celdas;
        for (int fila = 0; fila < celdas; fila++) {
            for (int col = 0; col < celdas; col++) {
                double x0 = -RADIO + col * paso;
                double y0 = -RADIO + fila * paso;
                Area celda = new Area(new Rectangle2D.Double(x0, y0, paso, paso));
                Area pedazo = new Area(corazon);
                pedazo.intersect(celda);
                if (pedazo.isEmpty()) {
                    continue;
                }
                Rectangle2D limites = pedazo.getBounds2D();
                double cx = limites.getCenterX();
                double cy = limites.getCenterY();
                boolean izquierda = cx < 0;
                double dirX = cx + (azar.nextDouble() - 0.5) * 10;
                double dirY = cy + (azar.nextDouble() - 0.5) * 10 + 20; // sesgo hacia abajo (gravedad)
                double norma = Math.max(1.0, Math.hypot(dirX, dirY));
                double rotVel = (azar.nextDouble() - 0.5) * 6.0;
                fragmentos.add(new Fragmento(pedazo, cx, cy, izquierda, dirX / norma, dirY / norma, rotVel));
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());

        if (transcurridoMs >= DURACION_TOTAL_MS - DURACION_PAUSA_NEGRA_MS) {
            return; // pausa final: pantalla completamente negra
        }

        double cx = getWidth() / 2.0;
        double cy = getHeight() / 2.0;

        double offsetMitad;
        double progresoEstallido;
        float alpha;
        if (transcurridoMs < DURACION_INTACTO_MS) {
            offsetMitad = 0;
            progresoEstallido = 0;
            alpha = 1f;
        } else if (transcurridoMs < DURACION_INTACTO_MS + DURACION_PARTIDO_MS) {
            double p = (transcurridoMs - DURACION_INTACTO_MS) / (double) DURACION_PARTIDO_MS;
            offsetMitad = SEPARACION_MITADES * p;
            progresoEstallido = 0;
            alpha = 1f;
        } else {
            offsetMitad = SEPARACION_MITADES;
            double p = (transcurridoMs - DURACION_INTACTO_MS - DURACION_PARTIDO_MS) / (double) DURACION_ESTALLIDO_MS;
            progresoEstallido = Math.min(1.0, p);
            alpha = (float) Math.max(0.0, 1.0 - progresoEstallido);
        }

        g.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, alpha));
        g.setColor(TemaUndertale.ROJO_ALERTA);
        for (Fragmento f : fragmentos) {
            double despX = (f.mitadIzquierda ? -offsetMitad : offsetMitad) + f.dirX * DISTANCIA_ESTALLIDO * progresoEstallido;
            double despY = f.dirY * DISTANCIA_ESTALLIDO * progresoEstallido;
            AffineTransform t = AffineTransform.getTranslateInstance(cx + despX, cy + despY);
            t.rotate(f.velocidadRotacion * progresoEstallido, f.centroX, f.centroY);
            Shape formaFinal = t.createTransformedShape(f.forma);
            g.fill(formaFinal);
        }
    }

    /** Misma forma de corazon que {@link PanelEsquive}, centrada en el origen. */
    private static Polygon formaCorazon(int r) {
        Polygon p = new Polygon();
        p.addPoint(0, r);
        p.addPoint(-r, (int) (-r * 0.2));
        p.addPoint((int) (-r * 0.5), -r);
        p.addPoint(0, (int) (-r * 0.4));
        p.addPoint((int) (r * 0.5), -r);
        p.addPoint(r, (int) (-r * 0.2));
        return p;
    }
}
