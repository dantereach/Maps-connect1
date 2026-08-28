import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Estado de un jugador dentro del modo "juego de cartas" (lider + mazo + mano + area de batalla + vida).
 */
public class CardPlayer {
    private final String name;
    private final LeaderCard leader;
    private final List<GameCard> deck = new ArrayList<>();
    private final List<GameCard> hand = new ArrayList<>();
    private final List<GameCard> battleArea = new ArrayList<>();
    private int life = 7;
    private int energyAvailable = 0;
    private int energyMax = 0;

    public CardPlayer(String name, LeaderCard leader) {
        this.name = name;
        this.leader = leader;
    }

    public String getName() {
        return name;
    }

    public LeaderCard getLeader() {
        return leader;
    }

    public List<GameCard> getDeck() {
        return deck;
    }

    public List<GameCard> getHand() {
        return hand;
    }

    public List<GameCard> getBattleArea() {
        return battleArea;
    }

    public int getLife() {
        return life;
    }

    public void setLife(int life) {
        this.life = Math.max(0, life);
        leader.checkTransform(this.life);
    }

    public boolean isDefeated() {
        return life <= 0;
    }

    public int getEnergyAvailable() {
        return energyAvailable;
    }

    public int getEnergyMax() {
        return energyMax;
    }

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
