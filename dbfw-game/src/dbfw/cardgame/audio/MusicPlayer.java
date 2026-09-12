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
 * Reproductor de la musica de fondo del juego.
 * <p>
 * Usa {@code javax.sound.sampled} (incluido en el JDK) apoyado en las librerias de terceros
 * {@code mp3spi}/{@code jlayer}/{@code tritonus-share} (carpeta {@code lib/} del proyecto), que
 * registran un {@code AudioFileReader} capaz de decodificar MP3 a PCM. Gracias a eso esta clase
 * no necesita saber nada de MP3: solo pide un {@link Clip} igual que si fuera un WAV.
 * <p>
 * El archivo de audio en si (por ejemplo la cancion tema del juego) <b>no</b> se distribuye con
 * el codigo fuente por derechos de autor: cada quien coloca su propio archivo en la carpeta
 * {@code music/} (ver {@link #RUTA_TEMA_POR_DEFECTO}), carpeta que esta excluida en
 * {@code .gitignore}. Si el archivo no existe, simplemente no hace
 * sonido y el juego sigue funcionando con normalidad.
 */
public final class MusicPlayer {

    /** Ruta por defecto donde cada jugador coloca localmente la cancion tema del juego. */
    public static final String RUTA_TEMA_POR_DEFECTO = "music/theme.mp3";

    private Clip clip;
    private boolean silenciado = false;

    /**
     * Intenta cargar y reproducir en bucle infinito el tema del juego desde
     * {@link #RUTA_TEMA_POR_DEFECTO}. Si el archivo no existe o no se puede decodificar, no
     * lanza ninguna excepcion hacia la interfaz: solo deja de sonar musica.
     *
     * @return true si la musica empezo a reproducirse, false si no se encontro/pudo cargar
     */
    public boolean reproducirTema() {
        return reproducir(RUTA_TEMA_POR_DEFECTO);
    }

    /**
     * Carga el archivo de audio indicado (MP3 o WAV) y lo reproduce en bucle infinito.
     *
     * @param rutaArchivo ruta (relativa o absoluta) al archivo de musica
     * @return true si se pudo abrir y empezar a reproducir, false en cualquier otro caso
     */
    public boolean reproducir(String rutaArchivo) {
        detener();
        File archivo = new File(rutaArchivo);
        if (!archivo.exists()) {
            return false;
        }
        try (AudioInputStream flujoOriginal = AudioSystem.getAudioInputStream(archivo)) {
            // El MP3 decodificado por mp3spi no trae un formato "totalmente especificado"
            // (tamano de frame NOT_SPECIFIED), y Clip.open lo exige. Por eso se decodifica
            // primero a PCM firmado antes de abrir el Clip, igual que si fuera un WAV.
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
                aplicarSilencio();
                clip.loop(Clip.LOOP_CONTINUOUSLY);
                return true;
            }
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            clip = null;
            return false;
        }
    }

    /** Detiene y libera la pista actual, si habia una sonando. */
    public void detener() {
        if (clip != null) {
            clip.stop();
            clip.close();
            clip = null;
        }
    }

    /**
     * Activa o desactiva el silencio de la musica actual sin perder la posicion de reproduccion.
     *
     * @return el nuevo estado (true = silenciado)
     */
    public boolean alternarSilencio() {
        silenciado = !silenciado;
        aplicarSilencio();
        return silenciado;
    }

    public boolean isSilenciado() {
        return silenciado;
    }

    private void aplicarSilencio() {
        if (clip == null || !clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            return;
        }
        FloatControl control = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        control.setValue(silenciado ? control.getMinimum() : 0f);
    }
}
