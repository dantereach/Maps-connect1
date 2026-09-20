package dbfw.cardgame;

/**
 * Niveles de dificultad de la partida.
 * Como la CPU ya no juega cartas, la dificultad solo ajusta la duracion,
 * frecuencia y velocidad del minijuego de esquive de su Lider.
 */
public enum Dificultad {
    FACIL("Facil", 1.25, 0.85, 0.85),
    NORMAL("Normal", 1.15, 0.85, 1.05),
    DIFICIL("Dificil", 0.85, 0.65, 1.25);

    /** Nombre visible en el selector de dificultad. */
    private final String etiqueta;
    /** Multiplicador de la duracion de la fase de esquive del Lider CPU. */
    private final double multiplicadorDuracion;
    /** Multiplicador del intervalo entre balas nuevas (menor a 1 = balas mas seguido). */
    private final double multiplicadorSpawn;
    /** Multiplicador de la velocidad de las balas de la fase de esquive. */
    private final double multiplicadorVelocidad;

    Dificultad(String etiqueta, double multiplicadorDuracion, double multiplicadorSpawn, double multiplicadorVelocidad) {
        this.etiqueta = etiqueta;
        this.multiplicadorDuracion = multiplicadorDuracion;
        this.multiplicadorSpawn = multiplicadorSpawn;
        this.multiplicadorVelocidad = multiplicadorVelocidad;
    }

    /** @return el multiplicador de la duracion de la fase de esquive del Lider CPU. */
    public double getMultiplicadorDuracion() {
        return multiplicadorDuracion;
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
