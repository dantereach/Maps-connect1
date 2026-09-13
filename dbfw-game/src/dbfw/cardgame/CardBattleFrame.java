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
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;
import java.util.Random;

/**
 * Ventana principal del juego hibrido Undertale/Slay the Spire,
 * inspirado en Dragon Ball Fusion World.
 * En el turno del jugador, las cartas son acciones instantaneas
 * y se organizan en un menu de Ataque e Item.
 * En el turno de la CPU, el ataque de su lider se resuelve como
 * una fase de esquive en tiempo real con {@link PanelEsquive}.
 * Esta clase maneja la interfaz, los turnos, la resolucion por
 * prioridad, la Ia simple de la CPU y la configuracion de volumen y musica.
 */
public class CardBattleFrame extends JFrame {
    /**
     * Categoria que se muestra en la mano.
     * ATAQUE hace daño; ITEM cubre cartas de utilidad.
     */
    private enum CategoriaAccion { ATAQUE, ITEM }

    private final CardPlayer human;
    private final CardPlayer cpu;
    private final Random random = new Random();
    private boolean gameOver = false;
    /** Dificultad elegida al iniciar; ajusta la CPU y la fase de esquive. */
    private final Dificultad dificultad;
    /** Categoria de cartas que se muestra en la mano. */
    private CategoriaAccion categoriaActual = CategoriaAccion.ATAQUE;
    /** Indica si la musica de fondo pudo cargarse y reproducirse. */
    private boolean musicaDisponible;

    /**
     * Orden de turnos entre humano y CPU.
     * Se rota con la {@link ListaCircular} propia.
     */
    private final ListaCircular<CardPlayer> ordenTurnos = new ListaCircular<>();
    /**
     * Historial navegable de eventos de la partida.
     * Usa la {@link ListaDoble} propia.
     */
    private final ListaDoble<String> historial = new ListaDoble<>();

    private final JLabel infoCpu = new JLabel();
    private final JLabel infoHuman = new JLabel();
    private final JPanel cpuBattlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 6));
    private final JPanel humanBattlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 6));
    private final JPanel handPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 6));
    /**
     * Letrero con el ultimo evento importante.
     * El historial completo se abre con {@link #mostrarHistorial()}.
     */
    private final JLabel eventBanner = new JLabel(" ", SwingConstants.CENTER);
    /** Boton Ataque: muestra las cartas que hacen daño directo. */
    private final JButton btnMenuAtaque = new JButton("ATAQUE");
    /** Boton Item: muestra cartas de utilidad, como robar. */
    private final JButton btnMenuItem = new JButton("ITEM");
    private final JButton btnBoost = new JButton("Potenciar tu proximo ataque (+1 dano, 1 energia)");
    private final JButton btnEndTurn = new JButton("Terminar Turno");
    private final JButton btnHistorial = new JButton("Ver Historial");
    private final JButton btnArbol = new JButton("Ver Arbol de Evolucion");
    private final JButton btnConfiguracion = new JButton("Configuracion");
    /** Borde del panel de mano; cambia entre "Ataque" e "Item". */
    private final TitledBorder bordeMano = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(TemaUndertale.VERDE_OSCURO, 2), "Tu mano - Ataque");
    /**
     * Reproductor de musica de fondo.
     * Si no existe {@code music/theme.mp3}, el juego sigue sin musica.
     */
    private final MusicPlayer musica = new MusicPlayer();

    /**
     * Crea la ventana, prepara a ambos jugadores y arma la interfaz.
     *
     * @param dificultad dificultad elegida al inicio; ajusta la CPU y la fase de esquive
     */
    public CardBattleFrame(Dificultad dificultad) {
        super("Tecmilenio Heroes - Undertale/Slay the Spire (" + dificultad + ")");
        this.dificultad = dificultad;

        human = new CardPlayer("Tu", LeaderCard.crearLiderAzul());
        // La CPU tiene el doble de vida del jugador para compensar su daño base mas bajo.
        cpu = new CardPlayer("CPU", LeaderCard.crearLiderCpu());
        human.buildDeck();
        cpu.buildDeck(dificultad.getConteoPorFamilia());
        human.drawInitialHand(5);
        cpu.drawInitialHand(5);
        human.startTurn();
        ordenTurnos.agregar(human);
        ordenTurnos.agregar(cpu);

        setLayout(new BorderLayout());

        // Tablero panoramico con ambas filas y el letrero de eventos en un solo panel.
        BoardPanel board = new BoardPanel();
        board.setLayout(new BorderLayout());

        eventBanner.setOpaque(true);
        eventBanner.setBackground(new Color(0, 0, 0, 210));
        eventBanner.setForeground(TemaUndertale.VERDE_BRILLANTE);
        eventBanner.setFont(new Font("Consolas", Font.BOLD, 14));
        eventBanner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(TemaUndertale.VERDE_OSCURO, 2),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));

        infoCpu.setOpaque(true);
        infoCpu.setBackground(new Color(30, 0, 0, 200));
        infoCpu.setForeground(TemaUndertale.VERDE_BRILLANTE);
        infoCpu.setFont(new Font("Consolas", Font.BOLD, 14));
        infoCpu.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(TemaUndertale.VERDE_OSCURO, 1),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)));

        infoHuman.setOpaque(true);
        infoHuman.setBackground(new Color(0, 0, 30, 200));
        infoHuman.setForeground(TemaUndertale.VERDE_BRILLANTE);
        infoHuman.setFont(new Font("Consolas", Font.BOLD, 14));
        infoHuman.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(TemaUndertale.VERDE_OSCURO, 1),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)));

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

        // Abajo van el menu Ataque/Item y los controles del turno.
        JPanel sur = new JPanel(new BorderLayout());
        TemaUndertale.fondoNegro(sur);
        bordeMano.setTitleColor(TemaUndertale.VERDE);
        bordeMano.setTitleFont(TemaUndertale.FUENTE_MENU);
        sur.setBorder(bordeMano);

        JPanel menuCategorias = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 4));
        TemaUndertale.fondoNegro(menuCategorias);
        TemaUndertale.estilizar(btnMenuAtaque);
        TemaUndertale.estilizar(btnMenuItem);
        btnMenuAtaque.addActionListener(e -> cambiarCategoria(CategoriaAccion.ATAQUE));
        btnMenuItem.addActionListener(e -> cambiarCategoria(CategoriaAccion.ITEM));
        menuCategorias.add(btnMenuAtaque);
        menuCategorias.add(btnMenuItem);
        sur.add(menuCategorias, BorderLayout.NORTH);
        sur.add(handPanel, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel(new FlowLayout());
        TemaUndertale.fondoNegro(controlPanel);
        btnBoost.addActionListener(e -> onBoost());
        btnEndTurn.addActionListener(e -> onEndTurn());
        btnHistorial.addActionListener(e -> mostrarHistorial());
        btnArbol.addActionListener(e -> mostrarArbolEvolucion());
        btnConfiguracion.addActionListener(e -> mostrarConfiguracion());
        for (JButton boton : new JButton[]{btnBoost, btnEndTurn, btnHistorial, btnArbol, btnConfiguracion}) {
            TemaUndertale.estilizar(boton);
            controlPanel.add(boton);
        }
        sur.add(controlPanel, BorderLayout.SOUTH);

        add(sur, BorderLayout.SOUTH);
        getContentPane().setBackground(TemaUndertale.FONDO);

        appendLog("=== TECMILENIO HEROES - Modo Undertale/Slay the Spire ===");
        appendLog("Dificultad: " + dificultad + ".");
        appendLog("Elige ATAQUE o ITEM para ver esas cartas de tu mano, o ataca con tu Lider.");
        refreshUI();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 750);
        setLocationRelativeTo(null);

        musicaDisponible = musica.reproducirTema();
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                musica.detener();
            }
        });
    }

    /** Cambia la categoria de accion mostrada en la mano (Ataque/Item) y refresca la interfaz. */
    private void cambiarCategoria(CategoriaAccion categoria) {
        this.categoriaActual = categoria;
        refreshUI();
    }

    /** Abre la ventana de configuracion para ver la dificultad y ajustar la musica. */
    private void mostrarConfiguracion() {
        JPanel panel = new JPanel(new GridLayout(0, 1, 4, 8));
        TemaUndertale.fondoNegro(panel);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        JLabel infoDificultad = new JLabel("Dificultad de la partida: " + dificultad);
        TemaUndertale.estilizar(infoDificultad);
        panel.add(infoDificultad);

        JLabel etiquetaVolumen = new JLabel("Volumen de la musica");
        TemaUndertale.estilizar(etiquetaVolumen);
        panel.add(etiquetaVolumen);

        JSlider sliderVolumen = new JSlider(0, 100, Math.round(musica.getVolumen() * 100));
        sliderVolumen.setMajorTickSpacing(25);
        sliderVolumen.setPaintTicks(true);
        sliderVolumen.setPaintLabels(true);
        TemaUndertale.fondoNegro(sliderVolumen);
        sliderVolumen.setForeground(TemaUndertale.VERDE);
        sliderVolumen.setEnabled(musicaDisponible);
        sliderVolumen.addChangeListener(e -> musica.setVolumen(sliderVolumen.getValue() / 100f));
        panel.add(sliderVolumen);

        JCheckBox casillaSilencio = new JCheckBox("Silenciar musica", musica.isSilenciado());
        TemaUndertale.fondoNegro(casillaSilencio);
        casillaSilencio.setForeground(TemaUndertale.VERDE);
        casillaSilencio.setEnabled(musicaDisponible);
        casillaSilencio.addActionListener(e -> musica.alternarSilencio());
        panel.add(casillaSilencio);

        if (!musicaDisponible) {
            JLabel aviso = new JLabel("(No se encontro " + MusicPlayer.RUTA_TEMA_POR_DEFECTO + ")");
            TemaUndertale.estilizar(aviso);
            panel.add(aviso);
        }

        JOptionPane.showMessageDialog(this, panel, "Configuracion", JOptionPane.PLAIN_MESSAGE);
    }

    /** Muestra el ultimo mensaje en pantalla y lo guarda en el historial. */
    private void appendLog(String texto) {
        eventBanner.setText("<html>" + texto.replace("\n", "<br>") + "</html>");
        historial.agregarFinal(texto);
    }

    /** Abre un dialogo para recorrer el historial de jugadas, un evento a la vez. */
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

    /** Abre un dialogo para elegir una familia y ver su arbol de evolucion completo. */
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
     * Resuelve los efectos pendientes de un jugador en orden de prioridad.
     * Las cartas de robo hacen jalar 1 carta y las demas hacen daño directo.
     * El combo y Potenciar solo se suman al primer ataque de esta tanda.
     *
     * @param jugador jugador cuyos efectos pendientes se resuelven
     * @param oponente jugador que recibe el daño directo
     * @param comboExtra daño extra de combo para el primer ataque
     */
    private void resolverEfectosPendientes(CardPlayer jugador, CardPlayer oponente, int comboExtra) {
        boolean primerAtaque = true;
        while (!jugador.getEfectosPendientes().esVacia()) {
            GameCard carta;
            try {
                carta = jugador.getEfectosPendientes().desencolar();
            } catch (ColaPrioridadVaciaException e) {
                break; // no deberia pasar; la cola ya estaba validada
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
            // El jugador humano siempre hace 1 de daño base por golpe; la CPU usa el daño normal de la carta.
            int danoBase = jugador == human ? 1 : carta.getDanoDirecto();
            int dano = danoBase * golpes;
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

    /** Refresca toda la interfaz segun el estado actual de la partida. */
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
            boolean esCartaItem = c.drawsOnPlay();
            boolean coincideConCategoria = categoriaActual == CategoriaAccion.ITEM ? esCartaItem : !esCartaItem;
            if (coincideConCategoria) {
                handPanel.add(crearBotonCartaMano(c));
            }
        }
        handPanel.revalidate();
        handPanel.repaint();

        bordeMano.setTitle(categoriaActual == CategoriaAccion.ATAQUE ? "Tu mano - Ataque" : "Tu mano - Item");
        TemaUndertale.marcarSeleccionado(btnMenuAtaque, categoriaActual == CategoriaAccion.ATAQUE);
        TemaUndertale.marcarSeleccionado(btnMenuItem, categoriaActual == CategoriaAccion.ITEM);
        handPanel.getParent().repaint();

        boolean puedePotenciar = human.getLeader().canBoost() && !human.getLeader().isBoostUsedThisTurn()
                && human.getEnergyAvailable() >= human.getLeader().getBoostCost();
        btnBoost.setEnabled(!gameOver && puedePotenciar);
        btnEndTurn.setEnabled(!gameOver);
    }

    /**
     * Crea el boton del lider y, si es el humano, permite atacar con el.
     *
     * @param p jugador dueño del lider
     * @param interactivoParaAtacar true si el boton debe permitir atacar
     */
    private JButton crearBotonLider(CardPlayer p, boolean interactivoParaAtacar) {
        LeaderCard l = p.getLeader();
        // El daño base del jugador humano siempre es 1; la CPU usa el calculo normal del lider.
        int dano = p == human ? 1 : l.getDanoAtaque(0);
        String texto = "<html><center>" + l.getName() + "<br>Dano " + dano
                + (l.isTransformed() ? "<br>(Transformado)" : "") + (l.isRested() ? "<br>[ya ataco]" : "") + "</center></html>";
        JButton b = new JButton(texto);
        Color colorBase = p == human ? Color.BLUE : Color.RED;
        b.setIcon(CardArt.leaderIcon(colorBase, l.isTransformed()));
        b.setHorizontalTextPosition(SwingConstants.CENTER);
        b.setVerticalTextPosition(SwingConstants.BOTTOM);
        b.setBackground(l.isTransformed() ? new Color(255, 200, 120) : new Color(200, 220, 255));
        b.setOpaque(true);
        b.setBorder(BorderFactory.createLineBorder(TemaUndertale.VERDE_OSCURO, 2));
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
        String descAccion = c.drawsOnPlay() ? "Roba 1 carta" : ("Dano 1" + (c.isDoubleStrike() ? " x2" : ""));
        String texto = "<html><center>" + c.getName() + "<br>" + etiquetaTipo(c.getType())
                + "<br>" + descAccion + "<br>Costo " + c.getCost()
                + "<br>Combo +" + c.getComboPower() + "</center></html>";
        JButton b = new JButton(texto);
        b.setIcon(CardArt.stickmanIcon(c.getType(), new Color(70, 70, 70)));
        b.setHorizontalTextPosition(SwingConstants.CENTER);
        b.setVerticalTextPosition(SwingConstants.BOTTOM);
        b.setBackground(colorTipo(c.getType()));
        b.setOpaque(true);
        b.setBorder(BorderFactory.createLineBorder(TemaUndertale.VERDE_OSCURO, 2));
        b.setEnabled(!gameOver && human.getEnergyAvailable() >= c.getCost());
        b.addActionListener(e -> jugarCartaDeMano(c));
        return b;
    }

    /** Boton de una carta ya jugada; solo sirve como referencia visual del turno. */
    private JButton crearBotonCartaAreaBatalla(GameCard c, boolean esDelJugador) {
        String texto = "<html><center>" + c.getName() + "<br>" + etiquetaTipo(c.getType()) + "</center></html>";
        JButton b = new JButton(texto);
        b.setIcon(CardArt.stickmanIcon(c.getType(), esDelJugador ? Color.BLUE : Color.RED));
        b.setHorizontalTextPosition(SwingConstants.CENTER);
        b.setVerticalTextPosition(SwingConstants.BOTTOM);
        b.setBackground(colorTipo(c.getType()));
        b.setOpaque(true);
        b.setBorder(BorderFactory.createLineBorder(TemaUndertale.VERDE_OSCURO, 2));
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

    /** Ataca con el lider del jugador humano: daño base de 1, permite combo/Potenciar y roba una carta. */
    private void atacarConLider() {
        if (gameOver || human.getLeader().isRested()) {
            return;
        }
        // Daño base fijo en 1; el combo y Potenciar todavia pueden sumar daño extra encima.
        int dano = 1;
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
        ordenTurnos.avanzar(); // ahora juega la CPU
        turnoCpu();
        if (!gameOver) {
            ordenTurnos.avanzar(); // vuelve el turno al humano
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
     * Ejecuta el turno de la CPU: roba, juega acciones y ataca con su lider.
     * Ese ataque se resuelve con la fase de esquive de {@link #iniciarFaseEsquive}.
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

        // La CPU juega mientras tenga energia; el ciclo se corta en cuanto la mano cambia.
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
                    // Se resuelve al momento para que ese daño cuente antes de otra jugada.
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

        // El lider CPU ataca con una fase de esquive; antes puedes usar combo para ganar escudos.
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

            // La dificultad cambia la duracion, la frecuencia y la velocidad de las balas.
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
     * Abre el dialogo modal con {@link PanelEsquive}, espera a que termine
     * y devuelve los golpes recibidos.
     *
     * @param duracionMs duracion de la fase, en milisegundos
     * @param spawnMinMs intervalo minimo entre balas nuevas
     * @param spawnMaxMs intervalo maximo entre balas nuevas
     * @param velMin velocidad minima de las balas
     * @param velMax velocidad maxima de las balas
     * @param escudos golpes que se absorben sin quitar vida
     * @return la cantidad de golpes que recibio el corazon
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

    /** Decide si la CPU usa combo ofensivo y, si lo hace, gasta la carta con menor combo. */
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
     * Deja elegir cartas de la mano para quemarlas en combo.
     * Devuelve la suma total de su poder.
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
