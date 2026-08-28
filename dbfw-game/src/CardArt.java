import javax.swing.ImageIcon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * Genera el "arte" de las cartas: pequenos stickman dibujados por codigo, con una pose distinta
 * segun el tipo de carta, y un icono especial para los lideres.
 */
public final class CardArt {
    private CardArt() {
    }

    public static ImageIcon stickmanIcon(CardType type, Color color) {
        int w = 80, h = 90;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Fondo suave para que se note la carta
        g.setColor(new Color(255, 255, 255, 160));
        g.fillRoundRect(2, 2, w - 4, h - 4, 12, 12);

        g.setColor(color);
        g.setStroke(new BasicStroke(3));

        int cx = w / 2;
        int headR = 11;
        int headY = 12;
        int neckY = headY + headR * 2;
        int hipY = neckY + 26;
        int groundY = hipY + 26;

        g.drawOval(cx - headR, headY, headR * 2, headR * 2);
        g.drawLine(cx, neckY, cx, hipY);

        switch (type) {
            case DRAW:
                // Estira el brazo hacia arriba, como robando una carta.
                g.drawLine(cx, neckY + 5, cx + 18, neckY - 12);
                g.drawLine(cx, neckY + 5, cx - 14, neckY + 16);
                g.drawLine(cx, hipY, cx - 10, groundY);
                g.drawLine(cx, hipY, cx + 10, groundY);
                break;
            case GUARD:
                // Brazos cruzados al frente y un pequeno escudo.
                g.drawLine(cx, neckY + 5, cx + 15, neckY + 4);
                g.drawLine(cx, neckY + 5, cx - 15, neckY + 4);
                g.drawRect(cx - 7, neckY + 8, 14, 16);
                g.drawLine(cx, hipY, cx - 10, groundY);
                g.drawLine(cx, hipY, cx + 10, groundY);
                break;
            case DOUBLE_STRIKE:
                // Dos golpes: ambos brazos lanzados hacia adelante.
                g.drawLine(cx, neckY + 4, cx + 22, neckY - 2);
                g.drawLine(cx, neckY + 10, cx + 22, neckY + 16);
                g.drawLine(cx, hipY, cx - 10, groundY);
                g.drawLine(cx, hipY, cx + 16, hipY + 4);
                break;
            default: // BASIC
                g.drawLine(cx, neckY + 5, cx - 15, neckY + 16);
                g.drawLine(cx, neckY + 5, cx + 15, neckY + 16);
                g.drawLine(cx, hipY, cx - 10, groundY);
                g.drawLine(cx, hipY, cx + 10, groundY);
        }
        g.dispose();
        return new ImageIcon(img);
    }

    /** Icono del lider: stickman mas grande, con capa; si esta transformado, se agrega un aura dorada. */
    public static ImageIcon leaderIcon(Color color, boolean transformado) {
        int w = 90, h = 100;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(new Color(255, 255, 255, 160));
        g.fillRoundRect(2, 2, w - 4, h - 4, 14, 14);

        int cx = w / 2;
        int headR = 13;
        int headY = 14;
        int neckY = headY + headR * 2;
        int hipY = neckY + 28;
        int groundY = hipY + 28;

        if (transformado) {
            g.setColor(new Color(255, 215, 0, 150));
            g.fillOval(cx - 32, headY - 15, 64, 64);
            g.setColor(new Color(255, 140, 0));
        } else {
            g.setColor(color);
        }
        g.setStroke(new BasicStroke(4));

        g.drawOval(cx - headR, headY, headR * 2, headR * 2);
        g.drawLine(cx, neckY, cx, hipY);
        // Capa
        g.drawLine(cx - 6, neckY + 3, cx - 16, hipY + 6);
        g.drawLine(cx + 6, neckY + 3, cx + 16, hipY + 6);
        // Pose heroica: puños en alto
        g.drawLine(cx, neckY + 6, cx - 18, neckY - 10);
        g.drawLine(cx, neckY + 6, cx + 18, neckY - 10);
        g.drawLine(cx, hipY, cx - 12, groundY);
        g.drawLine(cx, hipY, cx + 12, groundY);

        g.dispose();
        return new ImageIcon(img);
    }
}
