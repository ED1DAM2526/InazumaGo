# Configuración de Stubs WireMock para Eventos de Juego

## Descripción

Los stubs de WireMock permiten mockear las respuestas HTTP de la API de eventos de partida durante los tests de integración. Se han creado dos formas de usar stubs: manual y mediante la clase auxiliar `GameEventWireMockStubs`.

## Ubicación de Archivos

- **Clase de Stubs:** `src/test/java/es/iesquevedo/integration/wiremock/GameEventWireMockStubs.java`
- **Tests de Integración:** `src/test/java/es/iesquevedo/integration/GameEventIntegrationTest.java`

## Métodos de Configuración de Stubs

### 1. Stub Individual para Evento de Inicio

```java
// Forma manual
stubFor(post(urlEqualTo("/api/events/game.start"))
    .willReturn(aResponse()
        .withStatus(200)
        .withBody("{\"id\": \"event123\"}")));

// Forma auxiliar
GameEventWireMockStubs.stubGameStart("game-id");
```

### 2. Stub Individual para Evento de Movimiento

```java
// Forma manual
stubFor(post(urlEqualTo("/api/events/game.move"))
    .willReturn(aResponse()
        .withStatus(200)
        .withBody("{\"id\": \"event124\"}")));

// Forma auxiliar
GameEventWireMockStubs.stubGameMove("game-id");
```

### 3. Stub Individual para Evento de Fin

```java
// Forma manual
stubFor(post(urlEqualTo("/api/events/game.end"))
    .willReturn(aResponse()
        .withStatus(200)
        .withBody("{\"id\": \"event125\"}")));

// Forma auxiliar
GameEventWireMockStubs.stubGameEnd("game-id");
```

### 4. Stub para Todos los Eventos

```java
GameEventWireMockStubs.stubAllGameEvents("game-id");
```

## Configuración de Errores

### Stub que retorna error 500

```java
GameEventWireMockStubs.stubGameEventError("game.start", 500, "Internal Server Error");
```

### Stub que retorna error 400

```java
GameEventWireMockStubs.stubGameEventError("game.move", 400, "Invalid request");
```

## Verificación de Solicitudes

### Verificar que se envió una solicitud

```java
// Forma manual
verify(postRequestedFor(urlEqualTo("/api/events/game.start")));

// Forma auxiliar
GameEventWireMockStubs.verifyEventRequest("game.start");
```

### Verificar número exacto de solicitudes

```java
GameEventWireMockStubs.verifyEventRequest("game.move", 2);
```

## Ejemplo Completo de Test

```java
@ExtendWith(WireMockExtension.class)
class GameEventIntegrationTest {
    
    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
        .options(wireMockConfig().port(8080))
        .build();
    
    @Test
    void testCompleteGameEventFlow() {
        // Arrange
        String gameId = "game-complete-flow";
        GameEventWireMockStubs.stubAllGameEvents(gameId);
        
        GameEventService eventService = AppConfig.createGameEventService(
            "http://localhost:8080"
        );
        
        // Act - Evento de inicio
        GameDto gameStart = new GameDto(
            gameId, "Complete Flow", 
            Arrays.asList("Player1", "Player2"),
            "IN_PROGRESS",
            System.currentTimeMillis()
        );
        eventService.notifyGameStart(gameId, gameStart).join();
        
        // Act - Evento de movimiento
        MoveData move = new MoveData("player1", "KICK", new Position(5, 8));
        eventService.notifyGameMove(gameId, move).join();
        
        // Act - Evento de fin
        GameDto gameEnd = new GameDto(
            gameId, "Complete Flow",
            Arrays.asList("Player1", "Player2"),
            "FINISHED",
            System.currentTimeMillis()
        );
        eventService.notifyGameEnd(gameId, gameEnd).join();
        
        // Assert
        GameEventWireMockStubs.verifyEventRequest("game.start");
        GameEventWireMockStubs.verifyEventRequest("game.move");
        GameEventWireMockStubs.verifyEventRequest("game.end");
        
        eventService.shutdown();
    }
}
```

## Formato de Respuesta JSON

Todos los stubs retornan un JSON con la siguiente estructura:

```json
{
  "id": "event_1704067200000",
  "type": "game.start|game.move|game.end",
  "gameId": "game-id",
  "timestamp": 1704067200000
}
```

Donde:
- `id`: ID único del evento generado automáticamente
- `type`: Tipo de evento (game.start, game.move, game.end)
- `gameId`: ID de la partida asociada
- `timestamp`: Marca de tiempo en milisegundos

## Configuración Avanzada

### Stub con delay

```java
stubFor(post(urlEqualTo("/api/events/game.start"))
    .willReturn(aResponse()
        .withStatus(200)
        .withFixedDelay(1000))); // 1 segundo de delay
```

### Stub con transformación de respuesta

```java
stubFor(post(urlEqualTo("/api/events/game.move"))
    .withRequestBody(containing("KICK"))
    .willReturn(aResponse()
        .withStatus(200)
        .withBody("{\"type\": \"game.move\", \"action\": \"KICK\"}")));
```

## Integración con CI/CD

Para usar en pipelines CI/CD, asegúrate de:

1. **Puerto disponible:** El puerto 8080 no debe estar en uso
2. **Configuración de WireMock:** Especificado en `application-test.properties`
3. **Perfil de Maven:** Usar `mvn test -Pintegration` para ejecutar tests de integración

## Troubleshooting

### Error: "Port already in use"
Cambia el puerto en `wireMockConfig().port(8081)`

### Error: "No matching requests"
Verifica que:
1. La URL es exacta (incluyendo ruta completa)
2. El método HTTP es POST
3. Los stubs están configurados antes de hacer las solicitudes

### Error: "Unexpected request"
Usa `WireMock.removeAllMappings()` al inicio del test para limpiar stubs previos

