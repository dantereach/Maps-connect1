import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Jugador: tiene un Lider, mazo, mano, area de energia, area de batalla y puntos de vida (cartas de vida).
 */
public class Player {
    private final String name;
    private final int leaderPower;
    private final List<Card> deck = new ArrayList<>();
    private final List<Card> hand = new ArrayList<>();
    private final List<Card> battleArea = new ArrayList<>();
    private final List<Card> life = new ArrayList<>();
    private int energyAvailable = 0;
    private int energyMax = 0;

    public Player(String name, int leaderPower) {
        this.name = name;
        this.leaderPower = leaderPower;
    }

    public String getName() {
        return name;
    }

    public int getLeaderPower() {
        return leaderPower;
    }

    public List<Card> getDeck() {
        return deck;
    }

    public List<Card> getHand() {
        return hand;
    }

    public List<Card> getBattleArea() {
        return battleArea;
    }

    public List<Card> getLife() {
        return life;
    }

    public int getEnergyAvailable() {
        return energyAvailable;
    }

    public int getEnergyMax() {
        return energyMax;
    }

    /** Construye un mazo simple de 20 cartas y lo mezcla. */
    public void buildBasicDeck() {
        String[] nombres = {
            "Guerrero Saiyan", "Guerrero Namekiano", "Androide", "Guerrero Z",
            "Discipulo de Kame", "Guardian del Universo", "Luchador Terricola",
            "Guerrero del Otro Mundo", "Ki Blast Fighter", "Maestro Ancestral"
        };
        for (int i = 0; i < 20; i++) {
            String nombre = nombres[i % nombres.length];
            int power = 1 + (i % 5); // 1..5
            int cost = 1 + (i % 3);  // 1..3
            boolean fusable = i % 4 == 0; // algunas cartas son fusionables
            deck.add(new Card(nombre + " " + (i + 1), power, cost, fusable));
        }
        Collections.shuffle(deck);
    }

    /** Vida inicial: se separan 5 cartas del mazo como "cartas de vida". */
    public void setInitialLife(int amount) {
        for (int i = 0; i < amount && !deck.isEmpty(); i++) {
            life.add(deck.remove(0));
        }
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

    /** Al inicio de turno: se recupera energia y se enderezan las cartas. */
    public void startTurn() {
        // Se agrega 1 energia por turno (max 10) y se recupera toda la energia disponible.
        if (energyMax < 10) {
            energyMax++;
        }
        energyAvailable = energyMax;
        for (Card c : battleArea) {
            c.setRested(false);
        }
    }

    public boolean spendEnergy(int amount) {
        if (energyAvailable < amount) {
            return false;
        }
        energyAvailable -= amount;
        return true;
    }

    /** Recibe daño: pierde una carta de vida. Devuelve true si se quedo sin vida (derrota). */
    public boolean takeDamage() {
        if (!life.isEmpty()) {
            Card lifeCard = life.remove(0);
            hand.add(lifeCard); // en el juego real la carta de vida pasa a la mano
            System.out.println(name + " pierde una carta de vida! Vida restante: " + life.size());
        }
        return life.isEmpty();
    }

    public boolean isDefeated() {
        return life.isEmpty();
    }
}
