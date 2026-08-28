# Dragon Ball Fusion World - Version basica en Java

Simulacion muy simplificada y no oficial del TCG "Dragon Ball Fusion World".
Incluye tres modos:

1. **Juego de cartas (por defecto)**: interfaz grafica con lideres, poder en miles y efectos de carta.
2. **Modo stickman**: pelea grafica arcade de figuras de palitos.
3. **Modo texto**: version de consola basada en cartas (version anterior, mas simple).

## Juego de cartas (modo por defecto)

Reglas inspiradas en Dragon Ball Fusion World:

- Cada jugador tiene un **Lider** con vida (empieza en 7) y energia que crece 1 por turno.
- **Lider Azul (jugador)**: poder base 15000.
  - Puede **potenciar** una carta propia una vez por turno (+5000 de poder, cuesta 1 de energia).
  - Si tu mano tiene **7 cartas o menos**, tu lider ataca con **35000** de poder en vez de su poder normal.
  - Cuando tu vida llega a **4 o menos**, el lider se **transforma** (se da la vuelta) y su poder pasa a **20000**.
- **Lider CPU**: generico, poder fijo 15000, sin habilidades especiales.
- Tipos de carta de batalla:
  - **Basica**: 15000 de poder, costo 2.
  - **Explorador (Roba 1)**: 5000 de poder, costo 1; al jugarla robas 1 carta del mazo.
  - **Guardian (Guardia)**: 20000 de poder, pero al **defender** (turno del oponente) sube a **25000**.
  - **Golpeador Doble (Double Strike)**: 35000 de poder, costo 4; si conecta sin ser bloqueada, hace **2 de daño** de vida en vez de 1.
- En combate, si nadie bloquea un ataque, el defensor pierde vida (1, o 2 con Double Strike). Si se bloquea, gana la carta con mas poder (empate = ambas destruidas). Los lideres nunca son destruidos en combate.
- **El lider roba 1 carta del mazo cada vez que ataca** (tanto el tuyo como el de la CPU).
- **Cartas en combo**: al declarar un ataque (con el lider o con una carta) o al defenderte de un ataque, puedes quemar cartas de tu mano para sumar su "poder de combo" al choque. Las cartas usadas en combo se descartan permanentemente. Entre mas fuerte el efecto de la carta, menos poder de combo aporta:
  - Basica: **+10000** de combo (sin efecto, la mejor para combo).
  - Explorador (Roba 1): **+7000**.
  - Guardian: **+5000**.
  - Golpeador Doble: **+3000** (su efecto ya es muy fuerte).
- Cada carta tiene un pequeño arte generado (un stickman con una pose distinta segun su tipo); el lider tiene su propio icono, con un aura dorada cuando esta transformado.
- Gana quien deje al oponente sin vida (o sin cartas para robar).

## Modo stickman

Dos figuras de palitos (tu en azul, la CPU en rojo) pelean por turnos:
- **Puñetazo** / **Patada**: atacan e infligen daño segun un rango aleatorio (la patada pega mas fuerte).
- **Bloquear**: te pones en guardia; si la CPU te ataca en su siguiente turno, el daño se reduce a un tercio.
- La CPU alterna entre atacar y, ocasionalmente (25%), ponerse en guardia.
- Gana quien deje al otro con 0 de vida (barra roja/verde arriba de cada personaje).

## Modo texto (cartas, version simple)


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
java -cp out Main            # juego de cartas (por defecto)
java -cp out Main stickman   # pelea grafica de stickman
java -cp out Main texto      # version de consola con cartas (simple)
```

Este es un proyecto de aficionado con fines educativos/recreativos, no afiliado a Bandai ni a Dragon Ball.
