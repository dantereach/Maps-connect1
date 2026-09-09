package dbfw.cardgame.arbol;

import dbfw.cardgame.estructuras.ListaSimple;
import java.util.function.Function;

/**
 * Arbol propio (con nodos y referencias a hijos, sin usar {@code java.util.TreeMap} ni ninguna
 * otra clase de arbol de la biblioteca estandar) que representa la linea de evolucion de una
 * familia de cartas: nivel 1 (basica) -&gt; nivel 2 (mejorada) -&gt; nivel 3 (legendaria), y que
 * permite seguir agregando niveles mas alla de esos tres con {@link NodoArbol#agregarHijo}.
 * <p>
 * El recorrido completo del arbol ({@link #recorrerCompleto}) es <b>recursivo real</b>: visita
 * el nodo actual y luego se llama a si mismo sobre cada uno de sus hijos (un subarbol mas
 * pequeño que el original), hasta llegar al caso base de un nodo sin hijos, en vez de simular el
 * recorrido con una pila o cola manual y un ciclo.
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
     * Recorre el arbol completo en preorden (primero el nodo, luego sus hijos de izquierda a
     * derecha), devolviendo una linea de texto por nodo indentada segun su profundidad.
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
     * Metodo recursivo real: se llama a si mismo una vez por cada hijo del nodo actual, sobre
     * un subarbol cada vez mas pequeño, hasta que un nodo no tiene hijos (caso base, en el que
     * el for-each simplemente no itera y esa rama de la recursion termina).
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
            recorrerRecursivo(hijo, profundidad + 1, formato, resultado); // llamada recursiva sobre un subarbol menor
        }
    }
}
