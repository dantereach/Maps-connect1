package dbfw;

import dbfw.cardgame.CardBattleFrame;
import dbfw.cardgame.Dificultad;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Punto de entrada del juego.
 * Abre la interfaz de cartas y antes pide la {@link Dificultad}.
 * Si el dialogo se cierra, usa dificultad normal.
 */
public class Main {
    /**
     * Metodo principal.
     * Si recibe "texto", deja ese modo reservado; en otro caso abre la interfaz grafica.
     *
     * @param args argumentos de linea de comandos
     */
    public static void main(String[] args) {
        // Usa el look and feel Metal para que el tema negro/verde se vea igual en cualquier sistema.
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {
            // Si falla, se usa el estilo del sistema.
        }
        if (args.length > 0 && args[0].equalsIgnoreCase("texto")) {

        } else {
            SwingUtilities.invokeLater(() -> {
                Dificultad dificultad = elegirDificultad();
                new CardBattleFrame(dificultad).setVisible(true);
            });
        }
    }

    /**
     * Muestra un dialogo para elegir la dificultad.
     * Si el usuario cancela, usa {@link Dificultad#NORMAL}.
     *
     * @return la dificultad elegida
     */
    private static Dificultad elegirDificultad() {
        Dificultad[] opciones = Dificultad.values();
        Dificultad elegida = (Dificultad) JOptionPane.showInputDialog(null,
                "Elige la dificultad de la partida:", "Tecmilenio Heroes",
                JOptionPane.PLAIN_MESSAGE, null, opciones, Dificultad.NORMAL);
        return elegida != null ? elegida : Dificultad.NORMAL;
    }
}
