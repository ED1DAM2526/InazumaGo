# Quick Reference - Sincronización de Eventos de Partida

## 🎯 Uso Rápido

### 1. Crear el Servicio
```java
GameEventService eventService = AppConfig.createGameEventService(firebaseUrl);
```

### 2. Registrar Evento de Inicio
```java
GameDto game = new GameDto(
    "game-id",
    "Game Name",
    Arrays.asList("Player1", "Player2"),
    "IN_PROGRESS",
    System.currentTimeMillis()
);

eventService.notifyGameStart("game-id", game)
    .thenAccept(v -> System.out.println("✓ Partida iniciada"))
    .exceptionally(ex -> {
        System.err.println("✗ Error: " + ex.getMessage());
        return null;
    });
```

### 3. Registrar Movimiento
```java
MoveData move = new MoveData(
    "player-id",
    "KICK",      // KICK, PASS, TACKLE, etc.
    new Position(x, y)
);

eventService.notifyGameMove("game-id", move)
    .thenAccept(v -> System.out.println("✓ Movimiento registrado"))
    .exceptionally(ex -> {
        System.err.println("✗ Error: " + ex.getMessage());
        return null;
    });
```

### 4. Registrar Fin de Partida
```java
GameDto gameEnd = new GameDto(
    "game-id",
    "Game Name",
    Arrays.asList("Player1", "Player2"),
    "FINISHED",
    System.currentTimeMillis()
);

eventService.notifyGameEnd("game-id", gameEnd)
    .thenAccept(v -> System.out.println("✓ Partida finalizada"))
    .exceptionally(ex -> {
        System.err.println("✗ Error: " + ex.getMessage());
        return null;
    });
```

### 5. Liberar Recursos
```java
eventService.shutdown();
```

## 📁 Ubicaciones Clave

| Componente | Ubicación |
|-----------|-----------|
| Repositorio | `src/main/java/es/iesquevedo/repository/firebase/GameEventRepository.java` |
| Servicio (Interfaz) | `src/main/java/es/iesquevedo/service/GameEventService.java` |
| Servicio (Impl) | `src/main/java/es/iesquevedo/service/impl/GameEventServiceImpl.java` |
| Tests Unitarios | `src/test/java/es/iesquevedo/repository/firebase/GameEventRepositoryTest.java` |
| Tests Unitarios | `src/test/java/es/iesquevedo/service/impl/GameEventServiceImplTest.java` |
| Tests Integración | `src/test/java/es/iesquevedo/integration/GameEventIntegrationTest.java` |
| Stubs WireMock | `src/test/java/es/iesquevedo/integration/wiremock/GameEventWireMockStubs.java` |

## 🧪 Testing Rápido

### Configurar Stubs de WireMock
```java
@ExtendWith(WireMockExtension.class)
class MyTest {
    @Test
    void myTest() {
        // Configurar todos los stubs
        GameEventWireMockStubs.stubAllGameEvents("game-id");
        
        // Tu test aquí
        
        // Verificar que se envió la solicitud
        GameEventWireMockStubs.verifyEventRequest("game.start");
    }
}
```

### Configurar Stubs Individuales
```java
GameEventWireMockStubs.stubGameStart("game-id");
GameEventWireMockStubs.stubGameMove("game-id");
GameEventWireMockStubs.stubGameEnd("game-id");
```

### Configurar Error
```java
GameEventWireMockStubs.stubGameEventError("game.start", 500, "Server Error");
```

## 📊 Estructura de Datos

### GameDto
```java
new GameDto(
    String id,                      // "game-123"
    String name,                    // "Championship 2024"
    List<String> players,           // Arrays.asList("P1", "P2")
    String status,                  // "IN_PROGRESS" o "FINISHED"
    long createdAt                  // System.currentTimeMillis()
)
```

### MoveData
```java
new MoveData(
    String playerId,                // "player-1"
    String move,                    // "KICK", "PASS", "TACKLE", etc.
    Position position               // new Position(x, y)
)
```

### Position
```java
new Position(
    int x,                          // Coordenada X
    int y                           // Coordenada Y
)
```

## ⚙️ Configuración

### application.properties
```properties
# Firebase URL
firebase.rtdb.url=https://your-project.firebaseio.com

# Game Events
game.events.enabled=true
game.events.database-path=game_events
game.events.async-processing=true
game.events.executor-threads=2

# WireMock (solo testing)
wiremock.server.port=8080
wiremock.server.baseurl=http://localhost:8080
```

## 🔧 Métodos de AppConfig

```java
// Crear repositorio
GameEventRepository repo = 
    AppConfig.createGameEventRepository(firebaseUrl);

// Crear repositorio desde Firebase mockeado
GameEventRepository repo = 
    AppConfig.createGameEventRepositoryFromDatabase(firebaseDatabase);

// Crear servicio (recomendado)
GameEventService service = 
    AppConfig.createGameEventService(firebaseUrl);

// Crear servicio desde repositorio
GameEventService service = 
    AppConfig.createGameEventService(repository);
```

## 📚 Documentación Completa

| Documento | Propósito |
|-----------|----------|
| `INTEGRATION_COMPLETE.md` | Arquitectura y guía completa |
| `GAME_EVENTS_INTEGRATION.md` | Implementación detallada |
| `WIREMOCK_STUBS_GUIDE.md` | Guía de stubs para testing |
| `FIREBASE_WIREMOCK_CONFIG.md` | Configuración de Firebase y WireMock |
| `GAME_EVENTS_INTEGRATION_SUMMARY.md` | Resumen de archivos creados |
| `VERIFICACION_INTEGRACION.md` | Checklist de verificación |

## 💡 Ejemplo Completo

```java
public class GameController {
    private final GameEventService eventService;
    
    public GameController(GameEventService eventService) {
        this.eventService = eventService;
    }
    
    public void startGame(String gameId, GameDto game) {
        // Lógica de negocio
        
        // Notificar evento de inicio
        eventService.notifyGameStart(gameId, game)
            .exceptionally(ex -> {
                logger.error("Error al iniciar partida", ex);
                return null;
            });
    }
    
    public void recordMove(String gameId, MoveData move) {
        // Lógica de negocio
        
        // Notificar evento de movimiento
        eventService.notifyGameMove(gameId, move)
            .exceptionally(ex -> {
                logger.error("Error al registrar movimiento", ex);
                return null;
            });
    }
    
    public void endGame(String gameId, GameDto game) {
        // Lógica de negocio
        
        // Notificar evento de fin
        eventService.notifyGameEnd(gameId, game)
            .exceptionally(ex -> {
                logger.error("Error al finalizar partida", ex);
                return null;
            });
    }
}
```

## 🚀 Flujo Típico

1. **Inicio de Partida**
   - Crear GameDto
   - Llamar `eventService.notifyGameStart()`
   - Evento registrado en Firebase

2. **Durante la Partida**
   - Crear MoveData por cada acción
   - Llamar `eventService.notifyGameMove()` para cada movimiento
   - Eventos registrados en Firebase en tiempo real

3. **Fin de Partida**
   - Actualizar GameDto con status="FINISHED"
   - Llamar `eventService.notifyGameEnd()`
   - Evento final registrado en Firebase

4. **Limpieza**
   - Llamar `eventService.shutdown()`

## ✅ Verificación

Para verificar que todo funciona:

```bash
# Compilar
mvn clean compile

# Ejecutar todos los tests
mvn test

# Ejecutar solo tests de eventos
mvn test -Dtest=GameEvent*
```

Resultado esperado: ✅ **BUILD SUCCESS**

