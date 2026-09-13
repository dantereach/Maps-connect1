package dbfw.cardgame.estructuras;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Lista doble propia para el historial de jugadas de la partida.
 * Cada evento queda enlazado con el anterior y el siguiente para poder avanzar o retroceder por el registro.
 * Se hizo con nodos propios en vez de {@code List} o arreglos para insertar al final en O(1) y navegar sin desplazar elementos.
 *
 * @param <T> tipo de elemento que almacena la lista (en este juego, mensajes de texto)
 */
public class ListaDoble<T> implements Iterable<T> {

    /** Nodo interno de la lista: guarda un valor y referencias al nodo anterior y al siguiente. */
    private static class Nodo<T> {
        private final T valor;
        private Nodo<T> anterior;
        private Nodo<T> siguiente;

        private Nodo(T valor) {
            this.valor = valor;
        }
    }

    /** Primer nodo de la lista (el evento mas antiguo); null si esta vacia. */
    private Nodo<T> cabeza;
    /** Ultimo nodo de la lista (el evento mas reciente). */
    private Nodo<T> cola;
    /** Cantidad de elementos en la lista. */
    private int tamano;
    /** Nodo actualmente señalado por el cursor de navegacion. */
    private Nodo<T> cursor;

    /**
     * Agrega un elemento al final de la lista y mueve el cursor a ese nuevo ultimo elemento.
     */
    public void agregarFinal(T valor) {
        Nodo<T> nuevo = new Nodo<>(valor);
        if (cabeza == null) {
            cabeza = nuevo;
        } else {
            cola.siguiente = nuevo;
            nuevo.anterior = cola;
        }
        cola = nuevo;
        cursor = nuevo;
        tamano++;
    }

    /**
     * Mueve el cursor de navegacion un elemento hacia atras (mas antiguo), si es posible.
     * @return true si el cursor se movio; false si ya estaba en el primer elemento
     */
    public boolean irAnterior() {
        if (cursor != null && cursor.anterior != null) {
            cursor = cursor.anterior;
            return true;
        }
        return false;
    }

    /**
     * Mueve el cursor de navegacion un elemento hacia adelante (mas reciente), si es posible.
     * @return true si el cursor se movio; false si ya estaba en el ultimo elemento
     */
    public boolean irSiguiente() {
        if (cursor != null && cursor.siguiente != null) {
            cursor = cursor.siguiente;
            return true;
        }
        return false;
    }

    /** @return el elemento actualmente señalado por el cursor, o null si el historial esta vacio. */
    public T actual() {
        return cursor != null ? cursor.valor : null;
    }

    /** @return true si el historial no tiene elementos. */
    public boolean esVacia() {
        return cabeza == null;
    }

    /** @return la cantidad de elementos en el historial. */
    public int tamano() {
        return tamano;
    }

    /** Permite recorrer todo el historial de principio a fin con un for-each. */
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
