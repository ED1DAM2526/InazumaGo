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
- `doc/ia/user-prompt.md` contiene instrucciones (PowerShell) para añadir temporalmente la ruta del JDK al PATH de la sesión: por ejemplo:

  $env:JAVA_HOME = 'C:\Program Files\Java\jdk-17.0.8'
  $env:Path = "$env:JAVA_HOME\bin;$env:Path"

- Importante: `doc/ia/user-prompt.md` está pensado para uso local y no debe versionarse (añádelo al `.gitignore`).

Ejecución forzada con el JDK del usuario y confirmación
- Preferencia de JDK: Antes de compilar o ejecutar código, el asistente debe preferir usar la instalación de Java indicada por el desarrollador en `doc/ia/user-prompt.md` (variable `JAVA_HOME`) o por un parámetro explícito proporcionado por el usuario.
- Comprobación previa obligatoria: Siempre que vaya a ejecutar código (compilar, tests o ejecutar clases), el asistente debe:
  1. Buscar en `doc/ia/user-prompt.md` una línea con la asignación de `JAVA_HOME` (p. ej. `$env:JAVA_HOME = 'C:\ruta\a\tu\jdk'`).
  2. Si encuentra una ruta, verificar que exista en disco y que contenga `bin\java.exe`.
  3. Si existe, aplicar temporalmente esa ruta a la sesión (exportando `JAVA_HOME` y asegurándose de que `JAVA_HOME\bin` esté presente en `PATH` UNA SOLA VEZ — no anteponer duplicadas).
- Si la ruta NO existe o `doc/ia/user-prompt.md` no contiene una entrada válida:
  - El asistente debe pedir confirmación al usuario (pregunta breve y explícita) antes de crear o modificar `doc/ia/user-prompt.md` o antes de aplicar cualquier cambio al entorno de ejecución.
  - El mensaje de confirmación debe incluir: la ruta que falta (o indicar que no hay ruta), la acción propuesta (añadir/editar `doc/ia/user-prompt.md` y/o ejecutar `scripts/use-user-jdk.ps1`), y el efecto (temporal: solo afecta a la sesión actual del script/terminal).
  - Si el usuario confirma, el asistente puede:
    - Pedir la ruta concreta al JDK y validar que `bin\java.exe` exista, y
    - Añadir la entrada a `doc/ia/user-prompt.md` si no está presente (añadir únicamente una vez; no crear duplicados en el archivo), y
    - Aplicar temporalmente `JAVA_HOME` y ajustar `PATH` (asegurándose de no duplicar `JAVA_HOME\bin`).
  - Si el usuario no confirma, el asistente debe abortar la ejecución o preguntar si usar el JDK del sistema en su lugar.
  - Registro/justificación: siempre documentar en la nota de la tarea o en el mensaje de commit la ruta usada y la confirmación del usuario. Ejemplo de texto a añadir en el commit/note: "Ejecución con JDK local: C:\Users\... (confirmado por usuario)".

Plantilla de confirmación (usar este texto cuando pidas permiso al usuario):
"No se encuentra la ruta al JDK en `doc/ia/user-prompt.md`. Se propone crear/actualizar la entrada con la ruta: <RUTA_PROPOSITA>. Esto implicará añadir temporalmente `JAVA_HOME` y `JAVA_HOME\\bin` al PATH en la sesión actual y añadir la línea a `doc/ia/user-prompt.md` (archivo local y no versionado). ¿Confirmas? (S/N)"

Implementación en scripts y automatizaciones
- Los scripts del repositorio (por ejemplo `scripts/use-user-jdk.ps1`) deben implementar esta lógica: buscar `doc/ia/user-prompt.md`, validar la ruta, evitar duplicados en `PATH` y en el propio `user-prompt.md`, y pedir confirmación al usuario antes de crear/editar el archivo.
- Las herramientas automáticas o agentes deben respetar este flujo y no modificar `user-prompt.md` sin confirmación del usuario.

IntelliJ y Maven
- Nota: IntelliJ IDEA incluye una distribución de Maven que puede usar por defecto (o puedes configurar una instalación externa en Settings > Build, Execution, Deployment > Build Tools > Maven > "Maven home directory").

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
