package dbfw.cardgame.estructuras;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Implementacion propia de una Lista Simple (enlazada, con nodos que solo apuntan "hacia
 * adelante").
 * <p>
 * Se usa para representar el <b>area de batalla</b> de cada jugador: las cartas jugadas se
 * agregan al final con  y se quitan cuando son destruidas en combate
 * con , recorriendo la lista nodo por nodo hasta encontrarlas.
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
     * Busca la primera aparicion de un elemento (comparado con recorriendo
     * la lista nodo por nodo y lo quita reconectando los nodos vecinos.
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

    /** @return la cantidad de elementos en la lista. */
    public int tamano() {
        return tamano;
    }

    /**
     * Crea una copia temporal de solo lectura, util unicamente para
     * interoperar con componentes de Swing que exigen una lista/arreglo estandar (por ejemplo,
     * las opciones. El area de batalla real sigue almacenada en esta
     * lista enlazada; la copia no reemplaza ni se reutiliza como almacenamiento del juego.
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
