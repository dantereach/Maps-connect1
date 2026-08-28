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
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Ventana principal del modo "juego de cartas", con reglas inspiradas en Dragon Ball Fusion World:
 * lideres con vida, poder en miles, transformacion del lider a baja vida, y cartas con
 * distintos efectos (robo, guardia, double strike).
 */
public class CardBattleFrame extends JFrame {
    private final CardPlayer human;
    private final CardPlayer cpu;
    private final Random random = new Random();
    private boolean gameOver = false;

    private final JLabel infoCpu = new JLabel();
    private final JLabel infoHuman = new JLabel();
    private final JPanel cpuBattlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
    private final JPanel humanBattlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
    private final JPanel handPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
    private final JTextArea log = new JTextArea(10, 60);
    private final JButton btnBoost = new JButton("Potenciar carta (+5000, 1 energia)");
    private final JButton btnEndTurn = new JButton("Terminar Turno");

    public CardBattleFrame() {
        super("Dragon Ball Fusion World - Juego de Cartas");

        human = new CardPlayer("Tu", LeaderCard.crearLiderAzul());
        cpu = new CardPlayer("CPU", LeaderCard.crearLiderCpu());
        human.buildDeck();
        cpu.buildDeck();
        human.drawInitialHand(5);
        cpu.drawInitialHand(5);
        human.startTurn();

        setLayout(new BorderLayout(5, 5));

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setBorder(BorderFactory.createTitledBorder("CPU"));
        topContainer.add(infoCpu, BorderLayout.NORTH);
        topContainer.add(cpuBattlePanel, BorderLayout.CENTER);
        add(topContainer, BorderLayout.NORTH);

        log.setEditable(false);
        log.setFont(new Font("Consolas", Font.PLAIN, 13));
        add(new JScrollPane(log), BorderLayout.CENTER);

        JPanel bottomContainer = new JPanel(new GridLayout(3, 1));
        bottomContainer.setBorder(BorderFactory.createTitledBorder("Tu turno"));

        JPanel playerTop = new JPanel(new BorderLayout());
        playerTop.add(infoHuman, BorderLayout.NORTH);
        playerTop.add(humanBattlePanel, BorderLayout.CENTER);
        bottomContainer.add(playerTop);

        bottomContainer.add(handPanel);

        JPanel controlPanel = new JPanel(new FlowLayout());
        btnBoost.addActionListener(e -> onBoost());
        btnEndTurn.addActionListener(e -> onEndTurn());
        controlPanel.add(btnBoost);
        controlPanel.add(btnEndTurn);
        bottomContainer.add(controlPanel);

        add(bottomContainer, BorderLayout.SOUTH);

        appendLog("=== DRAGON BALL FUSION WORLD - Juego de Cartas ===");
        appendLog("Haz clic en una carta de tu mano para jugarla, o en una carta/lider de tu area para atacar.");
        refreshUI();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 750);
        setLocationRelativeTo(null);
    }

    private void appendLog(String texto) {
        log.append(texto + "\n");
        log.setCaretPosition(log.getDocument().getLength());
    }

    // ---------------- RENDER ----------------

    private void refreshUI() {
        infoCpu.setText("  Vida: " + cpu.getLife() + "   Energia: " + cpu.getEnergyAvailable() + "/" + cpu.getEnergyMax()
                + "   Mano: " + cpu.getHand().size() + " cartas   Mazo: " + cpu.getDeck().size());
        infoHuman.setText("  Vida: " + human.getLife() + "   Energia: " + human.getEnergyAvailable() + "/" + human.getEnergyMax()
                + "   Mano: " + human.getHand().size() + " cartas   Mazo: " + human.getDeck().size());

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
                && human.getEnergyAvailable() >= human.getLeader().getBoostCost() && !human.getBattleArea().isEmpty();
        btnBoost.setEnabled(!gameOver && puedePotenciar);
        btnEndTurn.setEnabled(!gameOver);
    }

    private JButton crearBotonLider(CardPlayer p, boolean interactivoParaAtacar) {
        LeaderCard l = p.getLeader();
        int poder = l == human.getLeader() ? l.getAttackPower(human.getHand().size()) : l.getDefensePower();
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

    private String etiquetaTipo(CardType tipo) {
        switch (tipo) {
            case DRAW: return "Roba 1";
            case GUARD: return "Guardia (25000 def.)";
            case DOUBLE_STRIKE: return "Double Strike";
            default: return "Basica";
        }
    }

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

    private void jugarCartaDeMano(GameCard c) {
        if (gameOver) {
            return;
        }
        if (!human.spendEnergy(c.getCost())) {
            appendLog("No tienes suficiente energia para jugar " + c.getName() + ".");
            return;
        }
        human.getHand().remove(c);
        human.getBattleArea().add(c);
        appendLog("Juegas " + c.getName() + " (PWR " + c.getEffectivePower(false) + ").");
        if (c.drawsOnPlay()) {
            if (!human.drawCard()) {
                declararDerrota(human);
                return;
            }
            appendLog("Robas 1 carta del mazo.");
        }
        refreshUI();
    }

    private void onBoost() {
        if (gameOver || human.getBattleArea().isEmpty()) {
            return;
        }
        GameCard[] opciones = human.getBattleArea().toArray(new GameCard[0]);
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

    private void atacarConLider() {
        if (gameOver || human.getLeader().isRested()) {
            return;
        }
        int poder = human.getLeader().getAttackPower(human.getHand().size());
        human.getLeader().setRested(true);
        appendLog("Tu lider ataca con " + poder + " de poder.");

        int combo = preguntarCombo(human, "el ataque de tu Lider");
        if (combo > 0) {
            poder += combo;
            appendLog("Usas combo: +" + combo + " de poder (total " + poder + ").");
        }

        if (!human.drawCard()) {
            declararDerrota(human);
            return;
        }
        appendLog("Tu lider roba 1 carta al atacar.");

        resolverAtaque(human, cpu, poder, false, "Tu Lider");
        refreshUI();
    }

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

    private void onEndTurn() {
        if (gameOver) {
            return;
        }
        btnEndTurn.setEnabled(false);
        appendLog("--- Terminas tu turno ---");
        turnoCpu();
        if (!gameOver) {
            human.startTurn();
            if (!human.drawCard()) {
                declararDerrota(human);
                return;
            }
            appendLog("\n=== Tu turno ===");
            appendLog("Robas 1 carta.");
            refreshUI();
        }
    }

    // ---------------- TURNO DE LA CPU ----------------

    private void turnoCpu() {
        appendLog("\n=== Turno de la CPU ===");
        cpu.startTurn();
        if (!cpu.drawCard()) {
            declararDerrota(cpu);
            return;
        }

        // La CPU juega cartas mientras tenga energia suficiente.
        boolean jugoAlgo = true;
        while (jugoAlgo) {
            jugoAlgo = false;
            for (GameCard c : new ArrayList<>(cpu.getHand())) {
                if (c.getCost() <= cpu.getEnergyAvailable()) {
                    cpu.spendEnergy(c.getCost());
                    cpu.getHand().remove(c);
                    cpu.getBattleArea().add(c);
                    appendLog("CPU juega " + c.getName() + " (PWR " + c.getEffectivePower(false) + ").");
                    if (c.drawsOnPlay() && !cpu.drawCard()) {
                        declararDerrota(cpu);
                        return;
                    }
                    jugoAlgo = true;
                    break;
                }
            }
        }
        refreshUI();

        // La CPU ataca con el lider (si no esta girado) y todas sus cartas sin girar.
        if (!cpu.getLeader().isRested()) {
            int poder = cpu.getLeader().getDefensePower();
            cpu.getLeader().setRested(true);
            appendLog("El Lider CPU ataca con " + poder + " de poder.");
            poder += cpuComboOfensivoOportunista();
            if (!cpu.drawCard()) {
                declararDerrota(cpu);
                return;
            }
            appendLog("El Lider CPU roba 1 carta al atacar.");
            resolverAtaque(cpu, human, poder, false, "Lider CPU");
            if (gameOver) {
                return;
            }
        }
        for (GameCard c : new ArrayList<>(cpu.getBattleArea())) {
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
        if (cpu.getHand().isEmpty() || random.nextDouble() > 0.3) {
            return 0;
        }
        GameCard elegido = cpu.getHand().stream().min(Comparator.comparingInt(GameCard::getComboPower)).orElse(null);
        if (elegido == null) {
            return 0;
        }
        cpu.getHand().remove(elegido);
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
                defensor.getBattleArea().remove(c);
                appendLog(c.getName() + " es destruida.");
            } else if (poderAtaque < poderDef) {
                appendLog("El ataque es repelido; el atacante no logra destruir a " + c.getName() + ".");
            } else {
                defensor.getBattleArea().remove(c);
                appendLog("Empate de poder: " + c.getName() + " es destruida.");
            }
        }
    }

    /**
     * Muestra un dialogo para que el jugador humano elija cartas de su mano para usar en combo
     * (se queman: se descartan permanentemente) y devuelve la suma de su poder de combo.
     */
    private int preguntarCombo(CardPlayer p, String contexto) {
        if (p.getHand().isEmpty()) {
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
            p.getHand().remove(c);
        }
        return total;
    }

    /**
     * IA simple de combo defensivo para la CPU: si el bloqueo por si solo no alcanza, intenta quemar
     * cartas de su mano (empezando por las de mayor poder de combo) hasta cubrir la diferencia.
     * Si no le alcanza con toda su mano, no arriesga cartas y deja que el bloqueo pierda igual.
     */
    private int cpuComboDefensivo(int poderAtaque, int poderDefActual) {
        int faltante = poderAtaque - poderDefActual;
        if (faltante <= 0 || cpu.getHand().isEmpty()) {
            return 0;
        }
        List<GameCard> ordenadas = new ArrayList<>(cpu.getHand());
        ordenadas.sort(Comparator.comparingInt(GameCard::getComboPower).reversed());
        int acumulado = 0;
        List<GameCard> usadas = new ArrayList<>();
        for (GameCard c : ordenadas) {
            if (acumulado >= faltante) {
                break;
            }
            acumulado += c.getComboPower();
            usadas.add(c);
        }
        if (acumulado < faltante) {
            return 0; // no alcanza ni usando toda la mano: no arriesga las cartas
        }
        for (GameCard c : usadas) {
            cpu.getHand().remove(c);
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
            GameCard mejor = cpu.getBattleArea().stream()
                    .filter(c -> !c.isRested())
                    .max(Comparator.comparingInt(c -> c.getEffectivePower(true)))
                    .orElse(null);
            if (mejor == null) {
                return null;
            }
            int totalComboDisponible = cpu.getHand().stream().mapToInt(GameCard::getComboPower).sum();
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

    private void declararDerrota(CardPlayer perdedor) {
        gameOver = true;
        String ganador = perdedor == human ? "CPU" : "Tu";
        appendLog("\n*** " + perdedor.getName() + " ha sido derrotado. Gana " + ganador + "! ***");
        refreshUI();
        JOptionPane.showMessageDialog(this, (ganador.equals("Tu") ? "¡GANASTE!" : "Perdiste. La CPU gana."),
                "Fin de la partida", JOptionPane.INFORMATION_MESSAGE);
    }
}
