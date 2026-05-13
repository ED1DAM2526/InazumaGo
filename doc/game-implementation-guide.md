# Guía para Implementar una Partida Jugable (Motor + UI)

## Estado Actual de Red
✅ FirebaseMainRepository (cliente HTTP OkHttp)  
✅ AuthService (mock para desarrollo)  
✅ 34 tests pasando  

---

## Lo que Motor Necesita Hacer

### 1. **Sincronización Optimistic + Rollback** (E2-US4-T3)
Crear en `GameService`:
```java
public CompletableFuture<Void> executeMove(String gameId, MoveData move) {
    // 1. Aplicar movimiento localmente (optimistic)
    localGame.getMoves().add(move);
    
    // 2. Enviar a Firebase
    MovePayload payload = new MovePayload(Arrays.asList(move), System.currentTimeMillis());
    return mainRepository.writeMoveMultiPath(gameId, payload)
        .exceptionally(e -> {
            // 3. Rollback si falla (403, timeout, etc.)
            localGame.getMoves().remove(move);
            throw new RuntimeException("Movimiento rechazado: " + e.getMessage());
        });
}
```

### 2. **Reglas de Validación de Movimientos** (E3-US2)
Crear interfaz `MoveValidator`:
```java
public interface MoveValidator {
    void validateMove(Game game, MoveData move) throws InvalidMoveException;
}
```

Implementación:
```java
public class InazumaGoMoveValidator implements MoveValidator {
    @Override
    public void validateMove(Game game, MoveData move) throws InvalidMoveException {
        // Validar: turno, posición, acción permitida, etc.
        if (!isPlayerTurn(game, move.getPlayerId())) {
            throw new InvalidMoveException("No es tu turno");
        }
        if (!isValidPosition(move.getPosition())) {
            throw new InvalidMoveException("Posición fuera del campo");
        }
    }
}
```

### 3. **Integración en GameService**
```java
public CompletableFuture<Void> executeMove(String gameId, MoveData move) {
    return CompletableFuture.runAsync(() -> {
        try {
            moveValidator.validateMove(currentGame, move);
            // Enviar a Firebase...
            mainRepository.writeMoveMultiPath(gameId, ...);
            currentGame.nextTurn(); // Cambiar turno
        } catch (InvalidMoveException e) {
            throw new RuntimeException(e);
        }
    });
}
```

---

## Lo que UI Necesita Hacer

### 1. **Pantalla de Partida**
Crear `GameController.fxml` + `GameController.java`:
```java
@FXML private Label playerNameLabel;
@FXML private Button kickButton, passButton;
@FXML private Canvas gameCanvas; // Dibujar campo

private GameService gameService;
private String currentGameId;

@FXML private void onKickPressed() {
    MoveData move = new MoveData(playerName, "KICK", selectedPosition);
    gameService.executeMove(currentGameId, move)
        .exceptionally(e -> {
            showError("Movimiento rechazado: " + e.getMessage());
            return null;
        });
}
```

### 2. **Listeners en Tiempo Real**
```java
private void subscribeToMoves(String gameId) {
    String listenerId = gameService.addMovesListener(gameId, moves -> {
        // Actualizar UI con nuevos movimientos
        Platform.runLater(() -> renderMoves(moves));
    });
}
```

### 3. **Flujo de Creación de Partida**
```
1. Pantalla de login (usa AuthService)
2. Pantalla de lobby (listar partidas o crear nueva)
3. Pantalla de espera (esperando segundo jugador)
4. Pantalla de partida (turnos, movimientos)
5. Pantalla de resultado (victoria/derrota/abandono)
```

---

## Para que Compile Todo Junto

Motor debe crear:
- [ ] `MoveValidator` interface + `InazumaGoMoveValidator`
- [ ] Método `executeMove()` en `GameService`
- [ ] Tests de `MoveValidator` (qué movimientos son válidos)

UI debe crear:
- [ ] `GameController.fxml` (diseño básico del campo)
- [ ] `GameController.java` (wiring con GameService)
- [ ] `AuthenticationController.fxml` + `.java` (login mock)
- [ ] Actualizar `MainGUI.java` para cargar `AuthenticationController` primero

---

## Orden de Implementación (Rápido)

**Día 1 - Motor:**
1. `MoveValidator` interface
2. `InazumaGoMoveValidator` (validar turno, posición)
3. `executeMove()` en `GameService` (con optimistic update + rollback)

**Día 1 - UI:**
1. `AuthenticationController` (login mock, reutiliza `AuthServiceImpl`)
2. `GameController.fxml` (canvas con campo + botones)

**Día 2 - Integración:**
1. Flujo completo: login → crear partida → jugar
2. Sync de movimientos (listeners)
3. Tests E2E con stubs

---

## Firebase (Red) - Paralelo
Mientras Motor y UI trabajan:
1. Configura Firebase Console (10 min)
2. Copia URL a `application.properties`
3. Escribe reglas RTDB

**No hace falta esperar a nada - Red ya dejó el código listo.**

---

## Checklist Final

- [ ] Motor: `MoveValidator` + `executeMove()`
- [ ] UI: `AuthenticationController` + `GameController`
- [ ] Tests: Mínimo 3 tests de validación de movimientos
- [ ] Firebase: Configurado en Console
- [ ] Partida: Crear, jugar 2 turnos mínimo, ver resultado

