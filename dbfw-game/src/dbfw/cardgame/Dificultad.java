package dbfw.cardgame;

/**
 * Niveles de dificultad de la partida.
 * Cada uno cambia el mazo de la CPU, la probabilidad de que haga combo
 * y la duracion, frecuencia y velocidad del minijuego de esquive.
 */
public enum Dificultad {
    FACIL("Facil", new int[]{8, 7, 6, 3}, 0.15, 0.85, 1.25, 0.85),
    NORMAL("Normal", new int[]{6, 5, 6, 7}, 0.35, 1.15, 0.85, 1.05),
    DIFICIL("Dificil", new int[]{4, 4, 7, 9}, 0.55, 1.35, 0.65, 1.25);

    /** Nombre visible en el selector de dificultad. */
    private final String etiqueta;
    /**
     * Cantidad de copias de cada familia en el mazo de la CPU, en el orden Ataque Basico,
     * Jalar Carta, Ataque Fuerte, Golpe Doble. Siempre suman 24.
     */
    private final int[] conteoPorFamilia;
    /** Probabilidad (0 a 1) de que la CPU queme una carta en combo ofensivo al atacar. */
    private final double probabilidadComboCpu;
    /** Multiplicador de la duracion de la fase de esquive del Lider CPU. */
    private final double multiplicadorDuracion;
    /** Multiplicador del intervalo entre balas nuevas (menor a 1 = balas mas seguido). */
    private final double multiplicadorSpawn;
    /** Multiplicador de la velocidad de las balas de la fase de esquive. */
    private final double multiplicadorVelocidad;

    Dificultad(String etiqueta, int[] conteoPorFamilia, double probabilidadComboCpu,
               double multiplicadorDuracion, double multiplicadorSpawn, double multiplicadorVelocidad) {
        this.etiqueta = etiqueta;
        this.conteoPorFamilia = conteoPorFamilia;
        this.probabilidadComboCpu = probabilidadComboCpu;
        this.multiplicadorDuracion = multiplicadorDuracion;
        this.multiplicadorSpawn = multiplicadorSpawn;
        this.multiplicadorVelocidad = multiplicadorVelocidad;
    }

    /**
     * @return copias por familia del mazo de la CPU, en el orden Ataque Basico, Jalar Carta,
     *         Ataque Fuerte y Golpe Doble
     */
    public int[] getConteoPorFamilia() {
        return conteoPorFamilia;
    }

    /** @return la probabilidad (0 a 1) de que la CPU queme una carta en combo ofensivo al atacar. */
    public double getProbabilidadComboCpu() {
        return probabilidadComboCpu;
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
