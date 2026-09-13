# Diagrama de Arbol: Arbol de Evolucion de cartas

Este diagrama corresponde exactamente a la implementacion en
`dbfw.cardgame.arbol` (paquete `dbfw-game/src/dbfw/cardgame/arbol`).

> Nota: desde el rediseño hibrido Undertale/Slay the Spire, las cartas ya no
> son "personajes" sino acciones de un solo uso, y por eso las familias se
> renombraron a nombres de accion ("Ataque Basico", "Jalar Carta", "Ataque
> Fuerte", "Golpe Doble"). El `basePower` de cada nivel se conserva solo como
> valor de progresion interno (no se muestra como "poder" en la interfaz; el
> daño real que aplica cada carta lo calcula `GameCard.getDanoDirecto()`).

- **`NodoArbol<T>`** — nodo del arbol: guarda un valor (`GameCard`) y una
  `ListaSimple<NodoArbol<T>>` propia con sus hijos directos.
- **`ArbolEvolucion<T>`** — envuelve la raiz (`NodoArbol<T>`) y expone
  `recorrerCompleto(...)`, que recorre el arbol en preorden con el metodo
  recursivo real `recorrerRecursivo(...)`.
- **`ArbolesEvolucion`** — tabla hash (`HashMap<String, ArbolEvolucion<GameCard>>`)
  que indexa **un `ArbolEvolucion` por familia**, buscando por el nombre de su
  nivel basico (ej. `"Ataque Fuerte"`).

Cada familia tiene exactamente **3 niveles** (3 nodos, sin ramificacion):
nivel 1 = raiz (basica, la que ya usa el mazo), nivel 2 = hijo directo de la
raiz (mejorada), nivel 3 = hijo directo del nivel 2 (legendaria). Los nombres
y valores de poder (`getBasePower()`) son literalmente los que registra
`ArbolesEvolucion.registrarLinea(...)` en su bloque `static { ... }`.

```mermaid
graph TD
    subgraph HASH["ArbolesEvolucion (HashMap&lt;String, ArbolEvolucion&lt;GameCard&gt;&gt;)"]
        direction LR
        K1["clave: 'Ataque Basico'"]
        K2["clave: 'Jalar Carta'"]
        K3["clave: 'Ataque Fuerte'"]
        K4["clave: 'Golpe Doble'"]
    end

    K1 --> AB1
    K2 --> JC1
    K3 --> AF1
    K4 --> GD1

    AB1["Ataque Basico<br/>PWR 15000<br/>(raiz, nivel 1)"] --> AB2["Ataque Mejorado<br/>PWR 22000<br/>(nivel 2)"]
    AB2 --> AB3["Ataque Legendario<br/>PWR 30000<br/>(nivel 3)"]

    JC1["Jalar Carta<br/>PWR 5000<br/>(raiz, nivel 1)"] --> JC2["Jalar Carta Mejorada<br/>PWR 9000<br/>(nivel 2)"]
    JC2 --> JC3["Jalar Carta Legendaria<br/>PWR 14000<br/>(nivel 3)"]

    AF1["Ataque Fuerte<br/>PWR 20000<br/>(raiz, nivel 1)"] --> AF2["Ataque Fuerte Mejorado<br/>PWR 27000<br/>(nivel 2)"]
    AF2 --> AF3["Ataque Fuerte Legendario<br/>PWR 34000<br/>(nivel 3)"]

    GD1["Golpe Doble<br/>PWR 35000<br/>(raiz, nivel 1)"] --> GD2["Golpe Doble Mejorado<br/>PWR 42000<br/>(nivel 2)"]
    GD2 --> GD3["Golpe Doble Legendario<br/>PWR 50000<br/>(nivel 3)"]
```

## Como leer el diagrama junto con el codigo

| En el diagrama | En el codigo |
|---|---|
| Caja `HASH` | `ArbolesEvolucion.ARBOLES` (`Map<String, ArbolEvolucion<GameCard>>`) |
| Flecha `K3 --> AF1` | `ArbolesEvolucion.buscar("Ataque Fuerte")` devuelve el `ArbolEvolucion` cuya raiz es `AF1` |
| Nodo `AB1` | `ArbolEvolucion.getRaiz()` → `NodoArbol<GameCard>` con `valor = new GameCard("Ataque Basico", 15000, 0, CardType.BASIC)` |
| Flecha `AB1 --> AB2` | `raiz.agregarHijo(...)` guardo a `AB2` dentro de la `ListaSimple<NodoArbol<T>>` de hijos de `AB1` |
| Flecha `AB2 --> AB3` | `mejorada.agregarHijo(...)` guardo a `AB3` dentro de los hijos de `AB2` |

## Recorrido recursivo (metodo `recorrerRecursivo`)

`ArbolEvolucion.recorrerCompleto(...)` llama a `recorrerRecursivo(raiz, 0, ...)`,
que visita el nodo actual y luego se llama a si mismo una vez por cada hijo
(caso base: un nodo sin hijos, como los de nivel 3, donde el `for` de hijos no
itera y esa rama de la recursion termina). Para la familia "Ataque Fuerte" el
recorrido impreso en el dialogo "Ver Arbol de Evolucion" es:

```
Ataque Fuerte (PWR 20000)
  |- Ataque Fuerte Mejorado (PWR 27000)
    |- Ataque Fuerte Legendario (PWR 34000)
```
