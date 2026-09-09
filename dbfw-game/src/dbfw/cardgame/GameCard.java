package dbfw.cardgame;

/**
 * Carta de batalla (no lider) del modo "juego de cartas".
 * <p>
 * Una  puede jugarse desde la mano al area de batalla (pagando su costo de
 * energia) para luego atacar o bloquear, o bien puede "quemarse" como carta de combo (ver
 *  para sumar poder extra a un ataque o a una defensa, descartandose
 * permanentemente en el proceso.
 */
public class GameCard {
    /** Nombre visible de la carta (se usa en botones y en el registro de la partida). */
    private final String name;
    /** Poder base de la carta, antes de aplicar bonos o efectos de tipo. */
    private final int basePower;
    /** Costo en energia para jugar esta carta desde la mano. */
    private final int cost;
    /** Tipo de carta, que determina su efecto especial (ver {@link CardType}). */
    private final CardType type;
    /** Poder que aporta esta carta si se usa como combo (se calcula una vez, segun el tipo). */
    private final int comboPower;
    /** Poder extra otorgado por efectos (por ejemplo, la habilidad de potenciar del lider azul). */
    private int bonus = 0;
    /** True si la carta ya ataco o bloqueo este turno y no puede volver a actuar hasta enderezarse. */
    private boolean rested = false;

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
     * Calcula el poder de combo segun el tipo de carta.
     * Entre mas fuerte el efecto de la carta, menos poder de combo aporta al quemarse:
     * las cartas sin efecto (BASIC) son las mejores para combo, y las de efecto mas fuerte
     * (DOUBLE_STRIKE) son las que menos aportan.
     *
     * @param type tipo de carta
     * @return poder de combo (en unidades de poder, ej. 10000)
     */
    private static int calcularComboPower(CardType type) {
        switch (type) {
            case BASIC: return 10000;         // sin efecto: el mayor poder de combo
            case DRAW: return 7000;           // efecto leve (robar)
            case GUARD: return 5000;          // efecto medio (guardia)
            case DOUBLE_STRIKE: return 3000;  // efecto fuerte (doble golpe): el menor poder de combo
            default: return 0;
        }
    }

    /** @return el poder que esta carta aporta si se quema como carta de combo. */
    public int getComboPower() {
        return comboPower;
    }

    /**
     * Prioridad de resolucion del efecto de esta carta, usada por la Cola de Prioridad propia
     * ({@code ColaPrioridad}) para decidir en que orden se resuelven los efectos de las cartas
     * jugadas en un mismo turno: cuanto mas fuerte la habilidad especial, mayor la prioridad, y
     * por lo tanto se resuelve antes, sin importar el orden en que se jugaron las cartas.
     *
     * @return 3 para Double Strike (habilidad fuerte), 2 para Guardia (media), 1 para Robo (leve)
     *         y 0 para las cartas basicas (sin habilidad especial).
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
     * Crea una copia nueva e independiente de esta carta (misma estadisticas base, mismo tipo)
     * con un numero agregado al nombre. Se usa junto con {@link dbfw.cardgame.CatalogoCartas}
     * para construir varias copias numeradas de una misma familia de cartas (ej. "Explorador 1",
     * "Explorador 2", ...) a partir de una sola plantilla indexada por nombre en la tabla hash.
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

    /** @return el poder extra acumulado por efectos (ej. la habilidad de potenciar del lider). */
    public int getBonus() {
        return bonus;
    }

    /** Suma poder extra permanente a esta carta (usado por la habilidad de potenciar del lider azul). */
    public void addBonus(int amount) {
        this.bonus += amount;
    }

    /** @return true si la carta ya actuo este turno (atacar o bloquear) y no puede volver a hacerlo. */
    public boolean isRested() {
        return rested;
    }

    /** Marca o desmarca la carta como "girada" (ya actuo este turno). */
    public void setRested(boolean rested) {
        this.rested = rested;
    }

    /**
     * Poder efectivo de la carta en este momento.
     * @param defendiendo true si esta carta esta defendiendo (es el turno del oponente).
     */
    public int getEffectivePower(boolean defendiendo) {
        int poder = basePower + bonus;
        if (type == CardType.GUARD && defendiendo) {
            poder += 5000; // 20000 -> 25000 al defender
        }
        return poder;
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
        return name + " [" + (basePower + bonus) + (type == CardType.GUARD ? "/25000 def" : "") + "]"
                + (rested ? " (girada)" : "");
    }
}
