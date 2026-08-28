/**
 * Tipos de carta de batalla disponibles en el mazo.
 */
public enum CardType {
    /** Carta basica, sin efecto especial. Poder 15000. */
    BASIC,
    /** Al jugarse, roba 1 carta del mazo. Poder 5000. */
    DRAW,
    /** Poder 20000, pero durante el turno del oponente (defendiendo) su poder sube a 25000. */
    GUARD,
    /** Poder 35000. Si conecta sin ser bloqueada, hace 2 de daño de vida en vez de 1 (Double Strike). */
    DOUBLE_STRIKE
}
