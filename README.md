# Inazuma Go
Este repositorio contiene el proyecto InazumaGo (Java + Maven).

- Para desarrollo local con PowerShell, puedes usar `doc/ia/user-prompt.md` para poner la ruta al JDK en la sesión.
- Para tareas automáticas y cambios significativos, lee `doc/ia/system-prompt.md` primero.
Notas rápidas:

- `doc/ia/user-prompt.md` — Archivo local para configurar temporalmente variables de entorno (por ejemplo, la ruta al JDK) en la sesión; está pensado para uso personal y no debe subirse al repositorio.
- `doc/ia/system-prompt.md` — Prompt de sistema que deben leer los asistentes/IA al trabajar en este repositorio.
Documentación importante:

- `doc/normas-trabajo-proyecto.md` — Normas de trabajo del proyecto: flujo de ramas, PR, revisiones, merges, incidencias, hotfix y criterios de cierre.

Cómo compilar y probar

En PowerShell (Windows):

```powershell
# Ejecuta tests (usa el JDK activo en la sesión o configura la ruta con doc/ia/user-prompt.md)
mvn -q -DskipTests=false test

# Empaqueta el proyecto
mvn -q -DskipTests=false package
```

## Instrucciones de ejecución JavaFX en README (PowerShell)

Para ejecutar la aplicación:

```powershell
# Opción 1: Con script que aplica JDK (recomendado)
powershell -ExecutionPolicy Bypass -File .\scripts\use-user-jdk.ps1 -NonInteractive; mvn exec:java -Dexec.mainClass="es.iesquevedo.Main"

# Opción 2: Con Maven Wrapper
mvn exec:java -Dexec.mainClass="es.iesquevedo.Main"

# Opción 3: Con Maven global
mvn exec:java -Dexec.mainClass="es.iesquevedo.Main"
```

**Resultado esperado:** Saludo de bienvenida y estado del sistema (Health check).

Notas
- Si necesitas poner temporalmente una ruta al JDK en la sesión, consulta `doc/ia/user-prompt.md`.
- IntelliJ IDEA suele usar su distribución integrada de Maven por defecto; revisa la configuración en Settings > Build, Execution, Deployment > Build Tools > Maven.

Uso del JDK local (`user-prompt.md`) con script

Se incluye un script PowerShell `scripts/use-user-jdk.ps1` que intenta leer la ruta del JDK desde `doc/ia/user-prompt.md` y la aplica temporalmente a la sesión (variables `JAVA_HOME` y `Path`). También puede compilar y ejecutar la clase `Main`.

Ejemplos de uso (PowerShell):

# Aplicar la ruta encontrada en `doc/ia/user-prompt.md` a la sesión actual
powershell -ExecutionPolicy Bypass -File .\scripts\use-user-jdk.ps1

# Aplicar la ruta y compilar
powershell -ExecutionPolicy Bypass -File .\scripts\use-user-jdk.ps1 -RunMaven

# Aplicar la ruta, compilar y ejecutar Main
powershell -ExecutionPolicy Bypass -File .\scripts\use-user-jdk.ps1 -RunMaven -RunMain

Nota: el script busca en `doc/ia/user-prompt.md` una línea del tipo:

$env:JAVA_HOME = 'C:\ruta\a\tu\jdk'

Si no la encuentra puedes pasar la ruta explícitamente:

powershell -ExecutionPolicy Bypass -File .\scripts\use-user-jdk.ps1 -JdkPath 'C:\Program Files\Java\jdk-17' -RunMaven -RunMain
