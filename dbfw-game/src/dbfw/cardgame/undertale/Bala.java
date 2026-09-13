package dbfw.cardgame.undertale;

/**
 * Proyectil del minijuego de esquive.
 * Puede ser una bala normal, una lanza o una bala con movimiento en onda.
 * Todas se mueven dentro de la caja y sirven para detectar choques con el corazon.
 */
public class Bala {
    private double x;
    private double y;
    private final double vx;
    private final double vy;
    private final int radio;
    private final boolean esLanza;

    private double baseY;
    private final double amplitudOnda;
    private final double frecuenciaOnda;
    private final double faseOnda;
    private int tick = 0;

    /** Bala simple con movimiento recto. */
    public Bala(double x, double y, double vx, double vy, int radio) {
        this(x, y, vx, vy, radio, false, 0, 0, 0);
    }

    /** Bala recta normal o tipo lanza. */
    public Bala(double x, double y, double vx, double vy, int radio, boolean esLanza) {
        this(x, y, vx, vy, radio, esLanza, 0, 0, 0);
    }

    /**
     * Bala con movimiento ondulado.
     *
     * @param amplitudOnda   amplitud de la oscilacion
     * @param frecuenciaOnda velocidad de la oscilacion
     * @param faseOnda       fase inicial de la onda
     */
    public Bala(double x, double y, double vx, double vy, int radio,
                double amplitudOnda, double frecuenciaOnda, double faseOnda) {
        this(x, y, vx, vy, radio, false, amplitudOnda, frecuenciaOnda, faseOnda);
    }

    private Bala(double x, double y, double vx, double vy, int radio, boolean esLanza,
                 double amplitudOnda, double frecuenciaOnda, double faseOnda) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.radio = radio;
        this.esLanza = esLanza;
        this.baseY = y;
        this.amplitudOnda = amplitudOnda;
        this.frecuenciaOnda = frecuenciaOnda;
        this.faseOnda = faseOnda;
    }

    /** Avanza la bala un paso: en linea recta, o siguiendo su oscilacion si tiene onda. */
    public void mover() {
        tick++;
        x += vx;
        if (amplitudOnda != 0) {
            baseY += vy;
            y = baseY + amplitudOnda * Math.sin(frecuenciaOnda * tick + faseOnda);
        } else {
            y += vy;
        }
    }

    /** @return true si la bala ya salio por completo del area indicada (con un margen de holgura). */
    public boolean fueraDeArea(int ancho, int alto) {
        int margen = radio * 4 + 20;
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

    public double getVx() {
        return vx;
    }

    public double getVy() {
        return vy;
    }

    public int getRadio() {
        return radio;
    }

    /** @return true si esta bala se dibuja como lanza. */
    public boolean isLanza() {
        return esLanza;
    }
}
