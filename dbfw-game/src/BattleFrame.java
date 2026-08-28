import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.util.Random;

/**
 * Ventana principal: pelea grafica entre dos stickman (jugador vs CPU).
 */
public class BattleFrame extends JFrame {
    private final Fighter player;
    private final Fighter cpu;
    private final BattlePanel panel;
    private final JButton btnPunch = new JButton("Puñetazo");
    private final JButton btnKick = new JButton("Patada");
    private final JButton btnBlock = new JButton("Bloquear");
    private final Random random = new Random();

    private boolean turnoJugador = true;
    private boolean animando = false;
    private boolean jugadorBloqueando = false;

    private final Timer animTimer;

    public BattleFrame() {
        super("Dragon Ball Fusion World - Pelea Stickman");

        int groundY = 300;
        player = new Fighter("Tu", Color.BLUE, 180, groundY, 1);
        cpu = new Fighter("CPU", Color.RED, 520, groundY, -1);

        panel = new BattlePanel(player, cpu);

        setLayout(new BorderLayout());
        add(panel, BorderLayout.CENTER);

        JPanel controls = new JPanel(new FlowLayout());
        controls.add(btnPunch);
        controls.add(btnKick);
        controls.add(btnBlock);
        add(controls, BorderLayout.SOUTH);

        btnPunch.addActionListener(e -> jugadorAtaca("punch"));
        btnKick.addActionListener(e -> jugadorAtaca("kick"));
        btnBlock.addActionListener(e -> jugadorBloquea());

        // Timer de animacion (aprox 30 FPS)
        animTimer = new Timer(33, e -> {
            player.tick();
            cpu.tick();
            checkImpact();
            panel.repaint();
        });
        animTimer.start();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setResizable(false);
        actualizarBotones();
    }

    private void actualizarBotones() {
        boolean habilitado = turnoJugador && !animando && !player.isKo() && !cpu.isKo();
        btnPunch.setEnabled(habilitado);
        btnKick.setEnabled(habilitado);
        btnBlock.setEnabled(habilitado);
    }

    private void jugadorAtaca(String tipo) {
        if (animando || !turnoJugador || player.isKo() || cpu.isKo()) {
            return;
        }
        animando = true;
        jugadorBloqueando = false;
        int duracion = 20;
        player.startPose(tipo, duracion);
        panel.setMessage("Tu atacas con " + (tipo.equals("punch") ? "puñetazo" : "patada") + "!");
        actualizarBotones();

        // Espera a que termine la animacion (duracion * intervalo del timer) y pasa el turno.
        Timer fin = new Timer(duracion * 33 + 50, e -> {
            animando = false;
            turnoJugador = false;
            cpuBloqueando = false; // el bloqueo de la CPU solo cubre este ataque
            actualizarBotones();
            turnoCpu();
        });
        fin.setRepeats(false);
        fin.start();
    }

    private void jugadorBloquea() {
        if (animando || !turnoJugador || player.isKo() || cpu.isKo()) {
            return;
        }
        jugadorBloqueando = true;
        player.startPose("block", 15);
        panel.setMessage("Te preparas para bloquear...");
        animando = true;
        actualizarBotones();

        Timer fin = new Timer(15 * 33 + 50, e -> {
            animando = false;
            turnoJugador = false;
            actualizarBotones();
            turnoCpu();
        });
        fin.setRepeats(false);
        fin.start();
    }

    private void turnoCpu() {
        if (player.isKo() || cpu.isKo()) {
            return;
        }
        Timer espera = new Timer(500, e -> {
            animando = true;
            double r = random.nextDouble();
            int duracion;
            if (r < 0.25) {
                // 25% de las veces la CPU se prepara para bloquear en vez de atacar.
                cpuBloqueando = true;
                duracion = 15;
                cpu.startPose("block", duracion);
                panel.setMessage("CPU se pone en guardia...");
            } else {
                String tipo = r < 0.6 ? "punch" : "kick";
                duracion = 20;
                cpu.startPose(tipo, duracion);
                panel.setMessage("CPU ataca con " + (tipo.equals("punch") ? "puñetazo" : "patada") + "!");
            }

            Timer fin = new Timer(duracion * 33 + 50, ev -> {
                animando = false;
                turnoJugador = true;
                jugadorBloqueando = false;
                actualizarBotones();
                if (!player.isKo() && !cpu.isKo()) {
                    panel.setMessage("Tu turno: elige una accion.");
                }
            });
            fin.setRepeats(false);
            fin.start();
        });
        espera.setRepeats(false);
        espera.start();
    }

    private boolean cpuBloqueando = false;
    private boolean impactoPlayerAplicado = false;
    private boolean impactoCpuAplicado = false;

    /** Revisa en cada frame si algun luchador esta en su frame de impacto para aplicar dano. */
    private void checkImpact() {
        if (player.isAtImpactFrame() && !impactoPlayerAplicado
                && (player.getPose().equals("punch") || player.getPose().equals("kick"))) {
            impactoPlayerAplicado = true;
            aplicarGolpe(player, cpu);
        }
        if (!player.getPose().equals("punch") && !player.getPose().equals("kick")) {
            impactoPlayerAplicado = false;
        }

        if (cpu.isAtImpactFrame() && !impactoCpuAplicado
                && (cpu.getPose().equals("punch") || cpu.getPose().equals("kick"))) {
            impactoCpuAplicado = true;
            aplicarGolpe(cpu, player);
        }
        if (!cpu.getPose().equals("punch") && !cpu.getPose().equals("kick")) {
            impactoCpuAplicado = false;
        }
    }

    private void aplicarGolpe(Fighter atacante, Fighter defensor) {
        boolean bloqueado = (defensor == player && jugadorBloqueando) || (defensor == cpu && cpuBloqueando);
        int danoBase = atacante.getPose().equals("kick") ? 12 + random.nextInt(8) : 6 + random.nextInt(6);
        int dano = bloqueado ? danoBase / 3 : danoBase;
        defensor.setHp(defensor.getHp() - dano);
        if (!bloqueado) {
            defensor.startPose("hit", 12);
        }
        panel.setMessage(bloqueado
                ? defensor.getName() + " bloquea y recibe solo " + dano + " de daño."
                : atacante.getName() + " golpea a " + defensor.getName() + " por " + dano + " de daño!");
        if (defensor.isKo()) {
            panel.setMessage(defensor.getName() + " ha sido derrotado!");
        }
    }
}
