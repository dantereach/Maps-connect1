package dbfw.cardgame.excepciones;

/**
 * Excepcion de dominio lanzada cuando un jugador intenta robar una carta y su mazo (la
 * {@link dbfw.cardgame.estructuras.Pila} de robo) ya no tiene cartas.
 * <p>
 * Se construye a partir de la {@link PilaVaciaException} de bajo nivel que lanza la propia
 * estructura de datos, envolviendola para darle un significado propio del juego: en las reglas
 * de Fusion World, quedarse sin cartas para robar es una condicion de derrota inmediata.
 */
public class MazoVacioException extends Exception {
    public MazoVacioException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
