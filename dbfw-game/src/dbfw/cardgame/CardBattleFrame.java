package dbfw.cardgame;

import dbfw.cardgame.arbol.ArbolEvolucion;
import dbfw.cardgame.arbol.ArbolesEvolucion;
import dbfw.cardgame.audio.MusicPlayer;
import dbfw.cardgame.estructuras.ListaCircular;
import dbfw.cardgame.estructuras.ListaDoble;
import dbfw.cardgame.estructuras.ListaSimple;
import dbfw.cardgame.excepciones.ColaPrioridadVaciaException;
import dbfw.cardgame.excepciones.MazoVacioException;
import dbfw.cardgame.undertale.PanelEsquive;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
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
import java.util.List;
import java.util.Random;

/**
 * Ventana principal del modo hibrido "Undertale/Slay the Spire", con reglas inspiradas en
 * Dragon Ball Fusion World: lideres con vida, transformacion del lider a baja vida, y cartas
 * que ya no son "personajes" sino acciones de un solo uso (atacar, robar, golpe doble).
 * <p>
 * En el turno del jugador humano el gameplay es estilo Slay the Spire: las cartas de la mano
 * son las acciones disponibles y, al jugarlas, resuelven su efecto de inmediato (daño directo
 * al rival o robar una carta), sin comparar poder contra ninguna otra carta. En el turno de la
 * CPU, sus cartas se resuelven igual, pero el ataque de su Lider (una vez por turno) se
 * dramatiza como una fase de esquive en tiempo real al estilo Undertale ({@link PanelEsquive}):
 * el jugador mueve un corazon con las flechas del teclado para esquivar una lluvia de balas.
 * <p>
 * Esta clase concentra toda la logica de la partida entre el jugador humano y la CPU:
 * <ul>
 *   <li>Construccion de la interfaz (tablero panoramico en perspectiva, lideres, historial de
 *       acciones jugadas, mano y letrero de eventos).</li>
 *   <li>Turnos: inicio de turno, jugar cartas de la mano, atacar con el lider, usar la
 *       habilidad de potenciar y terminar el turno (lo que dispara el turno automatico de la
 *       CPU, incluyendo la fase de esquive).</li>
 *   <li>Resolucion de acciones ({@link #resolverEfectosPendientes}): aplica daño directo o
 *       robo de cartas en orden de prioridad, con combo ofensivo opcional.</li>
 *   <li>Inteligencia artificial simple de la CPU: que carta jugar y cuando usar combo.</li>
 * </ul>
 */
public class CardBattleFrame extends JFrame {
    private final CardPlayer human;
    private final CardPlayer cpu;
    private final Random random = new Random();
    private boolean gameOver = false;
    /** Dificultad elegida antes de iniciar la partida (ver {@code dbfw.Main}); afecta el mazo de la CPU, su IA de combo y la fase de esquive. */
    private final Dificultad dificultad;

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
    private final JButton btnBoost = new JButton("Potenciar tu proximo ataque (+1 dano, 1 energia)");
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
     *
     * @param dificultad dificultad elegida en el selector inicial (ver {@code dbfw.Main}):
     *                   ajusta el mazo de la CPU, su IA de combo y la fase de esquive
     */
    public CardBattleFrame(Dificultad dificultad) {
        super("Tecmilenio Heroes - Undertale/Slay the Spire (" + dificultad + ")");
        this.dificultad = dificultad;

        human = new CardPlayer("Tu", LeaderCard.crearLiderAzul());
        cpu = new CardPlayer("CPU", LeaderCard.crearLiderCpu());
        human.buildDeck();
        cpu.buildDeck(dificultad.getConteoPorFamilia());
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

        appendLog("=== TECMILENIO HEROES - Modo Undertale/Slay the Spire ===");
        appendLog("Dificultad: " + dificultad + ".");
        appendLog("Haz clic en una carta de tu mano para usarla como accion, o ataca con tu Lider.");
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
     * <p>
     * Como las cartas son acciones de un solo uso (estilo Slay the Spire), cada una resuelve su
     * efecto de inmediato: las de tipo {@code DRAW} hacen robar 1 carta, y el resto aplica su
     * {@code getDanoDirecto()} (doblado si es Double Strike) como daño directo al oponente. El
     * combo ofensivo ({@code comboExtra}) y el bono de "Potenciar" del jugador se suman solo al
     * primer ataque que se resuelva en esta tanda.
     *
     * @param jugador    jugador cuyos efectos pendientes se van a resolver
     * @param oponente   jugador que recibe el daño directo de las acciones de ataque
     * @param comboExtra daño extra de combo a sumar al primer ataque resuelto (0 si no hubo combo)
     */
    private void resolverEfectosPendientes(CardPlayer jugador, CardPlayer oponente, int comboExtra) {
        boolean primerAtaque = true;
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
                continue;
            }

            int golpes = carta.isDoubleStrike() ? 2 : 1;
            int dano = carta.getDanoDirecto() * golpes;
            if (primerAtaque) {
                dano += comboExtra;
            }
            primerAtaque = false;

            int bonoPotenciar = jugador.consumirBonusAtaque();
            if (bonoPotenciar > 0) {
                dano += bonoPotenciar;
                appendLog(jugador.getName() + " usa su bono de Potenciar: +" + bonoPotenciar + " de daño.");
            }

            appendLog(carta.getName() + " golpea a " + oponente.getName() + " por " + dano + " de vida"
                    + (carta.isDoubleStrike() ? " (Double Strike, 2 golpes)." : "."));
            boolean derrotado = oponente.takeDamage(dano);
            if (derrotado) {
                declararDerrota(oponente);
                return;
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
                && human.getEnergyAvailable() >= human.getLeader().getBoostCost();
        btnBoost.setEnabled(!gameOver && puedePotenciar);
        btnEndTurn.setEnabled(!gameOver);
    }

    /**
     * Crea el boton que representa al lider de un jugador. El area de batalla ya no muestra
     * "atacantes en la mesa": ahora solo el Lider ataca en su turno, mostrando el daño directo
     * que causaria ({@link LeaderCard#getDanoAtaque(int)}).
     * @param p                      jugador dueño del lider
     * @param interactivoParaAtacar true si el boton debe permitir atacar con este lider (solo el humano)
     */
    private JButton crearBotonLider(CardPlayer p, boolean interactivoParaAtacar) {
        LeaderCard l = p.getLeader();
        int dano = l.getDanoAtaque(p == human ? human.getHand().tamano() : 0);
        String texto = "<html><center>" + l.getName() + "<br>Dano " + dano
                + (l.isTransformed() ? "<br>(Transformado)" : "") + (l.isRested() ? "<br>[ya ataco]" : "") + "</center></html>";
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

    /** @return el texto descriptivo corto para la accion asociada a un tipo de carta. */
    private String etiquetaTipo(CardType tipo) {
        switch (tipo) {
            case DRAW: return "Jalar Carta";
            case GUARD: return "Ataque Fuerte";
            case DOUBLE_STRIKE: return "Golpe Doble";
            default: return "Ataque Basico";
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

    /** Boton para una carta en la mano del jugador: al hacer clic, se juega como una accion instantanea (si hay energia). */
    private JButton crearBotonCartaMano(GameCard c) {
        String descAccion = c.drawsOnPlay() ? "Roba 1 carta" : ("Dano " + c.getDanoDirecto() + (c.isDoubleStrike() ? " x2" : ""));
        String texto = "<html><center>" + c.getName() + "<br>" + etiquetaTipo(c.getType())
                + "<br>" + descAccion + "<br>Costo " + c.getCost()
                + "<br>Combo +" + c.getComboPower() + "</center></html>";
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

    /** Boton para una carta ya jugada: al ser una accion de un solo uso, solo sirve de historial visual del turno. */
    private JButton crearBotonCartaAreaBatalla(GameCard c, boolean esDelJugador) {
        String texto = "<html><center>" + c.getName() + "<br>" + etiquetaTipo(c.getType()) + "</center></html>";
        JButton b = new JButton(texto);
        b.setIcon(CardArt.stickmanIcon(c.getType(), esDelJugador ? Color.BLUE : Color.RED));
        b.setHorizontalTextPosition(SwingConstants.CENTER);
        b.setVerticalTextPosition(SwingConstants.BOTTOM);
        b.setBackground(colorTipo(c.getType()));
        b.setOpaque(true);
        b.setEnabled(false);
        return b;
    }

    // ---------------- ACCIONES DEL JUGADOR ----------------

    /** Juega una carta de la mano como una accion instantanea: robar, o daño directo con combo opcional. */
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
        appendLog("Juegas " + c.getName() + ".");

        int comboExtra = 0;
        if (!c.drawsOnPlay()) {
            comboExtra = preguntarCombo(human, "reforzar el ataque de " + c.getName());
            if (comboExtra > 0) {
                appendLog("Usas combo: +" + comboExtra + " de daño extra.");
            }
        }
        human.encolarEfectoDeCarta(c);
        resolverEfectosPendientes(human, cpu, comboExtra);
        if (gameOver) {
            return;
        }
        refreshUI();
    }

    /** Maneja el clic en el boton de potenciar: gasta energia y prepara un bono de daño para tu proximo ataque. */
    private void onBoost() {
        if (gameOver) {
            return;
        }
        if (human.getLeader().isBoostUsedThisTurn()) {
            appendLog("Ya usaste Potenciar este turno.");
            return;
        }
        if (!human.spendEnergy(human.getLeader().getBoostCost())) {
            appendLog("No tienes suficiente energia para potenciar.");
            return;
        }
        human.getLeader().setBoostUsedThisTurn(true);
        int bono = human.getLeader().getBoostAmount();
        human.agregarBonusAtaque(bono);
        appendLog("Tu lider potencia tu proximo ataque (+" + bono + " de daño).");
        refreshUI();
    }

    /** Ataca con el lider del jugador humano: daño directo (con bono de mano), permite combo y roba una carta. */
    private void atacarConLider() {
        if (gameOver || human.getLeader().isRested()) {
            return;
        }
        int dano = human.getLeader().getDanoAtaque(human.getHand().tamano());
        human.getLeader().setRested(true);
        appendLog("Tu lider ataca.");

        int combo = preguntarCombo(human, "el ataque de tu Lider");
        if (combo > 0) {
            dano += combo;
            appendLog("Usas combo: +" + combo + " de daño (total " + dano + ").");
        }
        int bono = human.consumirBonusAtaque();
        if (bono > 0) {
            dano += bono;
            appendLog("Usas tu bono de Potenciar: +" + bono + " de daño (total " + dano + ").");
        }

        try {
            human.drawCard();
            appendLog("Tu lider roba 1 carta al atacar.");
        } catch (MazoVacioException e) {
            declararDerrota(human);
            return;
        }

        appendLog("Tu lider golpea a la CPU por " + dano + " de vida.");
        boolean derrotado = cpu.takeDamage(dano);
        if (derrotado) {
            declararDerrota(cpu);
            return;
        }
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
     * Ejecuta el turno completo de la CPU: robar, jugar cartas (acciones) mientras tenga
     * energia, y luego atacar con su lider si no esta girado. El ataque del lider CPU es el
     * clímax del turno: se dramatiza como una fase de esquive en tiempo real estilo Undertale
     * (ver {@link #iniciarFaseEsquive}) en vez de resolverse por comparacion de poder.
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
                    appendLog("CPU juega " + c.getName() + ".");
                    int comboCpu = c.drawsOnPlay() ? 0 : cpuComboOfensivoOportunista();
                    cpu.encolarEfectoDeCarta(c);
                    // Se resuelve de inmediato (en orden de prioridad si hubiera mas de un
                    // efecto encolado) para que el daño de esta carta se aplique antes de decidir
                    // si la CPU sigue jugando otra.
                    resolverEfectosPendientes(cpu, human, comboCpu);
                    if (gameOver) {
                        return;
                    }
                    jugoAlgo = true;
                    break;
                }
            }
        }
        refreshUI();

        // El Lider CPU ataca una vez por turno (si no esta girado): en vez de comparar poder,
        // el jugador humano esquiva el ataque en tiempo real (estilo Undertale). Antes de que
        // empiece la lluvia de balas, puede quemar cartas de su mano en combo para conseguir
        // "escudos" (golpes que se absorben sin perder vida).
        if (!cpu.getLeader().isRested()) {
            cpu.getLeader().setRested(true);
            int danoPorGolpe = cpu.getLeader().getDanoAtaque(0);
            appendLog("\n¡El Lider CPU ataca! Prepara tu corazon para esquivar...");

            int escudos = preguntarCombo(human, "prepararte con escudos para esquivar al Lider CPU");
            if (escudos > 0) {
                appendLog("Preparas " + escudos + " escudo(s) con combo.");
            }

            boolean fasesDificiles = cpu.getLeader().isTransformed();
            int duracionBase = fasesDificiles ? 8000 : 6000;
            int spawnMinBase = fasesDificiles ? 350 : 500;
            int spawnMaxBase = fasesDificiles ? 700 : 1000;
            double velMinBase = fasesDificiles ? 2.5 : 1.8;
            double velMaxBase = fasesDificiles ? 4.5 : 3.2;

            // La dificultad elegida al iniciar la partida escala la fase de esquive: mas
            // duracion, balas mas seguidas (intervalo menor) y mas rapidas en Dificil.
            int duracionMs = (int) Math.round(duracionBase * dificultad.getMultiplicadorDuracion());
            int spawnMinMs = Math.max(120, (int) Math.round(spawnMinBase * dificultad.getMultiplicadorSpawn()));
            int spawnMaxMs = Math.max(spawnMinMs + 80, (int) Math.round(spawnMaxBase * dificultad.getMultiplicadorSpawn()));
            double velMin = velMinBase * dificultad.getMultiplicadorVelocidad();
            double velMax = velMaxBase * dificultad.getMultiplicadorVelocidad();

            int golpes = iniciarFaseEsquive(duracionMs, spawnMinMs, spawnMaxMs, velMin, velMax, escudos);

            try {
                cpu.drawCard();
                appendLog("El Lider CPU roba 1 carta al atacar.");
            } catch (MazoVacioException e) {
                declararDerrota(cpu);
                return;
            }

            if (golpes <= 0) {
                appendLog("¡Esquivaste todos los ataques del Lider CPU! No recibes daño.");
            } else {
                int danoTotal = golpes * danoPorGolpe;
                appendLog("El corazon recibio " + golpes + " golpe(s): pierdes " + danoTotal + " de vida.");
                boolean derrotado = human.takeDamage(danoTotal);
                if (derrotado) {
                    declararDerrota(human);
                    return;
                }
            }
        }
        refreshUI();
    }

    /**
     * Abre un dialogo modal con la fase de esquive ({@link PanelEsquive}) y espera a que
     * termine. Como un {@code JDialog} modal sigue despachando eventos (incluidos los del
     * {@code Timer} interno del panel) mientras esta visible, este metodo puede escribirse de
     * forma sincrona: no retorna hasta que la fase de esquive termino.
     *
     * @param duracionMs          duracion de la fase, en milisegundos
     * @param spawnMinMs          intervalo minimo entre balas nuevas
     * @param spawnMaxMs          intervalo maximo entre balas nuevas
     * @param velMin              velocidad minima de las balas
     * @param velMax              velocidad maxima de las balas
     * @param escudos             golpes que se absorben sin quitar vida (de combo previo)
     * @return la cantidad de golpes que el corazon recibio (ya sin contar los escudos)
     */
    private int iniciarFaseEsquive(int duracionMs, int spawnMinMs, int spawnMaxMs,
                                    double velMin, double velMax, int escudos) {
        JDialog dialogo = new JDialog(this, "¡Esquiva el ataque del Lider CPU!", true);
        PanelEsquive panel = new PanelEsquive(duracionMs, spawnMinMs, spawnMaxMs, velMin, velMax, escudos);
        dialogo.getContentPane().add(panel);
        dialogo.pack();
        dialogo.setResizable(false);
        dialogo.setLocationRelativeTo(this);
        panel.iniciar(dialogo::dispose);
        dialogo.setVisible(true);
        return panel.getGolpesRecibidos();
    }

    /**
     * IA simple de combo ofensivo para la CPU: segun la probabilidad de la dificultad elegida
     * (ver {@link Dificultad#getProbabilidadComboCpu()}), quema la carta de su mano con menor
     * poder de combo (para no gastar sus mejores comodines) y suma ese daño extra al ataque.
     */
    private int cpuComboOfensivoOportunista() {
        if (cpu.getHand().esVacia() || random.nextDouble() > dificultad.getProbabilidadComboCpu()) {
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
        appendLog("CPU quema " + elegido.getName() + " en combo (+" + elegido.getComboPower() + " de daño).");
        return elegido.getComboPower();
    }

    // ---------------- COMBO ----------------

    /**
     * Muestra un dialogo para que el jugador humano elija cartas de su mano para usar en combo
     * (se queman: se descartan permanentemente) y devuelve la suma de su poder de combo. Se usa
     * tanto para reforzar un ataque como, defensivamente, para preparar escudos antes de la fase
     * de esquive del Lider CPU.
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
