# System prompt (para asistentes/IA) — InazumaGo

Propósito
- Archivo de referencia que debe leerse como prompt de sistema por asistentes y agentes automáticos que trabajen en este repositorio.
- Proveer contexto del proyecto, restricciones, convenciones de código y expectativas de entrega para que las acciones automáticas sean coherentes y seguras.

Resumen del proyecto
- Nombre: InazumaGo
- Lenguaje principal: Java (JDK compatible con Maven)
- Sistema de construcción: Maven (archivo `pom.xml` en la raíz)
- Estructura relevante:
  - `src/main/java/es/iesquevedo/Main.java` (punto de entrada actual)
  - `src/main/resources/`
  - `doc/ia/` (documentación y prompts para agentes)

Diagrama de aplicación (archivo)
- Ruta: `doc/diagram-app.puml`.
- Propósito: diagrama PlantUML que representa la arquitectura cliente (escritorio) actual. Debe leerse junto con este fichero para entender componentes y dependencias.
- Resumen del diagrama: actor "Interfaz de Usuario (Escritorio/CLI)" → `Main` (punto de entrada) → `Controllers` → `Service` → `Repository` (p. ej. `InMemoryMainRepository`) y componentes auxiliares (DTO/Mapper, Model, Util, Excepciones). No asume una API HTTP/REST ni que los controladores expongan endpoints remotos; los controladores coordinan la interacción entre la UI y la lógica de negocio.

Aclaración importante sobre persistencia:
- Firebase Realtime Database es la persistencia principal y de producción prevista por el proyecto. En el diagrama, la nube `Firebase Realtime DB` representa la base de datos en línea que debe usarse como almacenamiento definitivo.
- Las implementaciones `InMemoryMainRepository` u otras implementaciones en memoria son únicamente para pruebas, simulación o desarrollo local. NO deben usarse como sustituto de Firebase en entornos de producción.
- Si en alguna parte del diagrama aparece la palabra "opcional" respecto a Firebase, debe interpretarse como "opcional para pruebas/simulación" y **no** como una recomendación de producción.

Nota importante: si el diagrama o el código se modifican (por ejemplo, añadir una API REST o convertir el cliente en un servicio), actualizar `doc/diagram-app.puml` y dejar constancia en este fichero (`doc/ia/project-context.md` si procede).

Objetivos para el asistente
- Entender el contexto del repositorio antes de proponer o aplicar cambios.
- Priorizar cambios que mantengan la compilación limpia, añadan/testeen funcionalidad y sigan las convenciones Java/OpenJDK.
- Cuando modifiques código: incluye pruebas unitarias (JUnit) para la nueva funcionalidad o para cubrir regresiones.
- Proveer mensajes de commit claros y, si generas ramas, usar nombres descriptivos como `feat/<breve-descripción>`, `fix/<breve-descripción>` o `chore/<breve-descripción>`.

Convenciones y restricciones
- Idioma de los comentarios y documentación: español preferentemente (puedes usar inglés para mensajes de commit breves si lo requiere el flujo de CI).
- No realizar llamadas de red externas, no exfiltrar secretos ni credenciales.
- Evitar dependencias nuevas salvo que sean justificadas y ampliamente usadas; si añades dependencias, actualiza `pom.xml` y agrega una nota explicativa en el commit/PR.
- Entorno de ejecución del desarrollador: Windows (PowerShell); cuando proporciones comandos de terminal, usa sintaxis compatible con PowerShell.

Flujo recomendado al aplicar cambios
1. Leer este archivo y el `pom.xml` y la clase `Main` antes de actuar.
2. Crear una rama nueva para la tarea.
3. Implementar cambios mínimos y añadir pruebas.
4. Ejecutar `mvn -q -DskipTests=false test` localmente; si previenes tests lentos, documenta por qué y añade una prueba rápida.
5. Ejecutar `mvn -q -DskipTests=false package` para verificar el empaquetado.
6. Añadir un README o nota en `doc/` si el cambio introduce una nueva tarea de usuario o requerimiento de uso.

Expectativas del formato del prompt
- Ser claro y explícito sobre el objetivo de cada acción.
- Incluir la lista de archivos que se cambian y una breve justificación en cada commit.
- Si el asistente no tiene suficiente información, debe pedir una aclaración breve antes de realizar cambios significativos.

Metadatos útiles
- Fecha de referencia: 2026-03-09
- Usuario/propietario del repositorio: (no incluido aquí)

Uso
- Lectura obligatoria: cualquier asistente o subagente automático debe leer este archivo al comenzar una tarea relacionada con el código o la documentación.
- Si este archivo cambia, actualizar la nota en `doc/ia/project-context.md` indicando la nueva ruta.

# Documentos técnicos obligatorios
- `doc/firebase-realtime-plan.md`: plan técnico objetivo y obligatorio para cualquier cambio relacionado con la sincronización en tiempo real, persistencia en Firebase RTDB, reglas de seguridad y diseño de eventos. Los agentes deben leer este documento antes de implementar cambios que afecten a la capa de persistencia/transport.

Configuración local (user-prompt.md)
- Para facilitar la configuración local, la ruta al JDK y otras variables de sesión deben colocarse en `doc/ia/user-prompt.md`.
- Este proyecto seguirá una regla simple: los scripts y agentes deben leer la clave `JDK_PATH` de `doc/ia/user-prompt.md` y aplicar esa ruta como `JAVA_HOME` para la sesión. No se realizarán búsquedas automáticas extensas.

- Ejemplo (PowerShell):

  JDK_PATH = 'C:\Program Files\Java\jdk-21'

- Importante: `doc/ia/user-prompt.md` está pensado para uso local y no debe versionarse (añádelo al `.gitignore`).

Maven Wrapper
- Este proyecto incluye el Maven Wrapper (`mvnw` / `mvnw.cmd`). Todas las ejecuciones automáticas y scripts del repositorio deben utilizar el wrapper en lugar de depender de una instalación de Maven en PATH o de la instalación integrada de IntelliJ.

- Uso en Windows (ejemplo):

  .\mvnw.cmd -DskipTests=false test

- En IntelliJ: configura el proyecto para usar el Maven Wrapper (Settings > Build, Execution, Deployment > Build Tools > Maven -> Use Maven wrapper). Con esto, el IDE usará la versión de Maven definida por el wrapper y ya no dependerás de la ruta Maven integrada de cada usuario.

Ejecución forzada con el JDK del usuario (simplificado)
- Flujo recomendado por scripts y agentes:
  1. Leer `JDK_PATH` en `doc/ia/user-prompt.md`.
  2. Si existe, aplicar temporalmente `JAVA_HOME` y prefijar `JAVA_HOME\bin` en `PATH` (sin duplicados) para la sesión.
  3. No realizar comprobaciones de versión adicionales: se confía en que el usuario ha puesto la ruta correcta (la responsabilidad del desarrollador).
  4. Si no existe `JDK_PATH`, los scripts en modo non-interactive deben ABORTAR INMEDIATAMENTE y mostrar el siguiente mensaje de error exacto:

     ERROR: `JDK_PATH` no encontrado en `doc/ia/user-prompt.md`. Configure la ruta al JDK 21 en ese archivo y vuelva a intentarlo.

     No se realizará búsqueda automática ni fallback a otras ubicaciones cuando se ejecute en modo non-interactive.

- Nota: para ejecuciones automatizadas en CI se recomienda garantizar que el runner tenga JDK 21 disponible; el wrapper sólo cubre la versión de Maven, no la versión del JDK.

# Documentos técnicos obligatorios
- `doc/firebase-realtime-plan.md`: plan técnico objetivo y obligatorio para cualquier cambio relacionado con la sincronización en tiempo real, persistencia en Firebase RTDB, reglas de seguridad y diseño de eventos. Los agentes deben leer este documento antes de implementar cambios que afecten a la capa de persistencia/transport.

Configuración local (user-prompt.md)
- Para facilitar la configuración local, la ruta al JDK y otras variables de sesión deben colocarse en `doc/ia/user-prompt.md`.
- Este proyecto seguirá una regla simple: los scripts y agentes deben leer la clave `JDK_PATH` de `doc/ia/user-prompt.md` y aplicar esa ruta como `JAVA_HOME` para la sesión. No se realizarán búsquedas automáticas extensas.

- Ejemplo (PowerShell):

  JDK_PATH = 'C:\Program Files\Java\jdk-21'

- Importante: `doc/ia/user-prompt.md` está pensado para uso local y no debe versionarse (añádelo al `.gitignore`).

Maven Wrapper
- Este proyecto incluye el Maven Wrapper (`mvnw` / `mvnw.cmd`). Todas las ejecuciones automáticas y scripts del repositorio deben utilizar el wrapper en lugar de depender de una instalación de Maven en PATH o de la instalación integrada de IntelliJ.

- Uso en Windows (ejemplo):

  .\mvnw.cmd -DskipTests=false test

- En IntelliJ: configura el proyecto para usar el Maven Wrapper (Settings > Build, Execution, Deployment > Build Tools > Maven -> Use Maven wrapper). Con esto, el IDE usará la versión de Maven definida por el wrapper y ya no dependerás de la ruta Maven integrada de cada usuario.

Ejecución forzada con el JDK del usuario (simplificado)
- Flujo recomendado por scripts y agentes:
  1. Leer `JDK_PATH` en `doc/ia/user-prompt.md`.
  2. Si existe, aplicar temporalmente `JAVA_HOME` y prefijar `JAVA_HOME\bin` en `PATH` (sin duplicados) para la sesión.
  3. No realizar comprobaciones de versión adicionales: se confía en que el usuario ha puesto la ruta correcta (la responsabilidad del desarrollador).
  4. Si no existe `JDK_PATH`, los scripts en modo non-interactive deben ABORTAR INMEDIATAMENTE y mostrar el siguiente mensaje de error exacto:

     ERROR: `JDK_PATH` no encontrado en `doc/ia/user-prompt.md`. Configure la ruta al JDK 21 en ese archivo y vuelva a intentarlo.

     No se realizará búsqueda automática ni fallback a otras ubicaciones cuando se ejecute en modo non-interactive.

- Nota: para ejecuciones automatizadas en CI se recomienda garantizar que el runner tenga JDK 21 disponible; el wrapper sólo cubre la versión de Maven, no la versión del JDK.

## Decisiones y directrices específicas del proyecto (resumen para agentes)

A continuación se recogen las decisiones y recomendaciones acordadas para este repositorio. Cualquier asistente/ agente automático debe leer y aplicar estas reglas además de las generales indicadas arriba.

- Tipo de aplicación: cliente de escritorio JavaFX (no aplicación servidor por defecto).
- Base tecnológica:
  - Lenguaje: Java (configurado en `pom.xml`, actualmente Java 21).
  - Construcción: Maven.
  - UI: JavaFX con ficheros FXML en `src/main/resources/fxml/` y controladores en el paquete `es.iesquevedo.ui` (o `es.iesquevedo.view`).
- Inyección de dependencias en el cliente: usar Weld (CDI) como contenedor ligero. No introducir Spring Boot en la aplicación cliente.
  - Justificación: Weld provee DI/IoC sin la sobrecarga de Spring Boot y encaja mejor para una app de escritorio.
  - Reglas para agentes: si añades dependencias, actualiza `pom.xml` y documenta la justificación en el commit.

- Acceso a datos: Firebase Realtime Database
  - En el cliente JavaFX debes usar la REST API de Realtime Database (o streaming REST) para leer/escribir datos en tiempo real.
  - NO usar el Firebase Admin SDK en el cliente y NO incluir ninguna credencial de servicio (service account JSON) en el binario. Esto es crítico para la seguridad.
  - Autenticación: usar Firebase Authentication desde el cliente (email/password u otros providers) para obtener `idToken`. Incluir `idToken` en las llamadas REST hacia Realtime DB conforme a las reglas de seguridad.
  - Si necesitas acciones privilegiadas (custom tokens, administración, operaciones con service account), crear un backend seguro (por ejemplo, un microservicio separado). El backend puede implementarse con Spring Boot si procede — pero esto es un servicio separado, no parte del cliente.

- Arquitectura recomendada (cliente):
  - `es.iesquevedo.ui` (JavaFX controllers / FXML)
  - `es.iesquevedo.service` (lógica de juego, orquestación)
  - `es.iesquevedo.repository.firebase` (implementación Firebase via REST/streaming)
  - `es.iesquevedo.model`, `es.iesquevedo.dto`, `es.iesquevedo.util`, `es.iesquevedo.exception`
  - `es.iesquevedo.config` (configuración local y constantes, p. ej. endpoints, timeouts)
  - `es.iesquevedo.auth` o `es.iesquevedo.service.auth` (AuthService encargado de login y gestión de tokens)

- Bibliotecas sugeridas (Maven):
  - HTTP / streaming: okhttp (com.squareup.okhttp3:okhttp) o `java.net.http.HttpClient` (JDK 11+).
  - JSON: Jackson (com.fasterxml.jackson.core:jackson-databind) o Gson.
  - CDI/DI: weld-se / weld-core (versiones compatibles con Java 21).
  - Testing: JUnit Jupiter (ya presente), Mockito y AssertJ (si se justifican).

- Concurrencia y UI:
  - Toda I/O o streaming con Firebase debe ejecutarse fuera del hilo JavaFX (usar ExecutorService). Actualizaciones UI deben ejecutarse con `Platform.runLater`.

- Persistencia local / Offline:
  - Si requieres funcionalidad offline robusta, implementar cache local (SQLite, archivos JSON) y estrategias de reconciliación manualmente; el SDK de Android no está disponible en JavaFX.

- Seguridad y manejo de secretos (obligatorio):
  - No versionar ni embebar service account JSON en el repositorio ni en el cliente.
  - Para operaciones privilegiadas, usar un backend que mantenga las credenciales seguras y emita custom tokens o endpoints protegidos.
  - Mantener reglas de Realtime DB que validen `auth.uid` y restricciones de escritura/lectura.

- Flujo de trabajo para agentes que ejecutan código/local builds:
  - Seguir la sección "Ejecución forzada con el JDK del usuario y confirmación" ya presente en este archivo (`doc/ia/user-prompt.md` debe contener la ruta JDK si la provee el usuario).
  - Antes de ejecutar `mvn test` o `mvn package`, comprobar `doc/ia/user-prompt.md` y solicitar confirmación al usuario si no hay una ruta válida.

- Pruebas y CI:
  - Mantener pruebas unitarias con JUnit Jupiter. Incluir tests de servicio donde el `FirebaseRestRepository` pueda ser simulada con stubs/mocks.
  - Si se añaden nuevas dependencias, añadir tests que cubran la integración básica (sin conexión a Firebase real; usar mocks o respuestas simuladas).

- Documentación y commits:
  - Cuando enumeres cambios en commits o PRs, documentar: archivos cambiados, justificación para nuevas dependencias y la decisión de usar Weld en el cliente.
  - Si se crea un backend separado (p. ej. Spring Boot), documentar esa separación de responsabilidades en `doc/`.

## Aclaraciones adicionales

- No se añadirá ni utilizará ningún fichero JSON de credenciales (por ejemplo service account JSON) en el cliente ni en el repositorio. Todas las credenciales sensibles deben permanecer en un backend seguro si se necesita realizar operaciones privilegiadas.

- Persistencia: la persistencia se hace en Firebase Realtime Database (online). No se deberá usar almacenamiento de credenciales en archivos JSON en el cliente.

## Herramientas y versiones recomendadas

A continuación se listan las herramientas, runtimes y versiones recomendadas para el proyecto:

- Lenguaje / runtime: Java 21 (21 LTS)
- Build: Apache Maven 3.9.9
- UI: JavaFX 21.0.2
- DI / CDI: Weld 5.1.x
- Testing: JUnit 5 (Jupiter) 5.10.2
- Mocking: Mockito 5.11.x
- Persistencia / Online: Firebase Realtime Database (última estable a fecha 2026)
- SDK Firebase (para backend seguro únicamente): Firebase Admin SDK 9.2.x
- Empaquetado: jpackage (incluido en JDK 21)
- Fat JAR (opcional): maven-shade-plugin 3.5.x
- Code coverage: JaCoCo 0.8.11
- Static analysis: SpotBugs 4.8.x

Notas:
- El `Firebase Admin SDK` sólo debe usarse en un backend seguro (server-side). No incluirlo ni sus credenciales en la aplicación cliente.
- Si se añaden dependencias nuevas o versiones concretas, documentar la justificación en el commit/PR y actualizar este fichero si procede.

## Preguntas frecuentes rápidas (para agentes)
- ¿Puedo usar Spring Boot en el cliente? No. No introduzcas Spring Boot en la app JavaFX cliente; si necesitas backend, créalo como servicio separado.
- ¿Dónde pongo el service account JSON? Nunca en el cliente. Si lo necesitas para tareas administrativas, ponlo solo en el backend seguro y fuera del repositorio.
- ¿Qué DI usare? Weld (CDI) en el cliente; documenta la versión en `pom.xml`.
