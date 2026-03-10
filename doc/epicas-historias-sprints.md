# InazumaGoPrevio — Épicas, Historias de Usuario y Plan de 4 Sprints

Fecha: 2026-03-09

Resumen ejecutivo

Este documento extrae las épicas y las historias de usuario detectadas en el repositorio y propone un backlog organizado en 4 sprints (2 semanas cada uno). Cada sprint finaliza con entregables claros por equipo. Suposiciones principales: hay cinco equipos (UI, Motor, Red, DevOps y QA). Prioricé historias que permitan demos incrementales y builds verdes.

Suposiciones aplicadas

- Duración de sprint: 2 semanas.
- Equipos:
  - UI: 5 miembros (incluido jefe de equipo) — responsable de la interfaz cliente JavaFX y UX.
  - Motor: 5 miembros (incluido jefe de equipo) — responsable del núcleo, servicios y lógica de negocio.
  - Red: 5 miembros (incluido jefe de equipo) — responsable de repositorios, cliente HTTP y comunicaciones (Firebase).
  - DevOps: 4 miembros — pipelines, packaging, despliegue y scripts (no realizan desarrollo de features funcionales).
  - QA: 4 miembros — pruebas, definición de casos y automatización (no realizan desarrollo de features funcionales).
- No incluir credenciales reales en el repositorio. Integraciones externas (Firebase) se efectuarán mediante stubs/mocks en CI salvo que se indique lo contrario.

Lista de épicas

- E1 — Core & API: Modelo, servicios, controladores y manejo de errores.
- E2 — Persistencia & Integración Firebase: Repositorios (InMemory → Firebase REST) y gestión de tokens.
- E3 — Cliente UI (JavaFX) y flujo de usuario: pantallas, controladores y lógica de presentación.
- E4 — Testing, Calidad y CI/CD: pipelines, pruebas automáticas y packaging.
- E5 — Infraestructura & Deploy: scripts de empaquetado y publicación de artefactos.
- E6 — Documentación & Onboarding: README y documentación operativa. (Responsabilidad repartida entre DevOps y QA para documentación operativa; contenido técnico de código seguido por Motor/Red según corresponda.)

Historias de usuario por épica

(E1) Core & API

- E1-US1 — Como desarrollador, quiero una API de salud y servicios básicos para comprobar que la app compila y responde.
  - ID: E1-US1
  - Criterios de aceptación:
    1. El endpoint/función de salud devuelve `OK` o un equivalente y existe test unitario que pasa.
    2. `mvn test` y `mvn package` pasan localmente para esta historia.
    3. Comando/documentación para ejecutar los tests en PowerShell añadido al README.
  - Estimación: S
  - Dependencias: Ninguna
  - Equipo: Motor, QA

- E1-US2 — Como desarrollador, quiero `MainService` (implementación y tests) para encapsular la lógica de la aplicación.
  - ID: E1-US2
  - Criterios de aceptación:
    1. `MainService` y su implementación existen y hay tests unitarios que verifican comportamiento clave.
    2. `MainController` delega al servicio y existe al menos una prueba de integración simple.
    3. Documentación de la ubicación del servicio en `doc/` o `doc/estructura-paquetes.md`.
  - Estimación: S
  - Dependencias: E1-US1
  - Equipo: Motor, QA

- E1-US3 — Como desarrollador, quiero manejo centralizado de errores (p. ej. `ApiError`, `NotFoundException`) para respuestas estandarizadas.
  - ID: E1-US3
  - Criterios de aceptación:
    1. Las clases `ApiError` y `NotFoundException` están implementadas y usadas en al menos un flujo.
    2. Existe un test que verifica el mapeo de excepciones a respuestas estándar.
    3. Patrón de error documentado en `doc/`.
  - Estimación: S
  - Dependencias: E1-US2
  - Equipo: Motor, QA

(E2) Persistencia & Integración Firebase

- E2 — Persistencia & Integración Firebase: Repositorios (InMemory → Firebase REST) y gestión de tokens.

- E2-US1 — Como desarrollador, quiero un repositorio en memoria (`InMemoryMainRepository`) reutilizable para tests.
  - ID: E2-US1
  - Criterios de aceptación:
    1. La implementación in-memory cubre operaciones CRUD necesarias.
    2. Tests unitarios del servicio usan esta implementación para aislar dependencias externas.
    3. Documentación breve de la interfaz `MainRepository`.
  - Estimación: S
  - Dependencias: E1-US2
  - Equipo: Motor, QA

- E2-US2 — Como producto, quiero una implementación de repositorio basada en REST para Firebase (cliente configurado), para futura integración.
  - ID: E2-US2
  - Criterios de aceptación:
    1. `FirebaseMainRepository` (o similar) implementa `MainRepository` y permite configurar endpoint/timeout.
    2. Soporta operaciones multi-path (PATCH) atómicas para persistir `moves` y `meta` en una única petición.
    3. Los repositorios aceptan `idToken` (proporcionado por `AuthService`) en las llamadas y exponen hooks para streaming/listening (o interfaces para tests que simulen listeners).
    4. Tests unitarios usan mocks HTTP (WireMock) y no requieren acceso a Firebase real; hay al menos un test que simula un PATCH exitoso y otro que simula rechazo por reglas (403).
    5. Configuración en `application.properties` o `config/AppConfig` para endpoints y timeouts.
  - Estimación: M
  - Dependencias: E2-US1, E1-US3
  - Equipo: Red, QA

- E2-US3 — Como desarrollador, quiero un `AuthService` que gestione idToken para llamadas autenticadas a Firebase.
  - ID: E2-US3
  - Criterios de aceptación:
    1. Interfaz `AuthService` y una implementación `AuthServiceImpl` (mock) con `login(email,password)` que devuelve token de desarrollo.
    2. Repositorios aceptan token o usan el `AuthService` internamente.
    3. Tests que validan flujo de token sin contacto con servicios externos.
    4. `AuthService` debe permitir inyección de tokens en tests y simular expiración/renovación para probar reintentos.
  - Estimación: M
  - Dependencias: E2-US2
  - Equipo: Red, Motor, QA

- E2-US4 — Como producto, quiero el modelo de movimientos y la lógica de sincronización en tiempo real (client-side) para reflejar movimientos mínimos (deltas) y soportar optimistic update + reconciliación.
  - ID: E2-US4
  - Criterios de aceptación:
    1. Definición del modelo `Move` y `Game.meta` en `es.iesquevedo.model` y DTOs correspondientes.
    2. Cliente produce `move` con `moveId`, `clientNonce`, `clientTs` y `payload` mínimo (from,to,piece).
    3. `MoveService` aplica optimistic update en UI y persiste usando `FirebaseMainRepository.writeMoveMultiPath(gameId, move)`.
    4. En caso de rechazo por reglas, `MoveService` realiza rollback y hay test que simula este escenario usando WireMock.
    5. Tests unitarios/integración cubren happy path y conflicto concurrente.
  - Estimación: M
  - Dependencias: E2-US2, E2-US3, E3-US1
  - Equipo: Red, Motor, UI, QA

- E2-US5 — Como equipo, quiero reglas de seguridad RTDB y validaciones para prevenir writes fuera de turno y replays (clientNonce), documentadas y probadas con mocks. (Asignar a Red con apoyo de Motor y QA)
  - ID: E2-US5
  - Criterios de aceptación:
    1. Reglas RTDB en pseudocódigo y en formato JSON listo para pegar en consola están documentadas en `doc/`.
    2. Al menos una prueba de integración con WireMock simula un intento out-of-turn y el repositorio responde con 403; el cliente reacciona apropiadamente.
    3. Documentación de fallback (Cloud Function) para operaciones que requieran verificación server-side.
  - Estimación: S
  - Dependencias: E2-US2, E2-US3
  - Equipo: Red, Motor, QA

(E3) Cliente UI (JavaFX) y flujo de usuario

- E3-US1 — Como usuario, quiero una pantalla principal mínima (JavaFX) que muestre el saludo y el estado, para una demo visual.
  - ID: E3-US1
  - Criterios de aceptación:
    1. Ventana JavaFX con label que muestra saludo (puede invocar `MainController`/servicio).
    2. Recursos FXML en `src/main/resources/fxml/` y controlador vinculado.
    3. Instrucciones para ejecutar la app en README o scripts.
  - Estimación: M
  - Dependencias: E1-US2, E2-US1
  - Equipo: UI, QA

- E3-US2 — Como usuario, quiero un flujo de login UI para introducir credenciales y simular autenticación, para probar token flow.
  - ID: E3-US2
  - Criterios de aceptación:
    1. Pantalla de login (FXML) que llama a `AuthService.login()` y guarda token en memoria de la app.
    2. Indicador visual de éxito/error y tests donde sea posible.
    3. Documentación de credenciales de prueba en `doc/`.
  - Estimación: M
  - Dependencias: E3-US1, E2-US3
  - Equipo: UI, QA

(E4) Testing, Calidad y CI/CD

- E4-US1 — Como equipo, quiero una pipeline CI que ejecute `mvn test` y `mvn package` para garantizar builds reproducibles.
  - ID: E4-US1
  - Criterios de aceptación:
    1. Archivo de pipeline o scripts documentados que ejecutan `mvn -DskipTests=false test` y `mvn package`.
    2. El pipeline falla si los tests fallan.
    3. Documentación en `doc/ci.md` con instrucciones locales (PowerShell).
  - Estimación: M
  - Dependencias: E1-US1, E1-US2, E2-US1
  - Equipo: DevOps, Motor, Red, UI

- E4-US2 — Como QA, quiero tests de integración que simulen Firebase (stubs) y cubran flujos básicos, para evitar regresiones.
  - ID: E4-US2
  - Criterios de aceptación:
    1. Tests de integración que arranquen los componentes principales con repositorios mockeados y verifiquen saludo + login.
    2. Reportes de test visibles (`surefire-reports`).
    3. Casos de prueba documentados en `doc/test-cases.md`.
    4. Tests que simulan:
       - PATCH multi-path exitoso (200) y confirmación del flujo optimistic -> confirmed.
       - Rechazo por reglas (403) que provoca rollback en cliente.
       - Conflicto concurrente: dos writes simultáneos donde uno es aceptado y el otro rechazado; comprobar reconciliación.
       - Dedupe por `clientNonce`: reintentos con mismo nonce no crean duplicados.
    5. Utilizar WireMock (o similar) para simular RTDB REST API en CI; no depender de Firebase real.
  - Estimación: M
  - Dependencias: E2-US2, E3-US1, E4-US1
  - Equipo: QA, Motor, Red

(E5) Infraestructura & Deploy

- E5-US1 — Como DevOps, quiero scripts de empaquetado (jpackage/jar ejecutable) y release para entregar instaladores o artefactos.
  - ID: E5-US1
  - Criterios de aceptación:
    1. Script `scripts/package.ps1` que genera JAR ejecutable y deposita artefacto en `target/releases/`.
    2. Documentación de packaging en `doc/deploy.md`.
    3. Checklists de seguridad para no incluir credenciales en artefactos.
  - Estimación: M
  - Dependencias: E4-US1, E3-US1
  - Equipo: DevOps, Red

(E6) Documentación & Onboarding

- E6-US1 — Como nuevo desarrollador, quiero README y `doc/` con pasos para ejecutar, probar y desarrollar.
  - ID: E6-US1
  - Criterios de aceptación:
    1. README actualizado con comandos PowerShell para ejecutar tests y la app.
    2. `doc/ci.md`, `doc/deploy.md` y `doc/test-cases.md` (mínimos) presentes.
    3. Instrucciones reproducibles para añadir credenciales localmente (sin subirlas al repo).
  - Estimación: S
  - Dependencias: E1-US1, E4-US1, E5-US1
  - Equipo: Motor, UI, DevOps

Plan de sprints (4 sprints — 2 semanas cada uno)

- Nota: Los días de trabajo efectivos serán los martes y los miércoles; sin embargo, los sprints se presentan como semanas completas.

Sprint 1 (16–26 de marzo) — Fundación & Build verde

- Objetivo del sprint: Establecer base de código con tests y servicio básico; build verde.
- Historias asignadas:
  - Motor: E1-US1, E1-US2, E2-US1
  - UI: E3-US1 (iniciar esqueleto UI)
  - Red: diseño de interfaz de repositorio y contratos para E2-US2
  - DevOps: script para ejecutar tests localmente y documentar (apoyo a documentación)
  - QA: validar pruebas unitarias y preparar casos de prueba básicos
- Entregables por equipo (al final del sprint):
  - Motor: Implementación de `MainService`, tests unitarios verdes, `InMemoryMainRepository` funcional.
  - UI: Esqueleto de la aplicación JavaFX (ventana principal, FXML base) y guía de ejecución.
  - Red: Especificación y contratos de repositorio (interfaz `MainRepository`) y documentación de endpoints necesarios.
  - DevOps: `scripts/run-tests.ps1` simple y guía en README.
  - QA: Reporte de pruebas unitarias y checklist de pruebas a ejecutar en siguientes sprints.
- Criterio de demo: `mvn test` y `mvn package` pasan; demo local que muestre salida de `Main.main()` y tests verdes.

Sprint 2 (7–16 de abril) — Repositorios y UI esqueleto

- Objetivo del sprint: Implementar AuthService (mock), UI interacción básica (saludo) y stub de repositorio Firebase.
- Historias asignadas:
  - Motor: colaborar en integración con servicio y exponer API para UI
  - UI: E3-US1 (integrar saludo con `MainService`), empezar E3-US2 (login UI básico)
  - Red: E2-US2 (Firebase repository stub)
  - DevOps: comenzar CI básico (script/pipeline ligero)
  - QA: implementar tests de integración básicos que usen repositorios mock
- Entregables:
  - Motor: Contratos de servicio estables y adaptadores para UI.
  - UI: `AuthService` llamado desde UI simulado y pantalla de saludo funcionando.
  - Red: Stub de `FirebaseMainRepository` y configuración en `application.properties`.
  - DevOps: pipeline/archivo inicial que lanza `mvn test`.
  - QA: Casos de prueba de integración e informe.
- Criterio de demo: App arranca (JavaFX), muestra saludo; login simulado funciona; pipeline CI ejecuta tests automáticamente en PR.

Sprint 3 (20–29 de abril) — Integración Firebase (staging)

- Objetivo del sprint: Implementar cliente HTTP configurable para Firebase, login UI con token y pruebas de integración más sólidas.
- Historias asignadas:
  - Motor: apoyo en la integración del servicio con repositorios (manejo de errores y contratos)
  - UI: E3-US2 (login UI realista usando `AuthService` + token flow)
  - Red: Completar E2-US2 (cliente HTTP real configurable, tests con mocks) y E2-US3 (AuthService integrado)
  - DevOps: E5-US1 iniciar scripts de packaging y manejo de variables (documentado)
  - QA: Tests de integración que mockean respuestas Firebase y cubren login+fetch
- Entregables:
  - Motor: Adaptaciones del core para integración realista (mapeo de errores, contratos estables).
  - UI: Login UI que persiste token en memoria y muestra estado de usuario.
  - Red: `FirebaseMainRepository` con tests que mockean HTTP responses y `AuthService` integrado.
  - DevOps: Script de packaging básico y checklist de seguridad.
  - QA: Test suite de integración en CI con stubs.
- Criterio de demo: Demo mostrando login → token → fetch de datos simulado; tests de integración en CI.

Sprint 4 (4–15 de mayo) — Pulido, QA y Release

- Objetivo del sprint: Pulir, completar tests, pipeline completo y empaquetado de release candidate.
- Historias asignadas:
  - Motor: Polishing del core y cobertura de tests faltantes (E1-US3 complementos)
  - UI: Mejoras UI/UX, manejo de errores visuales y cache básico (opcional)
  - Red: Optimización del cliente HTTP y ajustes finales de repositorios
  - DevOps: Pipeline completo (E4-US1) y empaquetado final (E5-US1)
  - QA: Test de aceptación end-to-end y reporte final
- Entregables:
  - Motor: Código final del core y cobertura mínima acordada (unit + integración).
  - UI: UI lista para demo y validada con packaging.
  - Red: Cliente HTTP robusto y documentación de integración.
  - DevOps: Pipeline que publica artefactos en `target/releases/` y `scripts/package.ps1`.
  - QA: Informe de pruebas y OK para release candidate.
- Criterio de demo: Release candidate empaquetado; demo de app funcional con login + fetch simulado; todos los tests pasan en CI.

Criterios transversales (mínimos)

- Tests: Cada historia funcional crítica debe incluir al menos un test unitario que pase.
- Build: `mvn -DskipTests=false test` y `mvn package` deben pasar en CI para las historias del sprint de integración.
- Documentación: Comandos para ejecutar y validar historias deben añadirse en `README.md` o `doc/` relevante.
- Seguridad: No subir credenciales. Documentar cómo y dónde añadirlas para pruebas locales.
- Demo: Cada sprint debe permitir una demo local reproducible.

Estimaciones y prioridades (resumen)

- Prioridad alta: E1-US1, E1-US2, E2-US1, E3-US1, E4-US1 (permiten demos y build estable).
- Estimaciones: S = Small (1-2 días persona), M = Medium (3-7 días persona), L = Large (>7 días persona).
- Dependencias clave:
  - E1-US2 depende de E1-US1
  - E2-US2 depende de E2-US1 y E1-US3
  - E2-US3 depende de E2-US2
  - E3-US2 depende de E3-US1 y E2-US3
  - E4-US2 depende de E2-US2 y E3-US1
  - E5-US1 depende de E4-US1 y E3-US1

Riesgos y notas

- Riesgos técnicos:
  - Integración con Firebase puede demandar entornos de prueba y revisión de reglas de seguridad.
  - JavaFX en CI/headless puede requerir configuración adicional.
- Mitigaciones:
  - Usar `InMemoryMainRepository` y HTTP stubs en CI.
  - Documentar pasos para ejecutar pruebas locales y para añadir credenciales fuera del repo.


---

# Tareas por Historia de Usuario (atómicas, por equipo)

A continuación cada historia de usuario se descompone en tareas lo más pequeñas y descriptivas posible. Cada tarea incluye: ID (historia-tarea), Equipo, descripción corta, criterios de aceptación (AC) y estimación orientativa (horas).

(E1-US1) API de salud y servicios básicos
- E1-US1-T1 (Motor): Añadir endpoint/método `health()` en `MainController`.
  - AC: Método devuelve "OK"; integrado en la aplicación; test unitario que invoca el endpoint/método y comprueba respuesta.
  - Est. 1h
- E1-US1-T2 (Motor): Test unitario JUnit para `health()` con cobertura básica.
  - AC: Test pasa en `mvn test` y aparece en `surefire-reports`.
  - Est. 1h
- E1-US1-T3 (DevOps): Documentar comando PowerShell para ejecutar tests en README.
  - AC: Línea en README ejecutable en Windows `powershell` que lanza `mvn -DskipTests=false test` y explica salida. PR con la actualización.
  - Est. 0.5h
- E1-US1-T4 (QA): Crear caso de prueba manual para verificar que la app inicia y `health()` responde.
  - AC: Pasos documentados en `doc/test-cases.md` y verificación manual aceptada.
  - Est. 0.5h

(E1-US2) `MainService` e integración básica
- E1-US2-T1 (Motor): Definir interfaz `MainService` con métodos mínimos (p. ej. `start()`, `status()`).
  - AC: Interfaz en `es.iesquevedo.service`, JavaDoc con contratos; compilación OK.
  - Est. 1.5h
- E1-US2-T2 (Motor): Implementación `MainServiceImpl` con lógica mínima y wiring a `MainController`.
  - AC: Implementación disponible y `MainController` delega a ella; prueba de integración simple.
  - Est. 3h
- E1-US2-T3 (Motor): Tests unitarios para `MainServiceImpl` (mocks donde proceda).
  - AC: Tests que cubren comportamiento básico y pasan en CI local.
  - Est. 2h
- E1-US2-T4 (QA): Definir test de integración que arranque `MainController` y verifique delegación al servicio.
  - AC: Test de integración en `src/test` usando Mockito/JUnit; reporte en `surefire-reports`.
  - Est. 1.5h

(E1-US3) Manejo centralizado de errores
- E1-US3-T1 (Motor): Añadir clase `ApiError` y excepciones (`NotFoundException`) y su mapeo básico.
  - AC: Clases en paquete `es.iesquevedo.exception`; ejemplo de uso en un controlador; compilación OK.
  - Est. 2h
- E1-US3-T2 (Motor): Implementar un manejador global (p. ej. `ExceptionHandler`) que transforme excepciones a `ApiError`.
  - AC: Handler que captura `NotFoundException` y devuelve `ApiError` con código y mensaje; test unitario.
  - Est. 2h
- E1-US3-T3 (QA): Test que lanza `NotFoundException` y verifica respuesta mapeada.
  - AC: Test unitario/integración que comprueba cuerpo y código de error.
  - Est. 1h
- E1-US3-T4 (Doc): Añadir breve sección en `doc/` que describa el patrón de errores y ejemplos de payload.
  - AC: Documento actualizado en `doc/` con ejemplo JSON de `ApiError`.
  - Est. 0.5h

(E2-US1) Repositorio InMemory reutilizable
- E2-US1-T1 (Motor): Definir interfaz `MainRepository` (CRUD mínimos) con JavaDoc que especifique contratos.
  - AC: Interfaz en `es.iesquevedo.repository`; métodos y contratos documentados; compilación OK.
  - Est. 1.5h
- E2-US1-T2 (Motor): Implementar `InMemoryMainRepository` con operaciones CRUD y método `reset()` para tests.
  - AC: Implementación en `es.iesquevedo.repository.inmemory`; tests unitarios que usan la repo.
  - Est. 3h
- E2-US1-T3 (QA): Tests del servicio que inyecten `InMemoryMainRepository` para aislar dependencias.
  - AC: Tests que pasan en local y sirven como ejemplos para CI.
  - Est. 2h
- E2-US1-T4 (Doc): Documentar uso de la repo in-memory en `doc/` (ejemplo de configuración para tests).
  - AC: Ejemplo en `doc/test-cases.md` o `doc/` específico.
  - Est. 0.5h

(E2-US2) Firebase REST repository (contracto y stub)
- E2-US2-T1 (Red): Diseñar la interfaz `FirebaseGameRepository` (getGame, writeMoveMultiPath, addMovesListener) y ejemplos de payload.
  - AC: Interfaz y contrato en `es.iesquevedo.repository` o `es.iesquevedo.repository.firebase`; ejemplos JSON incluidos.
  - Est. 2h
- E2-US2-T2 (Red): Implementar stub ligero `FirebaseGameRepositoryStub` que simule respuestas y acepte configuración de endpoint/timeout.
  - AC: Stub en `test` que responde a llamadas (200/403) y puede usarse en pruebas de integración.
  - Est. 3h
- E2-US2-T3 (Red): Implementar `FirebaseGameRepositoryImpl` (esqueleto) con método `writeMoveMultiPath(gameId, payload)` que construye la petición PATCH.
  - AC: Método construye payload correctamente; tests unitarios que verifican la petición formada usando WireMock o inspección del cliente.
  - Est. 4h
- E2-US2-T4 (QA): Crear tests con WireMock que simulen PATCH exitoso (200) y rechazo (403) y validen comportamiento del repo.
  - AC: Stubs en `src/test/resources/stubs/`; tests automáticos que pasan.
  - Est. 6h
- E2-US2-T5 (Doc): Añadir ejemplos en `application.properties`/`AppConfig` y en `doc/` de cómo configurar endpoint y timeout.
  - AC: Entrada de configuración documentada y comprobada por al menos un test.
  - Est. 1h

(E2-US3) AuthService (tokens)
- E2-US3-T1 (Red): Definir interfaz `AuthService` con `login(email,password)` y `getToken()`.
  - AC: Interfaz documentada en `es.iesquevedo.service.auth` y compilación OK.
  - Est. 1h
- E2-US3-T2 (Red): Implementación mock `AuthServiceMock` que devuelve token fijo y permite inyección en tests.
  - AC: Mock disponible y usable en tests; incluye método para simular expiración.
  - Est. 2h
- E2-US3-T3 (Motor/Red): Integrar token en `FirebaseHttpClient` (añadir query param o header cuando token presente).
  - AC: Cliente adjunta token a las peticiones cuando `AuthService` lo provee; test unitario.
  - Est. 2h
- E2-US3-T4 (QA): Tests que validen flujo de login->uso de token->expiroón->renew (simulado).
  - AC: Tests automáticos simulando expiración y reintento; resultados reproducibles.
  - Est. 3h

(E2-US4) Modelo Move y sincronización optimistic + reconciliación
- E2-US4-T1 (Motor): Definir POJOs `Move`, `Game` y DTOs (`MoveDto`, `GameDto`) con campos mínimos (moveId, clientNonce, clientTs, payload).
  - AC: Clases en `es.iesquevedo.model` y `es.iesquevedo.dto`; serialización/deserialización verificada.
  - Est. 2h
- E2-US4-T2 (Motor): Implementar `MoveService.applyPrediction()` que aplique move en memoria y marque estado `PENDING`.
  - AC: Método aplica move localmente sin persistir; test unitario que verifica estado `PENDING`.
  - Est. 3h
- E2-US4-T3 (Red): Implementar método que llame `FirebaseGameRepository.writeMoveMultiPath(gameId, move)` y procese respuesta (confirm/rollback).
  - AC: Si 200 -> confirmar; si 403/4xx -> devolver error para rollback; tests con WireMock.
  - Est. 4h
- E2-US4-T4 (UI): UI hook para mostrar estado `PENDING` y aceptar rollback visual si se recibe rechazo.
  - AC: Indicador visual y botón/función de revert si el servicio solicita rollback; prueba manual.
  - Est. 3h
- E2-US4-T5 (QA): Test de integración completo que simule optimistic->confirmed y optimistic->rejected con WireMock.
  - AC: Test automatizado que verifica aplicación local + persistencia o rollback.
  - Est. 6h

(E2-US5) Reglas de seguridad RTDB y validaciones
- E2-US5-T1 (Red): Escribir reglas RTDB en JSON con validación de turno y `clientNonce`.
  - AC: Archivo `doc/rules/firebase-rtdb-rules.json` con reglas comentadas listo para pegar.
  - Est. 2h
- E2-US5-T2 (QA/Red): Simular out-of-turn write con WireMock devolviendo 403 y comprobar rollback del cliente.
  - AC: Test que reproduce rechazo y que cliente reacciona con rollback y notificación.
  - Est. 3h
- E2-US5-T3 (Red): Documentar fallback con Cloud Function (pseudocódigo + contrato HTTP) si se requiere verificación server-side.
  - AC: Documento `doc/firebase-cloud-function-spec.md` con ejemplos.
  - Est. 2h

(E3-US1) Pantalla principal JavaFX (saludo)
- E3-US1-T1 (UI): Crear FXML base y `MainController` con label saludo.
  - AC: FXML en `src/main/resources/fxml/` y controlador muestra saludo; app arranca localmente.
  - Est. 3h
- E3-US1-T2 (UI): Integrar `MainController` con `MainService.status()` para obtener el texto del saludo.
  - AC: Saludo proviene del servicio; test manual y unitario del controlador.
  - Est. 2h
- E3-US1-T3 (DevOps): Añadir instrucciones de ejecución JavaFX en README (PowerShell) y verificar en máquina dev.
  - AC: Comandos reproducibles y verificados.
  - Est. 1h
- E3-US1-T4 (QA): Caso de prueba visual que compruebe arranque de la ventana y presencia del label.
  - AC: Pasos documentados y comprobación manual.
  - Est. 1h

(E3-US2) Login UI y token flow
- E3-US2-T1 (UI): Crear FXML de login y `LoginController` con campos email/password y botón de envío.
  - AC: Interfaz visible y disparador que llama a `AuthService.login()`.
  - Est. 3h
- E3-US2-T2 (Red): Conectar `LoginController` a `AuthServiceMock` para devolver token y almacenarlo en app memory.
  - AC: Token almacenado en `AppState` o similar; test unitario del controlador.
  - Est. 2h
- E3-US2-T3 (UI): Mostrar estado de login (éxito/error) y mensajes adecuados.
  - AC: UI muestra feedback; pruebas manuales.
  - Est. 1.5h
- E3-US2-T4 (QA): Tests de integración que verifiquen login->token guardado y uso posterior en llamadas.
  - AC: Test automatizado que simula login y comprueba que `FirebaseHttpClient` incluye token.
  - Est. 3h

(E4-US1) Pipeline CI básica
- E4-US1-T1 (DevOps): Script `scripts/run-tests.ps1` que lanza `mvn -DskipTests=false test` y exporta resultados.
  - AC: Script probado localmente y documentado.
  - Est. 1.5h
- E4-US1-T2 (DevOps): Plantilla YAML de pipeline que ejecuta `mvn test` y `mvn package` (sintaxis genérica).
  - AC: Archivo en `ci/` y README con instrucciones para adaptar al proveedor CI.
  - Est. 3h
- E4-US1-T3 (QA): Integrar ejecución de tests de integración con WireMock en pipeline (en modo de ejemplo).
  - AC: Paso en pipeline que arranca WireMock y ejecuta tests; documentación.
  - Est. 4h

(E4-US2) Tests de integración que simulan Firebase
- E4-US2-T1 (QA): Añadir dependencia y configuración de WireMock en `pom.xml` para pruebas.
  - AC: `pom.xml` actualizado en branch de feature; tests locales pasan.
  - Est. 1h
- E4-US2-T2 (QA): Crear stubs de WireMock para los escenarios listados (200, 403, concurrentes, dedupe).
  - AC: Stubs en `src/test/resources/stubs/` y tests que los usan.
  - Est. 6h
- E4-US2-T3 (QA/Motor/Red): Tests automatizados que validen optimistic->confirmed, optimistic->rollback y conflicto concurrente.
  - AC: Tests reproducibles en CI y pase en `mvn test`.
  - Est. 8h

(E5-US1) Scripts de empaquetado y release
- E5-US1-T1 (DevOps): Crear `scripts/package.ps1` que ejecute `mvn package` y copie artefactos a `target/releases/`.
  - AC: Script probado localmente; artefacto presente en `target/releases/`.
  - Est. 2h
- E5-US1-T2 (DevOps): Documentar checklist de seguridad para packaging (no incluir credenciales).
  - AC: Documento en `doc/deploy.md` y checklist en PR template.
  - Est. 1h
- E5-US1-T3 (DevOps): Añadir paso en pipeline para publicar artefacto (sintético) en la plantilla CI.
  - AC: Paso documentado; ejemplo de publicación.
  - Est. 2h

(E6-US1) README y onboarding
- E6-US1-T1 (DevOps/QA): Actualizar README con comandos PowerShell para ejecutar tests y la app (incluir `use-user-jdk.ps1` si procede).
  - AC: README con comandos verificados y ejemplos.
  - Est. 1h
- E6-US1-T2 (QA): Crear `doc/ci.md` con pasos locales para ejecutar pipeline y tests.
  - AC: Documento reproducible y referenciado desde README.
  - Est. 1.5h
- E6-US1-T3 (QA): Añadir `doc/test-cases.md` con los casos de integración descritos y pasos de verificación.
  - AC: Documento con pasos para cada escenario (optimistic, rollback, conflict, dedupe).
  - Est. 2h

---

# Notas de uso y asignación en Taiga
- Cada tarea atómica tiene un ID (por ejemplo `E2-US4-T3`) que facilita crear tarjetas en Taiga y asignarlas a los jefes de equipo para distribuirlas internamente.
- Al exportar a Taiga recomendamos: una épica por E#, una historia por cada E#-US# y tareas (tasks) por cada E#-US#-T#. Así los jefes de equipo pueden reasignar tareas atómicas a sus miembros.
- Los estimados son orientativos y sirven para planificar el sprint en Taiga; ajustar en la planificación de sprint.
