/**
 * Carta de batalla (no lider). Tiene un poder base, un costo de energia y un tipo con su efecto.
 */
public class GameCard {
    private final String name;
    private final int basePower;
    private final int cost;
    private final CardType type;
    private final int comboPower;
    private int bonus = 0; // poder extra otorgado por efectos (ej. habilidad del lider azul)
    private boolean rested = false;

    public GameCard(String name, int basePower, int cost, CardType type) {
        this.name = name;
        this.basePower = basePower;
        this.cost = cost;
        this.type = type;
        this.comboPower = calcularComboPower(type);
    }

    /** Entre mas fuerte el efecto de la carta, menos poder de combo aporta al quemarse. */
    private static int calcularComboPower(CardType type) {
        switch (type) {
            case BASIC: return 10000;         // sin efecto: el mayor poder de combo
            case DRAW: return 7000;           // efecto leve (robar)
            case GUARD: return 5000;          // efecto medio (guardia)
            case DOUBLE_STRIKE: return 3000;  // efecto fuerte (doble golpe): el menor poder de combo
            default: return 0;
        }
    }

    public int getComboPower() {
        return comboPower;
    }

    public String getName() {
        return name;
    }

    public int getBasePower() {
        return basePower;
    }

    public int getCost() {
        return cost;
    }

    public CardType getType() {
        return type;
    }

    public int getBonus() {
        return bonus;
    }

    public void addBonus(int amount) {
        this.bonus += amount;
    }

    public boolean isRested() {
        return rested;
    }

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

    public boolean isDoubleStrike() {
        return type == CardType.DOUBLE_STRIKE;
    }

    public boolean drawsOnPlay() {
        return type == CardType.DRAW;
    }

    @Override
    public String toString() {
        return name + " [" + (basePower + bonus) + (type == CardType.GUARD ? "/25000 def" : "") + "]"
                + (rested ? " (girada)" : "");
    }
}
