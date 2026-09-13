package dbfw.cardgame.arbol;

import dbfw.cardgame.estructuras.ListaSimple;

/**
 * Nodo de un Arbol N-ario propio: guarda un valor y la lista de sus nodos hijos directos.
 * <p>
 * Los hijos se guardan en una {@link ListaSimple} propia (ya construida para el area de
 * batalla) en vez de un arreglo o de {@code java.util.List}, para mantener el arbol construido
 * enteramente con estructuras propias, sin depender de las colecciones de la biblioteca
 * estandar.
 *
 * @param <T> tipo de valor que guarda cada nodo (en este juego, una {@code GameCard} que
 *            representa un nivel de evolucion)
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
     * Agrega un nuevo nivel (nodo hijo) directamente debajo de este nodo y lo devuelve, para
     * poder seguir anidando mas niveles a partir de el (por ejemplo, agregar la version
     * "legendaria" como hijo de la "mejorada").
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
