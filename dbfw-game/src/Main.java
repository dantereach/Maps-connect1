import javax.swing.SwingUtilities;

/**
 * Punto de entrada del juego.
 * Por defecto abre la interfaz grafica (pelea de stickman).
 * Ejecuta con el argumento "texto" para jugar la version de consola con cartas.
 */
public class Main {
    public static void main(String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("texto")) {
            new Game().start();
        } else {
            SwingUtilities.invokeLater(() -> new BattleFrame().setVisible(true));
        }
    }
}
