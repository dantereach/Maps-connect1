package dbfw.cardgame;

/**
 * Niveles de dificultad de la partida.
 * Como la CPU ya no juega cartas, la dificultad solo ajusta la frecuencia
 * y velocidad de las lanzas en el minijuego de esquive de su Lider.
 * La duracion de esa fase es fija (15 segundos) sin importar la dificultad.
 */
public enum Dificultad {
    FACIL("Facil", 1.05, 0.75),
    NORMAL("Normal", 0.85, 1.0),
    DIFICIL("Dificil", 0.5, 1.5);

    /** Nombre visible en el selector de dificultad. */
    private final String etiqueta;
    /** Multiplicador del intervalo entre balas nuevas (menor a 1 = balas mas seguido). */
    private final double multiplicadorSpawn;
    /** Multiplicador de la velocidad de las balas de la fase de esquive. */
    private final double multiplicadorVelocidad;

    Dificultad(String etiqueta, double multiplicadorSpawn, double multiplicadorVelocidad) {
        this.etiqueta = etiqueta;
        this.multiplicadorSpawn = multiplicadorSpawn;
        this.multiplicadorVelocidad = multiplicadorVelocidad;
    }

    /** @return el multiplicador del intervalo entre balas nuevas (menor a 1 = balas mas seguido). */
    public double getMultiplicadorSpawn() {
        return multiplicadorSpawn;
    }

    /** @return el multiplicador de la velocidad de las balas de la fase de esquive. */
    public double getMultiplicadorVelocidad() {
        return multiplicadorVelocidad;
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
