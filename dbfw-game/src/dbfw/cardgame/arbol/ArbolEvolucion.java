package dbfw.cardgame.arbol;

import dbfw.cardgame.estructuras.ListaSimple;
import java.util.function.Function;

/**
 * Arbol propio que representa la linea de evolucion de una familia de cartas.
 * Parte de la carta base y enlaza sus versiones mejoradas como hijos.
 * El recorrido completo es recursivo de verdad: visita el nodo actual y luego se llama a si mismo una vez por cada hijo.
 *
 * @param <T> tipo de valor que guarda cada nivel del arbol (en este juego, {@code GameCard})
 */
public class ArbolEvolucion<T> {
    /** Nodo raiz del arbol: el nivel 1, la version basica de la familia. */
    private final NodoArbol<T> raiz;

    public ArbolEvolucion(T valorRaiz) {
        this.raiz = new NodoArbol<>(valorRaiz);
    }

    /** @return el nodo raiz del arbol (nivel 1, la version basica). */
    public NodoArbol<T> getRaiz() {
        return raiz;
    }

    /**
     * Recorre todo el arbol en preorden y arma una linea de texto por nodo con sangria segun la profundidad.
     *
     * @param formato funcion que convierte el valor de un nodo en el texto a mostrar
     * @return una Lista Simple propia con una linea de texto por nodo, en orden de recorrido
     */
    public ListaSimple<String> recorrerCompleto(Function<T, String> formato) {
        ListaSimple<String> resultado = new ListaSimple<>();
        recorrerRecursivo(raiz, 0, formato, resultado);
        return resultado;
    }

    /**
     * Metodo recursivo real: procesa el nodo actual y luego se llama a si mismo una vez por cada hijo.
     * La recursion termina cuando un nodo ya no tiene hijos.
     *
     * @param nodo        nodo actual a visitar
     * @param profundidad nivel de profundidad del nodo actual (0 para la raiz)
     * @param formato     funcion para convertir el valor del nodo en texto
     * @param resultado   lista donde se va acumulando una linea de texto por nodo visitado
     */
    private void recorrerRecursivo(NodoArbol<T> nodo, int profundidad, Function<T, String> formato,
                                    ListaSimple<String> resultado) {
        StringBuilder linea = new StringBuilder();
        for (int i = 0; i < profundidad; i++) {
            linea.append("  ");
        }
        if (profundidad > 0) {
            linea.append("|- ");
        }
        linea.append(formato.apply(nodo.getValor()));
        resultado.agregar(linea.toString());

        for (NodoArbol<T> hijo : nodo.getHijos()) {
            recorrerRecursivo(hijo, profundidad + 1, formato, resultado); // llamada recursiva sobre cada hijo
        }
    }
}
