
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


## Uso del repositorio en memoria para pruebas

El repositorio `InMemoryMainRepository` se utiliza para pruebas sin depender de Firebase.

### Ejemplo de configuración en un test

```java
import es.iesquevedo.config.AppConfig;
import es.iesquevedo.controller.MainController;
import es.iesquevedo.repository.MainRepository;
import es.iesquevedo.service.impl.MainServiceImpl;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class EjemploTest {

    @Test
    void testConRepositorioEnMemoria() {
        // Crear repositorio en memoria (no necesita Firebase)
        MainRepository repo = AppConfig.createInMemoryRepository();
        
        // Inyectar repositorio en el servicio
        MainServiceImpl service = new MainServiceImpl(repo);
        
        // Crear controlador con el servicio
        MainController controller = new MainController(service);
        
        // Ejecutar método a probar
        String resultado = controller.greet();
        
        // Verificar resultado
        assertEquals("Hello, InazumaGoPrevio!", resultado);
    }
}