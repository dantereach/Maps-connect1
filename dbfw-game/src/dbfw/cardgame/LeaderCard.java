package dbfw.cardgame;

/**
 * Carta de Lider del jugador o de la CPU.
 * Tiene vida y puede transformarse de forma permanente cuando su dueño baja a 4 o menos.
 * Algunos lideres tambien tienen habilidades extra, como potenciar el siguiente ataque.
 */
public class LeaderCard {
    /** Nombre visible del lider. */
    private final String name;
    /** Poder base normal. */
    private final int basePower;
    /** Poder base ya transformado. */
    private final int transformedPower;
    /** True si el lider ya se transformo. */
    private boolean transformed = false;
    /** True si el lider ya actuo este turno. */
    private boolean rested = false;

    // Bono por mano reducida.
    /** True si este lider pega mas fuerte con poca mano. */
    private final boolean hasHandBonus;
    /** Maximo de cartas en mano para activar el bono. */
    private final int handBonusThreshold;
    /** Poder usado cuando el bono esta activo. */
    private final int handBonusPower;

    // Potenciar el siguiente ataque.
    /** True si este lider puede potenciar un ataque. */
    private final boolean canBoost;
    /** Daño extra que da la habilidad de potenciar. */
    private final int boostAmount;
    /** Costo de energia de la habilidad. */
    private final int boostCost;
    /** True si ya se uso potenciar este turno. */
    private boolean boostUsedThisTurn = false;

    /**
     * Crea una carta de Lider con sus datos y habilidades.
     *
     * @param name               nombre del lider
     * @param basePower          poder base normal
     * @param transformedPower   poder base transformado
     * @param hasHandBonus       si tiene bono por poca mano
     * @param handBonusThreshold tope de cartas para activar ese bono
     * @param handBonusPower     poder con el bono activo
     * @param canBoost           si puede potenciar el siguiente ataque
     * @param boostAmount        daño extra de potenciar
     * @param boostCost          costo de energia de potenciar
     */
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
        return new LeaderCard("Lider Azul", 15000, 20000, true, 7, 35000, true, 1, 1);
    }

    /** Lider generico y simple de la CPU, sin habilidades especiales. */
    public static LeaderCard crearLiderCpu() {
        return new LeaderCard("Lider CPU", 15000, 15000, false, 0, 0, false, 0, 0);
    }

    /** @return el nombre visible del lider. */
    public String getName() {
        return name;
    }

    /** @return true si el lider ya se transformo. */
    public boolean isTransformed() {
        return transformed;
    }

    /** @return true si el lider ya actuo este turno. */
    public boolean isRested() {
        return rested;
    }

    /** Marca al lider como ya usado o listo. */
    public void setRested(boolean rested) {
        this.rested = rested;
    }

    /** @return true si este lider tiene la habilidad de potenciar el proximo ataque. */
    public boolean canBoost() {
        return canBoost;
    }

    /** @return los puntos de daño extra que otorga la habilidad de potenciar. */
    public int getBoostAmount() {
        return boostAmount;
    }

    /** @return el costo en energia de la habilidad de potenciar. */
    public int getBoostCost() {
        return boostCost;
    }

    /** @return true si la habilidad de potenciar ya se uso en este turno. */
    public boolean isBoostUsedThisTurn() {
        return boostUsedThisTurn;
    }

    /** Marca si la habilidad de potenciar ya se uso en el turno actual. */
    public void setBoostUsedThisTurn(boolean used) {
        this.boostUsedThisTurn = used;
    }

    /** Poder base actual. */
    public int getCurrentBasePower() {
        return transformed ? transformedPower : basePower;
    }

    /** Transforma al lider si la vida llega a 4 o menos. */
    public void checkTransform(int vidaActual) {
        if (!transformed && vidaActual <= 4) {
            transformed = true;
        }
    }

    /**
     * Poder al atacar; puede aplicar el bono por poca mano.
     *
     * @param handSize cartas en mano al momento del ataque
     */
    public int getAttackPower(int handSize) {
        if (hasHandBonus && handSize <= handBonusThreshold) {
            return handBonusPower;
        }
        return getCurrentBasePower();
    }

    /** Poder al defender; no usa el bono de mano reducida. */
    public int getDefensePower() {
        return getCurrentBasePower();
    }

    /**
     * Convierte el poder de ataque del lider en daño directo.
     *
     * @param handSize cartas en mano al momento del ataque
     * @return daño directo del lider
     */
    public int getDanoAtaque(int handSize) {
        int poder = getAttackPower(handSize);
        if (poder >= 35000) {
            return 4;
        } else if (poder >= 20000) {
            return 2;
        }
        return 1;
    }

    @Override
    public String toString() {
        return name + (transformed ? " (Transformado)" : "") + " [" + getCurrentBasePower() + "]";
    }
}
