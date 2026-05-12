# Configuración Firebase y WireMock

## Endpoint y Timeout

### Configuración en `application.properties`

```properties
# Firebase Configuration
firebase.endpoint=https://firestore.googleapis.com/v1
firebase.timeout=30000
firebase.retry-attempts=3

# WireMock Configuration (para testing)
wiremock.server.port=8080
wiremock.server.baseurl=http://localhost:8080
```

### Parámetros explicados

| Parámetro | Valor | Descripción |
|-----------|-------|-------------|
| `firebase.endpoint` | `https://firestore.googleapis.com/v1` | URL base de Firestore |
| `firebase.timeout` | `30000` | Timeout en milisegundos (30 segundos) |
| `firebase.retry-attempts` | `3` | Número de reintentos en caso de fallo |
| `wiremock.server.port` | `8080` | Puerto del servidor WireMock para testing |
| `wiremock.server.baseurl` | `http://localhost:8080` | URL base de WireMock |

## Sincronización de Eventos de Partida

### Eventos soportados

- **`game.start`** - Inicio de partida
- **`game.move`** - Movimiento del jugador
- **`game.end`** - Fin de partida

Estos eventos se sincronizan automáticamente con Firestore cuando ocurren en la aplicación.

## Testing con WireMock

### Stub básico para testing

En tests unitarios, usar `@ExtendWith(MockitoExtension.class)` y stubs para mockear respuestas de Firebase:

```java
@ExtendWith(MockitoExtension.class)
class GameRepositoryTest {
    
    @Mock
    private FirebaseDatabase firebaseDatabase;
    
    @InjectMocks
    private GameRepository gameRepository;
    
    @Test
    void testGameStartEvent() {
        // Arrange
        MoveData moveData = new MoveData(/* ... */);
        
        // Act
        gameRepository.recordEvent("game.start", moveData);
        
        // Assert
        verify(firebaseDatabase).push(any());
    }
}
```

### Stub avanzado con WireMock

Para tests de integración con WireMock:

```java
@ExtendWith(WireMockExtension.class)
class FirebaseIntegrationTest {
    
    static WireMockExtension wireMock = WireMockExtension.newInstance()
        .options(wireMockConfig().port(8080))
        .build();
    
    @Test
    void testGameEventSyncWithFirebase() {
        // Arrange
        stubFor(post(urlEqualTo("/api/events"))
            .willReturn(aResponse()
                .withStatus(200)
                .withBody("{\"id\": \"event123\"}")));
        
        // Act & Assert
        // Test de sincronización
    }
}
```

## Referencia de implementación en AppConfig

Ver `AppConfig.java` para la configuración automática de propiedades.

