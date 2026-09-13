package dbfw.cardgame.excepciones;

/**
 * Se lanza cuando se intenta ver o quitar el frente de una
 * {@link dbfw.cardgame.estructuras.Cola} vacia.
 */
public class ColaVaciaException extends Exception {
    public ColaVaciaException(String mensaje) {
        super(mensaje);
    }
}
