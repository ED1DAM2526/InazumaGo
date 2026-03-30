# Casos de Prueba — InazumaGo

**Versión:** 1.0  
**Fecha:** 2026-03-30  
**Equipo:** QA, Motor, Red

## Tabla de contenidos

1. [Introducción](#introducción)
2. [Pruebas unitarias](#pruebas-unitarias)
3. [Pruebas de integración](#pruebas-de-integración)
4. [Pruebas de aceptación](#pruebas-de-aceptación)
5. [Ejecución local](#ejecución-local)

## Introducción

Este documento describe los casos de prueba (test cases) para validar funcionalidad crítica de InazumaGo. Incluye pruebas unitarias, de integración y de aceptación.

Los tests se organizan por sprint y están automatizados en Maven (`mvn test`).

## Pruebas unitarias

### TC-U1: Salud de la aplicación

**ID:** TC-U1  
**Componente:** `HealthController`  
**Descripción:** Verificar que el endpoint/método de salud devuelve "OK"  
**Pasos:**
1. Llamar a `HealthController.health()`
2. Verificar que el resultado es "OK" o equivalente

**Criterios de aceptación:**
- Respuesta es "OK"
- Método existe y es accesible
- Test automatizado pasa en `mvn test`

**Estado:** ✅ Implementado (Sprint 1)

---

### TC-U2: MainService - Saludo básico

**ID:** TC-U2  
**Componente:** `MainService`, `MainServiceImpl`  
**Descripción:** Verificar que MainService devuelve un saludo válido  
**Pasos:**
1. Instanciar `MainServiceImpl` con `InMemoryMainRepository`
2. Llamar a `greet()`
3. Verificar que devuelve un String no vacío

**Criterios de aceptación:**
- Respuesta no es nula
- Respuesta no está vacía
- Test automatizado pasa en `mvn test`

**Estado:** ✅ Implementado (Sprint 1)

---

### TC-U3: InMemoryMainRepository - CRUD básico

**ID:** TC-U3  
**Componente:** `InMemoryMainRepository`  
**Descripción:** Verificar operaciones básicas (Create, Read, Update, Delete)  
**Pasos:**
1. Instanciar repositorio en memoria
2. Crear un objeto (Create)
3. Leerlo (Read)
4. Actualizarlo (Update)
5. Eliminarlo (Delete)

**Criterios de aceptación:**
- CRUD completo funciona
- Datos persisten en memoria
- Tests automatizados pasan

**Estado:** ✅ Implementado (Sprint 1)

---

### TC-U4: ApiError - Mapeo de excepciones

**ID:** TC-U4  
**Componente:** `ApiError`, `NotFoundException`, `ExceptionHandler`  
**Descripción:** Verificar que excepciones se mapean correctamente a `ApiError`  
**Pasos:**
1. Lanzar `NotFoundException`
2. Capturar en manejador global
3. Verificar que respuesta es `ApiError` con código y mensaje

**Criterios de aceptación:**
- `ApiError` contiene código HTTP correcto (404)
- Mensaje es descriptivo
- Test automatizado pasa

**Estado:** ⏳ Planificado (Sprint 1)

---

## Pruebas de integración

### TC-I1: MainController - Integración con MainService

**ID:** TC-I1  
**Componente:** `MainController`, `MainService`  
**Descripción:** Verificar que el controlador delega correctamente al servicio  
**Pasos:**
1. Crear instancia de `MainController` con servicio mock
2. Llamar a `greet()`
3. Verificar que delega a `MainService.greet()`

**Criterios de aceptación:**
- Delegación correcta
- Respuesta del servicio se retorna
- Test automatizado pasa

**Estado:** ✅ Implementado (Sprint 1)

---

### TC-I2: JavaFX UI - Renderización de saludo

**ID:** TC-I2  
**Componente:** `MainController` (FXML), JavaFX Stage  
**Descripción:** Verificar que la UI JavaFX carga el FXML y muestra el saludo  
**Pasos:**
1. Arrancar la aplicación en modo GUI
2. Verificar que la ventana se abre
3. Verificar que el label muestra el saludo

**Criterios de aceptación:**
- Ventana visible
- Label con texto del saludo presente
- Prueba manual reproducible

**Nota:** Puede ser manual o usar TestFX para automatización

**Estado:** ✅ Implementado (Sprint 1)

---

### TC-I3: AuthService - Login simulado

**ID:** TC-I3  
**Componente:** `AuthService`, `AuthServiceMock`, `LoginController`  
**Descripción:** Verificar flujo de login y almacenamiento de token  
**Pasos:**
1. Llamar a `AuthService.login("user@example.com", "password")`
2. Verificar que retorna un token no nulo
3. Verificar que el token se almacena en la aplicación

**Criterios de aceptación:**
- Token devuelto no es nulo
- Token es accesible después del login
- Test automatizado pasa

**Estado:** ⏳ Planificado (Sprint 2)

---

### TC-I4: FirebaseMainRepository - PATCH multi-path (simulado)

**ID:** TC-I4  
**Componente:** `FirebaseMainRepository`, `FirebaseHttpClient`, WireMock  
**Descripción:** Verificar que el repositorio construye y envía PATCH correctamente  
**Pasos:**
1. Arrancar WireMock stub para Firebase
2. Llamar a `writeMoveMultiPath(gameId, move)`
3. Verificar que la petición PATCH se envía correctamente
4. Verificar respuesta 200 OK

**Criterios de aceptación:**
- PATCH contiene payload correcto
- Response status es 200
- Datos se persisten según respuesta
- Test automatizado con WireMock pasa

**Estado:** ⏳ Planificado (Sprint 2)

---

### TC-I5: Optimistic Update → Confirmed

**ID:** TC-I5  
**Componente:** `MoveService`, `FirebaseMainRepository`, WireMock  
**Descripción:** Verificar flujo completo: aplicación local + persistencia exitosa  
**Pasos:**
1. Ejecutar `MoveService.applyPrediction(move)` (optimistic)
2. Verificar que move está en estado PENDING localmente
3. Llamar a persistencia
4. Verificar que server responde 200
5. Cambiar estado a CONFIRMED

**Criterios de aceptación:**
- Move en PENDING después del step 1
- Move en CONFIRMED después del step 5
- WireMock simula respuesta exitosa
- Test automatizado pasa

**Estado:** ⏳ Planificado (Sprint 3)

---

### TC-I6: Optimistic Update → Rejected (rollback)

**ID:** TC-I6  
**Componente:** `MoveService`, `FirebaseMainRepository`, WireMock  
**Descripción:** Verificar rollback cuando server rechaza move  
**Pasos:**
1. Ejecutar `MoveService.applyPrediction(move)` (optimistic)
2. Llamar a persistencia
3. Verificar que server responde 403 (forbidden - out-of-turn)
4. Ejecutar rollback
5. Verificar que move se revierte en UI

**Criterios de aceptación:**
- Move rechazado por server
- Estado local revierte a antes del move
- UI muestra notificación de error
- Test automatizado con WireMock pasa

**Estado:** ⏳ Planificado (Sprint 3)

---

### TC-I7: Conflicto concurrente - Dos writes simultáneos

**ID:** TC-I7  
**Componente:** `MoveService`, `FirebaseMainRepository`, WireMock  
**Descripción:** Verificar reconciliación cuando dos clientes escriben simultáneamente  
**Pasos:**
1. Simular dos moves concurrentes (A y B) desde dos "clientes"
2. Server acepta A (200), rechaza B (409 Conflict)
3. Cliente B recibe rechazo
4. Cliente B sincroniza y ajusta su estado

**Criterios de aceptación:**
- Move A persiste
- Move B rechazado
- Cliente B reconcilia su estado
- Test automatizado pasa

**Estado:** ⏳ Planificado (Sprint 3)

---

### TC-I8: Dedupe por clientNonce

**ID:** TC-I8  
**Componente:** `FirebaseMainRepository`, `MoveService`, WireMock  
**Descripción:** Verificar que reintento con mismo nonce no crea duplicados  
**Pasos:**
1. Enviar move A con `clientNonce = "abc123"`
2. Simular timeout y reintento con mismo nonce
3. Server responde que nonce ya fue procesado (202 Duplicate)
4. Cliente no duplica el move

**Criterios de aceptación:**
- Primer envío se acepta
- Reintento devuelve estado ya procesado
- Move no se duplica
- Test automatizado pasa

**Estado:** ⏳ Planificado (Sprint 3)

---

## Pruebas de aceptación

### TC-A1: Demo local - Inicio de la app

**ID:** TC-A1  
**Descripción:** Demo reproducible del inicio de la aplicación  
**Pasos:**
1. Abrir PowerShell en raíz del proyecto
2. Ejecutar: `.\scripts\use-user-jdk.ps1 -RunMain`
3. Verificar que se abre ventana JavaFX
4. Verificar que muestra saludo

**Criterios de aceptación:**
- Ventana visible
- Saludo presente
- No hay errores en consola
- Demo reproducible en cualquier máquina con JDK 21

**Estado:** ✅ Implementado (Sprint 1)

---

### TC-A2: Demo local - Tests verdes

**ID:** TC-A2  
**Descripción:** Demo reproducible de tests unitarios pasando  
**Pasos:**
1. Abrir PowerShell
2. Ejecutar: `mvn -DskipTests=false test`
3. Verificar que BUILD SUCCESS
4. Verificar que reportes en `target/surefire-reports/`

**Criterios de aceptación:**
- Todos los tests pasan
- BUILD SUCCESS en output
- Reportes generados
- Reproducible en CI

**Estado:** ✅ Implementado (Sprint 1)

---

### TC-A3: Demo local - Packaging

**ID:** TC-A3  
**Descripción:** Demo reproducible de empaquetado  
**Pasos:**
1. Ejecutar: `mvn clean package`
2. Verificar JAR en `target/`
3. Ejecutar JAR: `java -jar target/InazumaGo-1.0-SNAPSHOT.jar`

**Criterios de aceptación:**
- JAR generado sin errores
- JAR ejecutable
- App inicia desde JAR

**Estado:** ⏳ Planificado (Sprint 4)

---

## Ejecución local

### Ejecutar tests automáticamente

```powershell
# En PowerShell
mvn -DskipTests=false test

# Con JDK local (si usas script)
.\scripts\use-user-jdk.ps1 -RunMaven
```

### Ver reportes

```powershell
# Los reportes de Surefire están en:
# target/surefire-reports/

# Puedes abrir el HTML en navegador:
# target/surefire-reports/index.html
```

### Ejecutar test específico

```powershell
# Tests unitarios de MainServiceImpl
mvn test -Dtest=MainServiceImplTest

# Tests unitarios de MainController
mvn test -Dtest=MainControllerTest
```

### Simular Firebase con WireMock (cuando esté implementado)

```powershell
# Tests de integración que usan WireMock stubs
mvn test -Dtest=FirebaseMainRepositoryIntegrationTest
```

---

## Status de implementación por sprint

| Sprint | Historias | Tests | Estado |
|--------|-----------|-------|--------|
| 1 | E1-US1, E1-US2, E2-US1, E3-US1 | TC-U1 a TC-I2, TC-A1, TC-A2 | ✅ En progreso |
| 2 | E3-US2, E2-US2 (stub), E2-US3 | TC-I3, TC-I4 (stub) | ⏳ Planificado |
| 3 | E2-US2 (completo), E2-US4, E4-US2 | TC-I5, TC-I6, TC-I7, TC-I8 | ⏳ Planificado |
| 4 | E1-US3, E4-US1, E5-US1 | TC-A3, cobertura completa | ⏳ Planificado |

---

**Próxima revisión:** Después de Sprint 2  
**Responsable:** Equipo QA

