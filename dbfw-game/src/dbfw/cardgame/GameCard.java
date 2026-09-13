package dbfw.cardgame;

/**
 * Carta de accion (ya no un "personaje") del modo hibrido Undertale/Slay the Spire.
 * <p>
 * Una carta se juega desde la mano pagando su costo de energia y su efecto se resuelve de
 * inmediato: o bien ataca directamente la vida del rival ({@link #getDanoDirecto()}, sin
 * comparar contra ninguna otra carta) o bien roba una carta del mazo ({@link #drawsOnPlay()}).
 * Tambien puede "quemarse" como carta de combo para sumar daño extra a un ataque (ver
 * {@link #getComboPower()}), descartandose permanentemente en el proceso.
 */
public class GameCard {
    /** Nombre visible de la carta (se usa en botones y en el registro de la partida). */
    private final String name;
    /** Poder base de la carta, antes de aplicar efectos de tipo. */
    private final int basePower;
    /** Costo en energia para jugar esta carta desde la mano. */
    private final int cost;
    /** Tipo de carta, que determina su efecto especial (ver {@link CardType}). */
    private final CardType type;
    /** Puntos de daño extra que aporta esta carta si se usa como combo (se calcula una vez, segun el tipo). */
    private final int comboPower;

    /**
     * Crea una carta de batalla.
     *
     * @param name      nombre de la carta
     * @param basePower poder base (sin bonos ni efectos de tipo)
     * @param cost      costo de energia para jugarla desde la mano
     * @param type      tipo de carta y su efecto asociado
     */
    public GameCard(String name, int basePower, int cost, CardType type) {
        this.name = name;
        this.basePower = basePower;
        this.cost = cost;
        this.type = type;
        this.comboPower = calcularComboPower(type);
    }

    /**
     * Calcula el poder de combo (en puntos de daño directo) segun el tipo de carta.
     * Entre mas fuerte el efecto de la carta, menos daño de combo aporta al quemarse:
     * las cartas sin efecto (BASIC) son las mejores para combo, y las de efecto mas fuerte
     * (DOUBLE_STRIKE) son las que menos aportan.
     *
     * @param type tipo de carta
     * @return puntos de daño de combo que aporta esta carta al quemarse
     */
    private static int calcularComboPower(CardType type) {
        switch (type) {
            case BASIC: return 3;          // sin efecto: el mayor aporte de combo
            case DRAW: return 2;           // efecto leve (robar)
            case GUARD: return 1;          // efecto medio (ataque fuerte)
            case DOUBLE_STRIKE: return 1;  // efecto fuerte (doble golpe): el menor aporte de combo
            default: return 0;
        }
    }

    /** @return los puntos de daño que esta carta aporta si se quema como carta de combo. */
    public int getComboPower() {
        return comboPower;
    }


    /**
     * Prioridad de resolucion del efecto de esta carta, usada por la Cola de Prioridad propia
     * ({@code ColaPrioridad}) para decidir en que orden se resuelven los efectos de las cartas
     * jugadas en un mismo turno: cuanto mas fuerte la habilidad especial, mayor la prioridad, y
     * por lo tanto se resuelve antes, sin importar el orden en que se jugaron las cartas.
     *
     * @return 3 para Golpe Doble (habilidad fuerte), 2 para Ataque Fuerte (media), 1 para
     *         Jalar Carta (leve) y 0 para las cartas basicas (sin habilidad especial).
     */
    public int getPrioridadEfecto() {
        switch (type) {
            case DOUBLE_STRIKE: return 3;
            case GUARD: return 2;
            case DRAW: return 1;
            default: return 0;
        }
    }

    /**
     * Daño directo que esta carta inflige a la vida del rival al jugarla, sin comparar contra
     * ninguna otra carta (a diferencia del antiguo sistema de bloqueo por poder): entre mayor
     * el poder base de la accion, mas vida quita. Las cartas de tipo {@link CardType#DRAW} no
     * atacan (devuelven 0; en vez de eso roban una carta, ver {@link #drawsOnPlay()}), y las de
     * tipo {@link CardType#DOUBLE_STRIKE} golpean dos veces por este mismo monto (ver
     * {@link #isDoubleStrike()}), por lo que son las que mas vida total quitan en un solo turno.
     *
     * @return puntos de vida que pierde el rival por cada golpe de esta carta
     */
    public int getDanoDirecto() {
        switch (type) {
            case DOUBLE_STRIKE: return 2; // golpea 2 veces -> 4 de daño total
            case GUARD: return 2;
            case DRAW: return 0;          // no ataca: roba una carta
            default: return 1;            // BASIC
        }
    }

    /**
     * Crea una copia nueva e independiente de esta carta (misma estadisticas base, mismo tipo)
     * con un numero agregado al nombre. Se usa junto con {@link dbfw.cardgame.CatalogoCartas}
     * para construir varias copias numeradas de una misma familia de cartas (ej. "Jalar Carta 1",
     * "Jalar Carta 2", ...) a partir de una sola plantilla indexada por nombre en la tabla hash.
     *
     * @param numero numero a agregar al nombre de la copia
     * @return una nueva carta independiente con el mismo nombre base, poder, costo y tipo
     */
    public GameCard crearCopiaNumerada(int numero) {
        return new GameCard(name + " " + numero, basePower, cost, type);
    }


    /** @return el nombre visible de la carta. */
    public String getName() {
        return name;
    }

    /** @return el poder base de la carta, sin bonos ni efectos de tipo. */
    public int getBasePower() {
        return basePower;
    }

    /** @return el costo en energia necesario para jugar esta carta desde la mano. */
    public int getCost() {
        return cost;
    }

    /** @return el tipo de la carta (define su efecto especial). */
    public CardType getType() {
        return type;
    }

    /** @return el poder base de la carta (solo para mostrarlo en la interfaz; el daño real se calcula con {@link #getDanoDirecto()}). */
    public int getEffectivePower(boolean defendiendo) {
        return basePower;
    }

    /** @return true si esta carta tiene el efecto Double Strike (2 de daño en vez de 1 si conecta). */
    public boolean isDoubleStrike() {
        return type == CardType.DOUBLE_STRIKE;
    }

    /** @return true si esta carta roba 1 carta del mazo automaticamente al ser jugada. */
    public boolean drawsOnPlay() {
        return type == CardType.DRAW;
    }

    @Override
    public String toString() {
        return name + " [dano " + getDanoDirecto() + (isDoubleStrike() ? " x2" : "") + "]";
    }
}
