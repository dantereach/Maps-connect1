package dbfw.cardgame.excepciones;

/**
 * Excepcion propia lanzada cuando se intenta ver o quitar el elemento de mayor prioridad de una
 * {@link dbfw.cardgame.estructuras.ColaPrioridad} que no tiene elementos.
 */
public class ColaPrioridadVaciaException extends Exception {
    public ColaPrioridadVaciaException(String mensaje) {
        super(mensaje);
    }
}
