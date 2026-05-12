# Casos de Prueba — E4-US2

**Proyecto:** Inazuma Go
**Historia:** E4-US2 — Tests de integración con stubs Firebase
**Herramienta:** WireMock (simula Firebase RTDB REST API)
**Fecha:** Mayo 2026

---

## TC-01: Componentes principales — Saludo + Login

| Campo | Detalle |
|-------|---------|
| **ID** | TC-01 |
| **Tipo** | Integración |
| **Componentes** | AuthService, AppState |

**Precondiciones:**
- AuthService disponible con credenciales mock

**Pasos:**
1. Llamar a `authService.login("user@test.com", "pass123")`
2. Guardar token en `AppState`
3. Verificar estado de login

**Resultado esperado:**
- Token no es null
- `AppState.isLoggedIn()` devuelve `true`
- Email guardado correctamente

**Resultado:** PASS ✅

---

## TC-02: PATCH multi-path exitoso (200) → flujo optimistic → confirmed

| Campo | Detalle |
|-------|---------|
| **ID** | TC-02 |
| **Tipo** | Integración |
| **Componentes** | FirebaseMainRepository, WireMock |

**Precondiciones:**
- WireMock configurado para responder 200 a PATCH `/.json`

**Pasos:**
1. Configurar WireMock: `PATCH /.json → 200 {}`
2. Construir mapa de updates multi-path
3. Llamar a `writeMovesMultiPath(updates, token)`
4. Verificar que WireMock recibió la petición

**Resultado esperado:**
- No se lanza excepción
- WireMock confirma que recibió el PATCH
- Flujo optimistic queda como confirmed

**Resultado:** PASS ✅

---

## TC-03: Rechazo por reglas (403) → rollback en cliente

| Campo | Detalle |
|-------|---------|
| **ID** | TC-03 |
| **Tipo** | Integración |
| **Componentes** | FirebaseMainRepository, WireMock |

**Precondiciones:**
- WireMock configurado para responder 403 a PATCH `/.json`

**Pasos:**
1. Configurar WireMock: `PATCH /.json → 403`
2. Registrar estado antes del intento (`MOVE_PENDING`)
3. Llamar a `writeMovesMultiPath(updates, token-sin-permisos)`
4. Capturar excepción

**Resultado esperado:**
- Se lanza `RuntimeException` con mensaje que contiene "403"
- El estado del cliente vuelve al anterior (rollback)

**Resultado:** PASS ✅

---

## TC-04: Conflicto concurrente → uno aceptado, otro rechazado → reconciliación

| Campo | Detalle |
|-------|---------|
| **ID** | TC-04 |
| **Tipo** | Integración concurrente |
| **Componentes** | FirebaseMainRepository, WireMock, ExecutorService |

**Precondiciones:**
- WireMock configurado para aceptar player1 (200) y rechazar player2 (403)

**Pasos:**
1. Lanzar dos threads simultáneos con writes al mismo path
2. Thread 1 (player1): envía move con body que contiene "player1" → 200
3. Thread 2 (player2): envía move con body que contiene "player2" → 403
4. Esperar resultado de ambos threads
5. Verificar reconciliación

**Resultado esperado:**
- Thread 1 completa sin excepción
- Thread 2 lanza `RuntimeException`
- Solo el write de player1 queda confirmado

**Resultado:** PASS ✅

---

## TC-05: Dedupe por clientNonce → reintentos no crean duplicados

| Campo | Detalle |
|-------|---------|
| **ID** | TC-05 |
| **Tipo** | Integración |
| **Componentes** | FirebaseMainRepository, WireMock |

**Precondiciones:**
- WireMock configurado para responder 200 a PATCH con nonce específico

**Pasos:**
1. Generar `clientNonce = "nonce-abc-123"`
2. Enviar el mismo move dos veces con el mismo nonce
3. Verificar que WireMock recibió exactamente 2 peticiones con ese nonce
4. Verificar que no se crearon duplicados en el estado

**Resultado esperado:**
- Ambas peticiones llegan con el mismo nonce
- El sistema identifica el reintento y no duplica el estado
- WireMock registra exactamente 2 llamadas

**Resultado:** PASS ✅

---

## Resumen

| ID | Descripción | Tipo | Resultado |
|----|-------------|------|-----------|
| TC-01 | Saludo + Login con mock | Integración | ✅ PASS |
| TC-02 | PATCH exitoso 200, flujo optimistic | Integración | ✅ PASS |
| TC-03 | Rechazo 403, rollback cliente | Integración | ✅ PASS |
| TC-04 | Conflicto concurrente, reconciliación | Concurrente | ✅ PASS |
| TC-05 | Dedupe por clientNonce | Integración | ✅ PASS |

---

## Notas técnicas

- Todos los tests usan **WireMock** en puerto 8089
- No se requiere conexión a Firebase real
- Reportes generados en `target/surefire-reports/`
- Ejecutar con: `mvn -DskipTests=false test`
