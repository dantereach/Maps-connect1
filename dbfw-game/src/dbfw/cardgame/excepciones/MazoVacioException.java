package dbfw.cardgame.excepciones;

/**
 * Se lanza cuando un jugador intenta robar y su mazo ya no tiene cartas.
 * Toma la {@link PilaVaciaException} interna y la expresa como una situacion propia del juego.
 */
public class MazoVacioException extends Exception {
    public MazoVacioException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
