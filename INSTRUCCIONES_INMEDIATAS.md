# 🚀 INSTRUCCIONES INMEDIATAS - Comienza en 5 Minutos

## 📍 Estás aquí

La integración de eventos de partida con Firebase y WireMock está **100% completada** y lista para usar.

---

## ⏱️ Paso 1 (2 minutos): Leer la Introducción

**Abre:** `QUICK_REFERENCE.md`

Verás:
- Cómo crear el servicio
- Cómo registrar eventos
- Ubicaciones de archivos
- Configuración necesaria

---

## 💻 Paso 2 (2 minutos): Ver un Ejemplo

Copia este código en tu aplicación:

```java
// Crear servicio (una sola vez)
GameEventService eventService = 
    AppConfig.createGameEventService("https://your-project.firebaseio.com");

String gameId = "game-123";

// 1. Cuando comienza la partida
GameDto game = new GameDto(
    gameId, "Mi Partida",
    Arrays.asList("Player1", "Player2"),
    "IN_PROGRESS",
    System.currentTimeMillis()
);
eventService.notifyGameStart(gameId, game);

// 2. Cuando hay un movimiento
MoveData move = new MoveData("player1", "KICK", new Position(5, 8));
eventService.notifyGameMove(gameId, move);

// 3. Cuando termina la partida
game.setStatus("FINISHED");
eventService.notifyGameEnd(gameId, game);

// 4. Al finalizar la aplicación
eventService.shutdown();
```

---

## ✅ Paso 3 (1 minuto): Ejecutar los Tests

```bash
mvn test
```

**Resultado esperado:**
```
✅ BUILD SUCCESS
18 tests passed
```

---

## 📚 Documentos Principales

| Archivo | Tiempo | Propósito |
|---------|--------|----------|
| **QUICK_REFERENCE.md** | 5 min | Referencia rápida |
| **INTEGRATION_COMPLETE.md** | 15 min | Entender arquitectura |
| **WIREMOCK_STUBS_GUIDE.md** | 10 min | Cómo hacer tests |

---

## 🎯 Acciones Inmediatas

### Para Usar en tu Código
1. Inyecta `GameEventService` en tu servicio
2. Llama `notifyGameStart()`, `notifyGameMove()`, `notifyGameEnd()`
3. Listo!

### Para Escribir Tests
1. Usa `@ExtendWith(WireMockExtension.class)`
2. Llama `GameEventWireMockStubs.stubAllGameEvents(gameId)`
3. Verifica con `GameEventWireMockStubs.verifyEventRequest("game.start")`

### Para Entender Cómo Funciona
1. Lee `INTEGRATION_COMPLETE.md`
2. Revisa `GameEventRepository.java`
3. Revisa `GameEventServiceImpl.java`

---

## 🔧 Configuración (Si es necesaria)

En `src/main/resources/application.properties`:

```properties
# Tu Firebase URL
firebase.rtdb.url=https://tu-proyecto.firebaseio.com

# Eventos (opcional - valores por defecto están OK)
game.events.enabled=true
game.events.database-path=game_events
game.events.async-processing=true
game.events.executor-threads=2
```

---

## 🎓 Estructura de Eventos

Los eventos se almacenan así en Firebase:

```json
{
  "game_events": {
    "-MX123": {
      "type": "game.start",
      "gameId": "game-123",
      "timestamp": 1704067200000,
      "payload": {...}
    }
  }
}
```

**Tipos:**
- `game.start` - Cuando comienza
- `game.move` - Cuando hay movimiento
- `game.end` - Cuando termina

---

## 📁 Dónde está todo

```
Código:
├── GameEventRepository.java
│   src/main/java/es/iesquevedo/repository/firebase/
├── GameEventService.java
│   src/main/java/es/iesquevedo/service/
├── GameEventServiceImpl.java
│   src/main/java/es/iesquevedo/service/impl/

Tests:
├── GameEventRepositoryTest.java
├── GameEventServiceImplTest.java
├── GameEventIntegrationTest.java
├── GameEventWireMockStubs.java
   src/test/java/es/iesquevedo/...

Documentación:
├── START_HERE.md
├── QUICK_REFERENCE.md
├── doc/INTEGRATION_COMPLETE.md
├── doc/WIREMOCK_STUBS_GUIDE.md
   (Este archivo y otros)
```

---

## 🧪 Para Tests

### Test Unitario
```java
@Mock
private FirebaseDatabase firebaseDatabase;

GameEventRepository repo = new GameEventRepository(firebaseDatabase);
repo.recordGameStart("game-id", gameDto);
```

### Test de Integración
```java
@ExtendWith(WireMockExtension.class)
class MyTest {
    @Test
    void test() {
        GameEventWireMockStubs.stubGameStart("game-id");
        // Tu código
        GameEventWireMockStubs.verifyEventRequest("game.start");
    }
}
```

---

## 🚀 Flujo Típico

```
1. Crear servicio
   ↓
2. notifyGameStart()
   ↓
3. notifyGameMove() × N
   ↓
4. notifyGameEnd()
   ↓
5. shutdown()
```

---

## ❓ Preguntas Rápidas

### ¿Cómo uso el servicio?
→ `QUICK_REFERENCE.md` o mira el ejemplo arriba

### ¿Cómo hago tests?
→ `WIREMOCK_STUBS_GUIDE.md`

### ¿Cómo entiendo el código?
→ `INTEGRATION_COMPLETE.md`

### ¿Dónde está X archivo?
→ `INDEX.md`

### ¿Está todo correcto?
→ Ejecuta `mvn test` - Si ves ✅ BUILD SUCCESS, todo está bien

---

## ✅ Checklist Rápido

- ✅ ¿Existe GameEventRepository.java?
- ✅ ¿Existe GameEventService.java?
- ✅ ¿Existe GameEventServiceImpl.java?
- ✅ ¿Los tests pasan? (`mvn test`)
- ✅ ¿Está la documentación?

Si todo está ✅, ¡estás listo!

---

## 📞 Si Algo No Funciona

### Error de compilación
→ Verifica que los archivos existan en las ubicaciones correctas

### Tests fallan
→ Asegúrate de que el puerto 8080 no esté en uso

### Conexión a Firebase falla
→ Verifica que `firebase.rtdb.url` es correcta en `application.properties`

---

## 🎉 ¡Listo!

Tu sistema de eventos está **completamente integrado y listo para usar**.

**Próximo paso:**
1. Abre `QUICK_REFERENCE.md`
2. Copia el código de ejemplo
3. Inyecta el servicio en tu controller
4. ¡Usa los eventos!

---

**Tiempo total:** 5 minutos ⏱️
**Status:** ✅ LISTO
**Pregunta:** ¿Alguna duda? Revisa la documentación 📚

---

**¡Bienvenido a tu nuevo sistema de eventos!** 🚀

