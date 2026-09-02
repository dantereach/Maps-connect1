package dbfw.cardgame.estructuras;

import dbfw.cardgame.excepciones.ColaVaciaException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Implementacion propia de una Cola (estructura FIFO - "First In, First Out") mediante nodos
 * enlazados, sin usar {@code java.util.Queue} ni {@code java.util.LinkedList}.
 * <p>
 * Se usa para representar la <b>mano</b> de cada jugador: las cartas robadas se agregan al
 * final con {@link #encolar(Object)}. Como en este juego el jugador puede jugar o quemar en
 * combo cualquier carta de su mano (no solo la primera que robo), se agrega
 * {@link #remover(Object)} como una extension practica que busca y quita un elemento especifico
 * recorriendo la cola nodo por nodo; {@link #iterator()} permite recorrerla para mostrar todas
 * las cartas en la interfaz sin exponer los nodos internos.
 *
 * @param <T> tipo de elemento que almacena la cola
 */
public class Cola<T> implements Iterable<T> {

    /** Nodo interno de la cola: guarda un valor y una referencia al siguiente nodo. */
    private static class Nodo<T> {
        private final T valor;
        private Nodo<T> siguiente;

        private Nodo(T valor) {
            this.valor = valor;
        }
    }

    /** Nodo al frente de la cola (el proximo en salir con {@link #desencolar()}). */
    private Nodo<T> frente;
    /** Nodo al final de la cola (el ultimo en entrar). */
    private Nodo<T> ultimo;
    /** Cantidad de elementos en la cola. */
    private int tamano;

    /** Agrega un elemento al final de la cola. Operacion O(1). */
    public void encolar(T valor) {
        Nodo<T> nuevo = new Nodo<>(valor);
        if (ultimo == null) {
            frente = nuevo;
        } else {
            ultimo.siguiente = nuevo;
        }
        ultimo = nuevo;
        tamano++;
    }

    /**
     * Quita y devuelve el elemento al frente de la cola. Operacion O(1).
     * @throws ColaVaciaException si la cola no tiene elementos
     */
    public T desencolar() throws ColaVaciaException {
        if (esVacia()) {
            throw new ColaVaciaException("No se puede desencolar: la cola esta vacia.");
        }
        T valor = frente.valor;
        frente = frente.siguiente;
        if (frente == null) {
            ultimo = null;
        }
        tamano--;
        return valor;
    }

    /**
     * Devuelve (sin quitar) el elemento al frente de la cola.
     * @throws ColaVaciaException si la cola no tiene elementos
     */
    public T verFrente() throws ColaVaciaException {
        if (esVacia()) {
            throw new ColaVaciaException("La cola esta vacia.");
        }
        return frente.valor;
    }

    /**
     * Busca la primera aparicion de un elemento (comparado con {@link Object#equals}) recorriendo
     * la cola nodo por nodo, sin importar si esta al frente o mas atras, y lo quita reconectando
     * los nodos vecinos. Se usa para jugar o quemar en combo una carta especifica de la mano.
     *
     * @param valor elemento a buscar y quitar
     * @return true si el elemento se encontro y se quito; false si no estaba en la cola
     */
    public boolean remover(T valor) {
        Nodo<T> anterior = null;
        Nodo<T> actual = frente;
        while (actual != null) {
            if (actual.valor.equals(valor)) {
                if (anterior == null) {
                    frente = actual.siguiente;
                } else {
                    anterior.siguiente = actual.siguiente;
                }
                if (actual == ultimo) {
                    ultimo = anterior;
                }
                tamano--;
                return true;
            }
            anterior = actual;
            actual = actual.siguiente;
        }
        return false;
    }

    /** @return true si la cola no tiene elementos. */
    public boolean esVacia() {
        return frente == null;
    }

    /** @return la cantidad de elementos en la cola. */
    public int tamano() {
        return tamano;
    }

    /** Permite recorrer los elementos de la cola de frente a final con un for-each, sin desencolarlos. */
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

    /**
     * Crea una copia de solo lectura de los elementos de la cola en un {@link java.util.ArrayList},
     * en el mismo orden de frente a final. Esto <b>no</b> es el almacenamiento real de la cola
     * (que sigue siendo la cadena de nodos enlazados): es unicamente un puente practico para
     * interoperar con componentes de Swing que requieren un arreglo o lista indexable
     * (por ejemplo {@code JOptionPane.showInputDialog} o {@code toArray}).
     *
     * @return copia temporal e independiente de los elementos de la cola
     */
    public java.util.ArrayList<T> comoListaTemporal() {
        java.util.ArrayList<T> copia = new java.util.ArrayList<>();
        for (T valor : this) {
            copia.add(valor);
        }
        return copia;
    }
}
