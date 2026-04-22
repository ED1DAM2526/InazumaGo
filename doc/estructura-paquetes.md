# Estructura de paquetes propuesta para InazumaGoPrevio

Este documento explica la estructura de carpetas y paquetes que hemos creado bajo el paquete raíz `es.iesquevedo`, por qué existe cada paquete, qué ficheros placeholder se han añadido y recomendaciones de siguientes pasos.

## Objetivo
- Separar responsabilidades (entrada, negocio, persistencia, modelos, utilidades, configuración y excepciones) para facilitar mantenimiento, testing y futura integración con frameworks (p. ej. Spring Boot).

> Nota arquitectónica (importante):
> - Tipo de aplicación: cliente de escritorio JavaFX con `Main` como punto de entrada. No es una aplicación servidor por defecto.
> - Persistencia principal: Firebase Realtime Database es la base de datos prevista para producción. Las implementaciones en memoria (`inmemory`) existen únicamente para pruebas y desarrollo local; NO deben utilizarse en producción.
> - Frameworks: No introducir Spring Boot en el cliente. Para DI en el cliente usar Weld (CDI) si se precisa inyección ligera. Spring Boot sólo es apropiado para backends separados (servicios o APIs).

## Resumen de paquetes y su propósito

- `es.iesquevedo.controller`
  - Propósito: capa de entrada (endpoints, controladores de la aplicación) que orquesta llamadas a los servicios.
  - Ficheros placeholder creados:
    - `HealthController.java` — método `health()` que devuelve "OK".
    - `MainController.java` — controlador que delega en `MainService`.

- `es.iesquevedo.service`
  - Propósito: lógica de negocio y orquestación entre repositorios y utilidades.
  - Ficheros:
    - `MainService.java` — interfaz del servicio.
    - `impl/MainServiceImpl.java` — implementación que usa el repositorio.

- `es.iesquevedo.repository`
  - Propósito: abstracción de persistencia (DAO / Repositorios). Permite cambiar la implementación (memoria, JPA, etc.).
  - Ficheros:
    - `MainRepository.java` — interfaz.
    - `inmemory/InMemoryMainRepository.java` — implementación en memoria para pruebas.
    - `firebase/FirebaseMainRepository.java` — implementación orientada a Firebase Realtime DB (producción). (esqueleto/placeholder presente en el repo)

- `es.iesquevedo.model`
  - Propósito: entidades o clases de dominio.
  - Ficheros:
    - `MainEntity.java` — entidad mínima con `id` y `name`.

- `es.iesquevedo.dto`
  - Propósito: objetos de transferencia (API/IO) para evitar exponer las entidades internas.
  - Ficheros:
    - `MainDto.java` — DTO simple.
    - `dto/mapper/MainMapper.java` — mapeador simple `entity -> dto`.

- `es.iesquevedo.config`
  - Propósito: configuración y wiring de la aplicación (placeholders para beans/constantes).
  - Ficheros:
    - `AppConfig.java` — placeholder para configuración.

- `es.iesquevedo.util`
  - Propósito: utilidades comunes reutilizables.
  - Ficheros:
    - `DateUtils.java` — utilitario para fecha en formato ISO.

- `es.iesquevedo.exception`
  - Propósito: tipos de excepción y modelos de error centralizados.
  - Ficheros:
    - `NotFoundException.java`
    - `ApiError.java`

## Recursos (src/main/resources)
- `application.properties` — propiedades de ejemplo (app.name, app.environment).
- `logging.properties` — configuración mínima de logging.

## Tests (src/test/java)
- `es.iesquevedo.controller.HealthControllerTest` — prueba unitaria de `HealthController`.
- `es.iesquevedo.service.MainServiceTest` — prueba unitaria de `MainService` usando `InMemoryMainRepository`.
- `es.iesquevedo.integration.MainControllerIntegrationTest` — prueba de integración que verifica la delegación entre `MainController` → `MainService` → `InMemoryMainRepository`.

Cómo ejecutar los tests (desde la raíz del proyecto):

```bash
mvn -DskipTests=false test
```

> Resultado actual: los tests creados pasan correctamente si ejecutas `mvn test`.

## Archivos creados (mapa rápido)
- src/main/java/es/iesquevedo/controller/HealthController.java
- src/main/java/es/iesquevedo/controller/MainController.java
- src/main/java/es/iesquevedo/service/MainService.java
- src/main/java/es/iesquevedo/service/impl/MainServiceImpl.java
- src/main/java/es/iesquevedo/repository/MainRepository.java
- src/main/java/es/iesquevedo/repository/inmemory/InMemoryMainRepository.java
- src/main/java/es/iesquevedo/model/MainEntity.java
- src/main/java/es/iesquevedo/dto/MainDto.java
- src/main/java/es/iesquevedo/dto/mapper/MainMapper.java
- src/main/java/es/iesquevedo/config/AppConfig.java
- src/main/java/es/iesquevedo/util/DateUtils.java
- src/main/java/es/iesquevedo/exception/NotFoundException.java
- src/main/java/es/iesquevedo/exception/ApiError.java
- src/main/resources/application.properties
- src/main/resources/logging.properties
- src/test/java/es/iesquevedo/controller/HealthControllerTest.java
- src/test/java/es/iesquevedo/service/MainServiceTest.java

## Recomendaciones / siguientes pasos
1. Frameworks y dirección técnica:
   - No usar Spring Boot en el cliente JavaFX; si necesitas funcionalidades de backend, créalo como servicio separado (por ejemplo, con Spring Boot) y documenta claramente la separación de responsabilidades.
   - Para inyección en el cliente, preferir Weld (CDI) como contenedor ligero; si añades dependencias actualiza `pom.xml` y documenta la justificación.
   - Si quieres, puedo adaptar la estructura para un backend Spring Boot separado (añadir dependencias y ejemplos), pero no en el cliente.
2. Añadir dependencias útiles al `pom.xml` según tus necesidades:
   - Spring Boot starters (web, test, data-jpa) si vas a exponer HTTP y usar persistencia.
   - Mockito / AssertJ para tests más avanzados.
   - Lombok (opcional) para reducir boilerplate.
3. Añadir más tests unitarios y de integración (mockear repositorios para servicios, `@WebMvcTest` si usas Spring Boot).
4. Crear CI (GitHub Actions) que ejecute `mvn test`.
5. Añadir `README.md` de desarrollo con convenciones de estilo y comandos útiles.

Si quieres que adapte la estructura para Spring Boot ahora, confirmamelo y:
- Actualizo `pom.xml` con Spring Boot y plugins necesarios.
- Transformo placeholders en componentes Spring.
- Añado un `Application` starter y un simple endpoint HTTP funcional.

---

Si prefieres que coloque este documento en otra ruta (por ejemplo `README.md` en la raíz) dímelo y lo muevo/duplico.
