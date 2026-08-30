package dbfw;

import dbfw.cardgame.CardBattleFrame;

import javax.swing.SwingUtilities;

/**
 * Punto de entrada del juego.
 * Por defecto abre el juego de cartas (estilo Dragon Ball Fusion World{}).
 *
 */
public class Main {
    /**
     * Metodo principal: elige que modo de juego lanzar segun el argumento recibido.
     * @param args argumentos de linea de comandos; ver la documentacion de la clase para las opciones.
     */
    public static void main(String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("texto")) {

        } else {
            SwingUtilities.invokeLater(() -> new CardBattleFrame().setVisible(true));
        }
    }
}
