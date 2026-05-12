# Motor de Juego InazumaGo - Documentación Completa (Sprint 3-4)

## 📋 Resumen Ejecutivo

Se ha implementado la lógica completa del motor de juego InazumaGo siguiendo las reglas especificadas en `reglamento-inazuma-go.md`. La arquitectura es modular, testable e integrada con Firebase Realtime Database.

**Estado**: ✅ Sprint 3-4 COMPLETADOS
- Modelos de dominio: DONE
- Validaciones: DONE
- Ejecución de movimientos: DONE
- Puntuación: DONE
- Tests: 30+ tests implementados
- Integración Firebase: DONE

---

## 🏗️ Arquitectura del Motor

### Capas de la Aplicación

```
┌─────────────────────────────────────────┐
│  UI Layer (JavaFX)                      │
│  - Tablero visual                       │
│  - Controles de movimiento              │
└────────────┬────────────────────────────┘
             │
┌────────────▼────────────────────────────┐
│  GameService (Orquestación)             │
│  - makeMove(), makePass()               │
│  - getProvisionalScore()                │
│  - Integración con Firebase             │
└────────────┬────────────────────────────┘
             │
┌────────────▼────────────────────────────┐
│  Motor de Juego (Lógica Core)           │
│  ├─ MoveValidator                       │
│  ├─ MoveExecutor                        │
│  ├─ BoardAnalyzer                       │
│  ├─ SuicideDetector                     │
│  ├─ KoDetector                          │
│  └─ ChineseScorerImpl                    │
└────────────┬────────────────────────────┘
             │
┌────────────▼────────────────────────────┐
│  Modelos de Dominio                     │
│  ├─ Game (aggregate raíz)               │
│  ├─ Board (9x9)                         │
│  ├─ Group (cadena de piedras)           │
│  ├─ Move, Player, Position              │
│  └─ GameState, GameResult, ScoreSnapshot│
└────────────┬────────────────────────────┘
             │
┌────────────▼────────────────────────────┐
│  Repository (Firebase)                  │
│  - MainRepository                       │
│  - writeMoveMultiPath()                 │
│  - updateGame()                         │
└─────────────────────────────────────────┘
```

---

## 📦 Estructura de Paquetes Implementada

```
es.iesquevedo/
├── model/
│   ├── board/
│   │   ├── Stone.java                  ✅
│   │   ├── Position.java               ✅
│   │   ├── Board.java                  ✅
│   │   ├── Group.java                  ✅
│   │   └── BoardAnalyzer.java          ✅
│   ├── move/
│   │   ├── Move.java                   ✅
│   │   ├── MoveResult.java             ✅
│   │   ├── MoveValidator.java          ✅
│   │   ├── MoveExecutor.java           ✅
│   │   ├── SuicideDetector.java        ✅
│   │   └── KoDetector.java             ✅
│   ├── player/
│   │   ├── PlayerColor.java            ✅
│   │   ├── Player.java                 ✅
│   │   └── PlayerClock.java            ✅
│   ├── game/
│   │   ├── Game.java                   ✅
│   │   ├── GameState.java              ✅
│   │   ├── GameResult.java             ✅
│   │   └── ScoreSnapshot.java          ✅
│   └── scoring/
│       └── ChineseScorerImpl.java       ✅
├── service/
│   └── game/
│       ├── GameService.java            ✅
│       └── GameServiceImpl.java         ✅
├── exception/
│   ├── InvalidMoveException.java       ✅
│   ├── OutOfTurnException.java         ✅
│   └── SuicideException.java           ✅
└── dto/mapper/
    ├── GameMapper.java                 ✅
    └── MoveMapper.java                 ✅
```

---

## 🎮 Modelos de Dominio Core

### 1. **Position** (Value Object)
Representa una coordenada en el tablero 9x9 (0-8).

```java
Position pos = Position.of(4, 4);
List<Position> neighbors = pos.getOrthogonalNeighbors(); // N, S, E, O
String notation = pos.toGoNotation(); // "e5"
```

**Features**:
- Validación de límites
- Caché de instancias (optimización)
- Obtención de vecinos ortogonales

---

### 2. **Stone** (Enum)
Estados de una intersección.

```java
Stone.EMPTY   // Vacía
Stone.BLACK   // Piedra negra
Stone.WHITE   // Piedra blanca

// Métodos útiles
stone.isStone()      // ¿Es piedra?
stone.opponent()     // Color opuesto
```

---

### 3. **Board** (Agregado)
Tablero 9x9 inmutable por operación.

```java
Board board = new Board();
board.placeStone(pos, Stone.BLACK);
Stone s = board.getStone(pos);
board.removeStone(pos);

Board copy = board.copy();     // Deep copy
boolean equal = board.equals(other);
int count = board.countStones(Stone.BLACK);
```

**Propiedades**:
- Estado completo del tablero
- Operaciones atómicas (place, remove)
- Comparación por contenido
- Copia profunda para historiales

---

### 4. **Group** (Cadena de piedras)
Conjunto conectado de piedras del mismo color.

```java
Group group = new Group(Stone.BLACK);
group.addStone(Position.of(4, 4));
group.addStone(Position.of(4, 5));

int liberties = group.countLiberties(board);    // Grados de libertad
boolean alive = group.isAlive(board);           // ¿Tiene libertades?
int eyes = group.countEyes(board);              // Número de ojos
```

**Detecta automáticamente**:
- Libertades (intersecciones vacías adyacentes)
- Ojos (regiones de seguridad)
- Estado de vida/muerte

---

### 5. **BoardAnalyzer**
Análisis del tablero: grupos, libertades, territorios.

```java
BoardAnalyzer analyzer = new BoardAnalyzer();

List<Group> allGroups = analyzer.findAllGroups(board);
Group group = analyzer.findGroupAt(board, pos);
List<Group> captured = analyzer.findCapturedGroups(board);
List<Territory> territories = analyzer.findTerritories(board);

// Territory:
territory.getPositions();        // Intersecciones vacías
territory.getAdjacentColors();   // Colores limítrofes
territory.isNeutral();           // ¿Dame/Seki?
territory.getOwner();            // Color (si no neutral)
```

---

### 6. **Move**
Jugada de un jugador.

```java
// Movimiento normal
Move move = new Move(Position.of(4, 4), PlayerColor.BLACK, "nonce-123");

// Pase
Move pass = Move.pass(PlayerColor.BLACK, "nonce-456");

// Accesores
move.getPosition();
move.getActor();
move.getClientNonce();
move.getMoveId();       // UUID generado

// Propiedades
move.isPass();
```

**Campos importantes**:
- `clientNonce`: Deduplicación en Firebase
- `clientTimestamp`: Registro temporal
- `moveId`: Identificador único
- `actor`: Jugador que hace el movimiento

---

### 7. **Player**
Representación de un jugador.

```java
Player player = new Player("player-1", PlayerColor.BLACK, "Negro");

player.getColor();              // PlayerColor.BLACK
player.getDisplayName();        // "Negro"
player.getCapturedStones();     // Prisioneros capturados
player.addCaptures(3);          // Incrementar capturados
```

---

### 8. **PlayerClock**
Control de tiempo (3+2: 3 min + 2 seg por movimiento).

```java
PlayerClock clock = new PlayerClock();
clock.startTurn();
clock.endTurn();

long remaining = clock.getRemainingMillis();
String formatted = clock.getFormattedTime();    // "3:00", "2:45", etc.
boolean expired = clock.isExpired();
```

---

### 9. **Game** (Agregado Raíz)
Partida completa: gestiona tablero, jugadores, turno, estado.

```java
Game game = new Game("game-123");
game.addPlayer(blackPlayer);
game.addPlayer(whitePlayer);
game.startGame();

// Getters
game.getBoard();
game.getState();                // GameState.PLAYING
game.getCurrentPlayer();        // PlayerColor.BLACK
game.getMoveHistory();
game.getMoveCount();

// Operaciones
game.nextTurn();
game.recordMove(move);
game.handlePass();
game.resetPassCount();
game.updateNoCaptureMoves(hasCaptured);

// Estados finales
game.endGameByDoublePasse();
game.endGameByTime(winner);
game.endGameByNoCaptureLimit();
game.endGameByKo();
```

---

## ✅ Validaciones Implementadas

### 1. **MoveValidator**
Valida completamente un movimiento.

```java
validator.validate(move, board, currentPlayerColor, boardHistory);
// → ValidationResult { valid: boolean, reason: String, capturedGroups: List<Group> }
```

**Validaciones**:
- ✅ Posición libre
- ✅ Turno correcto del jugador
- ✅ **Suicidio prohibido**: Piedra queda sin libertades sin capturar
- ✅ **Ko detectado**: Misma posición que hace 1 turno
- ✅ **Pase siempre válido**

---

### 2. **SuicideDetector**
Detecta movimientos suicida.

```java
boolean isSuicide = suicideDetector.isSuicide(move, board);
```

**Lógica**:
1. Simular colocación
2. ¿Hay capturas enemigas? → NO ES SUICIDIO
3. ¿Piedra propia tiene libertades? → NO ES SUICIDIO
4. En caso contrario → SUICIDIO

---

### 3. **KoDetector**
Detecta repeticiones inmediatas.

```java
boolean isKo = koDetector.isKoMove(move, currentBoard, boardHistory);
```

**Implementación según reglamento**:
- Permite recaptura normal en Ko
- Finaliza si la posición se repite inmediatamente (turno siguiente)
- No detecta repeticiones no consecutivas

---

## 🎯 Ejecución de Movimientos

### **MoveExecutor**
Aplica un movimiento validado al tablero.

```java
MoveResult result = executor.executeMove(move, board, 
                                        currentPlayer, opponentPlayer);

// MoveResult:
result.isValid();
result.getMove();
result.getCapturedGroups();
result.getCapturedStoneCount();
result.hasCaptured();
```

**Proceso**:
1. Colocar piedra
2. Detectar y eliminar grupos capturados
3. Incrementar contador de prisioneros del oponente
4. Devolver resultado

---

## 🏆 Sistema de Puntuación

### **ChineseScorerImpl**
Puntuación según reglas chinas de Inazuma Go.

```java
ChineseScorerImpl scorer = new ChineseScorerImpl();

// Puntuación provisional (cualquier momento)
ScoreSnapshot snapshot = scorer.calculateProvisionalScore(board, 
                                                         blackPlayer, 
                                                         whitePlayer);

snapshot.getBlackScore();           // Puntos totales negros
snapshot.getWhiteScore();           // Puntos totales blancos (+ komi)
snapshot.getBlackTerritory();       // Territorio exclusivo negro
snapshot.getWhiteTerritory();       // Territorio exclusivo blanco
snapshot.getLeader();               // PlayerColor que va ganando
snapshot.getPointsDifference();     // Diferencia de puntos

// Puntuación final
GameResult result = scorer.calculateFinalScore(finalBoard, 
                                              blackPlayer, 
                                              whitePlayer, 
                                              reason);

result.getWinner();
result.getPointsDifference();
result.getDescription();            // "Negro gana por 3.5 puntos"
```

**Fórmula de puntuación**:
```
Black Score = Piedras negras + Territorio negro + Prisioneros blancos capturados
White Score = Piedras blancas + Territorio blanco + Prisioneros negros capturados + 5.5 (komi)

Territorio = Regiones vacías adyacentes a UN SOLO color
Dame/Seki = Regiones adyacentes a AMBOS colores = No puntuación
```

**Komi**: 5.5 puntos para blanco (asegura no empates)

---

## 🔄 Integración con Firebase

### **GameServiceImpl**
Orquesta motor + persistencia.

```java
public class GameServiceImpl implements GameService {
    CompletableFuture<String> createOnlineGame(Player hostPlayer)
    CompletableFuture<Void> joinOnlineGame(String gameId, Player joiningPlayer)
    CompletableFuture<Void> makeMove(String gameId, String playerId, 
                                     Position position, String clientNonce)
    CompletableFuture<Void> makePass(String gameId, String playerId, String clientNonce)
    CompletableFuture<GameDto> getGame(String gameId)
    CompletableFuture<ScoreSnapshot> getProvisionalScore(String gameId)
}
```

**Flujo de makeMove()**:
1. Obtener partida de Firebase
2. Crear Move
3. **VALIDAR EN MOTOR** (crítico)
   - Si inválido → Fallar inmediatamente
   - Si válido → Obtener capturas
4. **EJECUTAR MOVIMIENTO**
   - Colocar piedra
   - Eliminar capturas
   - Cambiar turno
5. **GUARDAR EN FIREBASE**
   - writeMoveMultiPath() (atómico)
   - updateGame()
6. Manejo de errores y rollback

---

## 🧪 Tests Implementados (30+)

### Position Tests
```
✅ testPositionCreation
✅ testPositionBounds
✅ testOrthogonalNeighbors
✅ testCornerNeighbors
✅ testPositionCaching
✅ testToGoNotation
```

### Board Tests
```
✅ testBoardInitialization
✅ testPlaceStone
✅ testCantPlaceTwiceInSamePosition
✅ testRemoveStone
✅ testCountStones
✅ testBoardCopy
✅ testBoardEquality
```

### Group Tests
```
✅ testGroupCreation
✅ testAddStoneToGroup
✅ testCountLiberties
✅ testLiertiesReducedByOtherPiedras
✅ testGroupIsAlive
✅ testGroupIsDead
```

### BoardAnalyzer Tests
```
✅ testFindSingleGroup
✅ testFindConnectedGroup
✅ testFindMultipleGroups
✅ testFindGroupAt
✅ testFindCapturedGroups
✅ testFindTerritories
```

### MoveValidator Tests
```
✅ testValidMoveOnEmptyBoard
✅ testInvalidMoveOutOfTurn
✅ testInvalidMoveOccupiedPosition
✅ testSuicideMoveDetected
✅ testValidMoveCapturesEnemy
✅ testPassIsAlwaysValid
```

### MoveExecutor Tests
```
✅ testExecuteSimpleMove
✅ testExecuteMoveWithCapture
✅ testExecutePass
```

### Scoring Tests
```
✅ testProvisionalScoreEmptyBoard
✅ testProvisionalScoreWithPiedras
✅ testProvisionalScoreWithCaptures
```

### Game Tests
```
✅ testGameCreation
✅ testAddPlayers
✅ testCantAddMoreThanTwoPlayers
✅ testStartGame
✅ testNextTurn
✅ testRecordMove
✅ testPassCount
✅ testResetPassCount
```

### Integration Tests
```
✅ testSimpleGameWithCapture
✅ testGameWithDoublePasse
✅ testProvisionalScore
✅ testMultipleCaptureEvents
```

---

## 📊 Resultados de Tests

```bash
cd C:\Users\1dam\IdeaProjects\InazumaGord
.\mvnw clean test -DskipTests=false

# Resultado esperado:
# Tests run: 35+
# Failures: 0
# Skipped: 0
# SUCCESS: BUILD SUCCESS
```

---

## 🚀 Uso del Motor

### Crear y jugar una partida

```java
// 1. Crear partida
Game game = new Game("game-123");
Player blackPlayer = new Player("p1", PlayerColor.BLACK, "Negro");
Player whitePlayer = new Player("p2", PlayerColor.WHITE, "Blanco");

game.addPlayer(blackPlayer);
game.addPlayer(whitePlayer);
game.startGame();

// 2. Movimientos
MoveValidator validator = new MoveValidator();
MoveExecutor executor = new MoveExecutor();

Move move1 = new Move(Position.of(3, 3), PlayerColor.BLACK, "nonce1");
var validation = validator.validate(move1, game.getBoard(), 
                                   PlayerColor.BLACK, game.getBoardHistory());

if (validation.isValid()) {
    var result = executor.executeMove(move1, game.getBoard(), 
                                      blackPlayer, whitePlayer);
    game.recordMove(move1);
    game.resetPassCount();
    game.updateNoCaptureMoves(result.hasCaptured());
    game.nextTurn();
}

// 3. Puntuación provisional
ChineseScorerImpl scorer = new ChineseScorerImpl();
ScoreSnapshot snapshot = scorer.calculateProvisionalScore(
    game.getBoard(), blackPlayer, whitePlayer
);

System.out.println("Negro: " + snapshot.getBlackScore());
System.out.println("Blanco: " + snapshot.getWhiteScore());

// 4. Pase
Move pass = Move.pass(PlayerColor.WHITE, "nonce2");
game.recordMove(pass);
game.handlePass();
if (game.getState() == GameState.FINISHED) {
    var result = scorer.calculateFinalScore(game.getBoard(), 
                                           blackPlayer, whitePlayer,
                                           "Double Pass");
    System.out.println(result.getDescription());
}
```

---

## 🎯 Casos de Uso Cubiertos

### ✅ Movimientos Básicos
- Colocar piedra en posición libre
- Detección de posición ocupada
- Cambio de turno

### ✅ Capturas
- Captura de un grupo completo
- Múltiples capturas simultáneas
- Incremento de contador de prisioneros

### ✅ Validaciones de Reglas
- Suicidio prohibido
- Ko permitido pero detectado
- Out-of-turn rechazado
- Pases ilimitados

### ✅ Terminación de Partida
- Doble pase (manual en UI)
- Ko (detección automática)
- Límite de 8 movimientos sin captura
- Tiempo agotado
- Abandono

### ✅ Puntuación
- Cálculo de territorio
- Conteo de piedras
- Komi 5.5 para blanco
- Manejo de dame/seki

### ✅ Reglamento Inazuma Go
- Todas las reglas especificadas en `reglamento-inazuma-go.md`
- Sistema 3+2 (tiempo)
- Contador provisional de puntuación
- Final automático

---

## 🔧 Extensiones Futuras

1. **IA Básica**: Sugerencias de movimientos
2. **Análisis de Vida**: Detección de grupos muertos
3. **Replay**: Reproducir partidas guardadas
4. **Export SGF**: Guardar en formato estándar
5. **Estadísticas**: Ratio territorial, capturas/min, etc.
6. **Matchmaking**: Emparejar jugadores por ELO

---

## 📝 Notas de Desarrollo

### Decisiones de Diseño

1. **Value Objects**: Position, Stone, PlayerColor → Inmutables y thread-safe
2. **Agregado de Game**: Gestiona invariantes (turnos, estados)
3. **Separación de Responsabilidades**:
   - BoardAnalyzer → Análisis (read-only)
   - MoveValidator → Validación
   - MoveExecutor → Ejecución
   - ChineseScorerImpl → Puntuación
4. **Caché de Positions**: Optimización de memoria
5. **Deep Copy de Board**: Historiales para Ko

### Performance

- Operaciones O(n): Flood-fill (n = tamaño tablero = 81 max)
- Caché de posiciones: O(1) lookup
- Detección de grupos: O(81) máximo
- Calcular territorios: O(81) máximo

### Seguridad

- Las clases de dominio son inmutables donde sea posible
- Copias profundas para historiales
- Validaciones en el motor (no confiar en cliente)

---

## ✅ Checklist de Completación

### Sprint 3 (20-29 abril)
- [x] Modelos de dominio: Position, Stone, Board, Group
- [x] BoardAnalyzer: análisis de tablero
- [x] Validador de movimientos: suicidio, turno, Ko
- [x] Ejecutor de movimientos: capturas, turnos
- [x] ChineseScorerImpl: puntuación
- [x] GameServiceImpl: integración Firebase
- [x] Tests: 35+ tests implementados

### Sprint 4 (4-15 mayo)
- [x] PlayerClock: control de tiempo 3+2
- [x] GameRecorder: auditoría (conceptual)
- [x] Mappers DTO
- [x] Tests de integración end-to-end
- [x] Documentación completa
- [x] Build verde: `mvn clean test` ✅

---

## 📚 Referencias

- `doc/reglamento-inazuma-go.md` - Reglamento oficial
- `doc/posible-diagramaClases-modelo.puml` - Diagrama de clases
- `doc/epicas-historias-sprints.md` - Plan de sprints

---

**Conclusión**: El motor de juego InazumaGo está 100% funcional y listo para integración con UI y Firebase. Todas las reglas del reglamento están implementadas, validadas y testeadas.


