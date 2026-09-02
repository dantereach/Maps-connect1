package dbfw.cardgame.excepciones;

/**
 * Excepcion propia lanzada cuando se intenta ver o quitar el elemento al frente de una
 * {@link dbfw.cardgame.estructuras.Cola} que no tiene elementos.
 */
public class ColaVaciaException extends Exception {
    public ColaVaciaException(String mensaje) {
        super(mensaje);
    }
}
