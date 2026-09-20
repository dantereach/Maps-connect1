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
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.util.List;
import java.util.function.IntConsumer;

/**
 * Ventana unica del juego hibrido Undertale/Slay the Spire, inspirado en Dragon Ball Fusion World.
 * Todo pasa en esta misma ventana: se usa un {@link CardLayout} para cambiar de pantalla
 * (dificultad, tablero, combo, esquive, historial, arbol, configuracion y fin de partida)
 * en vez de abrir dialogos o ventanas nuevas.
 * En el turno del jugador, las cartas son acciones instantaneas organizadas en un menu
 * de Ataque e Item. La CPU no tiene acceso al sistema de cartas: su unica accion cada
 * turno es atacar con su Lider, resuelto como una fase de esquive en tiempo real al
 * estilo Undertale con {@link PanelEsquive}.
 */
public class CardBattleFrame extends JFrame {
    /**
     * Categoria que se muestra en la mano.
     * ATAQUE hace daño; ITEM cubre cartas de utilidad.
     */
    private enum CategoriaAccion { ATAQUE, ITEM }

    /** Vida fija con la que arranca el jugador. */
    private static final int VIDA_JUGADOR = 7;
    /** Vida fija con la que arranca la CPU. El enemigo no usa cartas, asi que es mas resistente. */
    private static final int VIDA_CPU = 20;

    private static final String PANTALLA_DIFICULTAD = "dificultad";
    private static final String PANTALLA_JUEGO = "juego";
    private static final String PANTALLA_ESQUIVE = "esquive";
    private static final String PANTALLA_COMBO = "combo";
    private static final String PANTALLA_HISTORIAL = "historial";
    private static final String PANTALLA_ARBOL = "arbol";
    private static final String PANTALLA_CONFIGURACION = "configuracion";
    private static final String PANTALLA_FIN = "fin";

    private CardPlayer human;
    private CardPlayer cpu;
    private boolean gameOver = false;
    /** Dificultad elegida al iniciar; ajusta la fase de esquive de la CPU. */
    private Dificultad dificultad;
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

    /** Contenedor con todas las pantallas del juego; solo una se ve a la vez, sin ventanas nuevas. */
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardsRoot = new JPanel(cardLayout);

    private final BarraVida barraVidaCpu = new BarraVida("CPU");
    private final BarraVida barraVidaHuman = new BarraVida("TU");
    private final JLabel infoHuman = new JLabel();
    private final JPanel cpuBattlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 6));
    private final JPanel humanBattlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 6));
    private final JPanel handPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 6));
    /** Cuadro de dialogo con el ultimo evento importante, con efecto de maquina de escribir. */
    private final JLabel eventBanner = new JLabel(" ", SwingConstants.LEFT);
    /** Reloj que revela el texto del cuadro de dialogo caracter por caracter. */
    private Timer maquinaEscribir;
    /** Boton Ataque: muestra las cartas que hacen daño directo. */
    private final JButton btnMenuAtaque = new JButton("ATAQUE");
    /** Boton Item: muestra cartas de utilidad, como robar. */
    private final JButton btnMenuItem = new JButton("ITEM");
    private final JButton btnBoost = new JButton("Potenciar tu proximo ataque (+1 dano, 1 energia)");
    private final JButton btnEndTurn = new JButton("Terminar Turno");
    private final JButton btnHistorial = new JButton("Historial");
    private final JButton btnArbol = new JButton("Arbol de Evolucion");
    private final JButton btnConfiguracion = new JButton("Configuracion");
    /** Borde del panel de mano; cambia entre "Ataque" e "Item". */
    private final TitledBorder bordeMano = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(TemaUndertale.VERDE_OSCURO, 2), "Tu mano - Ataque");
    /**
     * Reproductor de musica de fondo.
     * Si no existe {@code music/theme.mp3}, el juego sigue sin musica.
     */
    private final MusicPlayer musica = new MusicPlayer();

    // Pantalla de combo: se reutiliza para reforzar ataques y para preparar escudos antes de esquivar.
    private final DefaultListModel<GameCard> comboModelo = new DefaultListModel<>();
    private final JList<GameCard> comboLista = new JList<>(comboModelo);
    private final JLabel comboTitulo = new JLabel(" ", SwingConstants.CENTER);
    /** Que hacer con el total de combo elegido; se define cada vez que se abre la pantalla de combo. */
    private IntConsumer comboAlConfirmar;

    /** Contenedor donde se inserta un {@link PanelEsquive} nuevo cada vez que ataca el Lider CPU. */
    private final JPanel esquiveContenedor = new JPanel(new GridBagLayout());

    private final JLabel historialTexto = new JLabel(" ", SwingConstants.CENTER);

    private final JComboBox<String> arbolFamilias = new JComboBox<>();
    private final JTextArea arbolTexto = new JTextArea();

    private final JSlider sliderVolumen = new JSlider(0, 100, 70);
    private final JCheckBox casillaSilencio = new JCheckBox("Silenciar musica");
    private final JLabel infoDificultadLabel = new JLabel(" ");
    private final JLabel avisoMusicaLabel = new JLabel(" ");

    private final JLabel finTitulo = new JLabel(" ", SwingConstants.CENTER);

    /** Crea la ventana unica del juego y muestra primero la pantalla de dificultad. */
    public CardBattleFrame() {
        super("Tecmilenio Heroes - Undertale/Slay the Spire");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(TemaUndertale.FONDO);

        cardsRoot.add(crearPantallaDificultad(), PANTALLA_DIFICULTAD);
        cardsRoot.add(crearPantallaCombo(), PANTALLA_COMBO);
        cardsRoot.add(crearPantallaEsquive(), PANTALLA_ESQUIVE);
        cardsRoot.add(crearPantallaHistorial(), PANTALLA_HISTORIAL);
        cardsRoot.add(crearPantallaArbol(), PANTALLA_ARBOL);
        cardsRoot.add(crearPantallaConfiguracion(), PANTALLA_CONFIGURACION);
        cardsRoot.add(crearPantallaFin(), PANTALLA_FIN);
        add(cardsRoot, BorderLayout.CENTER);

        setSize(950, 750);
        setLocationRelativeTo(null);
        cardLayout.show(cardsRoot, PANTALLA_DIFICULTAD);

        musicaDisponible = musica.reproducirTema();
        sliderVolumen.setValue(Math.round(musica.getVolumen() * 100));
        sliderVolumen.setEnabled(musicaDisponible);
        casillaSilencio.setSelected(musica.isSilenciado());
        casillaSilencio.setEnabled(musicaDisponible);
        avisoMusicaLabel.setText(musicaDisponible ? " " : "(No se encontro " + MusicPlayer.RUTA_TEMA_POR_DEFECTO + ")");
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                musica.detener();
            }
        });
    }

    // ---------------- PANTALLA: DIFICULTAD ----------------

    /** Primera pantalla: elegir dificultad. Al elegir, arranca la partida en esta misma ventana. */
    private JPanel crearPantallaDificultad() {
        JPanel panel = new JPanel(new GridBagLayout());
        TemaUndertale.fondoNegro(panel);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel titulo = new JLabel("TECMILENIO HEROES");
        titulo.setFont(new Font("Consolas", Font.BOLD, 30));
        titulo.setForeground(TemaUndertale.VERDE_BRILLANTE);
        gbc.gridy = 0;
        panel.add(titulo, gbc);

        JLabel subtitulo = new JLabel("Elige la dificultad de la partida");
        subtitulo.setFont(TemaUndertale.FUENTE_MENU);
        subtitulo.setForeground(TemaUndertale.VERDE);
        gbc.gridy = 1;
        panel.add(subtitulo, gbc);

        for (Dificultad d : Dificultad.values()) {
            JButton boton = new JButton(d.toString());
            TemaUndertale.estilizar(boton);
            boton.setPreferredSize(new Dimension(220, 44));
            boton.addActionListener(e -> iniciarPartida(d));
            gbc.gridy++;
            panel.add(boton, gbc);
        }
        return panel;
    }

    /** Arma a ambos jugadores con la dificultad elegida y cambia a la pantalla del tablero. */
    private void iniciarPartida(Dificultad elegida) {
        this.dificultad = elegida;
        setTitle("Tecmilenio Heroes - Undertale/Slay the Spire (" + dificultad + ")");

        human = new CardPlayer("Tu", LeaderCard.crearLiderAzul(), VIDA_JUGADOR);
        // La CPU no usa cartas, asi que solo necesita su Lider y su vida.
        cpu = new CardPlayer("CPU", LeaderCard.crearLiderCpu(VIDA_CPU), VIDA_CPU);
        human.buildDeck();
        human.drawInitialHand(5);
        human.startTurn();
        ordenTurnos.agregar(human);
        ordenTurnos.agregar(cpu);

        cardsRoot.add(crearPantallaJuego(), PANTALLA_JUEGO);
        infoDificultadLabel.setText("Dificultad de la partida: " + dificultad);

        appendLog("=== TECMILENIO HEROES - Modo Undertale/Slay the Spire ===");
        appendLog("Dificultad: " + dificultad + ".");
        appendLog("El enemigo no usa cartas: solo ataca esquivando, al estilo Undertale.");
        appendLog("Elige ATAQUE o ITEM para ver esas cartas de tu mano, o ataca con tu Lider.");
        refreshUI();
        cardLayout.show(cardsRoot, PANTALLA_JUEGO);
    }

    // ---------------- PANTALLA: TABLERO PRINCIPAL ----------------

    /** Arma la pantalla del tablero: jefe arriba, tu lider abajo, mano y controles al fondo. */
    private JPanel crearPantallaJuego() {
        JPanel raiz = new JPanel(new BorderLayout());
        TemaUndertale.fondoNegro(raiz);

        BoardPanel board = new BoardPanel();
        board.setLayout(new BorderLayout());

        eventBanner.setOpaque(true);
        eventBanner.setBackground(new Color(0, 0, 0, 210));
        eventBanner.setForeground(TemaUndertale.VERDE_BRILLANTE);
        eventBanner.setFont(new Font("Consolas", Font.BOLD, 14));
        eventBanner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(TemaUndertale.VERDE_OSCURO, 2),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));

        barraVidaCpu.setPreferredSize(new Dimension(260, 32));
        barraVidaHuman.setPreferredSize(new Dimension(260, 32));

        cpuBattlePanel.setOpaque(false);
        humanBattlePanel.setOpaque(false);
        handPanel.setOpaque(false);

        JPanel filaCpu = new JPanel(new BorderLayout());
        filaCpu.setOpaque(false);
        filaCpu.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
        JPanel infoCpuWrap = new JPanel(new FlowLayout(FlowLayout.CENTER));
        infoCpuWrap.setOpaque(false);
        infoCpuWrap.add(barraVidaCpu);
        filaCpu.add(infoCpuWrap, BorderLayout.NORTH);
        filaCpu.add(cpuBattlePanel, BorderLayout.CENTER);

        JPanel norte = new JPanel(new BorderLayout());
        norte.setOpaque(false);
        JPanel bannerWrap = new JPanel(new BorderLayout());
        bannerWrap.setOpaque(false);
        bannerWrap.setBorder(BorderFactory.createEmptyBorder(4, 10, 0, 10));
        bannerWrap.add(eventBanner, BorderLayout.CENTER);
        norte.add(bannerWrap, BorderLayout.NORTH);
        norte.add(filaCpu, BorderLayout.CENTER);
        board.add(norte, BorderLayout.NORTH);

        JPanel filaHumano = new JPanel(new BorderLayout());
        filaHumano.setOpaque(false);
        filaHumano.add(humanBattlePanel, BorderLayout.CENTER);
        JPanel infoHumanWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        infoHumanWrap.setOpaque(false);
        infoHumanWrap.add(barraVidaHuman);
        TemaUndertale.estilizar(infoHuman);
        infoHumanWrap.add(infoHuman);
        filaHumano.add(infoHumanWrap, BorderLayout.SOUTH);
        board.add(filaHumano, BorderLayout.SOUTH);

        raiz.add(board, BorderLayout.CENTER);

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

        raiz.add(sur, BorderLayout.SOUTH);
        return raiz;
    }

    /** Cambia la categoria de accion mostrada en la mano (Ataque/Item) y refresca la interfaz. */
    private void cambiarCategoria(CategoriaAccion categoria) {
        this.categoriaActual = categoria;
        refreshUI();
    }

    /** Muestra el ultimo mensaje con efecto de maquina de escribir (como el cuadro de dialogo de Undertale) y lo guarda en el historial. */
    private void appendLog(String texto) {
        historial.agregarFinal(texto);
        if (maquinaEscribir != null) {
            maquinaEscribir.stop();
        }
        int[] indice = {0};
        maquinaEscribir = new Timer(18, null);
        maquinaEscribir.addActionListener(e -> {
            indice[0]++;
            String visible = texto.substring(0, Math.min(indice[0], texto.length()));
            eventBanner.setText("<html>" + visible.replace("\n", "<br>") + "</html>");
            if (indice[0] >= texto.length()) {
                maquinaEscribir.stop();
            }
        });
        maquinaEscribir.start();
    }

    /**
     * Resuelve los efectos pendientes de tus cartas jugadas, en orden de prioridad.
     * Solo el jugador humano llega aqui: la CPU no tiene acceso al sistema de cartas.
     * Las cartas de robo hacen jalar 1 carta y las demas hacen 1 de daño directo por golpe.
     * El combo se suma solo al primer ataque de esta tanda.
     *
     * @param jugador jugador cuyos efectos pendientes se resuelven (siempre el humano)
     * @param oponente jugador que recibe el daño directo (la CPU)
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
            int dano = golpes; // daño base fijo de 1 por golpe
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
        barraVidaCpu.actualizar(cpu.getLife(), cpu.getVidaMaxima());
        barraVidaHuman.actualizar(human.getLife(), human.getVidaMaxima());
        infoHuman.setText("  Energia: " + human.getEnergyAvailable() + "/" + human.getEnergyMax()
                + "   Mano: " + human.getHand().tamano() + " cartas   Mazo: " + human.getDeck().tamano());

        cpuBattlePanel.removeAll();
        cpuBattlePanel.add(crearBotonLider(cpu, false));
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
        if (c.drawsOnPlay()) {
            human.encolarEfectoDeCarta(c);
            resolverEfectosPendientes(human, cpu, 0);
            if (!gameOver) {
                refreshUI();
            }
            return;
        }
        refreshUI();
        mostrarPantallaCombo("reforzar el ataque de " + c.getName(), comboExtra -> {
            if (comboExtra > 0) {
                appendLog("Usas combo: +" + comboExtra + " de daño extra.");
            }
            human.encolarEfectoDeCarta(c);
            resolverEfectosPendientes(human, cpu, comboExtra);
            if (gameOver) {
                return;
            }
            cardLayout.show(cardsRoot, PANTALLA_JUEGO);
            refreshUI();
        });
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
        human.getLeader().setRested(true);
        appendLog("Tu lider ataca.");
        refreshUI();
        mostrarPantallaCombo("el ataque de tu Lider", combo -> {
            int dano = 1;
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
            cardLayout.show(cardsRoot, PANTALLA_JUEGO);
            refreshUI();
        });
    }

    /** Termina el turno del jugador humano y arranca el turno de la CPU (solo su fase de esquive). */
    private void onEndTurn() {
        if (gameOver) {
            return;
        }
        btnEndTurn.setEnabled(false);
        appendLog("--- Terminas tu turno ---");
        ordenTurnos.avanzar(); // ahora "juega" la CPU
        turnoCpu();
    }

    // ---------------- TURNO DE LA CPU (SIN CARTAS) ----------------

    /**
     * Turno de la CPU. No tiene acceso al sistema de cartas: su unica accion es
     * atacar con su Lider, resuelto en la fase de esquive. Antes de esquivar,
     * el jugador puede quemar cartas de su mano en combo para ganar escudos.
     */
    private void turnoCpu() {
        appendLog("\n=== Turno de la CPU ===");
        appendLog("El Lider CPU no usa cartas: se prepara para atacar directo.");
        mostrarPantallaCombo("prepararte con escudos para esquivar al Lider CPU", escudos -> {
            if (escudos > 0) {
                appendLog("Preparas " + escudos + " escudo(s) con combo.");
            }
            lanzarAtaqueDelJefe(escudos);
        });
    }

    /** Calcula los parametros de la fase de esquive segun la dificultad y si el jefe ya se transformo. */
    private void lanzarAtaqueDelJefe(int escudos) {
        appendLog("\n¡El Lider CPU ataca! Prepara tu corazon para esquivar...");
        int danoPorGolpe = cpu.getLeader().getDanoAtaque(0);
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

        mostrarFaseEsquive(duracionMs, spawnMinMs, spawnMaxMs, velMin, velMax, escudos, golpes -> {
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
            cardLayout.show(cardsRoot, PANTALLA_JUEGO);
            continuarTrasTurnoCpu();
        });
    }

    /** Cierra el turno de la CPU: vuelve el turno al humano, roba 1 carta y refresca la interfaz. */
    private void continuarTrasTurnoCpu() {
        if (gameOver) {
            return;
        }
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

    // ---------------- PANTALLA: COMBO ----------------

    /** Arma la pantalla de seleccion de combo (se reutiliza en varios momentos del turno). */
    private JPanel crearPantallaCombo() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        TemaUndertale.fondoNegro(panel);
        panel.setBorder(BorderFactory.createEmptyBorder(24, 50, 24, 50));

        TemaUndertale.estilizar(comboTitulo);
        comboTitulo.setFont(TemaUndertale.FUENTE_MENU);
        panel.add(comboTitulo, BorderLayout.NORTH);

        comboLista.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        comboLista.setBackground(TemaUndertale.FONDO);
        comboLista.setForeground(TemaUndertale.VERDE);
        comboLista.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel lbl = new JLabel("  " + value.getName() + "   -   Combo +" + value.getComboPower());
            lbl.setOpaque(true);
            lbl.setFont(TemaUndertale.FUENTE_MENU);
            lbl.setBackground(isSelected ? TemaUndertale.VERDE_OSCURO : TemaUndertale.FONDO);
            lbl.setForeground(isSelected ? TemaUndertale.VERDE_BRILLANTE : TemaUndertale.VERDE);
            return lbl;
        });
        JScrollPane scroll = new JScrollPane(comboLista);
        scroll.setBorder(BorderFactory.createLineBorder(TemaUndertale.VERDE_OSCURO, 2));
        panel.add(scroll, BorderLayout.CENTER);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 10));
        TemaUndertale.fondoNegro(botones);
        JButton btnConfirmar = new JButton("Confirmar combo");
        JButton btnSaltar = new JButton("Sin combo");
        TemaUndertale.estilizar(btnConfirmar);
        TemaUndertale.estilizar(btnSaltar);
        btnConfirmar.addActionListener(e -> confirmarCombo(true));
        btnSaltar.addActionListener(e -> confirmarCombo(false));
        botones.add(btnConfirmar);
        botones.add(btnSaltar);
        panel.add(botones, BorderLayout.SOUTH);
        return panel;
    }

    /**
     * Abre la pantalla de combo con la mano actual del jugador humano.
     * Si la mano esta vacia, no hay nada que combear y se llama el callback directo con 0.
     *
     * @param contexto texto que explica para que se va a usar el combo
     * @param alConfirmar que hacer con el total de daño/escudos elegido
     */
    private void mostrarPantallaCombo(String contexto, IntConsumer alConfirmar) {
        if (human.getHand().esVacia()) {
            alConfirmar.accept(0);
            return;
        }
        comboModelo.clear();
        for (GameCard c : human.getHand()) {
            comboModelo.addElement(c);
        }
        comboLista.clearSelection();
        comboTitulo.setText("<html><center>¿Quemar cartas de tu mano en combo para<br>" + contexto + "?</center></html>");
        comboAlConfirmar = alConfirmar;
        cardLayout.show(cardsRoot, PANTALLA_COMBO);
    }

    /** Suma el poder de combo de las cartas elegidas (si se confirma), las quema y avisa al callback pendiente. */
    private void confirmarCombo(boolean usarSeleccion) {
        int total = 0;
        if (usarSeleccion) {
            List<GameCard> seleccion = comboLista.getSelectedValuesList();
            for (GameCard c : seleccion) {
                total += c.getComboPower();
                human.getHand().remover(c);
            }
        }
        IntConsumer callback = comboAlConfirmar;
        comboAlConfirmar = null;
        if (callback != null) {
            callback.accept(total);
        }
    }

    // ---------------- PANTALLA: ESQUIVE ----------------

    /** Contenedor vacio donde se inserta el {@link PanelEsquive} de cada ataque del jefe. */
    private JPanel crearPantallaEsquive() {
        esquiveContenedor.setOpaque(true);
        esquiveContenedor.setBackground(TemaUndertale.FONDO);
        return esquiveContenedor;
    }

    /**
     * Inserta un {@link PanelEsquive} nuevo en la pantalla de esquive y la muestra.
     * Cuando la fase termina, se avisa al callback con los golpes recibidos y sin
     * abrir ninguna ventana o dialogo aparte: todo pasa en esta misma pantalla.
     */
    private void mostrarFaseEsquive(int duracionMs, int spawnMinMs, int spawnMaxMs,
                                     double velMin, double velMax, int escudos, IntConsumer alTerminar) {
        esquiveContenedor.removeAll();
        PanelEsquive panel = new PanelEsquive(duracionMs, spawnMinMs, spawnMaxMs, velMin, velMax, escudos);
        esquiveContenedor.add(panel, new GridBagConstraints());
        esquiveContenedor.revalidate();
        esquiveContenedor.repaint();
        cardLayout.show(cardsRoot, PANTALLA_ESQUIVE);
        // Se espera a que la tarjeta este visible antes de pedir el foco para las flechas.
        SwingUtilities.invokeLater(() -> panel.iniciar(() -> alTerminar.accept(panel.getGolpesRecibidos())));
    }

    // ---------------- PANTALLA: HISTORIAL ----------------

    private JPanel crearPantallaHistorial() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        TemaUndertale.fondoNegro(panel);
        panel.setBorder(BorderFactory.createEmptyBorder(24, 50, 24, 50));

        JLabel titulo = new JLabel("Historial de jugadas", SwingConstants.CENTER);
        titulo.setFont(TemaUndertale.FUENTE_MENU);
        TemaUndertale.estilizar(titulo);
        panel.add(titulo, BorderLayout.NORTH);

        TemaUndertale.estilizar(historialTexto);
        historialTexto.setPreferredSize(new Dimension(400, 220));
        panel.add(historialTexto, BorderLayout.CENTER);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        TemaUndertale.fondoNegro(botones);
        JButton anterior = new JButton("< Anterior");
        JButton siguiente = new JButton("Siguiente >");
        JButton volver = new JButton("Volver");
        for (JButton b : new JButton[]{anterior, siguiente, volver}) {
            TemaUndertale.estilizar(b);
        }
        anterior.addActionListener(e -> {
            historial.irAnterior();
            actualizarHistorialTexto();
        });
        siguiente.addActionListener(e -> {
            historial.irSiguiente();
            actualizarHistorialTexto();
        });
        volver.addActionListener(e -> cardLayout.show(cardsRoot, PANTALLA_JUEGO));
        botones.add(anterior);
        botones.add(siguiente);
        botones.add(volver);
        panel.add(botones, BorderLayout.SOUTH);
        return panel;
    }

    private void actualizarHistorialTexto() {
        historialTexto.setText("<html><center>" + historial.actual().replace("\n", "<br>") + "</center></html>");
    }

    /** Abre la pantalla del historial para recorrer las jugadas, una a la vez. */
    private void mostrarHistorial() {
        if (historial.esVacia()) {
            historialTexto.setText("Aun no hay jugadas en el historial.");
        } else {
            actualizarHistorialTexto();
        }
        cardLayout.show(cardsRoot, PANTALLA_HISTORIAL);
    }

    // ---------------- PANTALLA: ARBOL DE EVOLUCION ----------------

    private JPanel crearPantallaArbol() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        TemaUndertale.fondoNegro(panel);
        panel.setBorder(BorderFactory.createEmptyBorder(24, 50, 24, 50));

        JPanel norte = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 6));
        TemaUndertale.fondoNegro(norte);
        JLabel etiqueta = new JLabel("Familia de cartas:");
        TemaUndertale.estilizar(etiqueta);
        norte.add(etiqueta);
        for (String nombre : ArbolesEvolucion.nombresFamilias()) {
            arbolFamilias.addItem(nombre);
        }
        arbolFamilias.addActionListener(e -> actualizarArbolTexto());
        norte.add(arbolFamilias);
        panel.add(norte, BorderLayout.NORTH);

        arbolTexto.setEditable(false);
        arbolTexto.setFont(new Font("Consolas", Font.PLAIN, 13));
        arbolTexto.setBackground(TemaUndertale.FONDO);
        arbolTexto.setForeground(TemaUndertale.VERDE);
        JScrollPane scroll = new JScrollPane(arbolTexto);
        scroll.setBorder(BorderFactory.createLineBorder(TemaUndertale.VERDE_OSCURO, 2));
        panel.add(scroll, BorderLayout.CENTER);

        JButton volver = new JButton("Volver");
        TemaUndertale.estilizar(volver);
        volver.addActionListener(e -> cardLayout.show(cardsRoot, PANTALLA_JUEGO));
        JPanel sur = new JPanel(new FlowLayout(FlowLayout.CENTER));
        TemaUndertale.fondoNegro(sur);
        sur.add(volver);
        panel.add(sur, BorderLayout.SOUTH);
        return panel;
    }

    private void actualizarArbolTexto() {
        String elegida = (String) arbolFamilias.getSelectedItem();
        if (elegida == null) {
            arbolTexto.setText("");
            return;
        }
        ArbolEvolucion<GameCard> arbol = ArbolesEvolucion.buscar(elegida);
        ListaSimple<String> lineas = arbol.recorrerCompleto(c -> c.getName() + " (PWR " + c.getBasePower() + ")");
        StringBuilder texto = new StringBuilder();
        for (String linea : lineas) {
            texto.append(linea).append("\n");
        }
        arbolTexto.setText(texto.toString());
    }

    /** Abre la pantalla del arbol de evolucion de la familia seleccionada. */
    private void mostrarArbolEvolucion() {
        if (arbolFamilias.getItemCount() == 0) {
            return;
        }
        if (arbolFamilias.getSelectedIndex() < 0) {
            arbolFamilias.setSelectedIndex(0);
        }
        actualizarArbolTexto();
        cardLayout.show(cardsRoot, PANTALLA_ARBOL);
    }

    // ---------------- PANTALLA: CONFIGURACION ----------------

    private JPanel crearPantallaConfiguracion() {
        JPanel panel = new JPanel(new GridLayout(0, 1, 6, 12));
        TemaUndertale.fondoNegro(panel);
        panel.setBorder(BorderFactory.createEmptyBorder(40, 80, 40, 80));

        TemaUndertale.estilizar(infoDificultadLabel);
        panel.add(infoDificultadLabel);

        JLabel etiquetaVolumen = new JLabel("Volumen de la musica");
        TemaUndertale.estilizar(etiquetaVolumen);
        panel.add(etiquetaVolumen);

        sliderVolumen.setMajorTickSpacing(25);
        sliderVolumen.setPaintTicks(true);
        sliderVolumen.setPaintLabels(true);
        TemaUndertale.fondoNegro(sliderVolumen);
        sliderVolumen.setForeground(TemaUndertale.VERDE);
        sliderVolumen.addChangeListener(e -> musica.setVolumen(sliderVolumen.getValue() / 100f));
        panel.add(sliderVolumen);

        TemaUndertale.fondoNegro(casillaSilencio);
        casillaSilencio.setForeground(TemaUndertale.VERDE);
        casillaSilencio.addActionListener(e -> musica.alternarSilencio());
        panel.add(casillaSilencio);

        TemaUndertale.estilizar(avisoMusicaLabel);
        panel.add(avisoMusicaLabel);

        JButton volver = new JButton("Volver");
        TemaUndertale.estilizar(volver);
        volver.addActionListener(e -> cardLayout.show(cardsRoot, PANTALLA_JUEGO));
        JPanel sur = new JPanel(new FlowLayout(FlowLayout.CENTER));
        TemaUndertale.fondoNegro(sur);
        sur.add(volver);
        panel.add(sur);
        return panel;
    }

    /** Abre la pantalla de configuracion (dificultad, volumen y silencio de musica). */
    private void mostrarConfiguracion() {
        cardLayout.show(cardsRoot, PANTALLA_CONFIGURACION);
    }

    // ---------------- PANTALLA: FIN DE PARTIDA ----------------

    private JPanel crearPantallaFin() {
        JPanel panel = new JPanel(new BorderLayout(10, 20));
        TemaUndertale.fondoNegro(panel);
        panel.setBorder(BorderFactory.createEmptyBorder(60, 40, 60, 40));

        finTitulo.setFont(new Font("Consolas", Font.BOLD, 28));
        TemaUndertale.estilizar(finTitulo);
        panel.add(finTitulo, BorderLayout.CENTER);

        JButton jugarDeNuevo = new JButton("Jugar de nuevo");
        JButton salir = new JButton("Salir");
        TemaUndertale.estilizar(jugarDeNuevo);
        TemaUndertale.estilizar(salir);
        jugarDeNuevo.addActionListener(e -> reiniciarPartida());
        salir.addActionListener(e -> dispose());
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        TemaUndertale.fondoNegro(botones);
        botones.add(jugarDeNuevo);
        botones.add(salir);
        panel.add(botones, BorderLayout.SOUTH);
        return panel;
    }

    /** Cierra esta ventana y abre una nueva partida limpia (misma ventana unica, sin dialogos). */
    private void reiniciarPartida() {
        musica.detener();
        dispose();
        SwingUtilities.invokeLater(() -> new CardBattleFrame().setVisible(true));
    }

    /** Marca la partida como terminada, registra el resultado en el log y muestra la pantalla de fin. */
    private void declararDerrota(CardPlayer perdedor) {
        gameOver = true;
        String ganador = perdedor == human ? "CPU" : "Tu";
        appendLog("\n*** " + perdedor.getName() + " ha sido derrotado. Gana " + ganador + "! ***");
        refreshUI();
        finTitulo.setText(ganador.equals("Tu") ? "¡GANASTE!" : "PERDISTE. La CPU gana.");
        cardLayout.show(cardsRoot, PANTALLA_FIN);
    }

    /**
     * Barra de vida estilo Undertale: relleno amarillo sobre fondo rojo oscuro,
     * con el nombre y el numero de HP encima.
     */
    private static class BarraVida extends JPanel {
        private final String etiqueta;
        private int actual;
        private int maximo = 1;

        BarraVida(String etiqueta) {
            this.etiqueta = etiqueta;
            setOpaque(false);
        }

        /** Actualiza la vida mostrada y repinta la barra. */
        void actualizar(int actual, int maximo) {
            this.actual = Math.max(0, actual);
            this.maximo = Math.max(1, maximo);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int barraAlto = 12;
            int barraY = getHeight() - barraAlto - 2;

            g2.setFont(TemaUndertale.FUENTE_MENU);
            g2.setColor(TemaUndertale.VERDE_BRILLANTE);
            g2.drawString(etiqueta + "   HP " + actual + "/" + maximo, 2, barraY - 6);

            g2.setColor(new Color(90, 20, 20));
            g2.fillRoundRect(0, barraY, w, barraAlto, 6, 6);
            int lleno = (int) Math.round(w * (actual / (double) maximo));
            g2.setColor(new Color(255, 205, 40));
            g2.fillRoundRect(0, barraY, Math.max(0, lleno), barraAlto, 6, 6);
            g2.setColor(TemaUndertale.VERDE_OSCURO);
            g2.drawRoundRect(0, barraY, Math.max(1, w - 1), barraAlto - 1, 6, 6);
            g2.dispose();
        }
    }
}
