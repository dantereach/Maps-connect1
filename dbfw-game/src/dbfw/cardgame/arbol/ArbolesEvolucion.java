package dbfw.cardgame.arbol;

import dbfw.cardgame.CardType;
import dbfw.cardgame.GameCard;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Construye y expone, indexado por nombre de familia mediante una tabla hash
 * ({@link java.util.HashMap}), el {@link ArbolEvolucion} de cada familia de cartas del
 * catalogo: nivel 1 (basica, la que ya se usa en el mazo), nivel 2 (mejorada) y nivel 3
 * (legendaria), conservando el mismo tipo de efecto en cada nivel pero con mas poder base.
 * <p>
 * Esta clase es la que se muestra en el dialogo "Ver Arbol de Evolucion" de
 * {@code CardBattleFrame}: al elegir una familia se busca su arbol por nombre (O(1)) y se
 * recorre de forma recursiva con {@link ArbolEvolucion#recorrerCompleto}.
 */
public final class ArbolesEvolucion {

    /** Tabla hash que indexa el arbol de evolucion de cada familia por su nombre basico. */
    private static final Map<String, ArbolEvolucion<GameCard>> ARBOLES = new HashMap<>();

    static {
        registrarLinea("Guerrero Basico", "Guerrero Mejorado", "Guerrero Legendario",
                15000, 22000, 30000, CardType.BASIC);
        registrarLinea("Explorador", "Explorador Mejorado", "Explorador Legendario",
                5000, 9000, 14000, CardType.DRAW);
        registrarLinea("Guardian", "Guardian Mejorado", "Guardian Legendario",
                20000, 27000, 34000, CardType.GUARD);
        registrarLinea("Golpeador Doble", "Golpeador Mejorado", "Golpeador Legendario",
                35000, 42000, 50000, CardType.DOUBLE_STRIKE);
    }

    private ArbolesEvolucion() {
        // Clase de solo metodos estaticos: no se instancia.
    }

    /**
     * Crea el arbol de tres niveles de una familia (basica -&gt; mejorada -&gt; legendaria) y lo
     * registra en la tabla hash bajo el nombre de su nivel basico.
     */
    private static void registrarLinea(String nombreBasica, String nombreMejorada, String nombreLegendaria,
                                        int poderBasica, int poderMejorada, int poderLegendaria, CardType tipo) {
        ArbolEvolucion<GameCard> arbol = new ArbolEvolucion<>(new GameCard(nombreBasica, poderBasica, 0, tipo));
        NodoArbol<GameCard> mejorada = arbol.getRaiz().agregarHijo(new GameCard(nombreMejorada, poderMejorada, 0, tipo));
        mejorada.agregarHijo(new GameCard(nombreLegendaria, poderLegendaria, 0, tipo));
        ARBOLES.put(nombreBasica, arbol);
    }

    /**
     * Busca, por el nombre de la familia basica (tabla hash, O(1)), el arbol de evolucion
     * completo de esa familia de cartas.
     *
     * @param nombreFamiliaBasica nombre del nivel basico de la familia (ej. "Guardian")
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
