package dbfw.cardgame.audio;

import java.io.File;
import java.io.IOException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * Reproduce la musica de fondo del juego en bucle.
 * Usa {@code javax.sound.sampled} junto con librerias de terceros del proyecto para leer MP3.
 * Tambien permite ajustar volumen y silencio.
 */
public final class MusicPlayer {

    /** Ruta por defecto de la cancion principal. */
    public static final String RUTA_TEMA_POR_DEFECTO = "music/theme.mp3";

    private Clip clip;
    private boolean silenciado = false;
    /** Volumen relativo actual (0.0 = casi silencio, 1.0 = volumen original del archivo). */
    private float volumen = 1.0f;

    /**
     * Intenta reproducir el tema por defecto en bucle.
     * Si falla, solo se queda sin musica.
     *
     * @return true si la musica empezo a sonar
     */
    public boolean reproducirTema() {
        return reproducir(RUTA_TEMA_POR_DEFECTO);
    }

    /**
     * Carga un archivo de audio y lo reproduce en bucle infinito.
     *
     * @param rutaArchivo ruta del archivo
     * @return true si se pudo abrir y reproducir
     */
    public boolean reproducir(String rutaArchivo) {
        detener();
        File archivo = new File(rutaArchivo);
        if (!archivo.exists()) {
            return false;
        }
        try (AudioInputStream flujoOriginal = AudioSystem.getAudioInputStream(archivo)) {
            // Convierte primero a PCM porque Clip necesita un formato de audio ya definido.
            AudioFormat formatoOriginal = flujoOriginal.getFormat();
            AudioFormat formatoPcm = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    formatoOriginal.getSampleRate(),
                    16,
                    formatoOriginal.getChannels(),
                    formatoOriginal.getChannels() * 2,
                    formatoOriginal.getSampleRate(),
                    false);
            try (AudioInputStream flujoPcm = AudioSystem.getAudioInputStream(formatoPcm, flujoOriginal)) {
                clip = AudioSystem.getClip();
                clip.open(flujoPcm);
                aplicarGanancia();
                clip.loop(Clip.LOOP_CONTINUOUSLY);
                return true;
            }
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            clip = null;
            return false;
        }
    }

    /** Detiene y libera la pista actual. */
    public void detener() {
        if (clip != null) {
            clip.stop();
            clip.close();
            clip = null;
        }
    }

    /**
     * Activa o desactiva el silencio sin perder la reproduccion.
     *
     * @return el nuevo estado
     */
    public boolean alternarSilencio() {
        silenciado = !silenciado;
        aplicarGanancia();
        return silenciado;
    }

    public boolean isSilenciado() {
        return silenciado;
    }

    /**
     * Ajusta el volumen relativo de la musica (usado por el dialogo de Configuracion).
     *
     * @param volumen valor entre 0.0 (casi silencio) y 1.0 (volumen original del archivo)
     */
    public void setVolumen(float volumen) {
        this.volumen = Math.max(0f, Math.min(1f, volumen));
        aplicarGanancia();
    }

    /** @return el volumen relativo actual, entre 0.0 y 1.0. */
    public float getVolumen() {
        return volumen;
    }

    /** Recalcula la ganancia real segun el silencio y el volumen. */
    private void aplicarGanancia() {
        if (clip == null || !clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            return;
        }
        FloatControl control = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        if (silenciado || volumen <= 0f) {
            control.setValue(control.getMinimum());
        } else {
            // Ajusta la ganancia entre silencio total y volumen original.
            float minimo = control.getMinimum();
            control.setValue(minimo + (0f - minimo) * volumen);
        }
    }
}
