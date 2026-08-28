# Dragon Ball Fusion World - Version basica en Java

Simulacion muy simplificada y no oficial del TCG "Dragon Ball Fusion World".
Incluye dos modos:

1. **Modo grafico (por defecto)**: pelea de stickman con interfaz Swing.
2. **Modo texto**: version de consola basada en cartas (Lider, mazo, fusion, etc.).

## Modo grafico (stickman)

Dos figuras de palitos (tu en azul, la CPU en rojo) pelean por turnos:
- **Puñetazo** / **Patada**: atacan e infligen daño segun un rango aleatorio (la patada pega mas fuerte).
- **Bloquear**: te pones en guardia; si la CPU te ataca en su siguiente turno, el daño se reduce a un tercio.
- La CPU alterna entre atacar y, ocasionalmente (25%), ponerse en guardia.
- Gana quien deje al otro con 0 de vida (barra roja/verde arriba de cada personaje).

## Modo texto (cartas)

- Cada jugador tiene 5 cartas de vida y roba 1 carta al inicio de cada turno.
- Cada turno se gana 1 punto de energia extra (maximo 10) que se usa para jugar cartas de la mano (segun su costo).
- Puedes fusionar dos cartas marcadas con `(F)` en la mano por 1 de energia para crear una carta de fusion mas poderosa.
- En la fase de ataque, eliges que cartas sin girar atacan. El rival puede bloquear con una carta sin girar:
  - Si el poder del atacante es mayor, la carta bloqueadora es destruida.
  - Si es menor, la carta atacante es destruida.
  - Si es igual, ambas se destruyen.
  - Si no se bloquea, el defensor pierde una carta de vida.
- Si un jugador se queda sin cartas de vida (o sin cartas en el mazo al robar), pierde la partida.

## Como ejecutar

```powershell
cd dbfw-game
javac -d out src/*.java
java -cp out Main          # abre la interfaz grafica (stickman)
java -cp out Main texto    # abre la version de consola con cartas
```

Este es un proyecto de aficionado con fines educativos/recreativos, no afiliado a Bandai ni a Dragon Ball.
