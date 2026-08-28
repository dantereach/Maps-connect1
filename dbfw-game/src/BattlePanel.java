import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Panel donde se dibujan los dos stickman y sus barras de vida.
 */
public class BattlePanel extends JPanel {
    private final Fighter player;
    private final Fighter cpu;
    private String message = "";

    public BattlePanel(Fighter player, Fighter cpu) {
        this.player = player;
        this.cpu = cpu;
        setBackground(new Color(210, 230, 255));
        setPreferredSize(new java.awt.Dimension(700, 400));
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Suelo
        g2.setColor(new Color(120, 170, 90));
        g2.fillRect(0, player.getBaseY() + 10, getWidth(), getHeight() - player.getBaseY() - 10);

        drawFighter(g2, player);
        drawFighter(g2, cpu);

        drawHealthBar(g2, 20, 20, player.getName(), player.getHp());
        drawHealthBar(g2, getWidth() - 220, 20, cpu.getName(), cpu.getHp());

        if (message != null && !message.isEmpty()) {
            g2.setColor(Color.DARK_GRAY);
            g2.setFont(new Font("SansSerif", Font.BOLD, 16));
            g2.drawString(message, getWidth() / 2 - 150, 60);
        }

        if (player.isKo() || cpu.isKo()) {
            g2.setColor(Color.RED);
            g2.setFont(new Font("SansSerif", Font.BOLD, 32));
            String texto = player.isKo() ? cpu.getName() + " GANA!" : player.getName() + " GANA!";
            g2.drawString(texto, getWidth() / 2 - 110, 120);
        }
    }

    private void drawHealthBar(Graphics2D g2, int x, int y, String name, int hp) {
        g2.setColor(Color.BLACK);
        g2.drawString(name, x, y - 5);
        g2.setColor(Color.GRAY);
        g2.fillRect(x, y, 200, 18);
        Color barColor = hp > 50 ? Color.GREEN : (hp > 20 ? Color.ORANGE : Color.RED);
        g2.setColor(barColor);
        g2.fillRect(x, y, (int) (200 * (hp / (double) Fighter.MAX_HP)), 18);
        g2.setColor(Color.BLACK);
        g2.drawRect(x, y, 200, 18);
    }

    private void drawFighter(Graphics2D g2, Fighter f) {
        int x = f.getBaseX() + f.getOffsetX();
        int groundY = f.getBaseY();
        g2.setColor(f.getColor());
        g2.setStroke(new BasicStroke(4));

        if (f.isKo()) {
            // Figura caida en el suelo
            g2.drawOval(x - 12, groundY - 12, 24, 24); // cabeza
            g2.drawLine(x - 30, groundY, x + 40, groundY); // cuerpo tumbado
            g2.drawLine(x + 10, groundY, x + 30, groundY - 15); // pierna
            g2.drawLine(x - 5, groundY, x - 20, groundY - 15); // brazo
            return;
        }

        int headR = 15;
        int headY = groundY - 90;
        int neckY = headY + headR;
        int hipY = groundY - 30;

        // Cabeza
        g2.drawOval(x - headR, headY, headR * 2, headR * 2);
        // Cuerpo
        g2.drawLine(x, neckY, x, hipY);

        String pose = f.getPose();
        int facing = f.getFacing();

        if ("block".equals(pose)) {
            // Brazos cruzados al frente
            g2.drawLine(x, neckY + 15, x + 20 * facing, neckY + 10);
            g2.drawLine(x, neckY + 15, x + 15 * facing, neckY + 25);
            g2.drawLine(x, hipY, x - 15 * facing, groundY - 5);
            g2.drawLine(x, hipY, x + 15 * facing, groundY - 5);
        } else if ("punch".equals(pose)) {
            g2.drawLine(x, neckY + 15, x + 35 * facing, neckY + 15); // brazo golpeando
            g2.drawLine(x, neckY + 15, x - 10 * facing, neckY + 25); // otro brazo
            g2.drawLine(x, hipY, x - 15 * facing, groundY - 5);
            g2.drawLine(x, hipY, x + 15 * facing, groundY - 5);
        } else if ("kick".equals(pose)) {
            g2.drawLine(x, neckY + 15, x - 10 * facing, neckY + 25);
            g2.drawLine(x, neckY + 15, x + 10 * facing, neckY + 25);
            g2.drawLine(x, hipY, x - 10 * facing, groundY - 5); // pierna de apoyo
            g2.drawLine(x, hipY, x + 35 * facing, hipY + 5); // pierna pateando (horizontal)
        } else if ("hit".equals(pose)) {
            g2.drawLine(x, neckY + 15, x - 20 * facing, neckY + 5);
            g2.drawLine(x, neckY + 15, x + 20 * facing, neckY + 5);
            g2.drawLine(x, hipY, x - 20 * facing, groundY - 5);
            g2.drawLine(x, hipY, x + 10 * facing, groundY - 5);
        } else {
            // idle
            g2.drawLine(x, neckY + 15, x - 15, neckY + 25);
            g2.drawLine(x, neckY + 15, x + 15, neckY + 25);
            g2.drawLine(x, hipY, x - 12, groundY - 5);
            g2.drawLine(x, hipY, x + 12, groundY - 5);
        }
    }
}
