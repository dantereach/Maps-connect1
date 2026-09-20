package dbfw.cardgame;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.border.Border;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cursor de menu navegable solo con flechas y Enter, al estilo de los menus de Undertale.
 * Organiza los controles de una pantalla en filas: Arriba/Abajo cambia de fila y
 * Izquierda/Derecha cambia de control dentro de la fila actual. Enter activa el
 * control que tiene el cursor encima (un boton se hace clic, una casilla se marca).
 * Los controles deshabilitados nunca se agregan, asi que el cursor no puede caer
 * en algo que no se puede usar en ese momento.
 */
public class MenuTeclado {
    private final List<List<JComponent>> filas = new ArrayList<>();
    private final Map<JComponent, Border> bordesOriginales = new HashMap<>();
    private int fila;
    private int columna;

    /** Vacia el menu para reconstruirlo desde cero (por ejemplo, cuando cambia la mano). */
    public void limpiar() {
        quitarResaltado();
        filas.clear();
        bordesOriginales.clear();
        fila = 0;
        columna = 0;
    }

    /** Agrega una fila horizontal de controles; se ignoran los nulos y los deshabilitados. */
    public void agregarFila(JComponent... controles) {
        List<JComponent> f = new ArrayList<>();
        for (JComponent c : controles) {
            if (c != null && c.isEnabled()) {
                f.add(c);
            }
        }
        if (!f.isEmpty()) {
            filas.add(f);
        }
    }

    /** Mueve el cursor de fila (arriba con -1, abajo con +1); da la vuelta al llegar al borde. */
    public void moverFila(int delta) {
        if (filas.isEmpty()) {
            return;
        }
        quitarResaltado();
        fila = Math.floorMod(fila + delta, filas.size());
        columna = Math.min(columna, filas.get(fila).size() - 1);
        aplicarResaltado();
    }

    /** Mueve el cursor de columna dentro de la fila actual; da la vuelta al llegar al borde. */
    public void moverColumna(int delta) {
        if (filas.isEmpty()) {
            return;
        }
        quitarResaltado();
        List<JComponent> actual = filas.get(fila);
        columna = Math.floorMod(columna + delta, actual.size());
        aplicarResaltado();
    }

    /** Simula un Enter sobre el control con el cursor encima (clic de boton, marcar casilla, etc). */
    public void activar() {
        JComponent actual = controlActual();
        if (actual instanceof AbstractButton) {
            ((AbstractButton) actual).doClick();
        }
    }

    /** @return el control que tiene el cursor encima, o null si el menu esta vacio. */
    public JComponent controlActual() {
        if (filas.isEmpty()) {
            return null;
        }
        List<JComponent> f = filas.get(fila);
        return f.get(Math.min(columna, f.size() - 1));
    }

    /** Marca visualmente el control actual como el foco del cursor y le pide el foco de Swing. */
    public void aplicarResaltado() {
        JComponent actual = controlActual();
        if (actual == null) {
            return;
        }
        bordesOriginales.putIfAbsent(actual, actual.getBorder());
        actual.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(TemaUndertale.VERDE_BRILLANTE, 3),
                bordesOriginales.get(actual)));
        actual.requestFocusInWindow();
    }

    /** Regresa el control actual a su borde original antes de mover el cursor a otro lado. */
    private void quitarResaltado() {
        JComponent actual = controlActual();
        if (actual != null) {
            Border original = bordesOriginales.get(actual);
            if (original != null) {
                actual.setBorder(original);
            }
        }
    }
}
