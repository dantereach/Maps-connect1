package dbfw.cardgame.estructuras;

/**
 * Lista circular propia para el ciclo de turnos entre el jugador y la CPU.
 * El ultimo nodo vuelve al primero, asi el turno rota siempre de forma continua.
 * Se hizo con nodos propios en vez de {@code List} o arreglos para avanzar al siguiente turno en O(1), sin mover elementos.
 *
 * @param <T> tipo de elemento que almacena la lista circular (en este juego, {@code CardPlayer})
 */
public class ListaCircular<T> {

    /** Nodo interno de la lista circular: guarda un valor y una referencia al siguiente nodo. */
    private static class Nodo<T> {
        private final T valor;
        private Nodo<T> siguiente;

        private Nodo(T valor) {
            this.valor = valor;
        }
    }

    /** Nodo actualmente activo del anillo (de quien es el turno). */
    private Nodo<T> actual;
    /** Ultimo nodo agregado, para poder cerrar el anillo al insertar uno nuevo. */
    private Nodo<T> ultimoAgregado;
    /** Cantidad de elementos en el anillo. */
    private int tamano;

    /**
     * Agrega un elemento al anillo y mantiene el ciclo cerrado.
     */
    public void agregar(T valor) {
        Nodo<T> nuevo = new Nodo<>(valor);
        if (actual == null) {
            actual = nuevo;
            nuevo.siguiente = nuevo;
            ultimoAgregado = nuevo;
        } else {
            nuevo.siguiente = actual;
            ultimoAgregado.siguiente = nuevo;
            ultimoAgregado = nuevo;
        }
        tamano++;
    }

    /** Avanza el puntero "actual" al siguiente elemento del anillo, rotando el ciclo. */
    public void avanzar() {
        if (actual != null) {
            actual = actual.siguiente;
        }
    }

    /** @return el elemento actualmente activo del ciclo (de quien es el turno), o null si esta vacio. */
    public T actual() {
        return actual != null ? actual.valor : null;
    }

    /** @return la cantidad de elementos en el anillo. */
    public int tamano() {
        return tamano;
    }
}
