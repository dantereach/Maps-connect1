package dbfw.cardgame.estructuras;

import dbfw.cardgame.excepciones.ColaPrioridadVaciaException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Implementacion propia de una Cola de Prioridad mediante una lista de nodos enlazados que se
 * mantiene siempre ordenada de mayor a menor prioridad (insercion ordenada), sin usar
 * {@code java.util.PriorityQueue} ni ninguna otra coleccion de la biblioteca estandar.
 * <p>
 * Se usa para las cartas con habilidad especial: cuando un jugador juega varias cartas en su
 * fase de juego (ver {@code CardPlayer#encolarEfectoDeCarta}), sus efectos no se resuelven de
 * inmediato ni en el orden en que se jugaron, sino que se encolan aqui con la prioridad de su
 * habilidad ({@code GameCard#getPrioridadEfecto()}) y se resuelven de mayor a menor prioridad:
 * las cartas con la habilidad mas fuerte (Double Strike) siempre se resuelven antes que las de
 * habilidad media (Guardia), estas antes que las de habilidad leve (Robo), y estas antes que las
 * basicas (sin habilidad) — sin importar cual se jugo primero.
 * <p>
 * A diferencia de un monticulo binario (heap) clasico, que se implementa sobre un arreglo, esta
 * version usa nodos enlazados para mantener la misma filosofia de construccion "a mano" que el
 * resto de las estructuras del proyecto: {@link #encolar} inserta el nuevo nodo en su posicion
 * ordenada recorriendo la cadena de nodos (O(n)), y {@link #desencolar} siempre quita el nodo al
 * frente, que es el de mayor prioridad (O(1)).
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
     * Agrega un elemento con su prioridad asociada. Un numero de prioridad mas alto significa
     * que sale primero. Si dos elementos tienen la misma prioridad, se respeta el orden en que
     * se encolaron (el mas antiguo sale primero entre los de igual prioridad).
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
