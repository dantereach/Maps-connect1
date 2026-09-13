package dbfw.cardgame;

/**
 * Carta de accion de un solo uso.
 * No representa un personaje: al jugarse ataca directo o activa un efecto inmediato.
 * Su poder de combo es inverso a lo fuerte de su efecto, y su prioridad sirve para la
 * {@code ColaPrioridad} de efectos.
 */
public class GameCard {
    /** Nombre visible de la carta (se usa en botones y en el registro de la partida). */
    private final String name;
    /** Poder base de la carta. */
    private final int basePower;
    /** Costo de energia para jugarla. */
    private final int cost;
    /** Tipo de carta. */
    private final CardType type;
    /** Daño extra que aporta si se usa como combo. */
    private final int comboPower;

    /**
     * Crea una carta de accion.
     *
     * @param name      nombre de la carta
     * @param basePower poder base
     * @param cost      costo de energia
     * @param type      tipo de carta
     */
    public GameCard(String name, int basePower, int cost, CardType type) {
        this.name = name;
        this.basePower = basePower;
        this.cost = cost;
        this.type = type;
        this.comboPower = calcularComboPower(type);
    }

    /**
     * Calcula el poder de combo segun el tipo.
     * Entre mas fuerte es el efecto, menos combo aporta.
     *
     * @param type tipo de carta
     * @return daño extra al usarla como combo
     */
    private static int calcularComboPower(CardType type) {
        switch (type) {
            case BASIC: return 3;          // sin efecto, mejor combo
            case DRAW: return 2;           // efecto leve
            case GUARD: return 1;          // efecto medio
            case DOUBLE_STRIKE: return 1;  // efecto fuerte
            default: return 0;
        }
    }

    /** @return los puntos de daño que esta carta aporta si se quema como carta de combo. */
    public int getComboPower() {
        return comboPower;
    }


    /**
     * Devuelve la prioridad del efecto de la carta.
     * Entre mas fuerte es la habilidad, antes se resuelve.
     *
     * @return prioridad del efecto
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
     * Daño directo que hace la carta al jugarse.
     * Las cartas {@link CardType#DRAW} no dañan y las {@link CardType#DOUBLE_STRIKE} golpean dos veces.
     *
     * @return daño por golpe
     */
    public int getDanoDirecto() {
        switch (type) {
            case DOUBLE_STRIKE: return 2; // golpea 2 veces
            case GUARD: return 2;
            case DRAW: return 0;          // roba una carta
            default: return 1;            // basica
        }
    }

    /**
     * Crea una copia de la carta con un numero agregado al nombre.
     *
     * @param numero numero para el nombre
     * @return una nueva carta con los mismos datos base
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

    /** @return el poder base de la carta para mostrar en la interfaz. */
    public int getEffectivePower(boolean defendiendo) {
        return basePower;
    }

    /** @return true si esta carta golpea dos veces. */
    public boolean isDoubleStrike() {
        return type == CardType.DOUBLE_STRIKE;
    }

    /** @return true si esta carta roba 1 carta al jugarse. */
    public boolean drawsOnPlay() {
        return type == CardType.DRAW;
    }

    @Override
    public String toString() {
        return name + " [dano " + getDanoDirecto() + (isDoubleStrike() ? " x2" : "") + "]";
    }
}
