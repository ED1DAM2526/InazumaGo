# Configuración Firebase para InazumaGo

## Estado Actual
- ✅ Repositorio HTTP en OkHttp (FirebaseMainRepository) implementado
- ✅ AuthService mock para desarrollo
- ✅ Tests básicos pasando (sin dependencia de Firebase real)
- ⏳ **Falta: Configurar Firebase Console**

---

## Pasos para Configurar Firebase (10-15 min)

### 1. Crear Proyecto en Firebase Console
1. Ve a **https://console.firebase.google.com**
2. Click **"Crear proyecto"**
3. Nombre: `InazumaGo`
4. Deshabilita Analytics (por ahora, opcional)
5. Click **"Crear"**

### 2. Habilitar Realtime Database
1. En el proyecto, ve a **"Realtime Database"** (en el menú lateral)
2. Click **"Crear base de datos"**
3. Ubicación: **Europe (europe-west1)** o **us-central1**
4. Modo: **Start in test mode** (por ahora, abierta para desarrollo)
5. Click **"Crear"**

Firebase generará una URL como:
```
https://inazumago-abc123.firebaseio.com
```

### 3. Copiar URL a `application.properties`

**Archivo:** `src/main/resources/application.properties`

```properties
# Firebase URL (sin .json)
firebase.url=https://inazumago-abc123.firebaseio.com
firebase.timeout.seconds=30
firebase.auth.token=
```

### 4. (Alternativa) Usar Variable de Entorno

```powershell
# PowerShell
$env:FIREBASE_URL="https://inazumago-abc123.firebaseio.com"
```

O en `.env`:
```
FIREBASE_URL=https://inazumago-abc123.firebaseio.com
FIREBASE_AUTH_TOKEN=
```

### 5. Reglas de Seguridad RTDB (Desarrollo)

**Para DESARROLLO:** En Firebase Console → Realtime Database → Rules

```json
{
  "rules": {
    ".read": true,
    ".write": true,
    "games": {
      "$gameId": {
        ".validate": "newData.hasChild('players')",
        "players": {
          ".validate": "newData.val().length() <= 2"
        },
        "moves": {
          ".validate": "!data.exists() || newData.val().length() >= data.val().length()"
        }
      }
    }
  }
}
```

**Para PRODUCCIÓN:** (después, con autenticación real)

```json
{
  "rules": {
    ".read": "auth != null",
    ".write": "auth != null",
    "games": {
      "$gameId": {
        ".validate": "newData.hasChildren(['id', 'players', 'status'])",
        "players": {
          ".validate": "newData.val().length() >= 2 && newData.val().length() <= 2"
        },
        "moves": {
          ".validate": "root.child('games').child($gameId).child('currentTurn') != null"
        }
      }
    }
  }
}
```

---

## Testing con Firebase Real

**Una vez configurado, Red puede hacer:**

1. Tests WireMock (ya listos en `FirebaseMainRepositoryTest`)
2. Tests contra Firebase real (descomenta tras configurar):

```java
@Test
void testCreateGameAgainstFirebase() throws Exception {
    GameDto game = new GameDto("game-real-123", "Test", 
        Arrays.asList("p1", "p2"), "IN_PROGRESS", System.currentTimeMillis());
    
    FirebaseMainRepository repo = new FirebaseMainRepository(
        System.getenv("FIREBASE_URL")
    );
    CompletableFuture<GameDto> result = repo.createGame(game);
    GameDto created = result.get();
    
    assertNotNull(created);
    assertEquals("game-real-123", created.getId());
}
```

---

## Checklist para Red

- [ ] Firebase Console: Proyecto creado
- [ ] RTDB: Base de datos creada (EU o US)
- [ ] RTDB URL: Copiada a `application.properties`
- [ ] Reglas: Aplicadas (desarrollo first)
- [ ] Tests: Ejecutados contra Firebase real (opcional ahora, hacer después)

---

## Próximo Paso (E2-US3)

Cuando Firebase esté configurado, Red implementa:
- AuthService real (Firebase Authentication)
- Integración de tokens en peticiones
- SSE o WebSocket para listeners en tiempo real

**Responsable:** Red Team  
**Estimación:** 1-2 sprints  
**Bloqueador:** Firebase configurado
