import javax.swing.SwingUtilities;

/**
 * Punto de entrada del juego.
 * Por defecto abre el juego de cartas (estilo Dragon Ball Fusion World).
 * Argumentos: "texto" = consola con cartas (version anterior), "stickman" = pelea grafica de stickman.
 */
public class Main {
    public static void main(String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("texto")) {
            new Game().start();
        } else if (args.length > 0 && args[0].equalsIgnoreCase("stickman")) {
            SwingUtilities.invokeLater(() -> new BattleFrame().setVisible(true));
        } else {
            SwingUtilities.invokeLater(() -> new CardBattleFrame().setVisible(true));
        }
    }
}
