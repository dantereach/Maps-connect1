# Musica de fondo del juego

El juego reproduce automaticamente una cancion de fondo en bucle al abrir la
ventana, usando la clase `dbfw.cardgame.audio.MusicPlayer`. El boton
**"Silenciar Musica"** (abajo, junto a "Ver Arbol de Evolucion") permite
silenciarla/reanudarla sin perder la posicion de reproduccion.

## Por que el archivo de audio no esta en el repositorio

La cancion en si tiene derechos de autor, asi que **no se sube a git**. La
carpeta `dbfw-game/music/` esta en `.gitignore` (salvo un `.gitkeep` para que
la carpeta exista al clonar). Cada quien coloca su propio archivo ahi.

## Como poner tu cancion

1. Copia tu archivo de audio (MP3 o WAV) a:
   ```
   dbfw-game/music/theme.mp3
   ```
   (ese nombre exacto: `MusicPlayer.RUTA_TEMA_POR_DEFECTO`).
2. Si el archivo no existe, el juego simplemente arranca sin musica (el boton
   se deshabilita y dice "Musica no encontrada"); no se rompe nada.

## Soporte de MP3: librerias en `lib/`

`javax.sound.sampled` (lo unico incluido en el JDK) solo entiende WAV/AU/AIFF
de forma nativa. Para reproducir MP3 se usan 3 jars pequeños de terceros que
registran un decodificador adicional (`AudioFileReader`/`FormatConversionProvider`)
para que `AudioSystem` entienda MP3 como si fuera cualquier otro formato:

- `lib/jlayer-1.0.1.4.jar` — decodificador MP3.
- `lib/mp3spi-1.9.5.4.jar` — el "puente" (SPI) entre `javax.sound.sampled` y JLayer.
- `lib/tritonus-share-0.3.7.4.jar` — utilidades que necesita mp3spi.

### Configurar en IntelliJ

1. Clic derecho en el proyecto → **Open Module Settings** (o `F4`).
2. **Libraries** → **+** → **Java** → selecciona los 3 archivos `.jar` dentro
   de `dbfw-game/lib/`.
3. Aplica y ejecuta el juego normalmente (Run ▶ sobre `Main`).

### Compilar/ejecutar por linea de comandos

```powershell
cd dbfw-game
$cp = (Get-ChildItem lib\*.jar | ForEach-Object { $_.FullName }) -join ";"
javac -cp $cp -d out (Get-ChildItem -Recurse -Filter *.java -Path src | % { $_.FullName })
java -cp "out;$cp" dbfw.Main
```
