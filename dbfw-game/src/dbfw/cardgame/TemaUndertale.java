package dbfw.cardgame;

import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;

/**
 * Paleta de colores y utilidades de estilo compartidas por toda la interfaz del juego,
 * inspiradas en las pantallas de combate de Undertale: fondos negros con acentos verdes tipo
 * terminal, en vez de la paleta calida del tablero original. Se usa tanto en {@link CardBattleFrame}
 * como en {@link dbfw.cardgame.undertale.PanelEsquive} para que ambas pantallas compartan el
 * mismo estilo visual.
 */
public final class TemaUndertale {
    private TemaUndertale() {
        // Clase de solo constantes/metodos estaticos: no se instancia.
    }

    /** Negro de fondo, como las pantallas de combate de Undertale. */
    public static final Color FONDO = Color.BLACK;
    /** Verde principal (texto y bordes de elementos activos/interactivos). */
    public static final Color VERDE = new Color(60, 230, 110);
    /** Verde tenue (bordes de elementos no seleccionados, lineas de cuadricula). */
    public static final Color VERDE_OSCURO = new Color(15, 90, 40);
    /** Verde brillante, casi blanco (para resaltar el elemento de menu seleccionado). */
    public static final Color VERDE_BRILLANTE = new Color(190, 255, 200);
    /** Blanco-hueso: color de los proyectiles ("huesos"), como en las peleas de Undertale. */
    public static final Color HUESO = new Color(235, 235, 220);
    /** Rojo de alerta, para telegrafiar ataques peligrosos o la vida critica. */
    public static final Color ROJO_ALERTA = new Color(220, 50, 50);

    /** Fuente monoespaciada para los botones de menu, con aire de terminal retro. */
    public static final Font FUENTE_MENU = new Font("Consolas", Font.BOLD, 14);

    /** Aplica el estilo negro/verde a un boton (fondo negro, texto y borde verde). */
    public static void estilizar(JButton b) {
        b.setBackground(FONDO);
        b.setForeground(VERDE);
        b.setFont(FUENTE_MENU);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setBorder(BorderFactory.createLineBorder(VERDE_OSCURO, 2));
    }

    /** Resalta (o no) un boton de menu como la categoria/opcion actualmente seleccionada. */
    public static void marcarSeleccionado(JButton b, boolean seleccionado) {
        b.setForeground(seleccionado ? VERDE_BRILLANTE : VERDE);
        b.setBorder(BorderFactory.createLineBorder(seleccionado ? VERDE_BRILLANTE : VERDE_OSCURO, seleccionado ? 3 : 2));
    }

    /** Aplica el estilo negro/verde a una etiqueta informativa (fondo negro, texto y borde verde). */
    public static void estilizar(JLabel l) {
        l.setOpaque(true);
        l.setBackground(FONDO);
        l.setForeground(VERDE);
        l.setBorder(BorderFactory.createLineBorder(VERDE_OSCURO, 1));
    }

    /** Pinta el fondo de cualquier contenedor de negro (para que combine con el resto del tema). */
    public static void fondoNegro(JComponent c) {
        c.setOpaque(true);
        c.setBackground(FONDO);
    }
}
