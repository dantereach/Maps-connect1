package dbfw.cardgame;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Catalogo de las acciones de carta disponibles en el juego, indexado por nombre mediante
 * una tabla hash ({@link java.util.HashMap}) para poder buscar la plantilla de una familia
 * al instante (O(1) en promedio) en vez de recorrer una lista completa comparando nombres
 * uno por uno.
 * <p>
 * Cada entrada es una "plantilla": una carta-accion con las estadisticas base de su familia
 * (poder, costo y tipo). {@link CardPlayer#buildDeck()} usa este catalogo para construir el
 * mazo de 24 cartas buscando cada familia por su nombre y generando copias numeradas con
 * {@link GameCard#crearCopiaNumerada(int)}, en vez de repetir manualmente los datos de cada
 * carta con {@code new GameCard(...)}. Las 4 familias representan las acciones del jugador
 * (hibrido Undertale/Slay the Spire): "Ataque Basico" y "Ataque Fuerte" atacan de inmediato
 * la vida del rival, "Jalar Carta" roba del mazo y "Golpe Doble" ataca dos veces seguidas.
 */
public final class CatalogoCartas {

    /** Tabla hash que indexa cada plantilla de carta por el nombre de su familia. */
    private static final Map<String, GameCard> PLANTILLAS = new HashMap<>();

    static {
        PLANTILLAS.put("Ataque Basico", new GameCard("Ataque Basico", 15000, 2, CardType.BASIC));
        PLANTILLAS.put("Jalar Carta", new GameCard("Jalar Carta", 5000, 1, CardType.DRAW));
        PLANTILLAS.put("Ataque Fuerte", new GameCard("Ataque Fuerte", 20000, 3, CardType.GUARD));
        PLANTILLAS.put("Golpe Doble", new GameCard("Golpe Doble", 35000, 4, CardType.DOUBLE_STRIKE));
    }

    private CatalogoCartas() {
        // Clase de solo metodos estaticos: no se instancia.
    }

    /**
     * Busca la plantilla de una familia de cartas por su nombre exacto (busqueda O(1) en la
     * tabla hash, sin recorrer ninguna lista).
     *
     * @param nombreFamilia nombre de la familia (ej. "Ataque Fuerte")
     * @return la carta plantilla de esa familia
     * @throws NoSuchElementException si no existe ninguna familia registrada con ese nombre
     */
    public static GameCard buscar(String nombreFamilia) {
        GameCard plantilla = PLANTILLAS.get(nombreFamilia);
        if (plantilla == null) {
            throw new NoSuchElementException("No existe una familia de cartas llamada '" + nombreFamilia + "'.");
        }
        return plantilla;
    }

    /** @return los nombres de todas las familias de cartas registradas en el catalogo. */
    public static Set<String> nombresFamilias() {
        return Collections.unmodifiableSet(PLANTILLAS.keySet());
    }
}
