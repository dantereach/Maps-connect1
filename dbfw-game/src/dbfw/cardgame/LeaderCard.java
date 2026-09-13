package dbfw.cardgame;

/**
 * Carta de Lider del modo hibrido Undertale/Slay the Spire.
 * <p>
 * El lider representa al jugador (o a la CPU) en la mesa. En el turno del jugador humano, su
 * ataque quita vida de inmediato (sin bloqueo); en el turno de la CPU, su ataque es la fase de
 * esquive estilo Undertale (ver {@code CardBattleFrame#turnoCpu}). En el caso del lider azul,
 * tiene tres habilidades especiales configurables mediante el constructor:
 * <ol>
 *   <li><b>Transformacion</b>: cuando la vida de su dueño cae a 4 o menos, el lider se da la
 *       vuelta y su poder base cambia de {@code basePower} a {@code transformedPower}.</li>
 *   <li><b>Bono por mano reducida</b>: si al momento de atacar la mano tiene
 *       {@code handBonusThreshold} cartas o menos, ataca con {@code handBonusPower} en vez de
 *       su poder normal (su golpe mas fuerte, ver {@link #getDanoAtaque(int)}).</li>
 *   <li><b>Potenciar</b>: una vez por turno, puede sumar {@code boostAmount} de daño extra al
 *       proximo ataque del jugador (carta o lider), pagando {@code boostCost} de energia.</li>
 * </ol>
 * El lider de la CPU se crea sin ninguna de estas habilidades ({@link #crearLiderCpu()}).
 */
public class LeaderCard {
    /** Nombre visible del lider. */
    private final String name;
    /** Poder base antes de transformarse. */
    private final int basePower;
    /** Poder base una vez transformado (vida <= 4). */
    private final int transformedPower;
    /** True una vez que el lider se transformo (la transformacion es permanente). */
    private boolean transformed = false;
    /** True si el lider ya ataco este turno. */
    private boolean rested = false;

    // Habilidad: ataca con mas poder si la mano tiene pocas cartas.
    /** True si este lider tiene la habilidad de atacar mas fuerte con la mano reducida. */
    private final boolean hasHandBonus;
    /** Cantidad maxima de cartas en mano para que aplique el bono de ataque. */
    private final int handBonusThreshold;
    /** Poder de ataque cuando aplica el bono de mano reducida. */
    private final int handBonusPower;

    // Habilidad: puede sumar daño extra al proximo ataque del jugador (una vez por turno).
    /** True si este lider puede usar la habilidad de potenciar el proximo ataque. */
    private final boolean canBoost;
    /** Puntos de daño extra que otorga la habilidad de potenciar al proximo ataque. */
    private final int boostAmount;
    /** Costo en energia de la habilidad de potenciar. */
    private final int boostCost;
    /** True si ya se uso la habilidad de potenciar en el turno actual (se resetea cada turno). */
    private boolean boostUsedThisTurn = false;

    /**
     * Crea una carta de Lider con las habilidades indicadas.
     *
     * @param name               nombre del lider
     * @param basePower          poder base antes de transformarse
     * @param transformedPower   poder base despues de transformarse (vida <= 4)
     * @param hasHandBonus       si tiene la habilidad de atacar mas fuerte con poca mano
     * @param handBonusThreshold cartas en mano (o menos) para activar el bono de ataque
     * @param handBonusPower     poder de ataque cuando el bono esta activo
     * @param canBoost           si puede potenciar el proximo ataque una vez por turno
     * @param boostAmount        puntos de daño extra que otorga la habilidad de potenciar
     * @param boostCost          costo en energia de la habilidad de potenciar
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

    /** @return true si el lider ya se transformo (vida cayo a 4 o menos alguna vez). */
    public boolean isTransformed() {
        return transformed;
    }

    /** @return true si el lider ya actuo este turno (atacar o bloquear). */
    public boolean isRested() {
        return rested;
    }

    /** Marca o desmarca al lider como "girado" (ya actuo este turno). */
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

    /**
     * Poder de ataque, considerando el bono por mano reducida (solo aplica al atacar).
     * @param handSize cantidad de cartas en la mano del dueño de este lider en el momento del ataque.
     */
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

    /**
     * Daño directo que este lider inflige al atacar, traducido del poder de ataque
     * ({@link #getAttackPower(int)}) a puntos de vida: entre mayor el poder, mas vida quita.
     * El ataque del lider ya no se compara contra ninguna carta bloqueadora: en el turno del
     * jugador humano se aplica de inmediato, y en el turno de la CPU es el daño potencial de la
     * fase de esquive estilo Undertale (ver {@code CardBattleFrame#turnoCpu}).
     *
     * @param handSize cantidad de cartas en la mano del dueño de este lider en el momento del ataque
     * @return puntos de vida que pierde el rival: 1 con el poder base (15000), 2 transformado
     *         (20000) y 4 con el bono de mano reducida (35000, el golpe mas fuerte del lider azul)
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
