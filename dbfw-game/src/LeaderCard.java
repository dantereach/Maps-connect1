/**
 * Carta de Lider. El lider azul (jugador) tiene habilidades especiales;
 * la CPU usa un lider generico simple.
 */
public class LeaderCard {
    private final String name;
    private final int basePower;
    private final int transformedPower;
    private boolean transformed = false;
    private boolean rested = false;

    // Habilidad: ataca con mas poder si la mano tiene pocas cartas.
    private final boolean hasHandBonus;
    private final int handBonusThreshold;
    private final int handBonusPower;

    // Habilidad: puede agregar poder a una carta propia (una vez por turno).
    private final boolean canBoost;
    private final int boostAmount;
    private final int boostCost;
    private boolean boostUsedThisTurn = false;

    public LeaderCard(String name, int basePower, int transformedPower,
                       boolean hasHandBonus, int handBonusThreshold, int handBonusPower,
                       boolean canBoost, int boostAmount, int boostCost) {
        this.name = name;
        this.basePower = basePower;
        this.transformedPower = transformedPower;
        this.hasHandBonus = hasHandBonus;
        this.handBonusThreshold = handBonusThreshold;
        this.handBonusPower = handBonusPower;
        this.canBoost = canBoost;
        this.boostAmount = boostAmount;
        this.boostCost = boostCost;
    }

    /** Lider azul del jugador con todas sus habilidades especiales. */
    public static LeaderCard crearLiderAzul() {
        return new LeaderCard("Lider Azul", 15000, 20000, true, 7, 35000, true, 5000, 1);
    }

    /** Lider generico y simple de la CPU, sin habilidades especiales. */
    public static LeaderCard crearLiderCpu() {
        return new LeaderCard("Lider CPU", 15000, 15000, false, 0, 0, false, 0, 0);
    }

    public String getName() {
        return name;
    }

    public boolean isTransformed() {
        return transformed;
    }

    public boolean isRested() {
        return rested;
    }

    public void setRested(boolean rested) {
        this.rested = rested;
    }

    public boolean canBoost() {
        return canBoost;
    }

    public int getBoostAmount() {
        return boostAmount;
    }

    public int getBoostCost() {
        return boostCost;
    }

    public boolean isBoostUsedThisTurn() {
        return boostUsedThisTurn;
    }

    public void setBoostUsedThisTurn(boolean used) {
        this.boostUsedThisTurn = used;
    }

    /** Poder base actual (considerando si ya se transformo). */
    public int getCurrentBasePower() {
        return transformed ? transformedPower : basePower;
    }

    /** Revisa la vida actual y transforma al lider si corresponde (vida <= 4). */
    public void checkTransform(int vidaActual) {
        if (!transformed && vidaActual <= 4) {
            transformed = true;
        }
    }

    /** Poder de ataque, considerando el bono por mano reducida (solo aplica al atacar). */
    public int getAttackPower(int handSize) {
        if (hasHandBonus && handSize <= handBonusThreshold) {
            return handBonusPower;
        }
        return getCurrentBasePower();
    }

    /** Poder de defensa (bloqueo): no aplica el bono de mano reducida. */
    public int getDefensePower() {
        return getCurrentBasePower();
    }

    @Override
    public String toString() {
        return name + (transformed ? " (Transformado)" : "") + " [" + getCurrentBasePower() + "]";
    }
}
