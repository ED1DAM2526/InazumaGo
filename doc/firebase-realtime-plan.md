# Plan técnico: Sincronización en tiempo real y persistencia (Firebase Realtime Database)

Propósito
- Documento técnico que describe la arquitectura, modelo de datos, flujo de eventos, reglas de seguridad, librerías recomendadas y checklist priorizado para implementar sincronización de movimientos de tablero en tiempo real usando Firebase Realtime Database como única capa de persistencia y transporte.

Resumen ejecutivo
- Enfoque: Usar Firebase RTDB como única fuente de verdad y transporte. Cada movimiento es un delta (payload mínimo) persistido con una operación atómica multi-path (PATCH) que escribe el movimiento y actualiza `meta.turn`/`meta.lastMoveId` en la misma operación.
- Objetivo: minimizar latencia entregando solo cambios relevantes a cada jugador y asegurar consistencia mediante reglas de seguridad y transacciones atomizadas.

1) Modelo de datos (estructura sugerida)
- /games/{gameId}/meta:
  - gameId: string
  - players: { <uid>: true }
  - turn: integer
  - lastMoveId: string
  - nextSeq?: integer
  - stateHash?: string
  - createdAt, updatedAt: timestamps

- /games/{gameId}/moves/{moveId}:
  - moveId: string
  - seq?: integer
  - playerId: string
  - turnNumber: integer
  - payload: { from, to, piece, extras? }
  - clientNonce: string
  - clientTs: long
  - serverTs: long (provisionado por serverTimestamp)
  - status: "pending" | "confirmed" | "rejected"
  - reason?: string

- /presence/{gameId}/{playerId}:
  - connected: boolean
  - lastSeen: timestamp

- /gamesIndex/{userId}/{gameId}: true

Ejemplo de movimiento (compacto):
{ "moveId":"uuid-123","playerId":"uid_abc","turnNumber":5,"payload":{"from":"e2","to":"e4","piece":"P"},"clientNonce":"nonce-123","clientTs":1670000000000,"status":"pending" }

Notas:
- Mantener payload mínimo (referencias compactas). Guardar clientTs y serverTs para ordenación y reconciliación.
- `clientNonce` evita duplicados y permite idempotencia en reintentos.

2) Flujo de evento para un movimiento
1. Validación local en cliente (MoveService.validateLocal).
2. Generar `moveId` (UUID) y `clientNonce`.
3. Construir multi-path PATCH que incluya:
   - /games/{gameId}/moves/{moveId} = moveObj
   - /games/{gameId}/meta/turn = nextTurn
   - /games/{gameId}/meta/lastMoveId = moveId
   - /games/{gameId}/meta/updatedAt = serverTimestamp
4. Ejecutar PATCH atómico hacia `/games/{gameId}.json?auth=<idToken>`.
5. Firebase aplica la operación; si las reglas la permiten, la escritura persiste atómicamente.
6. Clientes suscritos (child_added en moves y child_changed en meta) reciben los cambios y aplican/reconcilian.
7. Si la escritura es rechazada por reglas (p. ej. out-of-turn), el cliente hace rollback y presenta UI/alert.

Secuencia textual (movimiento exitoso):
Cliente -> validación local -> Firebase PATCH multi-path -> DB escribe -> Clientes reciben child_added/meta change -> UI actualiza

Secuencia textual (conflicto concurrente):
Dos clientes envían PATCHs simultáneos; Firebase acepta la primera que pase las reglas; la segunda es rechazada (error 403) y debe reconciliar (rollback o reintento tras obtener meta actualizado).

3) Mecanismo de consistencia y ordenación
- Usar multi-path updates para hacer atómica la inserción del move y la actualización de `meta.turn`.
- Reglas de seguridad validan que solo el jugador con `auth.uid` igual a `playerId` y que coincida con `meta.turn` puede avanzar el turno.
- Usar `clientNonce` y validación de duplicados para evitar replay.
- Opcional: `meta.nextSeq` para secuencias estrictas; cliente lee meta y propone seq = meta.nextSeq+1 dentro de la misma multi-path.
- Fallback: si se requiere mayor garantía, implementar Cloud Function server-side que haga transacción segura.

4) Reglas de seguridad (pseudocódigo)
- Resumen: solo jugadores autorizados pueden leer la partida; solo el jugador al que le toca puede crear un `move` que avance `meta.turn`.

Pseudocódigo de reglas (RTDB):

```json
{
  "rules": {
    "games": {
      "$gameId": {
        ".read": "auth != null && root.child('games').child($gameId).child('meta').child('players').child(auth.uid).exists()",
        ".write": "false",
        "meta": {
          ".write": "(newData.child('turn').val() == data.child('turn').val() + 1) && root.child('games').child($gameId).child('moves').child(newData.child('lastMoveId').val()).exists() && root.child('games').child($gameId).child('moves').child(newData.child('lastMoveId').val()).child('playerId').val() == auth.uid"
        },
        "moves": {
          "$moveId": {
            ".write": "auth != null && newData.child('playerId').val() == auth.uid && newData.child('turnNumber').val() == data.parent().parent().child('meta').child('turn').val()"
          }
        }
      }
    }
  }
}
```

Notas:
- Las reglas anteriores son pseudocódigo: ajusta detalles sintácticos según la consola de Firebase.
- Se puede añadir verificación de `clientNonce` para evitar duplicados.

5) Librerías recomendadas (maven)
- com.squareup.okhttp3:okhttp:4.11.0 — cliente HTTP, control de timeouts y reintentos.
- com.fasterxml.jackson.core:jackson-databind:2.15.2 — serialización JSON.
- org.jboss.weld.se:weld-se-core:5.1.0.Final — DI (Weld) para Java SE.
- org.slf4j:slf4j-api:2.0.7 y ch.qos.logback:logback-classic:1.4.7 — logging.
- org.junit.jupiter:junit-jupiter:5.10.0, org.mockito:mockito-core:5.5.0, com.github.tomakehurst:wiremock-jre8:2.35.0 — testing/mocking.

6) Clases / interfaces sugeridas (ubicación y responsabilidad)
- es.iesquevedo.model: Game, Move, Presence
- es.iesquevedo.dto: MoveDto, GameDto, FirebaseWriteResultDto
- es.iesquevedo.repository.firebase:
  - FirebaseHttpClient (GET/PATCH/PUT/DELETE, token injector, retries)
  - FirebaseGameRepository (interface)
  - FirebaseGameRepositoryImpl (implementación: PATCH multi-path y streaming opcional)
- es.iesquevedo.service:
  - MoveService (validación local, apply prediction, persist, reconcile)
  - GameService (estado del juego y reconciliación)
  - PresenceService (actualizar y escuchar presencia)
- es.iesquevedo.util: IdGenerator, JsonMapper
- UI: MainController / GameController (aplicar optimistic update y mostrar estado pending)

7) Estrategia de pruebas
- Unitarias: MoveService.validateLocal, serialización, IdGenerator.
- Integración (sin Firebase real): WireMock para simular la API REST de RTDB, escenarios: success (200), rejected (403), reintentos y duplicados.
- Tests que simulan conflictos: dos peticiones simultáneas; verificar que el cliente que recibe rechazo hace rollback.

8) Optimización de latencia
- Payload mínimo (deltas). Listeners en `child_added` para moves.
- Predicción + rollback: aplicar move inmediatamente y marcar como pending.
- Mantener solo last N moves en memoria (cache) para reconciliación.
- Agregar pings/keep-alive en OkHttp si se usa streaming.

9) Riesgos y mitigaciones
- Desconexión en medio de write: cola local + reintento con same clientNonce.
- Reordenamiento de eventos: ordenar por serverTs o push-key.
- Clientes maliciosos: reglas estrictas y validación server-side si hace falta.

10) Checklist priorizado (MVP)
1. Documentación técnica (`doc/firebase-realtime-plan.md`) — criterios: documento revisado en repo. (0.5–1 h).
2. Modelos y JsonMapper + tests (2–4 h).
3. FirebaseHttpClient + FirebaseGameRepositoryImpl.writeMoveMultiPath + test WireMock (8–12 h).
4. MoveService con optimistic apply + tests (8–12 h).
5. Reglas de seguridad documentadas y ejemplo (1–2 h).

Siguientes pasos recomendados
- Implementar modelos + JsonMapper y tests unitarios.
- Implementar FirebaseHttpClient y escribir test con WireMock que demuestre la PATCH multi-path atómica.
- Implementar MoveService y validar casos de conflicto con tests.

Referencias y decisiones operativas
- No incluir service account JSON ni claves en el repositorio.
- Usar Firebase Authentication en el cliente (idToken). Para operaciones privilegiadas, crear backend separado.

---