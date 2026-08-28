import java.util.List;
import java.util.Scanner;

/**
 * Motor principal del juego: controla el flujo de turnos entre el jugador humano y la CPU.
 */
public class Game {
    private final Player human;
    private final Player cpu;
    private final Scanner scanner = new Scanner(System.in);

    public Game() {
        human = new Player("Tu", 3);
        cpu = new Player("CPU", 3);
        human.buildBasicDeck();
        cpu.buildBasicDeck();
        human.setInitialLife(5);
        cpu.setInitialLife(5);
        human.drawInitialHand(5);
        cpu.drawInitialHand(5);
    }

    public void start() {
        System.out.println("=== DRAGON BALL FUSION WORLD (version basica) ===");
        System.out.println("Objetivo: deja al oponente sin cartas de vida.\n");

        int turno = 1;
        Player current = human;
        Player other = cpu;

        while (true) {
            System.out.println("\n----- Turno " + turno + " (" + current.getName() + ") -----");
            boolean deckOut = !current.drawCard();
            if (deckOut) {
                System.out.println(current.getName() + " se quedo sin cartas en el mazo. Pierde!");
                break;
            }
            current.startTurn();

            if (current == human) {
                humanTurn(current, other);
            } else {
                cpuTurn(current, other);
            }

            if (other.isDefeated()) {
                System.out.println("\n*** " + other.getName() + " se quedo sin vida. " + current.getName() + " gana! ***");
                break;
            }

            // swap turns
            Player temp = current;
            current = other;
            other = temp;
            turno++;
        }
        scanner.close();
    }

    // ---------------- TURNO HUMANO ----------------

    private void humanTurn(Player me, Player opponent) {
        printStatus(me, opponent);

        // Fase de invocacion / fusion
        boolean seguir = true;
        while (seguir) {
            System.out.println("\nEnergia disponible: " + me.getEnergyAvailable() + "/" + me.getEnergyMax());
            System.out.println("Mano:");
            for (int i = 0; i < me.getHand().size(); i++) {
                System.out.println("  " + i + ": " + me.getHand().get(i));
            }
            System.out.println("Opciones: [j]ugar carta, [f]usionar dos cartas, [c]ontinuar a ataque");
            String op = scanner.nextLine().trim().toLowerCase();

            if (op.equals("j")) {
                jugarCarta(me);
            } else if (op.equals("f")) {
                fusionar(me);
            } else if (op.equals("c")) {
                seguir = false;
            } else {
                System.out.println("Opcion no valida.");
            }
        }

        // Fase de ataque
        List<Card> battleArea = me.getBattleArea();
        System.out.println("\nArea de batalla:");
        for (int i = 0; i < battleArea.size(); i++) {
            System.out.println("  " + i + ": " + battleArea.get(i));
        }
        System.out.println("Escribe los indices separados por espacio para atacar (o vacio para no atacar):");
        String linea = scanner.nextLine().trim();
        if (!linea.isEmpty()) {
            for (String tok : linea.split("\\s+")) {
                try {
                    int idx = Integer.parseInt(tok);
                    if (idx >= 0 && idx < battleArea.size() && !battleArea.get(idx).isRested()) {
                        atacar(me, opponent, battleArea.get(idx));
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
    }

    private void jugarCarta(Player me) {
        System.out.println("Indice de la carta a jugar:");
        String linea = scanner.nextLine().trim();
        try {
            int idx = Integer.parseInt(linea);
            if (idx < 0 || idx >= me.getHand().size()) {
                System.out.println("Indice invalido.");
                return;
            }
            Card carta = me.getHand().get(idx);
            if (!me.spendEnergy(carta.getCost())) {
                System.out.println("No tienes suficiente energia.");
                return;
            }
            me.getHand().remove(idx);
            me.getBattleArea().add(carta);
            System.out.println("Jugaste: " + carta);
        } catch (NumberFormatException e) {
            System.out.println("Entrada invalida.");
        }
    }

    private void fusionar(Player me) {
        System.out.println("Indices de las dos cartas fusionables (ej: 1 3):");
        String linea = scanner.nextLine().trim();
        String[] partes = linea.split("\\s+");
        if (partes.length != 2) {
            System.out.println("Debes indicar exactamente dos indices.");
            return;
        }
        try {
            int i1 = Integer.parseInt(partes[0]);
            int i2 = Integer.parseInt(partes[1]);
            if (i1 == i2 || i1 < 0 || i2 < 0 || i1 >= me.getHand().size() || i2 >= me.getHand().size()) {
                System.out.println("Indices invalidos.");
                return;
            }
            Card c1 = me.getHand().get(i1);
            Card c2 = me.getHand().get(i2);
            if (!c1.isFusable() || !c2.isFusable()) {
                System.out.println("Ambas cartas deben ser fusionables (marcadas con (F)).");
                return;
            }
            int costoFusion = 1;
            if (!me.spendEnergy(costoFusion)) {
                System.out.println("No tienes suficiente energia para fusionar.");
                return;
            }
            // Remover en orden descendente para no romper indices
            me.getHand().remove(Math.max(i1, i2));
            me.getHand().remove(Math.min(i1, i2));
            Card fusion = new Card("FUSION: " + c1.getName() + " + " + c2.getName(),
                    c1.getPower() + c2.getPower() + 2, 1, false);
            me.getBattleArea().add(fusion);
            System.out.println("Fusion creada! " + fusion);
        } catch (NumberFormatException e) {
            System.out.println("Entrada invalida.");
        }
    }

    private void atacar(Player atacante, Player defensor, Card carta) {
        carta.setRested(true);
        System.out.println(atacante.getName() + " ataca con " + carta.getName() + " (PWR " + carta.getPower() + ")");

        Card bloqueador = elegirBloqueador(defensor, carta);
        if (bloqueador != null) {
            System.out.println(defensor.getName() + " bloquea con " + bloqueador.getName());
            bloqueador.setRested(true);
            if (carta.getPower() > bloqueador.getPower()) {
                defensor.getBattleArea().remove(bloqueador);
                System.out.println(bloqueador.getName() + " es destruido.");
            } else if (carta.getPower() < bloqueador.getPower()) {
                atacante.getBattleArea().remove(carta);
                System.out.println(carta.getName() + " es destruido.");
            } else {
                defensor.getBattleArea().remove(bloqueador);
                atacante.getBattleArea().remove(carta);
                System.out.println("Ambas cartas son destruidas.");
            }
        } else {
            defensor.takeDamage();
        }
    }

    /** Logica simple: la CPU bloquea si tiene una carta sin girar con poder suficiente; el humano decide manualmente via consola aqui simplificado a auto-no-bloqueo si no es su turno de decidir. */
    private Card elegirBloqueador(Player defensor, Card atacante) {
        if (defensor == cpu) {
            // IA simple: bloquea con la primera carta disponible que pueda ganar o empatar.
            for (Card c : defensor.getBattleArea()) {
                if (!c.isRested() && c.getPower() >= atacante.getPower()) {
                    return c;
                }
            }
            return null;
        } else {
            // Turno del humano defendiendo un ataque de la CPU
            List<Card> disponibles = defensor.getBattleArea();
            boolean hayDisponible = disponibles.stream().anyMatch(c -> !c.isRested());
            if (!hayDisponible) {
                return null;
            }
            System.out.println("\nLa CPU te ataca con " + atacante.getName() + " (PWR " + atacante.getPower() + ")");
            System.out.println("Tu area de batalla:");
            for (int i = 0; i < disponibles.size(); i++) {
                System.out.println("  " + i + ": " + disponibles.get(i));
            }
            System.out.println("Indice de carta para bloquear (o vacio para recibir el ataque):");
            String linea = scanner.nextLine().trim();
            if (linea.isEmpty()) {
                return null;
            }
            try {
                int idx = Integer.parseInt(linea);
                if (idx >= 0 && idx < disponibles.size() && !disponibles.get(idx).isRested()) {
                    return disponibles.get(idx);
                }
            } catch (NumberFormatException ignored) {
            }
            return null;
        }
    }

    // ---------------- TURNO CPU ----------------

    private void cpuTurn(Player me, Player opponent) {
        // Juega cartas mientras tenga energia y cartas en mano.
        boolean jugoAlgo = true;
        while (jugoAlgo) {
            jugoAlgo = false;
            for (int i = 0; i < me.getHand().size(); i++) {
                Card carta = me.getHand().get(i);
                if (carta.getCost() <= me.getEnergyAvailable()) {
                    me.spendEnergy(carta.getCost());
                    me.getHand().remove(i);
                    me.getBattleArea().add(carta);
                    System.out.println("CPU juega: " + carta);
                    jugoAlgo = true;
                    break;
                }
            }
        }

        // Ataca con todas las cartas disponibles
        for (Card carta : me.getBattleArea()) {
            if (!carta.isRested()) {
                atacar(me, opponent, carta);
                if (opponent.isDefeated()) {
                    return;
                }
            }
        }
    }

    private void printStatus(Player me, Player opponent) {
        System.out.println(me.getName() + " - Vida: " + me.getLife().size() + " | " +
                opponent.getName() + " - Vida: " + opponent.getLife().size());
        System.out.println(opponent.getName() + " area de batalla:");
        for (Card c : opponent.getBattleArea()) {
            System.out.println("  - " + c);
        }
    }
}
