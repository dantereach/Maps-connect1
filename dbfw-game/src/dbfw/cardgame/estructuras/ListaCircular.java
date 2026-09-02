package dbfw.cardgame.estructuras;

/**
 * Implementacion propia de una Lista Circular (el ultimo nodo enlaza de vuelta al primero, sin
 * ningun extremo en null) mediante nodos enlazados, sin usar ninguna coleccion de la biblioteca
 * estandar.
 * <p>
 * Se usa para representar el <b>ciclo de turnos</b> entre el jugador humano y la CPU: en vez de
 * alternar manualmente entre dos variables, se arma un anillo con ambos jugadores y se rota con
 *  cada vez que termina el turno de alguien; en todo momento se puede
 * consultar de quien es el turno con . Al tener solo dos elementos, el anillo
 * siempre vuelve exactamente al mismo jugador que empezo, mostrando el ciclo que rota en cada
 * ronda de la partida.
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
     * Agrega un elemento al anillo, conectandolo de forma que el ciclo se mantenga siempre
     * cerrado (el nuevo ultimo nodo vuelve a apuntar al primero).
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
