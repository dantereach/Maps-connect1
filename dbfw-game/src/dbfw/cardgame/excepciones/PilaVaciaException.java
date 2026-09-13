package dbfw.cardgame.excepciones;

/**
 * Excepcion propia lanzada cuando se intenta ver o quitar el elemento de la cima de una
 * {@link dbfw.cardgame.estructuras.Pila} que no tiene elementos.
 */
public class PilaVaciaException extends Exception {
    public PilaVaciaException(String mensaje) {
        super(mensaje);
    }
}
