# Implementación Multijugador - MM-impl

## Descripción General

Esta rama implementa funcionalidad completa de multijugador utilizando Firebase Realtime Database. Permite que dos jugadores desde dispositivos diferentes se emparejen automáticamente y jueguen una partida sincronizada en tiempo real.

## Características Implementadas

### 1. **Servicios Base**
- `MultiplayerGameService` - Interfaz principal para operaciones multijugador
- `MultiplayerGameServiceImpl` - Implementación con Firebase
- DTOs nuevos:
  - `RemoteMoveDto` - Para sincronizar movimientos
  - `PlayerPresenceDto` - Para rastrear jugadores conectados

### 2. **Controladores**
- `MultiplayerGameController` - Controlador especializado para partidas multijugador
  - Sincronización en tiempo real de movimientos
  - Listeners para cambios remotos
  - Validación de turnos entre dispositivos

- `MultiplayerMatchingController` - Sistema de emparejamiento mejorado
  - Crear partidas (esperar oponente)
  - Buscar partidas disponibles
  - Unirse a partidas existentes
  - Listar partidas en espera

### 3. **Interfaz de Usuario**
- `MultiplayerGame.fxml` - Pantalla de juego multijugador
  - Indicador de conexión en tiempo real
  - Sincronización automática de estado
  
- `MultiplayerMatching.fxml` - Pantalla de emparejamiento
  - Crear nueva partida
  - Buscar partidas disponibles
  - Unirse a partida seleccionada

## Flujo de Uso

### Escenario 1: Jugador A crea partida, Jugador B se une

1. **Jugador A (Dispositivo 1)**
   - Login con sus credenciales
   - Navega a "Emparejamiento Multijugador"
   - Presiona "Crear Partida"
   - Espera a que otro jugador se una

2. **Jugador B (Dispositivo 2)**
   - Login con sus credenciales
   - Navega a "Emparejamiento Multijugador"
   - Presiona "Buscar Partidas"
   - Sistema lista partidas disponibles
   - Selecciona la partida de Jugador A
   - Presiona "Unirse a Partida Seleccionada"

3. **Sistema sincroniza**
   - Ambos se cargan la pantalla de juego
   - Se suscriben a cambios remotos
   - Comienza la partida

## Configuración Firebase Necesaria

### 1. **Estructura de Base de Datos Recomendada**

```
{
  "games": {
    "game-id-1": {
      "id": "game-id-1",
      "name": "Partida de Ejemplo",
      "status": "WAITING" | "IN_PROGRESS" | "FINISHED" | "ABANDONED",
      "players": ["player-uid-1", "player-uid-2"],
      "createdAt": 1234567890,
      "remoteMoves": {
        "move-id-1": {
          "moveId": "move-id-1",
          "gameId": "game-id-1",
          "playerId": "player-uid-1",
          "playerName": "Juan",
          "row": 3,
          "col": 3,
          "isPass": false,
          "timestamp": 1234567891,
          "turnNumber": 1,
          "status": "confirmed"
        }
      }
    }
  }
}
```

### 2. **Índices Firebase (Realtime Database)**

En la consola de Firebase:
1. Ir a "Database" → "Realtime Database"
2. Pestaña "Reglas"
3. Agregar índices en `.indexOn`:

```json
{
  "rules": {
    "games": {
      ".indexOn": ["status", "createdAt"],
      "$gameId": {
        ".read": true,
        ".write": "auth != null",
        "remoteMoves": {
          ".read": true,
          ".write": "auth != null"
        }
      }
    }
  }
}
```

### 3. **Reglas de Seguridad Sugeridas**

```json
{
  "rules": {
    "games": {
      ".read": true,
      ".write": "auth != null",
      "$gameId": {
        ".validate": "newData.hasChildren(['id', 'name', 'status', 'players'])",
        "status": {
          ".validate": "newData.val() in ['WAITING', 'IN_PROGRESS', 'FINISHED', 'ABANDONED']"
        },
        "players": {
          ".validate": "newData.val().size() <= 2"
        },
        "remoteMoves": {
          "$moveId": {
            ".write": "newData.child('playerId').val() === auth.uid"
          }
        }
      }
    }
  }
}
```

## Cómo Usar en Código

### 1. **Iniciar Partida Existente**

```java
MultiplayerGameController controller = loader.getController();
controller.initMultiplayerGame(gameId, currentPlayerId, firebaseUrl);
```

### 2. **Unirse a Partida**

```java
MultiplayerGameController controller = loader.getController();
controller.joinMultiplayerGame(gameId, player, firebaseUrl);
```

### 3. **Crear Partida con Servicio**

```java
MultiplayerGameService service = new MultiplayerGameServiceImpl(repository);
service.createMultiplayerGame("Mi Partida", player)
    .thenAccept(gameId -> {
        // Navegar a pantalla de juego
    });
```

### 4. **Buscar Partidas Disponibles**

```java
service.getAvailableGames()
    .thenAccept(gameIds -> {
        // Mostrar lista en UI
    });
```

## Cambios Necesarios en Firebase

**El usuario debe:**

1. ✅ Asegurar que Firebase Authentication esté habilitado
2. ✅ Configurar Firebase Realtime Database (ya existe)
3. ✅ Aplicar las reglas de seguridad anteriores
4. ✅ Verificar que la URL de Firebase sea correcta en `MultiplayerMatchingController.FIREBASE_URL`

## Pruebas Recomendadas

### Test Local
```bash
mvn test -Dtest=MultiplayerGameServiceImplTest
```

### Test de Integración Firebase
```bash
mvn verify -P integration-tests
```

## Notas Técnicas

- **Sincronización**: Usa CompletableFuture para operaciones asincrónicas
- **Listeners**: Implementa polling en memoria (TODO: mejorar con Firebase listeners reales)
- **Validación**: Valida movimientos localmente antes de enviar
- **Reconnección**: Maneja desconexiones pero no tiene reintentos automáticos (TODO)
- **Cache**: Caché local de partidas para reducir latencia

## Mejoras Futuras

1. WebSocket para sincronización en tiempo real real
2. Reintento automático de movimientos fallidos
3. Detección de desconexión y reconexión
4. Historial de partidas guardado
5. Sistema de chat en partida
6. Ranking y estadísticas

## Estructura de Archivos Nuevos

```
src/main/java/es/iesquevedo/
  ├── controller/
  │   └── MultiplayerGameController.java
  ├── dto/
  │   ├── PlayerPresenceDto.java
  │   └── RemoteMoveDto.java
  ├── service/
  │   ├── MultiplayerGameService.java (interfaz)
  │   └── impl/
  │       └── MultiplayerGameServiceImpl.java
  └── ui/
      └── MultiplayerMatchingController.java

src/main/resources/fxml/
  ├── MultiplayerGame.fxml
  └── MultiplayerMatching.fxml
```

## Contacto y Soporte

Para preguntas sobre la implementación multijugador, consulta la documentación de Firebase:
- https://firebase.google.com/docs/realtime/usage
- https://firebase.google.com/docs/auth

