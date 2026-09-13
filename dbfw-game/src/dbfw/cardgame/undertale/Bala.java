package dbfw.cardgame.undertale;

/**
 * Una "bala" del mini-juego de esquive estilo Undertale: un proyectil que se mueve dentro de la
 * caja de esquive. Ademas del movimiento simple en linea recta,
 * admite dos variantes inspiradas en los patrones de la pelea de Undyne the Undying:
 * <ul>
 *   <li><b>Lanza</b>: se dibuja alargada y girada segun su direccion (como las paredes de
 *       lanzas de Undyne), aunque para simplificar la deteccion de choque se sigue tratando
 *       como un circulo mas pequeño centrado en la bala.</li>
 *   <li><b>Onda</b>: en vez de avanzar en linea recta, su posicion vertical oscila con una
 *       funcion seno alrededor de una trayectoria base (como los ataques de onda de Undyne).</li>
 * </ul>
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

    /** Bala circular simple, con movimiento en linea recta. */
    public Bala(double x, double y, double vx, double vy, int radio) {
        this(x, y, vx, vy, radio, false, 0, 0, 0);
    }

    /** Bala circular (esLanza = false) o tipo lanza (esLanza = true), con movimiento en linea recta. */
    public Bala(double x, double y, double vx, double vy, int radio, boolean esLanza) {
        this(x, y, vx, vy, radio, esLanza, 0, 0, 0);
    }

    /**
     * Bala con movimiento ondulado (estilo ataque de onda de Undyne): avanza en linea recta en
     * el eje dominante de {@code vx}/{@code vy} mientras su posicion oscila con una funcion
     * seno alrededor de esa trayectoria base.
     *
     * @param amplitudOnda   amplitud de la oscilacion, en pixeles
     * @param frecuenciaOnda que tan rapido oscila (radianes por tick de animacion)
     * @param faseOnda       desfase inicial de la oscilacion, en radianes (para escalonar varias balas)
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

    /** @return true si esta bala se dibuja como una lanza alargada (estilo Undyne) en vez de un circulo. */
    public boolean isLanza() {
        return esLanza;
    }
}
