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

---

## ⚡ Inicio Rápido: Ejecutar Tests

**⭐ Usa GitHub Copilot:** Abre el archivo `doc/ia/copilot-run-tests.md` y pasa su contenido a Copilot para obtener ayuda interactiva paso a paso.

1. Verifica Java (debe ser 21):
```powershell
java -version
```

2. Ejecuta tests:
```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run-tests.ps1
```

3. Si no detecta Maven de IntelliJ, pásalo explícito:
```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run-tests.ps1 -MavenCmdPath 'C:\Program Files\JetBrains\IntelliJ IDEA 2025.1.4\plugins\maven\lib\maven3\bin\mvn.cmd'
```

4. Resultado esperado:
- `BUILD SUCCESS` o `BUILD FAILURE`
- Reportes en `target/surefire-reports/`

### 🔧 Troubleshooting

| Problema | Solución |
|----------|----------|
| "No compiler is provided" | Verifica `java -version`. Debe ser 21, no otra versión |
| Script no encuentra JDK | Coloca JDK 21 en `C:\Users\[usuario]\.jdks\jdk-21\` o usa `-JdkPath` |
| Tengo JDK 17/20/22 instalado | Descarga JDK 21 en `.jdks/jdk-21/`. El script lo encontrará automáticamente |
