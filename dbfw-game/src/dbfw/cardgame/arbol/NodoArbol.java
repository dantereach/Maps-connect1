package dbfw.cardgame.arbol;

import dbfw.cardgame.estructuras.ListaSimple;

/**
 * Nodo de un arbol n-ario propio.
 * Guarda un valor y sus hijos directos en una {@link ListaSimple} del proyecto.
 *
 * @param <T> tipo de valor que guarda cada nodo
 */
public class NodoArbol<T> {
    /** Valor guardado en este nodo (por ejemplo, la carta de este nivel de evolucion). */
    private final T valor;
    /** Hijos directos de este nodo (Lista Simple propia). */
    private final ListaSimple<NodoArbol<T>> hijos = new ListaSimple<>();

    public NodoArbol(T valor) {
        this.valor = valor;
    }

    /** @return el valor guardado en este nodo. */
    public T getValor() {
        return valor;
    }

    /** @return los nodos hijos directos de este nodo (Lista Simple propia). */
    public ListaSimple<NodoArbol<T>> getHijos() {
        return hijos;
    }

    /**
     * Agrega un nodo hijo debajo de este nodo y lo devuelve.
     * Sirve para seguir creando mas niveles a partir de ese hijo.
     *
     * @param valorHijo valor del nuevo nodo hijo
     * @return el nodo hijo recien creado y agregado
     */
    public NodoArbol<T> agregarHijo(T valorHijo) {
        NodoArbol<T> nuevo = new NodoArbol<>(valorHijo);
        hijos.agregar(nuevo);
        return nuevo;
    }
}
