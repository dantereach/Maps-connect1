package dbfw.cardgame.arbol;

import dbfw.cardgame.CardType;
import dbfw.cardgame.GameCard;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Reune los arboles de evolucion de las familias de cartas y los guarda por nombre en una tabla hash.
 * Se usa en "Ver Arbol de Evolucion": busca la familia en O(1) y luego recorre su arbol.
 */
public final class ArbolesEvolucion {

    /** Tabla hash que indexa el arbol de evolucion de cada familia por su nombre basico. */
    private static final Map<String, ArbolEvolucion<GameCard>> ARBOLES = new HashMap<>();

    static {
        registrarLinea("Ataque Basico", "Ataque Mejorado", "Ataque Legendario",
                15000, 22000, 30000, CardType.BASIC);
        registrarLinea("Jalar Carta", "Jalar Carta Mejorada", "Jalar Carta Legendaria",
                5000, 9000, 14000, CardType.DRAW);
        registrarLinea("Ataque Fuerte", "Ataque Fuerte Mejorado", "Ataque Fuerte Legendario",
                20000, 27000, 34000, CardType.GUARD);
        registrarLinea("Golpe Doble", "Golpe Doble Mejorado", "Golpe Doble Legendario",
                35000, 42000, 50000, CardType.DOUBLE_STRIKE);
    }

    private ArbolesEvolucion() {
        // Clase utilitaria: no se instancia.
    }

    /**
     * Crea la linea basica -&gt; mejorada -&gt; legendaria de una familia y la guarda en la tabla hash.
     */
    private static void registrarLinea(String nombreBasica, String nombreMejorada, String nombreLegendaria,
                                        int poderBasica, int poderMejorada, int poderLegendaria, CardType tipo) {
        ArbolEvolucion<GameCard> arbol = new ArbolEvolucion<>(new GameCard(nombreBasica, poderBasica, 0, tipo));
        NodoArbol<GameCard> mejorada = arbol.getRaiz().agregarHijo(new GameCard(nombreMejorada, poderMejorada, 0, tipo));
        mejorada.agregarHijo(new GameCard(nombreLegendaria, poderLegendaria, 0, tipo));
        ARBOLES.put(nombreBasica, arbol);
    }

    /**
     * Busca el arbol de evolucion de una familia por el nombre de su carta basica.
     *
     * @param nombreFamiliaBasica nombre del nivel basico de la familia (ej. "Ataque Fuerte")
     * @return el arbol de evolucion de esa familia
     * @throws NoSuchElementException si no hay ningun arbol registrado con ese nombre
     */
    public static ArbolEvolucion<GameCard> buscar(String nombreFamiliaBasica) {
        ArbolEvolucion<GameCard> arbol = ARBOLES.get(nombreFamiliaBasica);
        if (arbol == null) {
            throw new NoSuchElementException("No hay arbol de evolucion para '" + nombreFamiliaBasica + "'.");
        }
        return arbol;
    }

    /** @return los nombres de las familias basicas que tienen un arbol de evolucion registrado. */
    public static Set<String> nombresFamilias() {
        return Collections.unmodifiableSet(ARBOLES.keySet());
    }
}
