package dbfw.cardgame;

import dbfw.cardgame.estructuras.Cola;
import dbfw.cardgame.estructuras.ColaPrioridad;
import dbfw.cardgame.estructuras.ListaSimple;
import dbfw.cardgame.estructuras.Pila;
import dbfw.cardgame.excepciones.MazoVacioException;
import dbfw.cardgame.excepciones.PilaVaciaException;
import java.util.Random;

/**
 * Representa a un jugador del juego: lider, vida, energia y cartas.
 * Usa estructuras propias: mazo en {@link Pila}, mano en {@link Cola} y area de batalla en
 * {@link ListaSimple}, sin colecciones de Java.
 * El mazo se arma desde {@link CatalogoCartas} por nombre y los efectos pendientes se ordenan
 * en una {@link ColaPrioridad}.
 */
public class CardPlayer {
    /** Nombre visible del jugador ("Tu" o "CPU"). */
    private final String name;
    /** Lider del jugador. */
    private final LeaderCard leader;
    /** Mazo por robar, guardado en una {@link Pila}. */
    private final Pila<GameCard> deck = new Pila<>();
    /** Cartas en mano, guardadas en una {@link Cola}. */
    private final Cola<GameCard> hand = new Cola<>();
    /** Cartas jugadas este turno, guardadas en una {@link ListaSimple}. */
    private final ListaSimple<GameCard> battleArea = new ListaSimple<>();
    /**
     * Efectos pendientes del turno, ordenados por prioridad en una {@link ColaPrioridad}.
     */
    private final ColaPrioridad<GameCard> efectosPendientes = new ColaPrioridad<>();
    /** Generador aleatorio para barajar. */
    private final Random random = new Random();
    /** Vida restante. */
    private int life = 7;
    /** Energia disponible este turno. */
    private int energyAvailable = 0;
    /** Energia maxima acumulada, hasta 10. */
    private int energyMax = 0;
    /**
     * Daño extra reservado para el siguiente ataque del jugador.
     */
    private int bonusAtaquePendiente = 0;

    /**
     * Crea un jugador con su lider.
     *
     * @param name   nombre visible del jugador
     * @param leader lider del jugador
     */
    public CardPlayer(String name, LeaderCard leader) {
        this.name = name;
        this.leader = leader;
    }

    /** @return el nombre visible del jugador. */
    public String getName() {
        return name;
    }

    /** @return la carta de Lider de este jugador. */
    public LeaderCard getLeader() {
        return leader;
    }

    /** @return el mazo del jugador ({@link Pila}). */
    public Pila<GameCard> getDeck() {
        return deck;
    }

    /** @return la mano del jugador ({@link Cola}). */
    public Cola<GameCard> getHand() {
        return hand;
    }

    /** @return el area de batalla del jugador ({@link ListaSimple}). */
    public ListaSimple<GameCard> getBattleArea() {
        return battleArea;
    }

    /** @return la cola de prioridad de efectos pendientes. */
    public ColaPrioridad<GameCard> getEfectosPendientes() {
        return efectosPendientes;
    }

    /**
     * Guarda el efecto de una carta para resolverlo luego por prioridad.
     *
     * @param carta carta jugada
     */
    public void encolarEfectoDeCarta(GameCard carta) {
        efectosPendientes.encolar(carta, carta.getPrioridadEfecto());
    }

    /** @return la vida restante del jugador. */
    public int getLife() {
        return life;
    }

    /** Actualiza la vida sin bajar de 0 y revisa la transformacion del lider. */
    public void setLife(int life) {
        this.life = Math.max(0, life);
        leader.checkTransform(this.life);
    }

    /** @return true si el jugador quedo derrotado (vida en 0). */
    public boolean isDefeated() {
        return life <= 0;
    }

    /** @return la energia disponible para gastar en el turno actual. */
    public int getEnergyAvailable() {
        return energyAvailable;
    }

    /** @return la energia maxima acumulada (sube 1 por turno, tope 10). */
    public int getEnergyMax() {
        return energyMax;
    }

    /**
     * Intenta gastar energia (por ejemplo, para jugar una carta o usar una habilidad).
     * @param amount cantidad de energia a gastar
     * @return true si habia suficiente energia y se gasto; false si no alcanzaba (no se gasta nada)
     */
    public boolean spendEnergy(int amount) {
        if (energyAvailable < amount) {
            return false;
        }
        energyAvailable -= amount;
        return true;
    }

    /** Construye el mazo base: 6 copias de cada familia del {@link CatalogoCartas}. */
    public void buildDeck() {
        buildDeck(new int[]{6, 6, 6, 6});
    }

    /**
     * Construye un mazo de 24 cartas desde el {@link CatalogoCartas}, lo baraja y lo apila.
     *
     * @param conteoPorFamilia copias por familia en el orden Ataque Basico, Jalar Carta,
     *                         Ataque Fuerte y Golpe Doble
     */
    public void buildDeck(int[] conteoPorFamilia) {
        GameCard[] cartas = new GameCard[24];
        int idx = 0;
        idx = agregarFamilia(cartas, idx, "Ataque Basico", conteoPorFamilia[0]);
        idx = agregarFamilia(cartas, idx, "Jalar Carta", conteoPorFamilia[1]);
        idx = agregarFamilia(cartas, idx, "Ataque Fuerte", conteoPorFamilia[2]);
        idx = agregarFamilia(cartas, idx, "Golpe Doble", conteoPorFamilia[3]);
        barajar(cartas);
        for (GameCard carta : cartas) {
            deck.apilar(carta);
        }
    }

    /**
     * Busca una familia en el {@link CatalogoCartas} y agrega varias copias numeradas al arreglo.
     *
     * @param cartas        arreglo destino
     * @param indiceInicial indice donde empieza a agregar
     * @param nombreFamilia familia a buscar
     * @param cantidad      copias a crear
     * @return el siguiente indice libre
     */
    private int agregarFamilia(GameCard[] cartas, int indiceInicial, String nombreFamilia, int cantidad) {
        GameCard plantilla = CatalogoCartas.buscar(nombreFamilia);
        for (int i = 0; i < cantidad; i++) {
            cartas[indiceInicial + i] = plantilla.crearCopiaNumerada(i + 1);
        }
        return indiceInicial + cantidad;
    }

    /** Baraja un arreglo de cartas con Fisher-Yates hecho a mano. */
    private void barajar(GameCard[] cartas) {
        for (int i = cartas.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            GameCard temp = cartas[i];
            cartas[i] = cartas[j];
            cartas[j] = temp;
        }
    }

    /**
     * Reparte la mano inicial.
     * Si el mazo se vacia antes, se detiene.
     *
     * @param amount cantidad de cartas a robar
     */
    public void drawInitialHand(int amount) {
        for (int i = 0; i < amount; i++) {
            try {
                drawCard();
            } catch (MazoVacioException e) {
                break;
            }
        }
    }

    /**
     * Roba una carta de la cima del mazo y la manda al final de la mano.
     *
     * @throws MazoVacioException si ya no quedan cartas
     */
    public void drawCard() throws MazoVacioException {
        try {
            GameCard carta = deck.desapilar();
            hand.encolar(carta);
        } catch (PilaVaciaException e) {
            throw new MazoVacioException(name + " se quedo sin cartas en el mazo para robar.", e);
        }
    }

    /** Inicio de turno: gana energia, la recarga y limpia lo jugado el turno anterior. */
    public void startTurn() {
        if (energyMax < 10) {
            energyMax++;
        }
        energyAvailable = energyMax;
        // Las cartas jugadas son de un solo uso, asi que este historial se limpia cada turno.
        battleArea.vaciar();
        leader.setRested(false);
        leader.setBoostUsedThisTurn(false);
    }

    /** @return el daño extra pendiente para el siguiente ataque. */
    public int getBonusAtaquePendiente() {
        return bonusAtaquePendiente;
    }

    /** Agrega daño extra al siguiente ataque. */
    public void agregarBonusAtaque(int cantidad) {
        this.bonusAtaquePendiente += cantidad;
    }

    /**
     * Devuelve el daño extra pendiente y luego lo reinicia en 0.
     *
     * @return el daño extra pendiente
     */
    public int consumirBonusAtaque() {
        int bono = bonusAtaquePendiente;
        bonusAtaquePendiente = 0;
        return bono;
    }

    /** Aplica daño directo de vida (segun el ataque). Devuelve true si el jugador queda derrotado. */
    public boolean takeDamage(int amount) {
        setLife(life - amount);
        return isDefeated();
    }
}
