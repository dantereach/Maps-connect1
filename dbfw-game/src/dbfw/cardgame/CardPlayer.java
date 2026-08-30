package dbfw.cardgame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Estado completo de un jugador (humano o CPU) dentro del modo "juego de cartas": su lider,
 * su mazo, su mano, su area de batalla (cartas ya jugadas), su vida y su energia disponible.
 * <p>
 * Esta clase solo modela el estado y las reglas basicas de cada jugador (robar, gastar energia,
 * recibir daño, iniciar turno); la logica de combate entre dos jugadores y la interfaz grafica
 * viven en {@link CardBattleFrame}.
 */
public class CardPlayer {
    /** Nombre visible del jugador ("Tu" o "CPU"). */
    private final String name;
    /** Carta de Lider de este jugador. */
    private final LeaderCard leader;
    /** Mazo de cartas por robar, en orden (la carta 0 es la siguiente en robarse). */
    private final List<GameCard> deck = new ArrayList<>();
    /** Cartas actualmente en la mano del jugador. */
    private final List<GameCard> hand = new ArrayList<>();
    /** Cartas que el jugador ya jugo y estan en su area de batalla (pueden atacar/bloquear). */
    private final List<GameCard> battleArea = new ArrayList<>();
    /** Vida restante del jugador (el juego termina cuando llega a 0). */
    private int life = 7;
    /** Energia disponible para gastar en el turno actual. */
    private int energyAvailable = 0;
    /** Energia maxima acumulada hasta ahora (sube 1 por turno hasta un tope de 10). */
    private int energyMax = 0;

    /**
     * Crea un jugador con su lider asociado. El mazo, mano y area de batalla empiezan vacios.
     *
     * @param name   nombre visible del jugador
     * @param leader carta de Lider (ver {@link LeaderCard#crearLiderAzul()} / {@link LeaderCard#crearLiderCpu()})
     */
    public CardPlayer(String name, LeaderCard leader) {
        this.name = name;
        this.leader = leader;
    }

    /** @return el nombre visible del jugador. */
    public String getName() {
        return name;
    }

    /** @return la carta de Lider de este jugador. */
    public LeaderCard getLeader() {
        return leader;
    }

    /** @return el mazo de cartas por robar (mutable). */
    public List<GameCard> getDeck() {
        return deck;
    }

    /** @return las cartas actualmente en la mano del jugador (mutable). */
    public List<GameCard> getHand() {
        return hand;
    }

    /** @return las cartas jugadas en el area de batalla del jugador (mutable). */
    public List<GameCard> getBattleArea() {
        return battleArea;
    }

    /** @return la vida restante del jugador. */
    public int getLife() {
        return life;
    }

    /**
     * Actualiza la vida del jugador (no permite valores negativos) y revisa si el lider
     * debe transformarse segun la nueva vida.
     */
    public void setLife(int life) {
        this.life = Math.max(0, life);
        leader.checkTransform(this.life);
    }

    /** @return true si el jugador quedo derrotado (vida en 0). */
    public boolean isDefeated() {
        return life <= 0;
    }

    /** @return la energia disponible para gastar en el turno actual. */
    public int getEnergyAvailable() {
        return energyAvailable;
    }

    /** @return la energia maxima acumulada (sube 1 por turno, tope 10). */
    public int getEnergyMax() {
        return energyMax;
    }

    /**
     * Intenta gastar energia (por ejemplo, para jugar una carta o usar una habilidad).
     * @param amount cantidad de energia a gastar
     * @return true si habia suficiente energia y se gasto; false si no alcanzaba (no se gasta nada)
     */
    public boolean spendEnergy(int amount) {
        if (energyAvailable < amount) {
            return false;
        }
        energyAvailable -= amount;
        return true;
    }

    /** Construye un mazo de 24 cartas balanceado entre los 4 tipos y lo mezcla. */
    public void buildDeck() {
        for (int i = 0; i < 6; i++) {
            deck.add(new GameCard("Guerrero Basico " + (i + 1), 15000, 2, CardType.BASIC));
        }
        for (int i = 0; i < 6; i++) {
            deck.add(new GameCard("Explorador " + (i + 1), 5000, 1, CardType.DRAW));
        }
        for (int i = 0; i < 6; i++) {
            deck.add(new GameCard("Guardian " + (i + 1), 20000, 3, CardType.GUARD));
        }
        for (int i = 0; i < 6; i++) {
            deck.add(new GameCard("Golpeador Doble " + (i + 1), 35000, 4, CardType.DOUBLE_STRIKE));
        }
        Collections.shuffle(deck);
    }

    /**
     * Reparte la mano inicial robando cartas del mazo.
     * @param amount cantidad de cartas a robar (se detiene antes si el mazo se vacia)
     */
    public void drawInitialHand(int amount) {
        for (int i = 0; i < amount && !deck.isEmpty(); i++) {
            hand.add(deck.remove(0));
        }
    }

    /** Roba una carta del mazo a la mano. Devuelve false si el mazo esta vacio (derrota). */
    public boolean drawCard() {
        if (deck.isEmpty()) {
            return false;
        }
        hand.add(deck.remove(0));
        return true;
    }

    /** Inicio de turno: gana energia, se recupera toda la energia y se enderezan cartas y lider. */
    public void startTurn() {
        if (energyMax < 10) {
            energyMax++;
        }
        energyAvailable = energyMax;
        for (GameCard c : battleArea) {
            c.setRested(false);
        }
        leader.setRested(false);
        leader.setBoostUsedThisTurn(false);
    }

    /** Aplica dano de vida (1, o 2 si es Double Strike). Devuelve true si el jugador queda derrotado. */
    public boolean takeDamage(int amount) {
        setLife(life - amount);
        return isDefeated();
    }
}
