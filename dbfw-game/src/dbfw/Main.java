package dbfw;

import dbfw.cardgame.CardBattleFrame;
import dbfw.cardgame.Dificultad;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Punto de entrada del juego.
 * Por defecto abre el juego de cartas (estilo Dragon Ball Fusion World{}), pidiendo antes la
 * dificultad de la partida (ver {@link Dificultad}).
 *
 */
public class Main {
    /**
     * Metodo principal: elige que modo de juego lanzar segun el argumento recibido.
     * @param args argumentos de linea de comandos; ver la documentacion de la clase para las opciones.
     */
    public static void main(String[] args) {
        // Se fuerza el look and feel "Metal" (multiplataforma) para que los colores negro/verde
        // del tema estilo Undertale (ver TemaUndertale) se pinten igual en botones y paneles sin
        // importar el sistema operativo; algunos look and feel nativos ignoran el color de fondo
        // personalizado de los JButton.
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {
            // Si falla, simplemente se usa el look and feel por defecto del sistema.
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
     * Muestra un dialogo de seleccion antes de iniciar la partida para elegir la dificultad
     * (ver {@link Dificultad}: afecta el mazo de la CPU, su IA de combo y la fase de esquive
     * del ataque del Lider CPU). Si el jugador cierra el dialogo sin elegir, se usa Normal.
     *
     * @return la dificultad elegida por el jugador
     */
    private static Dificultad elegirDificultad() {
        Dificultad[] opciones = Dificultad.values();
        Dificultad elegida = (Dificultad) JOptionPane.showInputDialog(null,
                "Elige la dificultad de la partida:", "Tecmilenio Heroes",
                JOptionPane.PLAIN_MESSAGE, null, opciones, Dificultad.NORMAL);
        return elegida != null ? elegida : Dificultad.NORMAL;
    }
}
