# Diagrama de Arbol: Arbol de Evolucion de cartas

Este diagrama corresponde exactamente a la implementacion en
`dbfw.cardgame.arbol` (paquete `dbfw-game/src/dbfw/cardgame/arbol`):

- **`NodoArbol<T>`** — nodo del arbol: guarda un valor (`GameCard`) y una
  `ListaSimple<NodoArbol<T>>` propia con sus hijos directos.
- **`ArbolEvolucion<T>`** — envuelve la raiz (`NodoArbol<T>`) y expone
  `recorrerCompleto(...)`, que recorre el arbol en preorden con el metodo
  recursivo real `recorrerRecursivo(...)`.
- **`ArbolesEvolucion`** — tabla hash (`HashMap<String, ArbolEvolucion<GameCard>>`)
  que indexa **un `ArbolEvolucion` por familia**, buscando por el nombre de su
  nivel basico (ej. `"Guardian"`).

Cada familia tiene exactamente **3 niveles** (3 nodos, sin ramificacion):
nivel 1 = raiz (basica, la que ya usa el mazo), nivel 2 = hijo directo de la
raiz (mejorada), nivel 3 = hijo directo del nivel 2 (legendaria). Los nombres
y valores de poder (`getBasePower()`) son literalmente los que registra
`ArbolesEvolucion.registrarLinea(...)` en su bloque `static { ... }`.

```mermaid
graph TD
    subgraph HASH["ArbolesEvolucion (HashMap&lt;String, ArbolEvolucion&lt;GameCard&gt;&gt;)"]
        direction LR
        K1["clave: 'Guerrero Basico'"]
        K2["clave: 'Explorador'"]
        K3["clave: 'Guardian'"]
        K4["clave: 'Golpeador Doble'"]
    end

    K1 --> GB1
    K2 --> EX1
    K3 --> GD1
    K4 --> GP1

    GB1["Guerrero Basico<br/>PWR 15000<br/>(raiz, nivel 1)"] --> GB2["Guerrero Mejorado<br/>PWR 22000<br/>(nivel 2)"]
    GB2 --> GB3["Guerrero Legendario<br/>PWR 30000<br/>(nivel 3)"]

    EX1["Explorador<br/>PWR 5000<br/>(raiz, nivel 1)"] --> EX2["Explorador Mejorado<br/>PWR 9000<br/>(nivel 2)"]
    EX2 --> EX3["Explorador Legendario<br/>PWR 14000<br/>(nivel 3)"]

    GD1["Guardian<br/>PWR 20000<br/>(raiz, nivel 1)"] --> GD2["Guardian Mejorado<br/>PWR 27000<br/>(nivel 2)"]
    GD2 --> GD3["Guardian Legendario<br/>PWR 34000<br/>(nivel 3)"]

    GP1["Golpeador Doble<br/>PWR 35000<br/>(raiz, nivel 1)"] --> GP2["Golpeador Mejorado<br/>PWR 42000<br/>(nivel 2)"]
    GP2 --> GP3["Golpeador Legendario<br/>PWR 50000<br/>(nivel 3)"]
```

## Como leer el diagrama junto con el codigo

| En el diagrama | En el codigo |
|---|---|
| Caja `HASH` | `ArbolesEvolucion.ARBOLES` (`Map<String, ArbolEvolucion<GameCard>>`) |
| Flecha `K3 --> GD1` | `ArbolesEvolucion.buscar("Guardian")` devuelve el `ArbolEvolucion` cuya raiz es `GD1` |
| Nodo `GB1` | `ArbolEvolucion.getRaiz()` → `NodoArbol<GameCard>` con `valor = new GameCard("Guerrero Basico", 15000, 0, CardType.BASIC)` |
| Flecha `GB1 --> GB2` | `raiz.agregarHijo(...)` guardo a `GB2` dentro de la `ListaSimple<NodoArbol<T>>` de hijos de `GB1` |
| Flecha `GB2 --> GB3` | `mejorada.agregarHijo(...)` guardo a `GB3` dentro de los hijos de `GB2` |

## Recorrido recursivo (metodo `recorrerRecursivo`)

`ArbolEvolucion.recorrerCompleto(...)` llama a `recorrerRecursivo(raiz, 0, ...)`,
que visita el nodo actual y luego se llama a si mismo una vez por cada hijo
(caso base: un nodo sin hijos, como los de nivel 3, donde el `for` de hijos no
itera y esa rama de la recursion termina). Para la familia "Guardian" el
recorrido impreso en el dialogo "Ver Arbol de Evolucion" es:

```
Guardian (PWR 20000)
  |- Guardian Mejorado (PWR 27000)
    |- Guardian Legendario (PWR 34000)
```
