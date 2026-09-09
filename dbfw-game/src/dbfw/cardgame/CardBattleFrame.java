package dbfw.cardgame;

import dbfw.cardgame.arbol.ArbolEvolucion;
import dbfw.cardgame.arbol.ArbolesEvolucion;
import dbfw.cardgame.audio.MusicPlayer;
import dbfw.cardgame.estructuras.ListaCircular;
import dbfw.cardgame.estructuras.ListaDoble;
import dbfw.cardgame.estructuras.ListaSimple;
import dbfw.cardgame.excepciones.ColaPrioridadVaciaException;
import dbfw.cardgame.excepciones.MazoVacioException;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Ventana principal del modo "juego de cartas", con reglas inspiradas en Dragon Ball Fusion World:
 * lideres con vida, poder en miles, transformacion del lider a baja vida, y cartas con
 * distintos efectos (robo, guardia, double strike).
 * <p>
 * Esta clase concentra toda la logica de la partida entre el jugador humano y la CPU:
 * <ul>
 *   <li>Construccion de la interfaz (tablero panoramico en perspectiva, lideres, area de
 *       batalla, mano y letrero de eventos).</li>
 *   <li>Turnos: inicio de turno, jugar cartas de la mano, atacar, usar la habilidad de potenciar
 *       del lider y terminar el turno (lo que dispara el turno automatico de la CPU).</li>
 *   <li>Resolucion de combate ({@link #resolverAtaque}): eleccion de bloqueador, cartas de combo
 *       ofensivas/defensivas, comparacion de poder y aplicacion de daño (incluyendo Double Strike).</li>
 *   <li>Inteligencia artificial simple de la CPU: que carta jugar, a que atacar, con que bloquear
 *       y cuando usar combo.</li>
 * </ul>
 */
public class CardBattleFrame extends JFrame {
    private final CardPlayer human;
    private final CardPlayer cpu;
    private final Random random = new Random();
    private boolean gameOver = false;

    /**
     * Ciclo de turnos entre el jugador humano y la CPU: Lista Circular propia (ver
     * {@link ListaCircular}) que se rota con {@code avanzar()} en cada cambio de turno, en vez
     * de alternar manualmente entre dos variables.
     */
    private final ListaCircular<CardPlayer> ordenTurnos = new ListaCircular<>();
    /**
     * Historial navegable de jugadas de la partida: Lista Doblemente Enlazada propia (ver
     * {@link ListaDoble}) que permite recorrer los eventos hacia atras y hacia adelante con un
     * cursor, a diferencia del letrero de eventos de solo lectura ({@link #eventBanner}).
     */
    private final ListaDoble<String> historial = new ListaDoble<>();

    private final JLabel infoCpu = new JLabel();
    private final JLabel infoHuman = new JLabel();
    private final JPanel cpuBattlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 6));
    private final JPanel humanBattlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 6));
    private final JPanel handPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 6));
    /**
     * Aviso del ultimo evento de la partida, mostrado como un letrero sobre el tablero
     * panoramico (en vez del antiguo registro de texto siempre visible). El historial completo
     * sigue disponible y navegable mediante {@link #mostrarHistorial()}.
     */
    private final JLabel eventBanner = new JLabel(" ", SwingConstants.CENTER);
    private final JButton btnBoost = new JButton("Potenciar carta (+5000, 1 energia)");
    private final JButton btnEndTurn = new JButton("Terminar Turno");
    private final JButton btnHistorial = new JButton("Ver Historial");
    private final JButton btnArbol = new JButton("Ver Arbol de Evolucion");
    private final JButton btnMusica = new JButton("Silenciar Musica");
    /**
     * Reproductor de la musica de fondo del juego (ver {@link MusicPlayer}). Cada jugador debe
     * colocar su propio archivo en {@code music/theme.mp3} (excluido de git); si no existe,
     * el juego simplemente continua sin musica.
     */
    private final MusicPlayer musica = new MusicPlayer();

    /**
     * Construye la ventana, crea a ambos jugadores con sus mazos y manos iniciales,
     * arma todos los paneles de la interfaz y deja lista la partida para que el
     * jugador humano tome su primer turno.
     */
    public CardBattleFrame() {
        super("Tecmilenio Heroes - Juego de Cartas");

        human = new CardPlayer("Tu", LeaderCard.crearLiderAzul());
        cpu = new CardPlayer("CPU", LeaderCard.crearLiderCpu());
        human.buildDeck();
        cpu.buildDeck();
        human.drawInitialHand(5);
        cpu.drawInitialHand(5);
        human.startTurn();
        ordenTurnos.agregar(human);
        ordenTurnos.agregar(cpu);

        setLayout(new BorderLayout());

        // Tablero panoramico: un solo panel con piso en perspectiva sobre el que "flotan"
        // el letrero de eventos, la fila de la CPU (arriba, mas lejos) y la fila del jugador
        // (abajo, mas cerca), en vez de paneles separados con bordes rectangulares.
        BoardPanel board = new BoardPanel();
        board.setLayout(new BorderLayout());

        eventBanner.setOpaque(true);
        eventBanner.setBackground(new Color(20, 20, 25, 210));
        eventBanner.setForeground(Color.WHITE);
        eventBanner.setFont(new Font("SansSerif", Font.BOLD, 14));
        eventBanner.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));

        infoCpu.setOpaque(true);
        infoCpu.setBackground(new Color(30, 15, 15, 200));
        infoCpu.setForeground(Color.WHITE);
        infoCpu.setFont(new Font("SansSerif", Font.BOLD, 14));
        infoCpu.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));

        infoHuman.setOpaque(true);
        infoHuman.setBackground(new Color(10, 15, 35, 200));
        infoHuman.setForeground(Color.WHITE);
        infoHuman.setFont(new Font("SansSerif", Font.BOLD, 14));
        infoHuman.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));

        cpuBattlePanel.setOpaque(false);
        humanBattlePanel.setOpaque(false);
        handPanel.setOpaque(false);

        JPanel filaCpu = new JPanel(new BorderLayout());
        filaCpu.setOpaque(false);
        filaCpu.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
        JPanel infoCpuWrap = new JPanel(new FlowLayout(FlowLayout.CENTER));
        infoCpuWrap.setOpaque(false);
        infoCpuWrap.add(infoCpu);
        filaCpu.add(infoCpuWrap, BorderLayout.NORTH);
        filaCpu.add(cpuBattlePanel, BorderLayout.CENTER);

        JPanel norte = new JPanel(new BorderLayout());
        norte.setOpaque(false);
        JPanel bannerWrap = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bannerWrap.setOpaque(false);
        bannerWrap.add(eventBanner);
        norte.add(bannerWrap, BorderLayout.NORTH);
        norte.add(filaCpu, BorderLayout.CENTER);
        board.add(norte, BorderLayout.NORTH);

        JPanel filaHumano = new JPanel(new BorderLayout());
        filaHumano.setOpaque(false);
        filaHumano.add(humanBattlePanel, BorderLayout.CENTER);
        JPanel infoHumanWrap = new JPanel(new FlowLayout(FlowLayout.CENTER));
        infoHumanWrap.setOpaque(false);
        infoHumanWrap.add(infoHuman);
        filaHumano.add(infoHumanWrap, BorderLayout.SOUTH);
        board.add(filaHumano, BorderLayout.SOUTH);

        add(board, BorderLayout.CENTER);

        // Debajo del tablero: la mano del jugador (como cartas "en la mesa" frente a la camara)
        // y los controles de turno.
        JPanel sur = new JPanel(new BorderLayout());
        sur.setBorder(BorderFactory.createTitledBorder("Tu mano"));
        sur.add(handPanel, BorderLayout.NORTH);

        JPanel controlPanel = new JPanel(new FlowLayout());
        btnBoost.addActionListener(e -> onBoost());
        btnEndTurn.addActionListener(e -> onEndTurn());
        btnHistorial.addActionListener(e -> mostrarHistorial());
        btnArbol.addActionListener(e -> mostrarArbolEvolucion());
        btnMusica.addActionListener(e -> onToggleMusica());
        controlPanel.add(btnBoost);
        controlPanel.add(btnEndTurn);
        controlPanel.add(btnHistorial);
        controlPanel.add(btnArbol);
        controlPanel.add(btnMusica);
        sur.add(controlPanel, BorderLayout.SOUTH);

        add(sur, BorderLayout.SOUTH);

        appendLog("=== TECMILENIO HEROES - Juego de Cartas ===");
        appendLog("Haz clic en una carta de tu mano para jugarla, o en una carta/lider de tu area para atacar.");
        refreshUI();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 750);
        setLocationRelativeTo(null);

        boolean sonando = musica.reproducirTema();
        if (!sonando) {
            btnMusica.setText("Musica no encontrada");
            btnMusica.setEnabled(false);
        }
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                musica.detener();
            }
        });
    }

    /** Alterna el silencio de la musica de fondo con {@link #btnMusica}. */
    private void onToggleMusica() {
        boolean silenciado = musica.alternarSilencio();
        btnMusica.setText(silenciado ? "Reanudar Musica" : "Silenciar Musica");
    }

    /**
     * Agrega una linea al area de texto del registro de eventos (para lectura corrida) y
     * tambien la agrega al historial navegable ({@link #historial}, Lista Doble propia).
     */
    private void appendLog(String texto) {
        eventBanner.setText("<html>" + texto.replace("\n", "<br>") + "</html>");
        historial.agregarFinal(texto);
    }

    /**
     * Abre un dialogo que permite navegar el historial de jugadas (Lista Doblemente Enlazada)
     * hacia atras y hacia adelante usando su cursor interno, mostrando un evento a la vez.
     */
    private void mostrarHistorial() {
        if (historial.esVacia()) {
            JOptionPane.showMessageDialog(this, "Aun no hay jugadas en el historial.");
            return;
        }
        JLabel etiqueta = new JLabel("<html><center>" + historial.actual() + "</center></html>", SwingConstants.CENTER);
        etiqueta.setPreferredSize(new Dimension(420, 60));
        JButton anterior = new JButton("< Anterior");
        JButton siguiente = new JButton("Siguiente >");
        anterior.addActionListener(e -> {
            historial.irAnterior();
            etiqueta.setText("<html><center>" + historial.actual() + "</center></html>");
        });
        siguiente.addActionListener(e -> {
            historial.irSiguiente();
            etiqueta.setText("<html><center>" + historial.actual() + "</center></html>");
        });
        JPanel panelNav = new JPanel(new BorderLayout());
        panelNav.add(etiqueta, BorderLayout.CENTER);
        JPanel botones = new JPanel(new FlowLayout());
        botones.add(anterior);
        botones.add(siguiente);
        panelNav.add(botones, BorderLayout.SOUTH);
        JOptionPane.showMessageDialog(this, panelNav, "Historial de jugadas (" + historial.tamano() + " eventos)",
                JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * Abre un dialogo para elegir una familia de cartas y muestra el recorrido recursivo
     * completo de su {@link ArbolEvolucion} (nivel basico -&gt; mejorado -&gt; legendario),
     * buscando el arbol por nombre en {@link ArbolesEvolucion} (tabla hash).
     */
    private void mostrarArbolEvolucion() {
        String[] familias = ArbolesEvolucion.nombresFamilias().toArray(new String[0]);
        if (familias.length == 0) {
            return;
        }
        String elegida = (String) JOptionPane.showInputDialog(this, "Elige una familia de cartas:",
                "Arbol de Evolucion", JOptionPane.PLAIN_MESSAGE, null, familias, familias[0]);
        if (elegida == null) {
            return;
        }
        ArbolEvolucion<GameCard> arbol = ArbolesEvolucion.buscar(elegida);
        ListaSimple<String> lineas = arbol.recorrerCompleto(c -> c.getName() + " (PWR " + c.getBasePower() + ")");
        StringBuilder texto = new StringBuilder();
        for (String linea : lineas) {
            texto.append(linea).append("\n");
        }
        JTextArea area = new JTextArea(texto.toString());
        area.setEditable(false);
        area.setFont(new Font("Consolas", Font.PLAIN, 13));
        JOptionPane.showMessageDialog(this, new JScrollPane(area), "Arbol de evolucion: " + elegida,
                JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * Resuelve, en orden de prioridad (mayor a menor), los efectos de todas las cartas que un
     * jugador jugo y aun no ha resuelto (ver {@code CardPlayer#getEfectosPendientes}, Cola de
     * Prioridad propia): las cartas con habilidad especial mas fuerte se procesan antes que las
     * demas, sin importar en que orden se jugaron dentro de la fase de juego.
     *
     * @param jugador jugador cuyos efectos pendientes se van a resolver
     */
    private void resolverEfectosPendientes(CardPlayer jugador) {
        while (!jugador.getEfectosPendientes().esVacia()) {
            GameCard carta;
            try {
                carta = jugador.getEfectosPendientes().desencolar();
            } catch (ColaPrioridadVaciaException e) {
                break; // no deberia ocurrir: ya se valido con esVacia() justo arriba
            }
            appendLog("Se resuelve el efecto de " + carta.getName() + " (prioridad " + carta.getPrioridadEfecto() + ").");
            if (carta.drawsOnPlay()) {
                try {
                    jugador.drawCard();
                    appendLog(jugador.getName() + " roba 1 carta por el efecto de " + carta.getName() + ".");
                } catch (MazoVacioException e) {
                    declararDerrota(jugador);
                    return;
                }
            }
        }
    }

    // ---------------- RENDER ----------------

    /** Reconstruye toda la interfaz (paneles de info, areas de batalla y mano) a partir del estado actual. */
    private void refreshUI() {
        infoCpu.setText("  Vida: " + cpu.getLife() + "   Energia: " + cpu.getEnergyAvailable() + "/" + cpu.getEnergyMax()
                + "   Mano: " + cpu.getHand().tamano() + " cartas   Mazo: " + cpu.getDeck().tamano());
        infoHuman.setText("  Vida: " + human.getLife() + "   Energia: " + human.getEnergyAvailable() + "/" + human.getEnergyMax()
                + "   Mano: " + human.getHand().tamano() + " cartas   Mazo: " + human.getDeck().tamano());

        cpuBattlePanel.removeAll();
        cpuBattlePanel.add(crearBotonLider(cpu, false));
        for (GameCard c : cpu.getBattleArea()) {
            cpuBattlePanel.add(crearBotonCartaAreaBatalla(c, false));
        }
        cpuBattlePanel.revalidate();
        cpuBattlePanel.repaint();

        humanBattlePanel.removeAll();
        humanBattlePanel.add(crearBotonLider(human, true));
        for (GameCard c : human.getBattleArea()) {
            humanBattlePanel.add(crearBotonCartaAreaBatalla(c, true));
        }
        humanBattlePanel.revalidate();
        humanBattlePanel.repaint();

        handPanel.removeAll();
        for (GameCard c : human.getHand()) {
            handPanel.add(crearBotonCartaMano(c));
        }
        handPanel.revalidate();
        handPanel.repaint();

        boolean puedePotenciar = human.getLeader().canBoost() && !human.getLeader().isBoostUsedThisTurn()
                && human.getEnergyAvailable() >= human.getLeader().getBoostCost() && !human.getBattleArea().esVacia();
        btnBoost.setEnabled(!gameOver && puedePotenciar);
        btnEndTurn.setEnabled(!gameOver);
    }

    /**
     * Crea el boton que representa al lider de un jugador en su area de batalla.
     * @param p                      jugador dueño del lider
     * @param interactivoParaAtacar true si el boton debe permitir atacar con este lider (solo el humano)
     */
    private JButton crearBotonLider(CardPlayer p, boolean interactivoParaAtacar) {
        LeaderCard l = p.getLeader();
        int poder = l == human.getLeader() ? l.getAttackPower(human.getHand().tamano()) : l.getDefensePower();
        String texto = "<html><center>" + l.getName() + "<br>PWR " + poder
                + (l.isTransformed() ? "<br>(Transformado)" : "") + (l.isRested() ? "<br>[girado]" : "") + "</center></html>";
        JButton b = new JButton(texto);
        Color colorBase = p == human ? Color.BLUE : Color.RED;
        b.setIcon(CardArt.leaderIcon(colorBase, l.isTransformed()));
        b.setHorizontalTextPosition(SwingConstants.CENTER);
        b.setVerticalTextPosition(SwingConstants.BOTTOM);
        b.setBackground(l.isTransformed() ? new Color(255, 200, 120) : new Color(200, 220, 255));
        b.setOpaque(true);
        if (interactivoParaAtacar && p == human) {
            b.setEnabled(!gameOver && !l.isRested());
            b.addActionListener(e -> atacarConLider());
        } else {
            b.setEnabled(false);
        }
        return b;
    }

    /** @return el texto descriptivo corto para el efecto de un tipo de carta. */
    private String etiquetaTipo(CardType tipo) {
        switch (tipo) {
            case DRAW: return "Roba 1";
            case GUARD: return "Guardia (25000 def.)";
            case DOUBLE_STRIKE: return "Double Strike";
            default: return "Basica";
        }
    }

    /** @return el color de fondo asociado a cada tipo de carta, para diferenciarlas visualmente. */
    private Color colorTipo(CardType tipo) {
        switch (tipo) {
            case DRAW: return new Color(200, 255, 200);
            case GUARD: return new Color(200, 230, 255);
            case DOUBLE_STRIKE: return new Color(255, 200, 200);
            default: return Color.WHITE;
        }
    }

    /** Boton para una carta en la mano del jugador: al hacer clic, se juega (si hay energia). */
    private JButton crearBotonCartaMano(GameCard c) {
        String texto = "<html><center>" + c.getName() + "<br>PWR " + c.getEffectivePower(false)
                + "<br>" + etiquetaTipo(c.getType()) + "<br>Costo " + c.getCost()
                + "<br>Combo " + c.getComboPower() + "</center></html>";
        JButton b = new JButton(texto);
        b.setIcon(CardArt.stickmanIcon(c.getType(), new Color(70, 70, 70)));
        b.setHorizontalTextPosition(SwingConstants.CENTER);
        b.setVerticalTextPosition(SwingConstants.BOTTOM);
        b.setBackground(colorTipo(c.getType()));
        b.setOpaque(true);
        b.setEnabled(!gameOver && human.getEnergyAvailable() >= c.getCost());
        b.addActionListener(e -> jugarCartaDeMano(c));
        return b;
    }

    /** Boton para una carta en un area de batalla. Solo es interactiva (para atacar) si es del jugador. */
    private JButton crearBotonCartaAreaBatalla(GameCard c, boolean esDelJugador) {
        String texto = "<html><center>" + c.getName() + "<br>PWR " + c.getEffectivePower(false)
                + "<br>" + etiquetaTipo(c.getType())
                + (c.isRested() ? "<br>[girada]" : "") + "</center></html>";
        JButton b = new JButton(texto);
        b.setIcon(CardArt.stickmanIcon(c.getType(), esDelJugador ? Color.BLUE : Color.RED));
        b.setHorizontalTextPosition(SwingConstants.CENTER);
        b.setVerticalTextPosition(SwingConstants.BOTTOM);
        b.setBackground(colorTipo(c.getType()));
        b.setOpaque(true);
        if (esDelJugador) {
            b.setEnabled(!gameOver && !c.isRested());
            b.addActionListener(e -> atacarConCarta(c));
        } else {
            b.setEnabled(false);
        }
        return b;
    }

    // ---------------- ACCIONES DEL JUGADOR ----------------

    /** Juega una carta de la mano al area de batalla, si hay energia suficiente; aplica su efecto de robo si corresponde. */
    private void jugarCartaDeMano(GameCard c) {
        if (gameOver) {
            return;
        }
        if (!human.spendEnergy(c.getCost())) {
            appendLog("No tienes suficiente energia para jugar " + c.getName() + ".");
            return;
        }
        human.getHand().remover(c);
        human.getBattleArea().agregar(c);
        appendLog("Juegas " + c.getName() + " (PWR " + c.getEffectivePower(false) + ").");
        human.encolarEfectoDeCarta(c);
        resolverEfectosPendientes(human);
        if (gameOver) {
            return;
        }
        refreshUI();
    }

    /** Maneja el clic en el boton de potenciar: pide al jugador elegir una carta propia y le suma +5000 de poder. */
    private void onBoost() {
        if (gameOver || human.getBattleArea().esVacia()) {
            return;
        }
        GameCard[] opciones = human.getBattleArea().comoListaTemporal().toArray(new GameCard[0]);
        GameCard elegido = (GameCard) JOptionPane.showInputDialog(this, "Elige la carta a potenciar (+5000):",
                "Potenciar", JOptionPane.PLAIN_MESSAGE, null, opciones, opciones[0]);
        if (elegido == null) {
            return;
        }
        if (!human.spendEnergy(human.getLeader().getBoostCost())) {
            appendLog("No tienes suficiente energia para potenciar.");
            return;
        }
        elegido.addBonus(human.getLeader().getBoostAmount());
        human.getLeader().setBoostUsedThisTurn(true);
        appendLog("Tu lider potencia a " + elegido.getName() + " (+5000 poder).");
        refreshUI();
    }

    /** Ataca con el lider del jugador humano: calcula su poder (con bono de mano), permite combo y roba una carta. */
    private void atacarConLider() {
        if (gameOver || human.getLeader().isRested()) {
            return;
        }
        int poder = human.getLeader().getAttackPower(human.getHand().tamano());
        human.getLeader().setRested(true);
        appendLog("Tu lider ataca con " + poder + " de poder.");

        int combo = preguntarCombo(human, "el ataque de tu Lider");
        if (combo > 0) {
            poder += combo;
            appendLog("Usas combo: +" + combo + " de poder (total " + poder + ").");
        }

        try {
            human.drawCard();
            appendLog("Tu lider roba 1 carta al atacar.");
        } catch (MazoVacioException e) {
            declararDerrota(human);
            return;
        }

        resolverAtaque(human, cpu, poder, false, "Tu Lider");
        refreshUI();
    }

    /** Ataca con una carta del area de batalla del jugador humano (queda girada) y permite reforzarla con combo. */
    private void atacarConCarta(GameCard c) {
        if (gameOver || c.isRested()) {
            return;
        }
        int poder = c.getEffectivePower(false);
        c.setRested(true);
        appendLog(c.getName() + " ataca con " + poder + " de poder.");

        int combo = preguntarCombo(human, "el ataque de " + c.getName());
        if (combo > 0) {
            poder += combo;
            appendLog("Usas combo: +" + combo + " de poder (total " + poder + ").");
        }

        resolverAtaque(human, cpu, poder, c.isDoubleStrike(), c.getName());
        refreshUI();
    }

    /** Termina el turno del jugador humano, ejecuta el turno completo de la CPU y arranca el siguiente turno humano. */
    private void onEndTurn() {
        if (gameOver) {
            return;
        }
        btnEndTurn.setEnabled(false);
        appendLog("--- Terminas tu turno ---");
        ordenTurnos.avanzar(); // el ciclo de turnos (Lista Circular) rota: ahora le toca a la CPU
        turnoCpu();
        if (!gameOver) {
            ordenTurnos.avanzar(); // el ciclo vuelve a rotar: le toca de nuevo al humano
            human.startTurn();
            try {
                human.drawCard();
            } catch (MazoVacioException e) {
                declararDerrota(human);
                return;
            }
            appendLog("\n=== Tu turno (" + ordenTurnos.actual().getName() + ") ===");
            appendLog("Robas 1 carta.");
            refreshUI();
        }
    }

    // ---------------- TURNO DE LA CPU ----------------

    /**
     * Ejecuta el turno completo de la CPU: robar, jugar cartas mientras tenga energia,
     * atacar con su lider (si no esta girado) y luego con cada carta sin girar de su area de batalla.
     */
    private void turnoCpu() {
        appendLog("\n=== Turno de la CPU ===");
        cpu.startTurn();
        try {
            cpu.drawCard();
        } catch (MazoVacioException e) {
            declararDerrota(cpu);
            return;
        }

        // La CPU juega cartas mientras tenga energia suficiente. Se itera directamente sobre la
        // Cola de la mano: como se rompe el bucle for-each apenas se muta la mano (remover/agregar),
        // el iterador nunca se vuelve a usar despues de la mutacion, asi que no hace falta copiarla.
        boolean jugoAlgo = true;
        while (jugoAlgo) {
            jugoAlgo = false;
            for (GameCard c : cpu.getHand()) {
                if (c.getCost() <= cpu.getEnergyAvailable()) {
                    cpu.spendEnergy(c.getCost());
                    cpu.getHand().remover(c);
                    cpu.getBattleArea().agregar(c);
                    appendLog("CPU juega " + c.getName() + " (PWR " + c.getEffectivePower(false) + ").");
                    cpu.encolarEfectoDeCarta(c);
                    jugoAlgo = true;
                    break;
                }
            }
        }
        // Al terminar la fase de juego, se resuelven todos los efectos encolados en orden de
        // prioridad (Cola de Prioridad propia): si la CPU jugo varias cartas, las de habilidad
        // mas fuerte se resuelven antes que las demas, sin importar el orden en que se jugaron.
        resolverEfectosPendientes(cpu);
        if (gameOver) {
            return;
        }
        refreshUI();

        // La CPU ataca con el lider (si no esta girado) y todas sus cartas sin girar.
        if (!cpu.getLeader().isRested()) {
            int poder = cpu.getLeader().getDefensePower();
            cpu.getLeader().setRested(true);
            appendLog("El Lider CPU ataca con " + poder + " de poder.");
            poder += cpuComboOfensivoOportunista();
            try {
                cpu.drawCard();
                appendLog("El Lider CPU roba 1 carta al atacar.");
            } catch (MazoVacioException e) {
                declararDerrota(cpu);
                return;
            }
            resolverAtaque(cpu, human, poder, false, "Lider CPU");
            if (gameOver) {
                return;
            }
        }
        for (GameCard c : cpu.getBattleArea()) {
            if (!c.isRested()) {
                int poder = c.getEffectivePower(false);
                c.setRested(true);
                appendLog("CPU ataca con " + c.getName() + " (PWR " + poder + ").");
                poder += cpuComboOfensivoOportunista();
                resolverAtaque(cpu, human, poder, c.isDoubleStrike(), c.getName());
                if (gameOver) {
                    return;
                }
            }
        }
        refreshUI();
    }

    /**
     * IA simple de combo ofensivo para la CPU: con 30% de probabilidad, quema la carta de su mano
     * con menor poder de combo (para no gastar sus mejores comodines) y suma ese poder al ataque.
     */
    private int cpuComboOfensivoOportunista() {
        if (cpu.getHand().esVacia() || random.nextDouble() > 0.3) {
            return 0;
        }
        GameCard elegido = null;
        for (GameCard c : cpu.getHand()) {
            if (elegido == null || c.getComboPower() < elegido.getComboPower()) {
                elegido = c;
            }
        }
        if (elegido == null) {
            return 0;
        }
        cpu.getHand().remover(elegido);
        appendLog("CPU quema " + elegido.getName() + " en combo (+" + elegido.getComboPower() + " de poder).");
        return elegido.getComboPower();
    }

    // ---------------- RESOLUCION DE COMBATE ----------------

    /**
     * Resuelve un ataque de "atacante" contra "defensor" con el poder dado.
     * Si el defensor es la CPU, la bloquea una IA simple; si es el jugador humano, se le pregunta con un dialogo.
     */
    private void resolverAtaque(CardPlayer atacante, CardPlayer defensor, int poderAtaque, boolean doubleStrike, String nombreAtacante) {
        Object bloqueador = elegirBloqueador(defensor, poderAtaque);

        if (bloqueador == null) {
            int poderLider = defensor.getLeader().getDefensePower();

            // Aunque no bloquees con una carta, puedes usar combo para reforzar la resistencia de tu Lider.
            if (defensor == human) {
                int combo = preguntarCombo(human, "resistir el golpe con tu Lider");
                if (combo > 0) {
                    poderLider += combo;
                    appendLog("Usas combo para reforzar a tu Lider: +" + combo + " de poder (total " + poderLider + ").");
                }
            } else {
                int combo = cpuComboDefensivo(poderAtaque, poderLider);
                if (combo > 0) {
                    poderLider += combo;
                    appendLog("CPU usa combo para reforzar a su Lider: +" + combo + " de poder (total " + poderLider + ").");
                }
            }

            if (poderAtaque < poderLider) {
                appendLog(nombreAtacante + " (PWR " + poderAtaque + ") no logra superar el poder del Lider de "
                        + defensor.getName() + " (PWR " + poderLider + "). No hay daño de vida.");
                return;
            }
            int dano = doubleStrike ? 2 : 1;
            appendLog(nombreAtacante + " conecta sin bloqueo! " + defensor.getName() + " pierde " + dano + " de vida.");
            boolean derrotado = defensor.takeDamage(dano);
            if (derrotado) {
                declararDerrota(defensor);
            }
            return;
        }

        int poderDef;
        if (bloqueador instanceof LeaderCard) {
            poderDef = ((LeaderCard) bloqueador).getDefensePower();
        } else {
            poderDef = ((GameCard) bloqueador).getEffectivePower(true);
        }

        // Combo defensivo: solo tiene sentido si bloquea una CARTA (el lider nunca es destruido,
        // asi que gastar combo para defenderlo seria un desperdicio).
        if (bloqueador instanceof GameCard) {
            if (defensor == human) {
                int combo = preguntarCombo(human, "defenderte");
                if (combo > 0) {
                    poderDef += combo;
                    appendLog("Usas combo para defenderte: +" + combo + " de poder (total " + poderDef + ").");
                }
            } else {
                int combo = cpuComboDefensivo(poderAtaque, poderDef);
                if (combo > 0) {
                    poderDef += combo;
                    appendLog("CPU usa combo para defenderse: +" + combo + " de poder (total " + poderDef + ").");
                }
            }
        }

        if (bloqueador instanceof LeaderCard) {
            LeaderCard l = (LeaderCard) bloqueador;
            l.setRested(true);
            appendLog(defensor.getName() + " bloquea con su Lider (PWR " + poderDef + ").");
            if (poderAtaque > poderDef) {
                appendLog("El lider de " + defensor.getName() + " resiste el golpe, pero no es destruido (los lideres no mueren en combate).");
            } else {
                appendLog("El ataque es repelido por el Lider de " + defensor.getName() + ".");
            }
        } else {
            GameCard c = (GameCard) bloqueador;
            c.setRested(true);
            appendLog(defensor.getName() + " bloquea con " + c.getName() + " (PWR " + poderDef + ").");
            if (poderAtaque > poderDef) {
                defensor.getBattleArea().remover(c);
                appendLog(c.getName() + " es destruida.");
            } else if (poderAtaque < poderDef) {
                appendLog("El ataque es repelido; el atacante no logra destruir a " + c.getName() + ".");
            } else {
                defensor.getBattleArea().remover(c);
                appendLog("Empate de poder: " + c.getName() + " es destruida.");
            }
        }
    }

    /**
     * Muestra un dialogo para que el jugador humano elija cartas de su mano para usar en combo
     * (se queman: se descartan permanentemente) y devuelve la suma de su poder de combo.
     */
    private int preguntarCombo(CardPlayer p, String contexto) {
        if (p.getHand().esVacia()) {
            return 0;
        }
        DefaultListModel<GameCard> modelo = new DefaultListModel<>();
        for (GameCard c : p.getHand()) {
            modelo.addElement(c);
        }
        JList<GameCard> lista = new JList<>(modelo);
        lista.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        lista.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel lbl = new JLabel(value.getName() + "  -  Combo +" + value.getComboPower());
            lbl.setOpaque(true);
            lbl.setBackground(isSelected ? new Color(180, 220, 255) : Color.WHITE);
            return lbl;
        });
        JScrollPane scroll = new JScrollPane(lista);
        scroll.setPreferredSize(new Dimension(320, 150));
        int resultado = JOptionPane.showConfirmDialog(this, scroll,
                "¿Quemar cartas de tu mano en combo para " + contexto + "?", JOptionPane.OK_CANCEL_OPTION);
        if (resultado != JOptionPane.OK_OPTION) {
            return 0;
        }
        List<GameCard> seleccion = lista.getSelectedValuesList();
        int total = 0;
        for (GameCard c : seleccion) {
            total += c.getComboPower();
            p.getHand().remover(c);
        }
        return total;
    }

    /**
     * IA simple de combo defensivo para la CPU: si el bloqueo por si solo no alcanza, intenta quemar
     * cartas de su mano (empezando por las de mayor poder de combo) hasta cubrir la diferencia.
     * Si no le alcanza con toda su mano, no arriesga cartas y deja que el bloqueo pierda igual.
     * Implementado con una seleccion voraz manual (sin Collections.sort) sobre un arreglo temporal.
     */
    private int cpuComboDefensivo(int poderAtaque, int poderDefActual) {
        int faltante = poderAtaque - poderDefActual;
        if (faltante <= 0 || cpu.getHand().esVacia()) {
            return 0;
        }
        GameCard[] disponibles = cpu.getHand().comoListaTemporal().toArray(new GameCard[0]);
        boolean[] usada = new boolean[disponibles.length];
        int acumulado = 0;
        while (acumulado < faltante) {
            int mejorIdx = -1;
            for (int i = 0; i < disponibles.length; i++) {
                if (!usada[i] && (mejorIdx == -1 || disponibles[i].getComboPower() > disponibles[mejorIdx].getComboPower())) {
                    mejorIdx = i;
                }
            }
            if (mejorIdx == -1) {
                break; // ya no quedan cartas disponibles en la mano
            }
            usada[mejorIdx] = true;
            acumulado += disponibles[mejorIdx].getComboPower();
        }
        if (acumulado < faltante) {
            return 0; // no alcanza ni usando toda la mano: no arriesga las cartas
        }
        for (int i = 0; i < disponibles.length; i++) {
            if (usada[i]) {
                cpu.getHand().remover(disponibles[i]);
            }
        }
        return acumulado;
    }

    /** Determina que bloquea el defensor: IA simple para la CPU, dialogo para el humano. */
    private Object elegirBloqueador(CardPlayer defensor, int poderAtaque) {
        if (defensor == cpu) {
            // El lider nunca es destruido en combate, asi que bloquear con el es siempre seguro.
            if (!cpu.getLeader().isRested()) {
                return cpu.getLeader();
            }
            GameCard mejor = null;
            for (GameCard c : cpu.getBattleArea()) {
                if (!c.isRested() && (mejor == null || c.getEffectivePower(true) > mejor.getEffectivePower(true))) {
                    mejor = c;
                }
            }
            if (mejor == null) {
                return null;
            }
            int totalComboDisponible = 0;
            for (GameCard c : cpu.getHand()) {
                totalComboDisponible += c.getComboPower();
            }
            if (mejor.getEffectivePower(true) >= poderAtaque || mejor.getEffectivePower(true) + totalComboDisponible >= poderAtaque) {
                return mejor;
            }
            return null; // no vale la pena arriesgar la carta si de todas formas no alcanza
        } else {
            List<Object> opciones = new ArrayList<>();
            List<String> etiquetas = new ArrayList<>();
            for (GameCard c : human.getBattleArea()) {
                if (!c.isRested()) {
                    opciones.add(c);
                    etiquetas.add(c.getName() + " (PWR " + c.getEffectivePower(true) + ")");
                }
            }
            if (!human.getLeader().isRested()) {
                opciones.add(human.getLeader());
                etiquetas.add("Tu Lider (PWR " + human.getLeader().getDefensePower() + ")");
            }
            etiquetas.add("No bloquear (recibir el ataque)");

            if (opciones.isEmpty()) {
                return null;
            }

            String[] etiquetasArr = etiquetas.toArray(new String[0]);
            String eleccion = (String) JOptionPane.showInputDialog(this,
                    "La CPU te ataca con " + poderAtaque + " de poder. ¿Con que bloqueas?",
                    "Bloquear", JOptionPane.PLAIN_MESSAGE, null, etiquetasArr, etiquetasArr[etiquetasArr.length - 1]);
            if (eleccion == null || eleccion.equals("No bloquear (recibir el ataque)")) {
                return null;
            }
            int idx = etiquetas.indexOf(eleccion);
            return idx >= 0 ? opciones.get(idx) : null;
        }
    }

    /** Marca la partida como terminada, registra el resultado en el log y muestra el dialogo final. */
    private void declararDerrota(CardPlayer perdedor) {
        gameOver = true;
        String ganador = perdedor == human ? "CPU" : "Tu";
        appendLog("\n*** " + perdedor.getName() + " ha sido derrotado. Gana " + ganador + "! ***");
        refreshUI();
        JOptionPane.showMessageDialog(this, (ganador.equals("Tu") ? "¡GANASTE!" : "Perdiste. La CPU gana."),
                "Fin de la partida", JOptionPane.INFORMATION_MESSAGE);
    }
}
