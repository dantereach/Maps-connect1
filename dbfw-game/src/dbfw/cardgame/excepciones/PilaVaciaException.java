package dbfw.cardgame.excepciones;

/**
 * Se lanza cuando se intenta ver o quitar la cima de una
 * {@link dbfw.cardgame.estructuras.Pila} vacia.
 */
public class PilaVaciaException extends Exception {
    public PilaVaciaException(String mensaje) {
        super(mensaje);
    }
}
