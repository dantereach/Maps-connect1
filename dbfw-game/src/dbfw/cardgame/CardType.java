package dbfw.cardgame;

/**
 * Tipos de accion de carta disponibles en el mazo del modo "juego de cartas" (hibrido
 * Undertale/Slay the Spire): cada carta ya no representa un "personaje" que ataca o bloquea en
 * el area de batalla, sino una accion que el jugador ejecuta de inmediato al jugarla (atacar
 * directamente la vida del rival o robar una carta del mazo).
 * <p>
 * Cada tipo define un poder base (que se traduce en daño directo mediante
 * {@code GameCard#getDanoDirecto()}) y, opcionalmente, un efecto especial que se resuelve en
 * {@code CardBattleFrame#resolverEfectosPendientes} al jugar la carta. El poder de combo de
 * cada tipo ({@code GameCard#getComboPower()}) es inversamente proporcional a la fuerza de su
 * efecto: las acciones mas simples son las mejores comodines de combo.
 */
public enum CardType {
    /** Accion basica: ataca de inmediato con daño directo pequeño. Poder base 15000 (dano 1). */
    BASIC,
    /** Accion "Jalar Carta": al jugarse, roba 1 carta del mazo de inmediato. Poder base 5000. */
    DRAW,
    /** Accion de ataque fuerte: daño directo mediano. Poder base 20000 (dano 2). */
    GUARD,
    /**
     * Accion de golpe doble: ataca dos veces seguidas con daño directo (Double Strike), la
     * accion mas fuerte del mazo. Poder base 35000 (dano 2 por golpe, 2 golpes).
     */
    DOUBLE_STRIKE
}
