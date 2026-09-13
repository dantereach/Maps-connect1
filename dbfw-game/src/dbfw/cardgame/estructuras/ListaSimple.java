package dbfw.cardgame.estructuras;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Lista simple propia para el area de batalla de cada jugador.
 * Aqui quedan las cartas en juego y se recorren o quitan cuando salen del campo.
 * Se hizo con nodos propios en vez de {@code List} o arreglos para agregar al final en O(1), sin mover todo el contenido.
 *
 * @param <T> tipo de elemento que almacena la lista
 */
public class ListaSimple<T> implements Iterable<T> {

    /** Nodo interno de la lista: guarda un valor y una referencia al siguiente nodo. */
    private static class Nodo<T> {
        private final T valor;
        private Nodo<T> siguiente;

        private Nodo(T valor) {
            this.valor = valor;
        }
    }

    /** Primer nodo de la lista; null si esta vacia. */
    private Nodo<T> cabeza;
    /** Ultimo nodo de la lista, para poder agregar al final en O(1). */
    private Nodo<T> cola;
    /** Cantidad de elementos en la lista. */
    private int tamano;

    /** Agrega un elemento al final de la lista. Operacion O(1) gracias al puntero de cola. */
    public void agregar(T valor) {
        Nodo<T> nuevo = new Nodo<>(valor);
        if (cabeza == null) {
            cabeza = nuevo;
        } else {
            cola.siguiente = nuevo;
        }
        cola = nuevo;
        tamano++;
    }

    /**
     * Busca un elemento en la lista y lo quita reconectando los nodos.
     *
     * @param valor elemento a buscar y quitar
     * @return true si el elemento se encontro y se quito; false si no estaba en la lista
     */
    public boolean remover(T valor) {
        Nodo<T> anterior = null;
        Nodo<T> actual = cabeza;
        while (actual != null) {
            if (actual.valor.equals(valor)) {
                if (anterior == null) {
                    cabeza = actual.siguiente;
                } else {
                    anterior.siguiente = actual.siguiente;
                }
                if (actual == cola) {
                    cola = anterior;
                }
                tamano--;
                return true;
            }
            anterior = actual;
            actual = actual.siguiente;
        }
        return false;
    }

    /** @return true si la lista no tiene elementos. */
    public boolean esVacia() {
        return cabeza == null;
    }

    /** Quita todos los elementos de la lista, dejandola vacia (descarte de fin de turno). */
    public void vaciar() {
        cabeza = null;
        cola = null;
        tamano = 0;
    }

    /** @return la cantidad de elementos en la lista. */
    public int tamano() {
        return tamano;
    }

    /**
     * Crea una copia temporal solo para pasar los datos a componentes de Swing.
     * El area de batalla real sigue almacenada en esta lista enlazada.
     */
    public List<T> comoListaTemporal() {
        List<T> copia = new ArrayList<>();
        for (T valor : this) {
            copia.add(valor);
        }
        return copia;
    }

    /** Permite recorrer los elementos de la lista con un for-each, de principio a fin. */
    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private Nodo<T> actual = cabeza;

            @Override
            public boolean hasNext() {
                return actual != null;
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                T valor = actual.valor;
                actual = actual.siguiente;
                return valor;
            }
        };
    }
}
