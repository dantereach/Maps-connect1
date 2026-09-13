package dbfw.cardgame.undertale;

/**
 * Una "bala" del mini-juego de esquive estilo Undertale: un proyectil circular que se mueve en
 * linea recta con velocidad constante dentro de la caja de esquive (ver {@link PanelEsquive}).
 */
public class Bala {
    /** Posicion horizontal actual (centro de la bala). */
    private double x;
    /** Posicion vertical actual (centro de la bala). */
    private double y;
    /** Velocidad horizontal (pixeles por tick de animacion). */
    private final double vx;
    /** Velocidad vertical (pixeles por tick de animacion). */
    private final double vy;
    /** Radio de la bala, en pixeles. */
    private final int radio;

    /**
     * Crea una bala en una posicion inicial con una velocidad dada.
     *
     * @param x     posicion horizontal inicial
     * @param y     posicion vertical inicial
     * @param vx    velocidad horizontal (pixeles por tick)
     * @param vy    velocidad vertical (pixeles por tick)
     * @param radio radio de la bala en pixeles
     */
    public Bala(double x, double y, double vx, double vy, int radio) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.radio = radio;
    }

    /** Avanza la bala un paso segun su velocidad. */
    public void mover() {
        x += vx;
        y += vy;
    }

    /** @return true si la bala ya salio por completo del area indicada (con un margen de holgura). */
    public boolean fueraDeArea(int ancho, int alto) {
        int margen = radio * 4;
        return x < -margen || x > ancho + margen || y < -margen || y > alto + margen;
    }

    /**
     * @param cx centro horizontal del corazon
     * @param cy centro vertical del corazon
     * @param radioCorazon radio (de colision) del corazon
     * @return true si esta bala esta tocando al corazon (colision circulo-circulo)
     */
    public boolean chocaCon(double cx, double cy, int radioCorazon) {
        double dx = x - cx;
        double dy = y - cy;
        double distancia = Math.sqrt(dx * dx + dy * dy);
        return distancia <= (radio + radioCorazon);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public int getRadio() {
        return radio;
    }
}
