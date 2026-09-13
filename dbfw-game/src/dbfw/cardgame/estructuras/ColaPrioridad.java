package dbfw.cardgame.estructuras;

import dbfw.cardgame.excepciones.ColaPrioridadVaciaException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Cola de prioridad propia para los efectos pendientes de las cartas, ordenados de mayor a menor prioridad.
 * Asi los efectos se resuelven por prioridad y no solo por el orden en que se jugaron las cartas.
 * Se hizo con nodos propios en vez de {@code PriorityQueue}, {@code List} o arreglos para mantener el orden por enlaces y sacar el frente en O(1), sin desplazar elementos.
 *
 * @param <T> tipo de elemento que almacena la cola de prioridad
 */
public class ColaPrioridad<T> implements Iterable<T> {

    /** Nodo interno: guarda un valor, su prioridad y una referencia al siguiente nodo. */
    private static class Nodo<T> {
        private final T valor;
        private final int prioridad;
        private Nodo<T> siguiente;

        private Nodo(T valor, int prioridad) {
            this.valor = valor;
            this.prioridad = prioridad;
        }
    }

    /** Nodo de mayor prioridad (el proximo en salir con {@link #desencolar()}). */
    private Nodo<T> frente;
    /** Cantidad de elementos en la cola de prioridad. */
    private int tamano;

    /**
     * Agrega un elemento con su prioridad.
     * Un numero mas alto sale primero, y si hay empate se respeta el orden de llegada.
     *
     * @param valor     elemento a encolar
     * @param prioridad prioridad del elemento (mayor = se resuelve antes)
     */
    public void encolar(T valor, int prioridad) {
        Nodo<T> nuevo = new Nodo<>(valor, prioridad);
        if (frente == null || prioridad > frente.prioridad) {
            nuevo.siguiente = frente;
            frente = nuevo;
            tamano++;
            return;
        }
        Nodo<T> actual = frente;
        while (actual.siguiente != null && actual.siguiente.prioridad >= prioridad) {
            actual = actual.siguiente;
        }
        nuevo.siguiente = actual.siguiente;
        actual.siguiente = nuevo;
        tamano++;
    }

    /**
     * Quita y devuelve el elemento de mayor prioridad. Operacion O(1) gracias a que la cola se
     * mantiene siempre ordenada desde la insercion.
     * @throws ColaPrioridadVaciaException si la cola de prioridad no tiene elementos
     */
    public T desencolar() throws ColaPrioridadVaciaException {
        if (esVacia()) {
            throw new ColaPrioridadVaciaException("No se puede desencolar: la cola de prioridad esta vacia.");
        }
        T valor = frente.valor;
        frente = frente.siguiente;
        tamano--;
        return valor;
    }

    /**
     * Devuelve (sin quitar) el elemento de mayor prioridad.
     * @throws ColaPrioridadVaciaException si la cola de prioridad no tiene elementos
     */
    public T verFrente() throws ColaPrioridadVaciaException {
        if (esVacia()) {
            throw new ColaPrioridadVaciaException("La cola de prioridad esta vacia.");
        }
        return frente.valor;
    }

    /** @return true si la cola de prioridad no tiene elementos. */
    public boolean esVacia() {
        return frente == null;
    }

    /** @return la cantidad de elementos en la cola de prioridad. */
    public int tamano() {
        return tamano;
    }

    /** Permite recorrer los elementos de mayor a menor prioridad con un for-each, sin quitarlos. */
    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private Nodo<T> actual = frente;

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
