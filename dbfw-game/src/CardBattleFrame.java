import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
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
                + "<br>" + etiquetaTipo(c.getType()) + "<br>Costo " + c.getCost() + "</center></html>";
        JButton b = new JButton(texto);
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
                resolverAtaque(cpu, human, poder, c.isDoubleStrike(), c.getName());
                if (gameOver) {
                    return;
                }
            }
        }
        refreshUI();
    }

    // ---------------- RESOLUCION DE COMBATE ----------------

    /**
     * Resuelve un ataque de "atacante" contra "defensor" con el poder dado.
     * Si el defensor es la CPU, la bloquea una IA simple; si es el jugador humano, se le pregunta con un dialogo.
     */
    private void resolverAtaque(CardPlayer atacante, CardPlayer defensor, int poderAtaque, boolean doubleStrike, String nombreAtacante) {
        Object bloqueador = elegirBloqueador(defensor, poderAtaque);

        if (bloqueador == null) {
            int dano = doubleStrike ? 2 : 1;
            appendLog(nombreAtacante + " conecta sin bloqueo! " + defensor.getName() + " pierde " + dano + " de vida.");
            boolean derrotado = defensor.takeDamage(dano);
            if (derrotado) {
                declararDerrota(defensor);
            }
            return;
        }

        if (bloqueador instanceof LeaderCard) {
            LeaderCard l = (LeaderCard) bloqueador;
            l.setRested(true);
            int poderDef = l.getDefensePower();
            appendLog(defensor.getName() + " bloquea con su Lider (PWR " + poderDef + ").");
            if (poderAtaque > poderDef) {
                appendLog("El lider de " + defensor.getName() + " resiste el golpe, pero no es destruido (los lideres no mueren en combate).");
            } else {
                appendLog("El ataque es repelido por el Lider de " + defensor.getName() + ".");
            }
        } else {
            GameCard c = (GameCard) bloqueador;
            c.setRested(true);
            int poderDef = c.getEffectivePower(true);
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

    /** Determina que bloquea el defensor: IA simple para la CPU, dialogo para el humano. */
    private Object elegirBloqueador(CardPlayer defensor, int poderAtaque) {
        if (defensor == cpu) {
            for (GameCard c : cpu.getBattleArea()) {
                if (!c.isRested() && c.getEffectivePower(true) >= poderAtaque) {
                    return c;
                }
            }
            if (!cpu.getLeader().isRested() && cpu.getLeader().getDefensePower() >= poderAtaque) {
                return cpu.getLeader();
            }
            return null;
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
