# Integración de Eventos de Partida con Firebase y WireMock

## Descripción General

Este documento describe cómo se ha integrado la sincronización de eventos de partida (inicio, movimiento, fin) con Firebase Realtime Database usando stubs de WireMock para testing.

## Componentes Principales

### 1. GameEventRepository
**Ubicación:** `src/main/java/es/iesquevedo/repository/firebase/GameEventRepository.java`

Repositorio especializado para la sincronización de eventos de partida:

```java
// Registrar evento de inicio
CompletableFuture<Void> recordGameStart(String gameId, GameDto gameDto);

// Registrar movimiento
CompletableFuture<Void> recordGameMove(String gameId, MoveData moveData);

// Registrar fin de partida
CompletableFuture<Void> recordGameEnd(String gameId, GameDto gameDto);
```

**Tipos de eventos soportados:**
- `game.start` - Inicio de partida
- `game.move` - Movimiento de jugador
- `game.end` - Fin de partida

### 2. GameEventService
**Ubicación:** `src/main/java/es/iesquevedo/service/GameEventService.java`

Interfaz que define el servicio de eventos con operaciones asíncronas.

### 3. GameEventServiceImpl
**Ubicación:** `src/main/java/es/iesquevedo/service/impl/GameEventServiceImpl.java`

Implementación del servicio que:
- Encapsula el repositorio de eventos
- Ejecuta las notificaciones de forma asíncrona usando un `ExecutorService`
- Maneja errores y excepciones

## Tests de Integración

### GameEventRepositoryTest
**Ubicación:** `src/test/java/es/iesquevedo/repository/firebase/GameEventRepositoryTest.java`

Tests unitarios que verifican:
- Registro de evento de inicio
- Registro de movimiento
- Registro de fin de partida
- Inclusión de timestamp
- Tipos de evento correctos

### GameEventServiceImplTest
**Ubicación:** `src/test/java/es/iesquevedo/service/impl/GameEventServiceImplTest.java`

Tests que verifican:
- Notificación de inicio de partida
- Notificación de movimiento
- Notificación de fin de partida
- Procesamiento asíncrono
- Manejo de excepciones

### GameEventIntegrationTest
**Ubicación:** `src/test/java/es/iesquevedo/integration/GameEventIntegrationTest.java`

Tests de integración con WireMock que:
- Usan `@ExtendWith(WireMockExtension.class)`
- Configuran stubs para endpoints `/api/events/game.start`, `/api/events/game.move`, `/api/events/game.end`
- Verifican que los eventos se envíen correctamente
- Validan respuestas HTTP

## Configuración

### application.properties
Se han agregado propiedades para eventos de juego:

```properties
# Game Events Configuration
game.events.enabled=true
game.events.database-path=game_events
game.events.async-processing=true
game.events.executor-threads=2

# WireMock Configuration
wiremock.server.port=8080
wiremock.server.baseurl=http://localhost:8080
```

### AppConfig
Se han agregado métodos de fábrica:

```java
// Crear repositorio
GameEventRepository createGameEventRepository(String firebaseUrl);

// Crear repositorio desde database mockeado
GameEventRepository createGameEventRepositoryFromDatabase(FirebaseDatabase db);

// Crear servicio
GameEventService createGameEventService(String firebaseUrl);
GameEventService createGameEventService(GameEventRepository repository);
```

## Estructura de Eventos en Firebase

Los eventos se almacenan en la ruta `game_events` con la siguiente estructura:

```json
{
  "game_events": {
    "event_id_1": {
      "type": "game.start",
      "gameId": "game123",
      "timestamp": 1704067200000,
      "payload": {
        "id": "game123",
        "name": "Final Cup",
        "players": ["Player1", "Player2"],
        "status": "IN_PROGRESS",
        "createdAt": 1704067200000
      }
    },
    "event_id_2": {
      "type": "game.move",
      "gameId": "game123",
      "timestamp": 1704067210000,
      "payload": {
        "playerId": "player1",
        "move": "KICK",
        "position": {"x": 5, "y": 8},
        "timestamp": 1704067210000
      }
    }
  }
}
```

## Cómo Usar

### Opción 1: Usar el Servicio (Recomendado)

```java
// Crear el servicio
GameEventService eventService = AppConfig.createGameEventService(firebaseUrl);

String gameId = "game-123";

// Evento de inicio
GameDto gameStart = new GameDto(...);
eventService.notifyGameStart(gameId, gameStart);

// Evento de movimiento
MoveData move = new MoveData(...);
eventService.notifyGameMove(gameId, move);

// Evento de fin
GameDto gameEnd = new GameDto(...);
eventService.notifyGameEnd(gameId, gameEnd);

// Liberar recursos
eventService.shutdown();
```

### Opción 2: Usar el Repositorio Directamente

```java
// Crear repositorio
GameEventRepository eventRepo = AppConfig.createGameEventRepository(firebaseUrl);

String gameId = "game-123";

// Registrar eventos
eventRepo.recordGameStart(gameId, gameDto);
eventRepo.recordGameMove(gameId, moveData);
eventRepo.recordGameEnd(gameId, gameDto);
```

## Testing con WireMock

Para tests, los stubs de WireMock se configuran así:

```java
@ExtendWith(WireMockExtension.class)
class GameEventIntegrationTest {
    
    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
        .options(wireMockConfig().port(8080))
        .build();
    
    @Test
    void testGameEventSync() {
        // Configurar stub
        stubFor(post(urlEqualTo("/api/events/game.start"))
            .willReturn(aResponse()
                .withStatus(200)
                .withBody("{\"id\": \"event123\"}")));
        
        // Ejecutar test
        // ...
    }
}
```

## Configuración de Firebase en Tests

Para tests unitarios que necesitan Firebase mockeado:

```java
@ExtendWith(MockitoExtension.class)
class GameEventRepositoryTest {
    
    @Mock
    private FirebaseDatabase firebaseDatabase;
    
    @BeforeEach
    void setUp() {
        GameEventRepository repo = new GameEventRepository(firebaseDatabase);
    }
}
```

## Manejo de Errores

El sistema proporciona manejo automático de errores:

```java
eventService.notifyGameStart(gameId, gameDto)
    .exceptionally(ex -> {
        System.err.println("Error: " + ex.getMessage());
        return null;
    });
```

## Dependencias Necesarias

El proyecto ya tiene configuradas en `pom.xml`:
- `firebase-admin` (v9.8.0) - Para Firebase Realtime Database
- `wiremock-jre8` (v2.35.0) - Para tests de integración
- `junit-jupiter` (v5.10.0) - Para tests unitarios
- `mockito-core` y `mockito-junit-jupiter` - Para mocks

## Próximos Pasos

1. **Integración en Controllers:** Inyectar `GameEventService` en los controllers que manejan partidas
2. **Eventos en Tiempo Real:** Agregar listeners para eventos en tiempo real desde Firebase
3. **Reintento Automático:** Implementar política de reintento para fallos de sincronización
4. **Metricas:** Agregar contadores de eventos sincronizados exitosamente/fallidos

