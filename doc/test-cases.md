doc-test-cases
# Casos de Prueba de Integración - InazumaGo

**Documento:** `doc/test-cases.md`  
**Fecha Creación:** 2026-04-09  
**Versión:** 1.0  
**Estado:** Activo

---

## 🎯 Objetivo General

Este documento describe los casos de prueba de integración para el proyecto InazumaGo. Los casos de integración verifican que los componentes del sistema funcionen correctamente cuando se integran entre sí, asegurando que la comunicación entre capas (UI, Controladores, Servicios, Repositorios, etc.) sea correcta y robusta.

Los casos se basan en la arquitectura cliente de escritorio JavaFX con persistencia en Firebase Realtime Database.

---

## 📋 Alcance

| Aspecto | Detalle |
|---------|---------|
| **Componentes Probados** | UI, Controladores, Servicios, Repositorios, DTOs, Mappers, Configuración, Utilidades |
| **Plataformas** | Windows, Linux, macOS (con Java 21+) |
| **Tipo de Prueba** | Integración (componentes combinados) |
| **Herramientas** | JUnit, Mockito, Maven, Firebase RTDB (simulado) |

---

## 🔧 Prerrequisitos Generales

| Requisito | Versión | Mandatorio |
|-----------|---------|-----------|
| **JDK** | 21 o superior | ✅ Sí |
| **Maven** | 3.6+ | ✅ Sí |
| **JUnit** | 5.10+ | ✅ Sí |
| **Mockito** | 5.0+ | Para mocks |
| **Firebase RTDB** | Simulado | Para pruebas |

### 📦 Configuración de Pruebas

```java
// Configuración típica para pruebas de integración
@SpringBootTest  // Si se usa Spring (no recomendado para cliente)
@ExtendWith(MockitoExtension.class)
public class IntegrationTest {
    // Configurar mocks y dependencias
}
```

---

## 🧪 CASOS DE PRUEBA DE INTEGRACIÓN

### **CI-001: Integración UI-Controller-Service**

**ID Caso:** CI-001  
**Nombre:** Integración entre UI, Controlador y Servicio  
**Prioridad:** Alta  
**Componentes Involucrados:**
- `UIAdapter` (UI)
- `MainController` (Controlador)
- `MainService` (Servicio)
- `MainServiceImpl` (Implementación)

**Descripción:**  
Verifica que la capa de UI pueda comunicarse correctamente con el controlador, y que el controlador delegue apropiadamente al servicio para obtener datos.

**Pasos de Verificación:**

1. **Configurar Dependencias:**
   ```java
   // Crear servicio
   MainRepository repository = new InMemoryMainRepository();
   MainService service = new MainServiceImpl(repository);
   
   // Crear controlador
   MainController controller = new MainController(service);
   
   // Crear adaptador UI
   UIAdapter uiAdapter = new UIAdapter(controller);
   ```

2. **Ejecutar Método de Integración:**
   ```java
   String result = uiAdapter.greet();
   ```

3. **Verificar Resultado:**
   - El resultado debe ser `"Hello, InazumaGoPrevio!"`
   - No debe haber excepciones
   - El flujo debe pasar por: UIAdapter → MainController → MainService → MainRepository

**Resultados Esperados:**
- ✅ `result.equals("Hello, InazumaGoPrevio!")`
- ✅ No `NullPointerException`
- ✅ Logs muestran ejecución correcta
- ✅ Tiempo de ejecución < 100ms

**Posibles Fallos:**
- `NullPointerException` si servicio no está inyectado
- `IllegalArgumentException` si parámetros inválidos
- Error si `MainRepository.findDefaultName()` retorna null

**Notas:**  
Este caso verifica la cadena completa de inyección de dependencias desde UI hasta repositorio.

---

### **CI-002: Integración Service-Repository**

**ID Caso:** CI-002  
**Nombre:** Integración entre Servicio y Repositorio  
**Prioridad:** Alta  
**Componentes Involucrados:**
- `MainServiceImpl` (Servicio)
- `MainRepository` (Interfaz)
- `InMemoryMainRepository` (Implementación en memoria)
- `FirebaseMainRepository` (Implementación Firebase)

**Descripción:**  
Verifica que el servicio pueda obtener datos del repositorio correctamente, tanto en modo memoria como Firebase.

**Pasos de Verificación:**

1. **Prueba con Repositorio en Memoria:**
   ```java
   MainRepository repository = new InMemoryMainRepository();
   MainService service = new MainServiceImpl(repository);
   
   String result = service.greet();
   assertEquals("Hello, InazumaGoPrevio!", result);
   ```

2. **Prueba con Repositorio Firebase (simulado):**
   ```java
   // Usar mock para simular Firebase
   MainRepository firebaseRepo = mock(FirebaseMainRepository.class);
   when(firebaseRepo.findDefaultName()).thenReturn("FirebasePlayer");
   
   MainService service = new MainServiceImpl(firebaseRepo);
   String result = service.greet();
   
   assertEquals("Hello, FirebasePlayer!", result);
   ```

3. **Verificar Manejo de Null:**
   ```java
   MainRepository nullRepo = mock(MainRepository.class);
   when(nullRepo.findDefaultName()).thenReturn(null);
   
   MainService service = new MainServiceImpl(nullRepo);
   String result = service.greet();
   
   assertEquals("Hello, player!", result); // Usa valor por defecto
   ```

**Resultados Esperados:**
- ✅ Servicio retorna saludo correcto
- ✅ Manejo apropiado de valores null
- ✅ Funciona con ambas implementaciones de repositorio
- ✅ No excepciones en ejecución normal

**Posibles Fallos:**
- `NullPointerException` si repositorio es null
- Error de conexión si Firebase no está disponible
- `IllegalStateException` si configuración Firebase es inválida

**Notas:**  
Este caso es crítico para verificar que la abstracción de repositorio funciona correctamente.

---

### **CI-003: Integración con Firebase Realtime Database**

**ID Caso:** CI-003  
**Nombre:** Integración con Firebase RTDB  
**Prioridad:** Media  
**Componentes Involucrados:**
- `FirebaseMainRepository`
- Firebase REST API
- Autenticación Firebase
- `AppConfig`

**Descripción:**  
Verifica que la aplicación pueda conectarse y operar con Firebase Realtime Database usando la configuración del proyecto.

**Pasos de Verificación:**

1. **Configurar Firebase URL:**
   ```java
   String firebaseUrl = "https://inazumago-demo.firebaseio.com";
   MainRepository repository = AppConfig.createFirebaseRepository(firebaseUrl);
   ```

2. **Verificar Creación de Instancia:**
   ```java
   assertNotNull(repository);
   assertTrue(repository instanceof FirebaseMainRepository);
   ```

3. **Simular Operación (con mock):**
   ```java
   FirebaseMainRepository firebaseRepo = new FirebaseMainRepository(firebaseUrl);
   
   // Verificar que la URL se almacena correctamente
   // Nota: Requiere reflexión o método getter para verificar
   ```

4. **Verificar Configuración por Defecto:**
   ```java
   MainRepository defaultRepo = AppConfig.createMainRepository(null);
   assertTrue(defaultRepo instanceof InMemoryMainRepository);
   ```

**Resultados Esperados:**
- ✅ Repositorio Firebase se crea correctamente
- ✅ URL se configura apropiadamente
- ✅ Fallback a memoria cuando no hay URL
- ✅ No errores de configuración

**Posibles Fallos:**
- `IllegalArgumentException` si URL es inválida
- Error de red si Firebase no está accesible
- Problemas de autenticación si no hay token

**Notas:**  
Requiere configuración de Firebase real para pruebas completas. Usar mocks para pruebas unitarias.

---

### **CI-004: Integración de DTOs y Mappers**

**ID Caso:** CI-004  
**Nombre:** Integración entre Entidades y DTOs con Mappers  
**Prioridad:** Media  
**Componentes Involucrados:**
- `MainEntity` (Modelo)
- `MainDto` (DTO)
- `MainMapper` (Mapper)

**Descripción:**  
Verifica que la conversión entre entidades de dominio y objetos de transferencia funcione correctamente.

**Pasos de Verificación:**

1. **Crear Entidad:**
   ```java
   MainEntity entity = new MainEntity(1L, "TestPlayer");
   ```

2. **Mapear a DTO:**
   ```java
   MainDto dto = MainMapper.toDto(entity);
   ```

3. **Verificar Mapeo:**
   ```java
   assertNotNull(dto);
   assertEquals(1L, dto.getId());
   assertEquals("TestPlayer", dto.getName());
   ```

4. **Probar con Null:**
   ```java
   MainDto nullDto = MainMapper.toDto(null);
   assertNull(nullDto);
   ```

5. **Verificar Inmutabilidad:**
   ```java
   // DTO debe ser inmutable
   assertThrows(UnsupportedOperationException.class, () -> {
       // Intentar modificar si fuera mutable
   });
   ```

**Resultados Esperados:**
- ✅ Mapeo correcto de propiedades
- ✅ Manejo apropiado de valores null
- ✅ Objetos inmutables
- ✅ No pérdida de datos en conversión

**Posibles Fallos:**
- `NullPointerException` si entidad es null y no se maneja
- Pérdida de datos si mapeo es incompleto
- Problemas de tipos si conversión es incorrecta

**Notas:**  
Importante para la separación entre capa de dominio y capa de presentación.

---

### **CI-005: Integración de Manejo de Excepciones**

**ID Caso:** CI-005  
**Nombre:** Integración de Excepciones y Manejo de Errores  
**Prioridad:** Media  
**Componentes Involucrados:**
- `NotFoundException`
- `ApiError`
- Controladores y Servicios

**Descripción:**  
Verifica que las excepciones personalizadas se manejen correctamente a través de las capas.

**Pasos de Verificación:**

1. **Crear Excepción NotFound:**
   ```java
   NotFoundException ex = new NotFoundException("Recurso no encontrado");
   assertEquals("Recurso no encontrado", ex.getMessage());
   ```

2. **Crear ApiError:**
   ```java
   ApiError error = new ApiError("Error interno", 500);
   assertEquals("Error interno", error.getMessage());
   assertEquals(500, error.getCode());
   ```

3. **Simular Propagación en Servicio:**
   ```java
   MainRepository mockRepo = mock(MainRepository.class);
   when(mockRepo.findDefaultName()).thenThrow(new NotFoundException("No data"));
   
   MainService service = new MainServiceImpl(mockRepo);
   
   assertThrows(NotFoundException.class, () -> service.greet());
   ```

4. **Verificar que Excepciones se Propagan:**
   ```java
   // La excepción debe llegar hasta la capa superior
   try {
       service.greet();
       fail("Debería lanzar excepción");
   } catch (NotFoundException e) {
       assertEquals("No data", e.getMessage());
   }
   ```

**Resultados Esperados:**
- ✅ Excepciones se crean correctamente
- ✅ Mensajes y códigos apropiados
- ✅ Propagación correcta a través de capas
- ✅ No excepciones inesperadas

**Posibles Fallos:**
- Excepciones no se propagan correctamente
- Mensajes de error confusos
- Pérdida de información de error

**Notas:**  
Crítico para el manejo robusto de errores en la aplicación.

---

### **CI-006: Integración de Configuración**

**ID Caso:** CI-006  
**Nombre:** Integración del Sistema de Configuración  
**Prioridad:** Baja  
**Componentes Involucrados:**
- `AppConfig`
- `MainRepository` (creación)
- Variables de entorno

**Descripción:**  
Verifica que la configuración centralizada funcione correctamente para crear componentes.

**Pasos de Verificación:**

1. **Crear Repositorio con URL Firebase:**
   ```java
   String url = "https://test.firebaseio.com";
   MainRepository repo = AppConfig.createFirebaseRepository(url);
   
   assertNotNull(repo);
   assertTrue(repo instanceof FirebaseMainRepository);
   ```

2. **Crear Repositorio en Memoria:**
   ```java
   MainRepository memoryRepo = AppConfig.createInMemoryRepository();
   
   assertNotNull(memoryRepo);
   assertTrue(memoryRepo instanceof InMemoryMainRepository);
   ```

3. **Verificar Lógica de Selección Automática:**
   ```java
   // Con URL
   MainRepository firebase = AppConfig.createMainRepository("https://url.com");
   assertTrue(firebase instanceof FirebaseMainRepository);
   
   // Sin URL
   MainRepository memory = AppConfig.createMainRepository(null);
   assertTrue(memory instanceof InMemoryMainRepository);
   
   // URL vacía
   MainRepository memory2 = AppConfig.createMainRepository("");
   assertTrue(memory2 instanceof InMemoryMainRepository);
   ```

4. **Verificar que Métodos Estáticos no Fallan:**
   ```java
   // No debe haber excepciones en creación
   assertDoesNotThrow(() -> AppConfig.createMainRepository("invalid-url"));
   ```

**Resultados Esperados:**
- ✅ Configuración crea componentes correctos
- ✅ Lógica de selección automática funciona
- ✅ Manejo apropiado de casos edge
- ✅ No excepciones en configuración

**Posibles Fallos:**
- Creación de componentes incorrectos
- Excepciones en configuración
- Problemas con variables de entorno

**Notas:**  
La configuración debe ser centralizada y robusta.

---

### **CI-007: Integración de Utilidades**

**ID Caso:** CI-007  
**Nombre:** Integración de Utilidades del Sistema  
**Prioridad:** Baja  
**Componentes Involucrados:**
- `DateUtils`
- `Main` (punto de entrada)
- Logging

**Descripción:**  
Verifica que las utilidades auxiliares funcionen correctamente en el contexto de la aplicación.

**Pasos de Verificación:**

1. **Probar DateUtils:**
   ```java
   String isoDate = DateUtils.nowIso();
   
   assertNotNull(isoDate);
   assertTrue(isoDate.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z"));
   ```

2. **Verificar Integración en Main:**
   ```java
   // Simular ejecución de Main.main()
   // Verificar que DateUtils.nowIso() se usa correctamente en logs
   ```

3. **Probar Inmutabilidad:**
   ```java
   // DateUtils debe tener métodos estáticos
   assertThrows(NoSuchMethodException.class, () -> {
       DateUtils.class.getConstructor();
   });
   ```

4. **Verificar Formato ISO:**
   ```java
   String date1 = DateUtils.nowIso();
   Thread.sleep(10);
   String date2 = DateUtils.nowIso();
   
   // Fechas deben ser diferentes
   assertNotEquals(date1, date2);
   
   // Ambas deben ser válidas
   assertTrue(date1.compareTo(date2) < 0);
   ```

**Resultados Esperados:**
- ✅ Utilidades retornan valores correctos
- ✅ Formatos apropiados
- ✅ Integración correcta en aplicación
- ✅ Comportamiento determinístico

**Posibles Fallos:**
- Formatos de fecha incorrectos
- Problemas de zona horaria
- Excepciones en utilidades

**Notas:**  
Las utilidades deben ser confiables y bien probadas.

---

## 📊 Matriz de Cobertura de Integración

| Caso | UI | Controller | Service | Repository | DTO | Config | Utils | Prioridad |
|------|----|------------|---------|------------|-----|--------|-------|-----------|
| CI-001 | ✅ | ✅ | ✅ | ⚠️ | ❌ | ❌ | ❌ | Alta |
| CI-002 | ❌ | ❌ | ✅ | ✅ | ❌ | ❌ | ❌ | Alta |
| CI-003 | ❌ | ❌ | ❌ | ✅ | ❌ | ✅ | ❌ | Media |
| CI-004 | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | Media |
| CI-005 | ⚠️ | ⚠️ | ⚠️ | ⚠️ | ❌ | ❌ | ❌ | Media |
| CI-006 | ❌ | ❌ | ❌ | ✅ | ❌ | ✅ | ❌ | Baja |
| CI-007 | ⚠️ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | Baja |

### Leyenda:
- ✅ **Probado directamente**
- ⚠️ **Probado indirectamente**
- ❌ **No probado en este caso**

---

## 🐛 Guía de Troubleshooting

### Problema: "NullPointerException en integración"
**Solución:**
1. Verificar que todas las dependencias están inyectadas
2. Revisar orden de inicialización de componentes
3. Usar `@Autowired` o constructor injection apropiadamente

### Problema: "Fallo en conexión Firebase"
**Solución:**
1. Verificar URL de Firebase
2. Comprobar conectividad de red
3. Validar credenciales de autenticación
4. Usar mocks para pruebas locales

### Problema: "Mapeo DTO incorrecto"
**Solución:**
1. Revisar que todas las propiedades se mapean
2. Verificar tipos de datos compatibles
3. Probar con valores null y edge cases

---

## 📝 Notas Generales

- **Entorno de Pruebas:** Usar H2 o mocks para evitar dependencias externas
- **Paralelización:** Los casos pueden ejecutarse en paralelo si no hay dependencias
- **Datos de Prueba:** Usar datos determinísticos para resultados reproducibles
- **Limpieza:** Limpiar estado entre pruebas para evitar contaminación

---

## 🔄 Historial de Cambios

| Versión | Fecha | Autor | Cambios |
|---------|-------|-------|---------|
| 1.0 | 2026-04-09 | AI Assistant | Creación inicial del documento |
| | | | - 7 casos de integración completos |
| | | | - Pasos de verificación detallados |
| | | | - Cobertura de componentes principales |

---

**Fin del Documento**  
*Este documento debe mantenerse actualizado conforme evolucione la arquitectura del proyecto.*


---

E4-US2
## `doc/test-cases.md`

## Step 4 (Spanish): Create test cases documentation – `doc/test-cases.md`
Kyle

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
E4-US2
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
mvn test
Kyle dev
