package dbfw;

import dbfw.cardgame.CardBattleFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Punto de entrada del juego.
 * Abre la unica ventana del juego; la dificultad se elige ahi mismo, sin dialogos.
 */
public class Main {
    /**
     * Metodo principal: abre la interfaz grafica.
     *
     * @param args argumentos de linea de comandos (no se usan)
     */
    public static void main(String[] args) {
        // Usa el look and feel Metal para que el tema negro/verde se vea igual en cualquier sistema.
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {
            // Si falla, se usa el estilo del sistema.
        }
        SwingUtilities.invokeLater(() -> new CardBattleFrame().setVisible(true));
    }
}
