import java.awt.Color;

/**
 * Representa un personaje "stickman" (figura de palitos) en la pelea grafica.
 */
public class Fighter {
    public static final int MAX_HP = 100;

    private final String name;
    private final Color color;
    private final int baseX; // posicion horizontal de reposo
    private final int baseY; // linea del suelo
    private final int facing; // 1 = mira a la derecha, -1 = mira a la izquierda

    private int hp = MAX_HP;
    private String pose = "idle"; // idle, punch, kick, block, hit, ko
    private int poseFrame = 0;
    private int poseDuration = 0;
    private int offsetX = 0; // desplazamiento por animacion (embestida)

    public Fighter(String name, Color color, int baseX, int baseY, int facing) {
        this.name = name;
        this.color = color;
        this.baseX = baseX;
        this.baseY = baseY;
        this.facing = facing;
    }

    public String getName() {
        return name;
    }

    public Color getColor() {
        return color;
    }

    public int getBaseX() {
        return baseX;
    }

    public int getBaseY() {
        return baseY;
    }

    public int getFacing() {
        return facing;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = Math.max(0, Math.min(MAX_HP, hp));
    }

    public boolean isKo() {
        return hp <= 0;
    }

    public String getPose() {
        return pose;
    }

    public int getOffsetX() {
        return offsetX;
    }

    /** Inicia una nueva pose con una duracion de animacion en frames. */
    public void startPose(String pose, int durationFrames) {
        if (isKo()) {
            return;
        }
        this.pose = pose;
        this.poseFrame = 0;
        this.poseDuration = durationFrames;
    }

    /** Avanza un frame de animacion; calcula el offset de embestida segun la pose. */
    public void tick() {
        if (isKo()) {
            pose = "ko";
            offsetX = 0;
            return;
        }
        if (poseDuration <= 0) {
            offsetX = 0;
            return;
        }
        poseFrame++;
        double progress = (double) poseFrame / poseDuration; // 0..1
        double lunge;
        if (progress < 0.5) {
            lunge = progress * 2; // 0..1 (ida)
        } else {
            lunge = (1 - progress) * 2; // 1..0 (vuelta)
        }
        int maxOffset = pose.equals("punch") || pose.equals("kick") ? 40 : 10;
        offsetX = (int) (lunge * maxOffset) * facing;

        if (poseFrame >= poseDuration) {
            pose = "idle";
            poseFrame = 0;
            poseDuration = 0;
            offsetX = 0;
        }
    }

    /** Devuelve el frame de contacto (mitad de la animacion) para aplicar dano en ese instante. */
    public boolean isAtImpactFrame() {
        return poseDuration > 0 && poseFrame == poseDuration / 2;
    }
}
