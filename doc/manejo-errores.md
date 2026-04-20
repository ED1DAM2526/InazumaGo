# Patrón de manejo de errores

Versión: 1.0  
Fecha: 2026-04-14

## Objetivo

Estandarizar las respuestas de error de la aplicación con un formato común para UI y logs.

## Modelo de respuesta

Clase: `es.iesquevedo.exception.ApiError`

Campos:
- `code`: código numérico del error.
- `message`: mensaje legible para mostrar o registrar.

Ejemplo:

```json
{
  "code": 404,
  "message": "Default player name not found"
}
```

## Mapeo de excepciones

Clase: `es.iesquevedo.exception.MainErrorHandler`

Reglas actuales:
- `NotFoundException` -> `ApiError(404, mensaje original)`
- Cualquier otra `RuntimeException` -> `ApiError(500, "Internal error")`

## Flujo donde se aplica

- `MainServiceImpl.greet()` lanza `NotFoundException` si el repositorio no devuelve nombre por defecto.
- `MainController.initialize()` captura errores de runtime y muestra el formato estándar generado por `MainErrorHandler`.
