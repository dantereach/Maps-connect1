package dbfw.cardgame.audio;

import java.io.File;
import java.io.IOException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * Reproduce un efecto de sonido una sola vez (sin bucle), independiente de la musica de fondo.
 * Se usa para el sonido de Game Over: no reemplaza al {@link MusicPlayer}, que sigue llevando el tema.
 */
public final class SoundEffectPlayer {
    private SoundEffectPlayer() {
        // Clase utilitaria: no se instancia.
    }

    /**
     * Carga y reproduce un archivo de audio una sola vez.
     * Si el archivo no existe o no se puede abrir, simplemente no suena nada.
     *
     * @param rutaArchivo ruta del archivo de audio
     */
    public static void reproducir(String rutaArchivo) {
        File archivo = new File(rutaArchivo);
        if (!archivo.exists()) {
            return;
        }
        try (AudioInputStream flujoOriginal = AudioSystem.getAudioInputStream(archivo)) {
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
                Clip clip = AudioSystem.getClip();
                clip.open(flujoPcm);
                // Se cierra solo cuando termina de sonar, para no dejar el recurso abierto.
                clip.addLineListener(evento -> {
                    if (evento.getType() == LineEvent.Type.STOP) {
                        clip.close();
                    }
                });
                clip.start();
            }
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            // Sin efecto de sonido si algo falla; no interrumpe el juego.
        }
    }
}
