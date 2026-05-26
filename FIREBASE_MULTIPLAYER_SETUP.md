# Configuración Firebase para Multijugador - MM-impl

## ⚙️ Pasos a Realizar en Firebase Console

### 1. **Acceder a Firebase Console**
- Ve a https://console.firebase.google.com
- Selecciona tu proyecto "InazumaGo"

### 2. **Habilitar Realtime Database (si no está habilitada)**
- En el panel izquierdo: "Build" → "Realtime Database"
- Crea una base de datos en el mismo servidor (USA es recomendado)
- Modo: Inicia en modo de prueba (test mode)

### 3. **Actualizar Reglas de Seguridad**

Ve a "Realtime Database" → Pestaña "Rules" y reemplaza con:

```json
{
  "rules": {
    "games": {
      ".indexOn": ["status", "createdAt"],
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
            ".write": "auth != null",
            ".validate": "newData.hasChildren(['playerId', 'row', 'col', 'timestamp'])"
          }
        }
      }
    }
  }
}
```

**Haz clic en "Publicar"**

### 4. **Copiar URL de la Realtime Database**

- En "Realtime Database", busca la URL (algo como: `https://tu-proyecto-default-rtdb.firebaseio.com`)
- Cópiala y actualiza en el código:

**Archivo:** `MultiplayerMatchingController.java` (línea 18)
```java
private static final String FIREBASE_URL = "https://tu-proyecto-default-rtdb.firebaseio.com";
```

### 5. **Verificar Authentication**

- Ve a "Build" → "Authentication"
- Asegúrate que está habilitado (debe estar si ya has hecho login antes)
- Verifica que los proveedores configurados incluyan "Email/Password" o el que uses

## 📱 Flujo de Usuario Final

### Dispositivo 1 (Jugador A):
```
Login → Dashboard → "Emparejamiento Multijugador" → "Crear Partida"
         ↓
     Esperando jugador...
```

### Dispositivo 2 (Jugador B):
```
Login → Dashboard → "Emparejamiento Multijugador" → "Buscar Partidas"
         ↓
     Se ve partida de Jugador A → "Unirse"
         ↓
     Ambos ven la pantalla de juego sincronizada
```

## 🔄 Sincronización en Tiempo Real

Una vez configurado, el flujo es:

1. **Dispositivo 1** hace un movimiento
2. Se valida localmente + se envía a Firebase
3. **Firebase** persiste el movimiento
4. **Dispositivo 2** recibe notificación del cambio
5. Se aplica el movimiento al tablero remoto
6. Ambos ven el tablero sincronizado

## 🛠️ Ajustes Adicionales (Opcionales)

### Cambiar Listener Polling a Real-time (Firebase SDK)

Si quieres verdadera sincronización real-time en lugar de polling:

En `MultiplayerGameServiceImpl.java`, reemplaza el método `subscribeToRemoteMoves`:

```java
@Override
public String subscribeToRemoteMoves(String gameId, Consumer<List<RemoteMoveDto>> listener) {
    String listenerId = "remote_moves_" + UUID.randomUUID();
    
    // Usar Firebase SDK en lugar de polling
    DatabaseReference movesRef = repository.getDatabase()
        .getReference("games/" + gameId + "/remoteMoves");
    
    ValueEventListener vel = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot snapshot) {
            List<RemoteMoveDto> moves = new ArrayList<>();
            for (DataSnapshot child : snapshot.getChildren()) {
                RemoteMoveDto move = child.getValue(RemoteMoveDto.class);
                if (move != null) {
                    moves.add(move);
                }
            }
            listener.accept(moves);
        }
        
        @Override
        public void onCancelled(DatabaseError error) {
            LOGGER.log(Level.WARNING, "Error en listener de movimientos", error.toException());
        }
    };
    
    movesRef.addValueEventListener(vel);
    movesListenerIds.put(gameId, listenerId);
    return listenerId;
}
```

## 🧪 Probar Localmente

Puedes simular 2 dispositivos abriendo 2 instancias del cliente:

1. Abre InazumaGo en ventana 1 → Login A
2. Abre InazumaGo en ventana 2 → Login B
3. Ventana 1: Crear partida
4. Ventana 2: Buscar y unirse
5. ¡A jugar!

## ✅ Checklist de Configuración

- [ ] He copiado la URL de Firebase Realtime Database
- [ ] He actualizado `FIREBASE_URL` en `MultiplayerMatchingController`
- [ ] He publicado las nuevas reglas de seguridad
- [ ] He habilitado Realtime Database
- [ ] He probado crear una partida
- [ ] He probado unirse desde otro dispositivo/cliente
- [ ] Los movimientos se sincronizan correctamente

## ❓ Troubleshooting

### "No se pueden crear partidas"
→ Verifica que el usuario esté autenticado (`AppState.getAuthToken()` no sea null)

### "La lista de partidas está vacía"
→ Ve a Firebase Console → Realtime Database y verifica que haya datos bajo `/games`

### "Los movimientos no se sincronizan"
→ Abre la consola del navegador (DevTools) y busca errores de red
→ Verifica que la URL de Firebase sea correcta

### Error 403 (Forbidden)
→ Las reglas de seguridad pueden estar rechazando la escritura
→ Revisa las reglas en Firebase Console
→ Asegúrate que el usuario esté autenticado

## 📞 Contacto

Para ayuda específica sobre Firebase Realtime Database:
- https://firebase.google.com/docs/realtime/usage
- https://firebase.google.com/docs/database/security

