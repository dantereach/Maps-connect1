package dbfw.cardgame;

/**
 * Tipos de carta-accion del mazo.
 * No son personajes: al jugarse atacan directo o activan un efecto inmediato.
 * Cada tipo define el efecto base de la carta.
 */
public enum CardType {
    /** Ataque basico de daño directo. */
    BASIC,
    /** Roba 1 carta al jugarse. */
    DRAW,
    /** Ataque fuerte de daño directo. */
    GUARD,
    /** Ataca dos veces seguidas. */
    DOUBLE_STRIKE
}
