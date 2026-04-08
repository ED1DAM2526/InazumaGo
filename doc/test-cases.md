
---

## `doc/test-cases.md`

```markdown
# Casos de prueba de integración

## Escenarios de prueba

| ID | Escenario | Descripción | Resultado esperado |
|----|-----------|-------------|-------------------|
| TC-1 | PATCH multi-path exitoso | El cliente envía un movimiento con optimistic update | Firebase devuelve 200, movimiento confirmado |
| TC-2 | Rechazo por reglas | El movimiento viola las reglas del juego (turno incorrecto) | Firebase devuelve 403, el cliente hace rollback |
| TC-3 | Escrituras concurrentes | Dos jugadores intentan mover simultáneamente | Uno aceptado (200), otro rechazado (409), reconciliación se dispara |
| TC-4 | Deduplicación por nonce | Reintento del mismo movimiento con el mismo clientNonce | Segunda petición ignorada, no se crean duplicados |

## Cómo ejecutar los tests de integración

```powershell
mvn test