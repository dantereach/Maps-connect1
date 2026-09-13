package dbfw.cardgame.excepciones;

/**
 * Se lanza cuando se intenta ver o quitar el frente de una
 * {@link dbfw.cardgame.estructuras.ColaPrioridad} vacia.
 */
public class ColaPrioridadVaciaException extends Exception {
    public ColaPrioridadVaciaException(String mensaje) {
        super(mensaje);
    }
}
