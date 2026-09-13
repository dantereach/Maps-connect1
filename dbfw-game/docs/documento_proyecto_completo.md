# Instrucción para ChatGPT

Toma todo el contenido de este documento (desde el título "Reporte Técnico
del Proyecto" en adelante) y conviértelo en un archivo de Word (.docx) con
formato de reporte técnico académico: portada con título y fecha, tabla de
contenidos automática, los encabezados `#`/`##`/`###` como Título 1/2/3, las
tablas Markdown como tablas reales de Word, y los bloques de código como
fuente monoespaciada. Conserva el orden y el contenido tal cual, sin resumir
ni omitir secciones.

---

# Reporte Técnico del Proyecto
## Juego de cartas "Tecmilenio Heroes" — Híbrido Undertale / Slay the Spire
### Inspirado en Dragon Ball Fusion World — Implementado en Java (Swing)

---

## 1. Resumen ejecutivo

Este proyecto es un videojuego de escritorio hecho en Java (Swing), desarrollado de
forma incremental a lo largo de varias sesiones de trabajo. Nació como una
recreación básica del juego de cartas coleccionables *Dragon Ball Fusion
World* y evolucionó hasta convertirse en un híbrido de dos géneros muy
distintos:

- **Turno del jugador** → estilo **Slay the Spire**: las cartas de la mano
  ya no son "personajes", sino **acciones** de un solo uso (atacar, robar
  una carta, golpe doble, etc.) organizadas en un menú de dos categorías
  ("Ataque" / "Item"), igual que los menús de combate de Undertale.
- **Turno de la CPU** → estilo **Undertale**: el ataque del líder enemigo se
  resuelve como una mini-fase de esquive en tiempo real, en la que el
  jugador mueve un corazón con las flechas del teclado para esquivar
  distintos patrones de proyectiles lanzados por un jefe animado (inspirado
  en la pelea de Undyne the Undying).

Además de la mecánica del juego, el proyecto cumple con una serie de
requisitos académicos de la materia de Estructuras de Datos: reemplaza las
colecciones estándar de Java (`ArrayList`, `List`, `Collections`,
`PriorityQueue`) por **cinco estructuras de datos propias construidas desde
cero con nodos enlazados** (Pila, Cola, Lista Simple, Lista Doble, Lista
Circular), una **Cola de Prioridad propia**, un **Árbol propio** con
recorrido **recursivo real**, uso justificado de **HashMap** para índices, y
**excepciones propias** para el manejo de errores.

---

## 2. Evolución del proyecto (línea de tiempo)

| Fase | Qué se hizo |
|---|---|
| 1 | Juego de consola simple (texto) inspirado en Dragon Ball Fusion World: líder, mazo, energía, mecánica de fusión. |
| 2 | Primera interfaz gráfica (Swing): dos "stickmen" peleando por turnos (Puñetazo/Patada/Bloquear). |
| 3 | Rediseño completo a **juego de cartas** con reglas fieles al TCG real: líder con vida y transformación, bono de ataque por tamaño de mano, cartas básicas/de robo/de bloqueo/de golpe doble. |
| 4 | Robo de carta al atacar (líder), arte propio por carta (dibujos tipo *stickman*), sistema de **combo** (quemar cartas de la mano para sumar poder, ofensiva o defensivamente). |
| 5 | Regla de resistencia por poder: un ataque no bloqueado solo hace daño si su poder es igual o mayor al del líder defensor. |
| 6 | Combo también usable con cartas de la mano al atacar y al defender. |
| 7 | **Reorganización en paquetes Java** + Javadoc explicando cada clase y método. |
| 8 | **Respuesta a retroalimentación del profesor**: reemplazo de `ArrayList`/`List`/`Collections` por 5 estructuras propias (Pila, Cola, Lista Simple, Lista Doble, Lista Circular) + 3 excepciones propias. |
| 9 | Vista panorámica en perspectiva del tablero (inspirada en la vista 3D del juego oficial de Fusion World). |
| 10 | **Segunda ronda de requisitos**: Cola de Prioridad propia, catálogo de cartas indexado con `HashMap`, Árbol de evolución propio (básica → mejorada → legendaria) con recorrido recursivo real, y diagrama de árbol que corresponde exactamente al código. |
| 11 | Música de fondo (tema del juego) con reproducción en bucle. |
| 12 | **Rediseño de gameplay a híbrido Undertale / Slay the Spire**: las cartas dejan de ser "personajes" y pasan a ser acciones; el turno de la CPU se convierte en una fase de esquive en tiempo real con el corazón, al estilo Undertale. |
| 13 | Animaciones del jefe estilo *Undyne the Undying*, selector de dificultad (Fácil/Normal/Difícil) antes de iniciar la partida. |
| 14 | Menú de mano dividido en **Ataque** / **Item**, más patrones de ataque y animaciones del jefe, indicador visual de qué carriles van a ser golpeados por lanzas, botón de **Configuración** (volumen/silencio), y reskin visual completo a negro/verde estilo Undertale. |

---

## 3. Descripción del juego actual

### 3.1 Objetivo y estructura general

Dos líderes (jugador humano vs. CPU) se enfrentan por turnos. Cada uno tiene
**vida**, **energía** (que se regenera y aumenta su máximo cada turno) y un
**mazo** de 24 cartas del que roban una mano inicial. Gana quien reduce a 0
la vida del líder rival.

### 3.2 Turno del jugador (estilo Slay the Spire)

- Las cartas de la mano son **acciones instantáneas**, agrupadas en dos
  categorías seleccionables desde un menú superior:
  - **Ataque**: cartas que hacen daño directo al rival.
  - **Item**: cartas de utilidad, como "Jalar Carta" (robar del mazo).
- Cada carta tiene un **costo de energía**, un **daño directo** (si aplica)
  y un **poder de combo** (los efectos más simples dan más poder de combo,
  los más fuertes dan menos, ya que se "premia" quemar la carta más simple).
- El jugador puede **quemar cartas de su mano en combo**, tanto para
  reforzar un ataque propio como para reforzar la resistencia del líder al
  defender.
- Un ataque no bloqueado solo hace daño si su poder es **igual o mayor** al
  poder actual del líder defensor (si no, el líder "resiste" sin recibir
  daño).
- El líder roba una carta automáticamente cada vez que ataca.
- Cuando la vida del líder cae a 4 o menos, se **transforma de forma
  permanente**: su poder sube (de 15000 a 20000) y cambia su apariencia.
- El líder también puede **potenciar** una vez por turno (pagando energía)
  para sumar daño extra a su próximo ataque.

### 3.3 Turno de la CPU (estilo Undertale)

- La CPU juega sus cartas con una IA simple (elige qué jugar y cuándo usar
  combo, según la dificultad elegida).
- El ataque del líder CPU se dramatiza como una **fase de esquive en tiempo
  real**: se abre una ventana con un corazón controlado por el jugador
  (flechas del teclado) que debe evitar los proyectiles («balas») dentro de
  una arena.
- El jefe (líder CPU) está animado con distintas **poses según el ataque que
  está preparando**, inspirado en la pelea de *Undyne the Undying*: postura
  de reposo con patrulla lateral, apuntar con una lanza, invocar una ola,
  levantar ambos brazos para una ráfaga circular, o extender los dos brazos
  para un ataque en cruz.
- **Cinco patrones de ataque** rotan de forma aleatoria entre fases:
  1. **Lluvia** — proyectiles cayendo de forma continua.
  2. **Lanzas** — pared de lanzas telegrafiada por un lado, con un hueco
     para esquivar.
  3. **Ondas** — proyectiles con movimiento ondulado.
  4. **Cruz** — pared de lanzas simultánea por los 4 lados (patrón más
     difícil, con un único hueco central compartido).
  5. **Espiral** — ráfaga circular de proyectiles que se expande desde el
     jefe.
- Durante el telegrafiado de una pared de lanzas se dibuja un **indicador
  por carril** (franjas rojas donde va a caer una lanza, verdes donde está
  el hueco seguro) para que el jugador sepa de antemano hacia dónde
  moverse.
- Cada golpe recibido quita una vida de la fase de esquive (o consume un
  escudo si el jugador preparó defensa por combo).

### 3.4 Selector de dificultad

Antes de iniciar la partida se muestra un diálogo para elegir **Fácil**,
**Normal** o **Difícil**. La dificultad afecta:
1. La composición del mazo de la CPU (más copias de cartas fuertes en
   dificultades altas).
2. Qué tan seguido la CPU quema cartas en combo ofensivo.
3. La duración, frecuencia y velocidad de los proyectiles en la fase de
   esquive.

### 3.5 Interfaz y estética visual

- Tablero con **vista panorámica en perspectiva** (piso con líneas que
  convergen hacia un horizonte), inspirado en la cámara del juego oficial de
  Fusion World.
- Reskin visual completo a **paleta negro y verde estilo Undertale**,
  aplicado de forma consistente a botones, etiquetas, tablero y fase de
  esquive mediante una clase de tema compartida.
- Menú de mano dividido en dos botones (**Ataque** / **Item**) que resaltan
  la categoría activa y filtran qué cartas se muestran.
- Botón de **Configuración**: permite ajustar el volumen de la música,
  silenciarla, y consultar la dificultad activa, sin interrumpir la
  partida.
- Música de fondo en bucle (MP3), con soporte de volumen y silencio.

---

## 4. Estructuras de datos propias (requisito académico)

Uno de los objetivos centrales del proyecto fue **reemplazar las
colecciones estándar de Java por estructuras propias construidas con
nodos enlazados**, ya que el ejercicio evaluado es *cómo* se construyen
(complejidad algorítmica, manejo de referencias), no solo que el resultado
final se vea igual.

| Estructura propia | Dónde se usa en el juego | Por qué esa estructura |
|---|---|---|
| **Pila** (`Pila<T>`, LIFO, nodos enlazados) | El **mazo** de cada jugador: se apila ya barajado y cada robo saca la carta de la cima. | Todas las operaciones son O(1) porque solo se manipula el puntero a la cima, sin recorrer ni desplazar el resto — a diferencia de un `ArrayList`, donde quitar del inicio del arreglo obliga a correr todos los elementos restantes (O(n)). |
| **Cola** (`Cola<T>`, FIFO, nodos enlazados con cabeza y cola) | La **mano** de cada jugador: las cartas robadas entran por el final; incluye una extensión `remover(T)` para poder jugar o quemar en combo cualquier carta de la mano, no solo la primera. | Refleja el orden natural de robo, y la extensión está documentada como una ampliación práctica, no un cambio de la estructura base. |
| **Lista Simple** (`ListaSimple<T>`, enlazada, un solo sentido) | El **área de batalla** de cada jugador: las cartas jugadas se agregan al final y se quitan recorriendo nodo por nodo cuando corresponde. | Caso de uso directo de inserción al final y eliminación por valor. |
| **Lista Doble** (`ListaDoble<T>`, nodos con enlace anterior y siguiente) | El **historial de jugadas** de la partida: cada evento relevante (jugar carta, atacar, bloquear, usar combo, etc.) se agrega al final; un **cursor interno** permite navegarlo hacia atrás y hacia adelante desde un diálogo dedicado ("Ver Historial"). | Es una lista genuinamente navegable en ambos sentidos, no solo un registro de texto. |
| **Lista Circular** (`ListaCircular<T>`, el último nodo enlaza de vuelta al primero) | El **ciclo de turnos** entre el jugador humano y la CPU: en vez de alternar dos variables manualmente, se arma un anillo con ambos jugadores que rota cada vez que termina un turno. | Modela de forma natural un ciclo que se repite indefinidamente sin extremos. |
| **Cola de Prioridad** (`ColaPrioridad<T>`, lista de nodos ordenada por prioridad de inserción) | Los **efectos pendientes de las cartas**: cuando se juegan varias cartas en una fase, sus efectos se resuelven según su prioridad (por ejemplo, Golpe Doble antes que un ataque básico), sin importar el orden en que se jugaron. | Implementada por inserción ordenada (no con `java.util.PriorityQueue`), manteniendo el mismo enfoque de nodos propios que el resto de las estructuras. |

Todas estas estructuras están documentadas con Javadoc explicando su
propósito, su complejidad y, en los casos relevantes, la comparación
explícita con la colección estándar equivalente de Java.

### 4.1 Tabla hash (HashMap)

Se usa `java.util.HashMap` de forma justificada (no como reemplazo de las
estructuras propias) en dos lugares:
- **`CatalogoCartas`**: indexa las plantillas de cada familia de carta por
  nombre, permitiendo buscarlas al instante (O(1)) en vez de recorrer una
  lista.
- **`ArbolesEvolucion`**: indexa un árbol de evolución completo por familia
  de carta, también con búsqueda O(1) por nombre.

### 4.2 Árbol propio y recursividad

- **`NodoArbol<T>`**: nodo genérico de árbol que guarda un valor y sus
  hijos directos en una `ListaSimple<NodoArbol<T>>` propia (reutilizando la
  estructura ya construida).
- **`ArbolEvolucion<T>`**: envuelve la raíz del árbol y expone
  `recorrerCompleto(...)`, que llama a un método **recursivo real**
  (`recorrerRecursivo`): visita el nodo actual y se llama a sí mismo una vez
  por cada hijo, hasta llegar a un nodo sin hijos (caso base). Este es el
  método recursivo exigido por el requisito de la materia.
- **`ArbolesEvolucion`**: registra, para cada una de las 4 familias de
  carta, una línea de evolución de **3 niveles** (básica → mejorada →
  legendaria), donde el poder aumenta en cada nivel. Se puede consultar
  desde el botón "Ver Árbol de Evolución" de la interfaz, que muestra el
  recorrido recursivo completo en texto.
- Existe además un **diagrama de árbol** (`docs/diagrama_arbol_evolucion.md`)
  que corresponde exactamente a esta implementación: mismos nombres,
  mismos niveles y mismos valores de poder que en el código.

### 4.3 Manejo de errores con excepciones propias

Se crearon excepciones propias (checked exceptions) para los casos límite
de cada estructura, en vez de devolver valores especiales o `null`:

| Excepción | Se lanza cuando... |
|---|---|
| `PilaVaciaException` | Se intenta desapilar o ver la cima de una Pila vacía. |
| `ColaVaciaException` | Se intenta desencolar o ver el frente de una Cola vacía. |
| `ColaPrioridadVaciaException` | Se intenta desencolar de una Cola de Prioridad vacía. |
| `MazoVacioException` | Un jugador intenta robar una carta y su mazo (Pila) ya está vacío; esta excepción de dominio envuelve a `PilaVaciaException` como causa y, en la interfaz, provoca que ese jugador pierda la partida por no poder robar. |

---

## 5. Arquitectura del proyecto (paquetes)

```
dbfw-game/src/dbfw/
├── Main.java                          (punto de entrada; selector de dificultad)
└── cardgame/
    ├── CardBattleFrame.java           (ventana principal; toda la logica de turnos y combate)
    ├── CardPlayer.java                (estado de un jugador: mazo, mano, area de batalla, vida, energia)
    ├── GameCard.java                  (carta/accion: poder, costo, tipo, combo, prioridad de efecto)
    ├── LeaderCard.java                (lider: vida, transformacion, potenciar)
    ├── CardType.java                  (tipos de accion: BASIC, DRAW, GUARD, DOUBLE_STRIKE)
    ├── CardArt.java                   (arte tipo "stickman" generado por codigo para cada carta/lider)
    ├── CatalogoCartas.java            (HashMap: catalogo de plantillas de carta por nombre)
    ├── Dificultad.java                (Facil/Normal/Dificil y sus efectos en el juego)
    ├── BoardPanel.java                (tablero panoramico en perspectiva, tema negro/verde)
    ├── TemaUndertale.java             (paleta de colores y helpers de estilo compartidos)
    ├── arbol/
    │   ├── NodoArbol.java             (nodo generico de arbol)
    │   ├── ArbolEvolucion.java        (arbol + recorrido recursivo)
    │   └── ArbolesEvolucion.java      (HashMap de arboles de evolucion por familia)
    ├── audio/
    │   └── MusicPlayer.java           (reproduccion de musica de fondo, volumen, silencio)
    ├── estructuras/
    │   ├── Pila.java                  (mazo)
    │   ├── Cola.java                  (mano)
    │   ├── ListaSimple.java           (area de batalla)
    │   ├── ListaDoble.java            (historial de jugadas)
    │   ├── ListaCircular.java         (ciclo de turnos)
    │   └── ColaPrioridad.java         (efectos pendientes por prioridad)
    ├── excepciones/
    │   ├── PilaVaciaException.java
    │   ├── ColaVaciaException.java
    │   ├── ColaPrioridadVaciaException.java
    │   └── MazoVacioException.java
    └── undertale/
        ├── PanelEsquive.java          (mini-juego de esquive; jefe animado; patrones de ataque)
        └── Bala.java                  (proyectiles: normales, lanzas, ondas)
```

Todas las clases cuentan con **Javadoc** a nivel de clase y de método,
explicando su propósito, sus parámetros y, en las estructuras de datos, su
complejidad algorítmica y la justificación de diseño.

---

## 6. Inteligencia artificial de la CPU

La CPU tiene una lógica simple pero completa:
- Decide qué cartas jugar de su mano según la energía disponible.
- Decide cuándo quemar una carta en combo ofensivo (con una probabilidad
  que aumenta según la dificultad elegida).
- Al defender, siempre prioriza bloquear con su líder (riesgo cero) antes
  de arriesgar una carta, y solo compromete cartas de combo si puede cubrir
  por completo el déficit de poder necesario para resistir.
- La composición de su mazo (cuántas copias de cada tipo de carta) cambia
  según la dificultad, favoreciendo cartas más fuertes en dificultades
  altas.

---

## 7. Pruebas y validación realizadas

Durante el desarrollo, cada conjunto de cambios se validó con el siguiente
proceso antes de darse por terminado:

1. **Compilación completa** del proyecto con `javac`, incluyendo las
   librerías de terceros necesarias para la reproducción de MP3
   (`mp3spi`, `jlayer`, `tritonus-share`).
2. **Ejecución del juego** (`java dbfw.Main`) para confirmar que no arroja
   excepciones al iniciar ni al abrir los distintos diálogos (Configuración,
   Historial, Árbol de Evolución, selector de dificultad).
3. **Revisión manual de la lógica de combate** después de cada cambio de
   reglas (por ejemplo, verificar que un ataque de menor poder no cause
   daño, que el combo sume correctamente, que el líder se transforme
   exactamente al llegar a 4 de vida o menos).
4. **Limpieza del directorio de compilación** (`out/`) antes de cada commit,
   para no versionar artefactos generados.

---

## 8. Documentación adicional entregada

- **Diagrama de flujo** del funcionamiento general del juego (hecho a mano
  por el estudiante, entregado por separado).
- **Diagrama de árbol** (`docs/diagrama_arbol_evolucion.md`): diagrama en
  formato Mermaid que corresponde exactamente a la implementación de
  `dbfw.cardgame.arbol` (mismos nombres de clase, mismos niveles y mismos
  valores de poder que aparecen en el código fuente).

---

## 9. Conclusiones

El proyecto partió de una recreación muy básica de un juego de cartas y
terminó como una aplicación de escritorio completa, con:

- Un sistema de combate por turnos con reglas propias (transformación de
  líder, resistencia por poder, combos ofensivos y defensivos).
- Un híbrido de géneros (Slay the Spire + Undertale) con una fase de
  esquive en tiempo real, animaciones de jefe y múltiples patrones de
  ataque.
- Las **cinco estructuras de datos propias** exigidas por la materia (Pila,
  Cola, Lista Simple, Lista Doble, Lista Circular), más una **Cola de
  Prioridad** y un **Árbol** propios, con un método **recursivo real**, uso
  justificado de **HashMap**, y **excepciones propias** para el manejo de
  errores — todo ello construido con nodos enlazados en vez de las
  colecciones estándar de Java, cumpliendo tanto el resultado funcional
  como el objetivo pedagógico de la actividad.
- Una interfaz gráfica cuidada, con tema visual propio, música, arte de
  cartas generado por código y una experiencia de usuario clara (menús de
  Ataque/Item, indicadores visuales de peligro, configuración accesible).
