/**
 * Representa una carta de batalla del juego.
 * Version muy basica inspirada en Dragon Ball Fusion World.
 */
public class Card {
    private final String name;
    private final int power;
    private final int cost;
    private final boolean fusable;
    private boolean rested; // true = ya ataco o esta bloqueando (girada)

    public Card(String name, int power, int cost, boolean fusable) {
        this.name = name;
        this.power = power;
        this.cost = cost;
        this.fusable = fusable;
        this.rested = false;
    }

    public String getName() {
        return name;
    }

    public int getPower() {
        return power;
    }

    public int getCost() {
        return cost;
    }

    public boolean isFusable() {
        return fusable;
    }

    public boolean isRested() {
        return rested;
    }

    public void setRested(boolean rested) {
        this.rested = rested;
    }

    @Override
    public String toString() {
        String estado = rested ? " [girada]" : "";
        String fus = fusable ? " (F)" : "";
        return name + " [PWR " + power + " / COST " + cost + "]" + fus + estado;
    }
}
