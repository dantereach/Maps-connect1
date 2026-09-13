package dbfw.cardgame.estructuras;

import dbfw.cardgame.excepciones.PilaVaciaException;

/**
 * Implementacion propia de una Pila (estructura LIFO - "Last In, First Out") mediante nodos
 * enlazados, sin usar {@code java.util.Stack} ni ninguna coleccion de la biblioteca estandar.
 * <p>
 * Se usa para representar el <b>mazo</b> de cada jugador: el mazo ya mezclado se apila carta
 * por carta al armarlo, y cada carta que se roba sale de la cima.
 * <p>
 * Todas las operaciones son O(1) porque solo se manipula el puntero a la cima, sin recorrer ni
 * desplazar el resto de los elementos; esa es la diferencia real,
 * donde quitar del inicio del arreglo obliga a correr todos los elementos restantes (O(n)).
 *
 * @param <T> tipo de elemento que almacena la pila
 */
public class Pila<T> {

    /** Nodo interno de la pila: guarda un valor y una referencia al nodo que quedo debajo. */
    private static class Nodo<T> {
        private final T valor;
        private Nodo<T> siguiente;

        private Nodo(T valor, Nodo<T> siguiente) {
            this.valor = valor;
            this.siguiente = siguiente;
        }
    }

    /** Referencia al nodo en la cima de la pila (el ultimo en entrar); null si esta vacia. */
    private Nodo<T> cima;
    /** Cantidad de elementos actualmente en la pila. */
    private int tamano;

    /** Agrega un elemento a la cima de la pila. Operacion O(1). */
    public void apilar(T valor) {
        cima = new Nodo<>(valor, cima);
        tamano++;
    }

    /**
     * Quita y devuelve el elemento en la cima de la pila. Operacion O(1).
     * @return el elemento que estaba en la cima
     * @throws PilaVaciaException si la pila no tiene elementos
     */
    public T desapilar() throws PilaVaciaException {
        if (esVacia()) {
            throw new PilaVaciaException("No se puede desapilar: la pila esta vacia.");
        }
        T valor = cima.valor;
        cima = cima.siguiente;
        tamano--;
        return valor;
    }

    /**
     * Devuelve (sin quitar) el elemento en la cima de la pila.
     * @throws PilaVaciaException si la pila no tiene elementos
     */
    public T verCima() throws PilaVaciaException {
        if (esVacia()) {
            throw new PilaVaciaException("La pila esta vacia.");
        }
        return cima.valor;
    }

    /** @return true si la pila no tiene elementos. */
    public boolean esVacia() {
        return cima == null;
    }

    /** @return la cantidad de elementos en la pila. */
    public int tamano() {
        return tamano;
    }
}
