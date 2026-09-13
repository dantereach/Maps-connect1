package dbfw.cardgame;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;

/**
 * Panel decorativo del campo de batalla.
 * Dibuja un piso en perspectiva con paleta negro/verde estilo Undertale.
 * Solo pinta el fondo; la logica y los controles van encima.
 */
public class BoardPanel extends JPanel {

    public BoardPanel() {
        setOpaque(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();

        // Fondo negro base.
        g2.setColor(TemaUndertale.FONDO);
        g2.fillRect(0, 0, w, h);

        // Piso en perspectiva para simular profundidad.
        // Es una cuadricula verde sobre negro.
        int horizonteY = (int) (h * 0.16);
        int pisoTopeY = horizonteY;
        int pisoBaseY = h;
        int margenArriba = (int) (w * 0.30);
        int margenAbajo = (int) (w * 0.03);

        Polygon piso = new Polygon();
        piso.addPoint(margenArriba, pisoTopeY);
        piso.addPoint(w - margenArriba, pisoTopeY);
        piso.addPoint(w - margenAbajo, pisoBaseY);
        piso.addPoint(margenAbajo, pisoBaseY);

        g2.setColor(new Color(5, 20, 10));
        g2.fillPolygon(piso);
        g2.setColor(TemaUndertale.VERDE);
        g2.setStroke(new BasicStroke(2f));
        g2.drawPolygon(piso);

        // Lineas horizontales: mas juntas al fondo y mas separadas al frente.
        int filas = 8;
        for (int i = 1; i < filas; i++) {
            double t = Math.pow((double) i / filas, 1.6);
            int y = (int) (pisoTopeY + t * (pisoBaseY - pisoTopeY));
            int xIzq = (int) (margenArriba + t * (margenAbajo - margenArriba));
            int xDer = (int) ((w - margenArriba) + t * ((w - margenAbajo) - (w - margenArriba)));
            g2.setColor(new Color(40, 140, 70, 130));
            g2.drawLine(xIzq, y, xDer, y);
        }
        // Lineas verticales que convergen hacia el horizonte.
        int columnas = 10;
        for (int i = 1; i < columnas; i++) {
            double t = (double) i / columnas;
            int xTope = (int) (margenArriba + t * (w - 2 * margenArriba));
            int xBase = (int) (margenAbajo + t * (w - 2 * margenAbajo));
            g2.setColor(new Color(40, 140, 70, 100));
            g2.drawLine(xTope, pisoTopeY, xBase, pisoBaseY);
        }

        // Rocas laterales para decorar.
        g2.setColor(new Color(15, 60, 30));
        g2.fillOval(-50, (int) (h * 0.12), 150, 110);
        g2.fillOval(w - 100, (int) (h * 0.08), 150, 120);

        g2.dispose();
    }
}
