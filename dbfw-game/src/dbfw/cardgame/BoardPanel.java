package dbfw.cardgame;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.Polygon;
import java.awt.RenderingHints;

/**
 * Panel decorativo que dibuja el campo de batalla con una perspectiva de "tablero panoramico"
 * (un piso de baldosas visto en angulo, mas angosto en el horizonte y mas ancho hacia el
 * jugador), inspirado en la vista de combate de Dragon Ball Fusion World.
 * <p>
 * Es puramente visual: no contiene logica de juego. Los componentes reales (informacion de
 * cada jugador, lideres y cartas) se agregan encima como hijos normales de Swing con fondo
 * transparente ({@code setOpaque(false)}), de modo que el piso pintado aqui se vea detras
 * de ellos, dando la sensacion de una vista amplia del campo en vez de una simple lista.
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

        // Fondo tipo "cielo de arena" con degradado calido.
        g2.setPaint(new GradientPaint(0, 0, new Color(55, 35, 65), 0, h, new Color(150, 110, 60)));
        g2.fillRect(0, 0, w, h);

        // Piso en perspectiva: trapecio angosto arriba (horizonte, lado de la CPU) y ancho
        // abajo (frente, lado del jugador), lo que da la ilusion de profundidad/camara angulada.
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

        g2.setPaint(new GradientPaint(0, pisoTopeY, new Color(205, 185, 150), 0, pisoBaseY, new Color(140, 110, 80)));
        g2.fillPolygon(piso);
        g2.setColor(new Color(85, 65, 45));
        g2.setStroke(new BasicStroke(2f));
        g2.drawPolygon(piso);

        // Lineas de baldosas horizontales, interpoladas con un exponente para que se vean mas
        // juntas cerca del horizonte y mas separadas cerca del jugador (efecto de profundidad).
        int filas = 8;
        for (int i = 1; i < filas; i++) {
            double t = Math.pow((double) i / filas, 1.6);
            int y = (int) (pisoTopeY + t * (pisoBaseY - pisoTopeY));
            int xIzq = (int) (margenArriba + t * (margenAbajo - margenArriba));
            int xDer = (int) ((w - margenArriba) + t * ((w - margenAbajo) - (w - margenArriba)));
            g2.setColor(new Color(85, 65, 45, 110));
            g2.drawLine(xIzq, y, xDer, y);
        }
        // Lineas de baldosas verticales (columnas), convergiendo hacia el horizonte.
        int columnas = 10;
        for (int i = 1; i < columnas; i++) {
            double t = (double) i / columnas;
            int xTope = (int) (margenArriba + t * (w - 2 * margenArriba));
            int xBase = (int) (margenAbajo + t * (w - 2 * margenAbajo));
            g2.setColor(new Color(85, 65, 45, 90));
            g2.drawLine(xTope, pisoTopeY, xBase, pisoBaseY);
        }

        // Decoracion lateral simple (rocas) para dar ambiente de arena de combate.
        g2.setColor(new Color(90, 65, 45));
        g2.fillOval(-50, (int) (h * 0.12), 150, 110);
        g2.fillOval(w - 100, (int) (h * 0.08), 150, 120);

        g2.dispose();
    }
}
