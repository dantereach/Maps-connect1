package dbfw.cardgame;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Catalogo de plantillas de carta.
 * Usa un {@link HashMap} para guardar cada familia por nombre y encontrarla al instante.
 * {@link CardPlayer} toma estas plantillas para armar el mazo y crear copias numeradas.
 */
public final class CatalogoCartas {

    /** Mapa que guarda cada plantilla por nombre de familia. */
    private static final Map<String, GameCard> PLANTILLAS = new HashMap<>();

    static {
        PLANTILLAS.put("Ataque Basico", new GameCard("Ataque Basico", 15000, 2, CardType.BASIC));
        PLANTILLAS.put("Jalar Carta", new GameCard("Jalar Carta", 5000, 1, CardType.DRAW));
        PLANTILLAS.put("Ataque Fuerte", new GameCard("Ataque Fuerte", 20000, 3, CardType.GUARD));
        PLANTILLAS.put("Golpe Doble", new GameCard("Golpe Doble", 35000, 4, CardType.DOUBLE_STRIKE));
    }

    private CatalogoCartas() {
        // Clase utilitaria: no se instancia.
    }

    /**
     * Busca una familia por su nombre exacto.
     *
     * @param nombreFamilia nombre de la familia
     * @return la plantilla encontrada
     * @throws NoSuchElementException si no existe esa familia
     */
    public static GameCard buscar(String nombreFamilia) {
        GameCard plantilla = PLANTILLAS.get(nombreFamilia);
        if (plantilla == null) {
            throw new NoSuchElementException("No existe una familia de cartas llamada '" + nombreFamilia + "'.");
        }
        return plantilla;
    }

    /** @return los nombres de todas las familias del catalogo. */
    public static Set<String> nombresFamilias() {
        return Collections.unmodifiableSet(PLANTILLAS.keySet());
    }
}
