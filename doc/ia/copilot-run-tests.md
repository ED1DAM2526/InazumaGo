# Copilot Prompt: Configurar y ejecutar tests en InazumaGo

## Contexto
Este proyecto es InazumaGo, una aplicación Java + Maven que requiere **JDK 21 obligatoriamente**. Los desarrolladores necesitan ejecutar tests locales en Windows PowerShell usando el script `scripts/run-tests.ps1` que automatiza la detección de JDK y Maven integrado de IntelliJ IDEA.

## Tarea: Guiar a un desarrollador nuevo a ejecutar tests

### Paso 1: Verificar Java
Pide al usuario que ejecute:
```powershell
java -version
```
- Si muestra versión 21, continúa al Paso 2.
- Si muestra otra versión (8, 11, 17, 20, etc.), solicita que instale JDK 21:
  - Descargar desde: https://www.microsoft.com/openjdk
  - Colocar en: `C:\Users\[usuario]\.jdks\jdk-21\` (recomendado)
  - O en: `C:\Program Files\Java\jdk-21\`

### Paso 2: Ejecutar el script de tests
Instruye al usuario a ejecutar desde PowerShell en la raíz del proyecto:
```powershell
cd C:\ruta\del\proyecto\InazumaGo
powershell -ExecutionPolicy Bypass -File .\scripts\run-tests.ps1
```

**Resultado esperado:**
- `BUILD SUCCESS` → todos los tests pasaron ✓
- `BUILD FAILURE` → algunos tests fallaron (revisar `target/surefire-reports/`)

### Paso 3: Si el script no detecta Maven automáticamente
Si el script falla con: `No se encontro Maven...`

**Opción A (recomendada):** Asegurar que IntelliJ IDEA esté abierto
- Abre IntelliJ con el proyecto
- Vuelve a ejecutar el script (detectará Maven desde el proceso `idea64`)

**Opción B:** Pasar ruta explícita de Maven
1. Abre IntelliJ IDEA
2. Ve a: Settings > Build, Execution, Deployment > Build Tools > Maven
3. Copia la ruta de "Maven home directory"
4. Ejecuta:
```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run-tests.ps1 -MavenCmdPath 'C:\ruta\copiada\mvn.cmd'
```

### Paso 4: Revisar reportes de tests
Los reportes están en: `target/surefire-reports/`
- Archivos `.xml` para análisis automatizado
- Archivos `.txt` para lectura directa

## Comportamiento esperado del script

El script `scripts/run-tests.ps1` hace lo siguiente automáticamente:

1. **Detecta JDK 21** en este orden:
   - `-JdkPath` parámetro (si se pasa)
   - `$env:JAVA_HOME` en `doc/ia/user-prompt.md`
   - Variable de entorno `JAVA_HOME` de la sesión
   - Rutas comunes: `C:\Users\[usuario]\.jdks\jdk-21`, `C:\Program Files\Java\jdk-21`

2. **Valida que sea versión 21** (si no, falla con error claro)

3. **Detecta Maven** en este orden:
   - `-MavenCmdPath` parámetro (si se pasa)
   - Proceso `idea64.exe` abierto → deriva Maven de IntelliJ
   - Búsqueda en rutas estándar de IntelliJ/Toolbox
   - `mvn.cmd` en PATH del sistema

4. **Ejecuta tests** con `mvn -DskipTests=false test`

5. **Reporta resultado**:
   - `BUILD SUCCESS` con código de salida 0
   - `BUILD FAILURE` con código de salida ≠ 0

## Troubleshooting

| Problema | Causa | Solución |
|----------|-------|----------|
| "No compiler is provided" | JDK no es versión 21 | Verifica `java -version` y descarga JDK 21 |
| "No se encontro Maven" | Maven de IntelliJ no detectado | Abre IntelliJ o usa `-MavenCmdPath` |
| "Se detecto un JDK distinto de version 21" | JDK activo no es 21 | Configura JDK 21 en `doc/ia/user-prompt.md` |
| Tests en `BUILD FAILURE` | Fallos en el código | Revisa `target/surefire-reports/` |

## Referencia del script

**Parámetros opcionales:**
```powershell
# Ruta explícita a JDK 21
-JdkPath 'C:\Users\usuario\.jdks\jdk-21'

# Ruta explícita a Maven
-MavenCmdPath 'C:\Program Files\JetBrains\IntelliJ IDEA 2025.3.3.win\plugins\maven\lib\maven3\bin\mvn.cmd'
```

**Ejemplo con ambos parámetros:**
```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run-tests.ps1 -JdkPath 'C:\Users\1dam\.jdks\jdk-21' -MavenCmdPath 'C:\tools\idea-2025.3.3.win\plugins\maven\lib\maven3\bin\mvn.cmd'
```

## Notas importantes

- Este script requiere **PowerShell en Windows**. No funciona en Bash/Git Bash.
- El script solo funciona con **JDK 21**. Otras versiones causarán fallo.
- Si usas múltiples JDKs, define el JDK 21 en `doc/ia/user-prompt.md` para que el script lo priorice.
- IntelliJ IDEA debe estar abierto O debes pasar `-MavenCmdPath` explícitamente.

## Próximos pasos (si todo funciona)

1. ✅ Tests ejecutados exitosamente
2. Hacer cambios en el código
3. Ejecutar tests de nuevo con: `powershell -ExecutionPolicy Bypass -File .\scripts\run-tests.ps1`
4. Hacer commit y push en rama `dev` (NO en `main`)

