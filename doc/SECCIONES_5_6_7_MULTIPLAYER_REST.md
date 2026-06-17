# Secciones 5, 6 y 7 — Firebase Realtime Database REST + Java (OkHttp/Gson)

## Objetivo

Este documento resume cómo implementar el flujo multijugador usando **Firebase Realtime Database REST API** desde una app cliente Java (JavaFX) con **OkHttp** y **Gson**, sin usar `firebase-admin`.

---

## 5. REAL-TIME LISTENERS

### Opciones disponibles

#### A) SSE (Server-Sent Events)
- En Firebase RTDB REST **no es la opción ideal** para un cliente Java puro.
- Firebase RTDB REST está pensado principalmente para **GET/PUT/PATCH/DELETE**.
- Si quieres un listener real, normalmente se usa el **SDK oficial** (no REST puro).

#### B) Polling con `GET` periódico
- Es la opción **más práctica y recomendada** para una app cliente JavaFX que usa REST puro.
- Ventajas:
  - Simple de implementar
  - Funciona con OkHttp/Gson
  - Control total del intervalo
  - Fácil de depurar
- Inconveniente:
  - Más latencia que un listener real
  - Más llamadas HTTP

#### C) Polling con `ETag` / `If-None-Match`
- Mejor que polling simple si quieres reducir tráfico.
- Firebase RTDB REST puede devolver cabecera `ETag` en algunas respuestas.
- Útil para comprobar si un recurso cambió sin descargar todo el JSON.

### Recomendación

**Recomendación para un juego multijugador usando REST API:**

> **Polling con `GET` + `ETag` cuando sea posible**

Si quieres algo más robusto y fácil de mantener, usa:
- `GET` cada 300–1000 ms
- Detecta cambios en `status`, `moves`, `whitePlayer`, etc.
- Si el juego es por turnos, una latencia de 500 ms suele ser aceptable.

### Pseudocódigo OkHttp recomendado

```java
private volatile boolean running = true;
private String lastEtag = null;

private void pollGame(String gameId) {
    while (running) {
        try {
            String url = firebaseUrl + "/games/" + gameId + ".json?auth=" + idToken;

            Request.Builder builder = new Request.Builder()
                    .url(url)
                    .get();

            if (lastEtag != null) {
                builder.addHeader("If-None-Match", lastEtag);
            }

            Request request = builder.build();
            System.out.println("URL de polling: " + request.url());

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.code() == 304) {
                    // No hay cambios
                    Thread.sleep(500);
                    continue;
                }

                if (!response.isSuccessful() || response.body() == null) {
                    // Manejar error HTTP
                    break;
                }

                String body = response.body().string();
                String etag = response.header("ETag");
                if (etag != null) {
                    lastEtag = etag;
                }

                GameDto game = gson.fromJson(body, GameDto.class);
                if (game != null) {
                    // actualizar UI / comprobar moves / turnos
                }
            }

            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            break;
        } catch (Exception e) {
            // backoff / reconexión
            break;
        }
    }
}
```

### Cuándo usarlo
- partida por turnos
- matchmaking
- sincronización de estado de juego
- detección de oponente unido / partida lista

---

## 6. AUTENTICACIÓN EN PETICIONES

### Cómo pasar `idToken` en RTDB REST API

Para Firebase Realtime Database REST, en cliente Java con REST puro, la forma más segura y directa es:

```text
https://<tu-proyecto>.firebaseio.com/games.json?auth=<idToken>
```

Ejemplo:

```text
https://inazumago-default-rtdb.firebaseio.com/games.json?auth=eyJhbGciOiJSUzI1NiIs...
```

### `Authorization: Bearer {idToken}` vs `?auth={idToken}`

#### `Authorization: Bearer ...`
- Se usa mucho en APIs REST genéricas.
- Puede funcionar en algunos contextos, pero **no es la forma más simple ni la más clara para RTDB REST**.
- En Firebase RTDB REST, lo normal es usar `?auth=`.

#### `?auth={idToken}`
- Es la forma más común para RTDB REST.
- Fácil de depurar.
- Te permite ver de inmediato si el token llegó o no.
- Es la que recomiendo en tu caso.

### Reglas prácticas

1. **Siempre imprime la URL final antes de ejecutar la petición**.
2. **Verifica que `idToken` no sea `null` ni vacío**.
3. **Si sale 401, revisa la URL real completa**.

### Ejemplo de logging antes de la petición

```java
String url = firebaseUrl + "/games.json?auth=" + idToken;
Request request = new Request.Builder()
        .url(url)
        .post(body)
        .build();

System.out.println("URL de createGame: " + request.url());
```

### Manejo de tokens expirados

El `idToken` de Firebase Auth dura aproximadamente **1 hora**.

#### Estrategia recomendada
- Guardar en memoria:
  - `idToken`
  - `refreshToken`
  - `tokenExpirationTime`
- Antes de cada request importante:
  - comprobar si el token está por expirar
  - refrescar si faltan pocos minutos

### Detección de expiración

```java
private boolean isTokenExpiring() {
    long timeUntilExpiry = tokenExpirationTime - System.currentTimeMillis();
    return timeUntilExpiry < (5 * 60 * 1000); // 5 minutos
}
```

### Refresh del token con Firebase Auth REST

Endpoint:

```text
https://securetoken.googleapis.com/v1/token?key=TU_API_KEY
```

Ejemplo de petición:

```java
JsonObject body = new JsonObject();
body.addProperty("grant_type", "refresh_token");
body.addProperty("refresh_token", refreshToken);

Request request = new Request.Builder()
        .url("https://securetoken.googleapis.com/v1/token?key=" + API_KEY)
        .post(RequestBody.create(gson.toJson(body), MediaType.get("application/json")))
        .build();

System.out.println("URL de refresh token: " + request.url());
```

Respuesta típica:

```json
{
  "access_token": "...",
  "expires_in": "3600",
  "token_type": "Bearer",
  "refresh_token": "...",
  "id_token": "...",
  "user_id": "...",
  "project_id": "..."
}
```

### Buenas prácticas para `AppState`

Guardar ahí:
- `authToken` o `idToken`
- `refreshToken`
- `currentUserId`
- `currentUserEmail`
- `tokenExpirationTime`

Ventajas:
- acceso fácil desde controllers
- estado global centralizado
- útil para logout y refresco

### Recomendación clave

- `idToken` → úsalo en cada request REST
- `refreshToken` → úsalo solo para renovar sesión
- no mezcles Firebase Admin SDK con REST cliente

---

## 7. FLUJO MULTIPLAYER

### Detección robusta de desconexiones

Con REST puro no tienes presencia real tipo SDK, así que la forma práctica es combinar:

1. **Heartbeat / lastSeen**
2. **Polling del estado de la partida**
3. **Timeout de inactividad**

### Estrategia recomendada

En el nodo de juego puedes guardar campos como:
- `status`
- `players`
- `lastSeenBlack`
- `lastSeenWhite`
- `updatedAt`

Ejemplo:

```json
{
  "status": "IN_PROGRESS",
  "blackPlayer": "uid1",
  "whitePlayer": "uid2",
  "lastSeenBlack": 1710000000000,
  "lastSeenWhite": 1710000005000,
  "updatedAt": 1710000005000
}
```

### Heartbeat

Cada jugador puede actualizar su `lastSeen` cada pocos segundos.

```java
private void sendHeartbeat(String gameId, String playerRole) {
    long now = System.currentTimeMillis();

    JsonObject updates = new JsonObject();
    updates.addProperty("updatedAt", now);
    updates.addProperty(playerRole.equals("BLACK") ? "lastSeenBlack" : "lastSeenWhite", now);

    Request request = new Request.Builder()
            .url(firebaseUrl + "/games/" + gameId + ".json?auth=" + idToken)
            .patch(RequestBody.create(gson.toJson(updates), MediaType.get("application/json")))
            .build();

    System.out.println("URL de heartbeat: " + request.url());
}
```

### Detectar desconexión

En el polling:
- si `lastSeen` es demasiado antiguo (por ejemplo > 10–15 s)
- marcar jugador como desconectado
- decidir si la partida se cancela, se pausa o se espera reconexión

### Ejemplo lógico

```java
boolean isDisconnected(long lastSeen) {
    return System.currentTimeMillis() - lastSeen > 15000;
}
```

### Eliminar una partida cuando termina o se cancela

Usa `DELETE` sobre la ruta del juego:

```java
String url = firebaseUrl + "/games/" + gameId + ".json?auth=" + idToken;

Request request = new Request.Builder()
        .url(url)
        .delete()
        .build();

System.out.println("URL de deleteGame: " + request.url());

try (Response response = httpClient.newCall(request).execute()) {
    if (!response.isSuccessful()) {
        throw new IOException("HTTP " + response.code());
    }
}
```

### Cuándo borrar la partida
- cuando termina
- cuando un jugador cancela
- cuando el otro no responde tras timeout
- cuando hay logout forzado

### Flujo recomendado de cierre

```text
1. Detener listeners/polling
2. Marcar partida como FINISHED si aplica
3. Hacer DELETE si ya no se necesita historial
4. Limpiar AppState
5. Logout
```

---

## Conclusión rápida

### Para tu caso:
- **Listener recomendado**: polling con `GET` + `ETag` si quieres reducir tráfico
- **Autenticación recomendada**: `?auth=<idToken>` en la URL
- **Refresh**: usa `refreshToken` contra `securetoken.googleapis.com`
- **Presencia**: heartbeat con `lastSeen`
- **Eliminar partida**: `DELETE /games/{gameId}.json?auth=<idToken>`

---

## Nota final de depuración

Si sigue apareciendo 401:
1. imprime la URL completa
2. verifica que `idToken` no sea `null`
3. confirma que la petición lleva `?auth=`
4. comprueba si el token está caducado
5. revisa las rules de Firebase

