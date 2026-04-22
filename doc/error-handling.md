# Patrón de Manejo de Errores

**Versión:** 1.0  
**Última actualización:** 2026-04-15

## Tabla de Contenidos

1. [Introducción](#introducción)
2. [Estructura de Errores](#estructura-de-errores)
3. [Excepciones Personalizadas](#excepciones-personalizadas)
4. [Ejemplos de Payload](#ejemplos-de-payload)
5. [Mapeo de Excepciones](#mapeo-de-excepciones)

---

## Introducción

El patrón de manejo de errores centralizado en InazumaGo utiliza una clase `ApiError` que estandariza las respuestas de error en toda la aplicación. Este patrón permite:

- **Consistencia**: Todas las respuestas de error siguen un formato único.
- **Legibilidad**: Clientes reciben información clara sobre qué salió mal.
- **Debugging**: Códigos de error facilitan la identificación de problemas.

---

## Estructura de Errores

### Clase `ApiError`

Ubicación: `es.iesquevedo.exception.ApiError`

```java
public class ApiError {
    private final String message;  // Descripción legible del error
    private final int code;        // Código numérico de error
}
```

**Atributos:**
- `message`: Texto descriptivo que explica qué ocurrió (p. ej., "Recurso no encontrado").
- `code`: Código entero que identifica el tipo de error (p. ej., 404, 403, 500).

---

## Excepciones Personalizadas

### `NotFoundException`

Ubicación: `es.iesquevedo.exception.NotFoundException`

Se lanza cuando se intenta acceder a un recurso que no existe.

**Uso:**
```java
if (gameId == null || gameId.isEmpty()) {
    throw new NotFoundException("Juego con ID " + gameId + " no encontrado");
}
```

**Mapeo a ApiError:**
- Código: `404`
- Mensaje: Se toma del mensaje de la excepción

---

## Ejemplos de Payload

### 1. Recurso No Encontrado

**Escenario:** Se solicita un juego que no existe.

**Solicitud HTTP:**
```http
GET /game/invalid-game-id
```

**Respuesta (404):**
```json
{
  "code": 404,
  "message": "Juego con ID invalid-game-id no encontrado"
}
```

---

### 2. Error Interno del Servidor

**Escenario:** Error inesperado durante el procesamiento.

**Solicitud HTTP:**
```http
POST /move
Content-Type: application/json

{
  "gameId": "game-123",
  "moveData": { "from": "a1", "to": "a2" }
}
```

**Respuesta (500):**
```json
{
  "code": 500,
  "message": "Error interno del servidor. Por favor, intente más tarde."
}
```

---

### 3. Validación Fallida (PATCH Multi-Path)

**Escenario:** Move rechazado por reglas de seguridad (fuera de turno).

**Solicitud HTTP:**
```http
PATCH /game/game-123/moves
Content-Type: application/json
Authorization: Bearer token_xyz

{
  "move": {
    "moveId": "move-001",
    "clientNonce": "nonce-123",
    "clientTs": 1708524000000,
    "payload": { "from": "a1", "to": "a2" }
  }
}
```

**Respuesta (403):**
```json
{
  "code": 403,
  "message": "Movimiento rechazado: no es tu turno"
}
```

---

### 4. Sin Autenticación

**Escenario:** Solicitud sin token válido.

**Solicitud HTTP:**
```http
PATCH /game/game-123/moves
(sin header Authorization)
```

**Respuesta (401):**
```json
{
  "code": 401,
  "message": "Autenticación requerida"
}
```

---

## Mapeo de Excepciones

El siguiente tabla describe cómo se mapean las excepciones a códigos de error HTTP:

| Excepción | Código HTTP | Mensaje Típico |
|-----------|------------|----------------|
| `NotFoundException` | 404 | "Recurso no encontrado" |
| `UnauthorizedException` | 401 | "Autenticación requerida" |
| `ForbiddenException` | 403 | "Movimiento rechazado: ..." |
| `Exception` (genérica) | 500 | "Error interno del servidor" |

---

## Notas para Desarrollo

- **Mensajes legibles**: Los mensajes deben ser claros y ayudar al usuario/cliente a entender qué hizo mal.
- **Códigos estándar HTTP**: Utilizar códigos HTTP estandarizados (401, 403, 404, 500) para máxima compatibilidad.
- **Logging**: Registrar excepciones con stack trace para debugging.
- **No exponer detalles internos**: Evitar devolver información sensible o de implementación en el mensaje de error.

---

## Enlaces Relacionados

- `src/main/java/es/iesquevedo/exception/` — Clases de excepción
- `src/main/java/es/iesquevedo/controller/` — Controladores que lanzan excepciones

