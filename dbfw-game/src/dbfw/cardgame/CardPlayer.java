package dbfw.cardgame;

import dbfw.cardgame.estructuras.Cola;
import dbfw.cardgame.estructuras.ColaPrioridad;
import dbfw.cardgame.estructuras.ListaSimple;
import dbfw.cardgame.estructuras.Pila;
import dbfw.cardgame.excepciones.MazoVacioException;
import dbfw.cardgame.excepciones.PilaVaciaException;
import java.util.Random;

/**
 * Estado completo de un jugador (humano o CPU) dentro del modo hibrido Undertale/Slay the
 * Spire: su lider, su mazo, su mano, el historial visual de acciones jugadas este turno, su
 * vida y su energia disponible.
 * <p>
 * Esta clase solo modela el estado y las reglas basicas de cada jugador (robar, gastar energia,
 * recibir daño, iniciar turno); la logica de combate entre dos jugadores y la interfaz grafica
 * viven en {@link CardBattleFrame}.
 * <p>
 * El mazo, la mano y el area de batalla se implementan con estructuras de datos propias en vez
 * de {@code java.util.ArrayList}: el mazo es una {@link Pila} (se roba desde la cima, O(1)), la
 * mano es una {@link Cola} (las cartas entran por el final al robarse) y el area de batalla es
 * una {@link ListaSimple} (aqui ya no representa "atacantes en la mesa": como las cartas son
 * acciones de un solo uso, solo sirve de historial visual del turno y se vacia al empezar el
 * siguiente). Ademas, los efectos de las cartas jugadas se encolan en una {@link ColaPrioridad}
 * propia para resolverse en orden de prioridad, y el mazo se construye buscando cada familia de
 * cartas por nombre en el {@link CatalogoCartas} (tabla hash).
 */
public class CardPlayer {
    /** Nombre visible del jugador ("Tu" o "CPU"). */
    private final String name;
    /** Carta de Lider de este jugador. */
    private final LeaderCard leader;
    /** Mazo de cartas por robar: Pila propia (se roba siempre desde la cima). */
    private final Pila<GameCard> deck = new Pila<>();
    /** Cartas actualmente en la mano del jugador: Cola propia (entran por el final al robarse). */
    private final Cola<GameCard> hand = new Cola<>();
    /** Cartas que el jugador ya jugo y estan en su area de batalla: Lista Simple propia. */
    private final ListaSimple<GameCard> battleArea = new ListaSimple<>();
    /**
     * Efectos de cartas jugadas este turno que aun no se han resuelto: Cola de Prioridad propia.
     * Las cartas con habilidad especial mas fuerte se resuelven antes que las demas, sin
     * importar el orden en que se jugaron (ver {@link GameCard#getPrioridadEfecto()}).
     */
    private final ColaPrioridad<GameCard> efectosPendientes = new ColaPrioridad<>();
    /** Generador de numeros aleatorios usado para barajar el mazo manualmente. */
    private final Random random = new Random();
    /** Vida restante del jugador (el juego termina cuando llega a 0). */
    private int life = 7;
    /** Energia disponible para gastar en el turno actual. */
    private int energyAvailable = 0;
    /** Energia maxima acumulada hasta ahora (sube 1 por turno hasta un tope de 10). */
    private int energyMax = 0;
    /**
     * Daño extra pendiente para el proximo ataque de este jugador, otorgado por la habilidad de
     * potenciar del lider (ver {@link LeaderCard#getBoostAmount()}). Se consume por completo la
     * primera vez que se aplica ({@link #consumirBonusAtaque()}).
     */
    private int bonusAtaquePendiente = 0;

    /**
     * Crea un jugador con su lider asociado. El mazo, mano y area de batalla empiezan vacios.
     *
     * @param name   nombre visible del jugador
     * @param leader carta de Lider (ver {@link LeaderCard#crearLiderAzul()} / {@link LeaderCard#crearLiderCpu()})
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

    /** @return el mazo de cartas por robar (Pila propia, mutable). */
    public Pila<GameCard> getDeck() {
        return deck;
    }

    /** @return las cartas actualmente en la mano del jugador (Cola propia, mutable). */
    public Cola<GameCard> getHand() {
        return hand;
    }

    /** @return las cartas jugadas en el area de batalla del jugador (Lista Simple propia, mutable). */
    public ListaSimple<GameCard> getBattleArea() {
        return battleArea;
    }

    /** @return la cola de prioridad de efectos de cartas jugadas este turno que faltan por resolver. */
    public ColaPrioridad<GameCard> getEfectosPendientes() {
        return efectosPendientes;
    }

    /**
     * Encola el efecto de una carta recien jugada para resolverse mas tarde, en orden de
     * prioridad (ver {@link GameCard#getPrioridadEfecto()}) en vez de en el orden en que se
     * jugo: las cartas con habilidad especial mas fuerte se resuelven primero, sin importar
     * cuando se jugaron.
     *
     * @param carta carta recien jugada cuyo efecto debe resolverse
     */
    public void encolarEfectoDeCarta(GameCard carta) {
        efectosPendientes.encolar(carta, carta.getPrioridadEfecto());
    }

    /** @return la vida restante del jugador. */
    public int getLife() {
        return life;
    }

    /**
     * Actualiza la vida del jugador (no permite valores negativos) y revisa si el lider
     * debe transformarse segun la nueva vida.
     */
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

    /**
     * Construye el mazo balanceado por defecto: 6 copias de cada una de las 4 familias del
     * {@link CatalogoCartas} (24 cartas en total). Se usa para el jugador humano.
     */
    public void buildDeck() {
        buildDeck(new int[]{6, 6, 6, 6});
    }

    /**
     * Construye un mazo de 24 cartas repartidas entre las 4 familias del {@link CatalogoCartas}
     * (tabla hash indexada por nombre) segun las cantidades indicadas, lo baraja con un
     * Fisher-Yates manual (sin {@code Collections.shuffle}) y lo apila carta por carta en la
     * Pila de robo. Se usa para variar la dificultad del mazo de la CPU (ver {@link Dificultad}).
     *
     * @param conteoPorFamilia cantidad de copias de cada familia, en el orden Ataque Basico,
     *                         Jalar Carta, Ataque Fuerte, Golpe Doble (debe sumar 24)
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
     * Busca la plantilla de una familia de cartas en el {@link CatalogoCartas} (tabla hash,
     * busqueda O(1) por nombre) y agrega al arreglo tantas copias numeradas de esa familia como
     * se pidan, empezando en el indice dado.
     *
     * @param cartas        arreglo destino donde se van colocando las cartas del mazo
     * @param indiceInicial indice del arreglo donde se coloca la primera copia
     * @param nombreFamilia nombre de la familia a buscar en el catalogo
     * @param cantidad      cantidad de copias numeradas a generar
     * @return el indice siguiente al de la ultima copia agregada
     */
    private int agregarFamilia(GameCard[] cartas, int indiceInicial, String nombreFamilia, int cantidad) {
        GameCard plantilla = CatalogoCartas.buscar(nombreFamilia);
        for (int i = 0; i < cantidad; i++) {
            cartas[indiceInicial + i] = plantilla.crearCopiaNumerada(i + 1);
        }
        return indiceInicial + cantidad;
    }

    /**
     * Baraja un arreglo de cartas con el algoritmo de Fisher-Yates: recorre el arreglo de atras
     * hacia adelante e intercambia cada posicion con una posicion aleatoria anterior (o igual).
     * Se implementa a mano en vez de usar {@code Collections.shuffle} para no depender de las
     * colecciones de la biblioteca estandar.
     */
    private void barajar(GameCard[] cartas) {
        for (int i = cartas.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            GameCard temp = cartas[i];
            cartas[i] = cartas[j];
            cartas[j] = temp;
        }
    }

    /**
     * Reparte la mano inicial robando cartas del mazo. Si el mazo se queda sin cartas antes de
     * completar la cantidad pedida, se detiene silenciosamente (no deberia ocurrir con un mazo
     * de 24 cartas y una mano inicial de 5).
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
     * Roba una carta: la saca de la cima del mazo (Pila) y la agrega al final de la mano (Cola).
     * @throws MazoVacioException si el mazo ya no tiene cartas; en las reglas de Fusion World,
     *                             esto significa la derrota inmediata de este jugador.
     */
    public void drawCard() throws MazoVacioException {
        try {
            GameCard carta = deck.desapilar();
            hand.encolar(carta);
        } catch (PilaVaciaException e) {
            throw new MazoVacioException(name + " se quedo sin cartas en el mazo para robar.", e);
        }
    }

    /** Inicio de turno: gana energia, se recupera toda la energia y se descartan las acciones del turno anterior. */
    public void startTurn() {
        if (energyMax < 10) {
            energyMax++;
        }
        energyAvailable = energyMax;
        // Las cartas jugadas son acciones de un solo uso (no "personajes" persistentes): el
        // area de batalla ahora funciona solo como historial visual de lo jugado este turno,
        // asi que se descarta al empezar el turno siguiente.
        battleArea.vaciar();
        leader.setRested(false);
        leader.setBoostUsedThisTurn(false);
    }

    /** @return el daño extra pendiente para el proximo ataque de este jugador (habilidad de potenciar). */
    public int getBonusAtaquePendiente() {
        return bonusAtaquePendiente;
    }

    /** Agrega daño extra pendiente para el proximo ataque de este jugador (habilidad de potenciar del lider). */
    public void agregarBonusAtaque(int cantidad) {
        this.bonusAtaquePendiente += cantidad;
    }

    /**
     * Consume por completo el daño extra pendiente (lo devuelve y lo resetea a 0). Se usa al
     * resolver el proximo ataque de este jugador, sea con una carta o con el lider.
     *
     * @return el daño extra que estaba pendiente (0 si no habia ninguno)
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
