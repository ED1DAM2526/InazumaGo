# Inazuma Go
Este repositorio contiene el proyecto InazumaGo (Java + Maven).

- Para desarrollo local con PowerShell, configura `JDK_PATH` en `doc/ia/user-prompt.md` y aplica la ruta con `scripts/use-user-jdk.ps1`.

Cómo compilar y probar (Windows PowerShell)

```powershell
# Aplica el JDK definido en doc/ia/user-prompt.md (no persistente)
powershell -ExecutionPolicy Bypass -File .\scripts\use-user-jdk.ps1 -NonInteractive

# Ejecuta tests usando el Maven Wrapper incluido en el proyecto
.\mvnw.cmd -DskipTests=false test

# O usa el script recomendado que aplica JDK y ejecuta tests
powershell -ExecutionPolicy Bypass -File .\scripts\run-tests.ps1
```

### Importante: usar siempre el Maven Wrapper

Este proyecto incluye el Maven Wrapper (`mvnw` / `mvnw.cmd`). Usar siempre el wrapper evita depender de la instalación de Maven que trae IntelliJ u otra instalación local. En IntelliJ puedes configurar el proyecto para usar el Maven Wrapper (Settings > Build, Execution, Deployment > Build Tools > Maven → Use Maven wrapper).

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

powershell -ExecutionPolicy Bypass -File .\scripts\use-user-jdk.ps1 -JdkPath 'C:\Program Files\Java\jdk-17' -RunMaven -RunMain

## Configuración de rutas (JDK y Maven Wrapper)

### ⚠️ IMPORTANTE: JDK versión 21 REQUERIDA

**Este proyecto SOLO funciona con JDK versión 21. No uses otras versiones.**

Razones:
- Compatibilidad con las dependencias del proyecto
- Evitar desconfiguración del entorno
- Garantizar que los tests se ejecuten correctamente

### ¿Dónde encontrar mi JDK versión 21?

El proyecto requiere que la ruta al JDK (clave `JDK_PATH`) esté configurada en `doc/ia/user-prompt.md`. Los scripts del repositorio (por ejemplo `scripts\use-user-jdk.ps1` y `scripts\run-tests.ps1`) leerán esa clave y aplicarán la ruta a la sesión antes de ejecutar Maven.

IMPORTANTE: Si `JDK_PATH` no existe o está vacía, los scripts en modo non-interactive ABORTARÁN y mostrarán un mensaje de error claro similar a:

    ERROR: `JDK_PATH` no encontrado en `doc/ia/user-prompt.md`. Configure la ruta al JDK 21 en ese archivo y vuelva a intentarlo.

No se realizará búsqueda automática de JDK en ubicaciones del sistema para ejecuciones non-interactive; la forma soportada de configuración local es mediante `doc/ia/user-prompt.md`.

#### Ejemplos de rutas VÁLIDAS (versión 21):

```
✓ C:\Users\1dam\.jdks\ms-21.0.10        ← Microsoft OpenJDK 21
✓ C:\Users\1dam\.jdks\jdk-21.0.1        ← Oracle JDK 21
✓ C:\Program Files\Java\jdk-21          ← JDK 21 en Program Files
✓ C:\Program Files\Java\openjdk-21      ← OpenJDK 21 en Program Files
```

#### Ejemplos de rutas INVÁLIDAS (NO usar):

```
✗ C:\Users\1dam\.jdks\jdk-17            ← Versión 17 (RECHAZADO)
✗ C:\Users\1dam\.jdks\jdk-20            ← Versión 20 (RECHAZADO)
✗ C:\Program Files\Java\jdk-11          ← Versión 11 (RECHAZADO)
✗ C:\Program Files\Java\jdk-22          ← Versión 22 (RECHAZADO)
```

### ¿Cómo instalar JDK versión 21 en la ubicación correcta?

#### Opción A: Usar la carpeta `.jdks` del usuario (RECOMENDADO)

1. **Descarga SOLO JDK versión 21** desde:
   - Microsoft OpenJDK 21: https://www.microsoft.com/openjdk
   - Oracle JDK 21: https://www.oracle.com/java/technologies/downloads/
   - Eclipse Temurin 21: https://adoptium.net/

   ⚠️ **IMPORTANTE**: Asegúrate de descargar la versión **21** (no 17, 20, 22, etc.)

2. Crea la carpeta si no existe:
   ```powershell
   mkdir C:\Users\$env:USERNAME\.jdks
   ```

3. Extrae el JDK versión 21 en esa carpeta:
   ```
   C:\Users\[tu-usuario]\.jdks\
   ├── jdk-21/               ← Carpeta con JDK 21
   │   ├── bin/
   │   ├── lib/
   │   └── ...
   └── ms-21.0.10/           ← O con este nombre (JDK 21)
       ├── bin/
       ├── lib/
       └── ...
   ```

   ⚠️ **NO extraigas** jdk-17, jdk-20, jdk-22, etc.

4. El script lo encontrará automáticamente al ejecutar:
   ```powershell
   .\scripts\run-tests.ps1
   ```

#### Opción B: Instalar JDK 21 en Program Files

1. Descarga **SOLO JDK versión 21** desde:
   - https://www.oracle.com/java/technologies/downloads/
   - https://www.microsoft.com/openjdk

2. Instala normalmente (el instalador te pedirá la carpeta destino)

3. Asegúrate de que se instala en:
   ```
   C:\Program Files\Java\jdk-21\
   ```

   ⚠️ **Verifica que es versión 21:**
   ```powershell
   Test-Path "C:\Program Files\Java\jdk-21\bin\java.exe"
   ```

4. Ejecuta el script:
   ```powershell
   .\scripts\run-tests.ps1
   ```

### Verificar que el JDK versión 21 está correctamente instalado

Abre PowerShell y ejecuta:

```powershell
java -version
```

**Resultado CORRECTO (versión 21):**

```
openjdk version "21.0.10" 2026-01-20 LTS
OpenJDK Runtime Environment Microsoft-13106404 (build 21.0.10+7-LTS)
OpenJDK 64-Bit Server VM Microsoft-13106404 (build 21.0.10+7-LTS, mixed mode, sharing)
```

**Resultado INCORRECTO (otra versión):**

```
openjdk version "17.0.5" 2022-10-18
OpenJDK Runtime Environment (build 17.0.5+8-LTS)
OpenJDK 64-Bit Server VM (build 17.0.5+8-LTS, mixed mode, sharing)
```

⚠️ **Si ves otra versión (17, 20, 22, etc.), debes desinstalarlo y descargar JDK versión 21**

### ¿Dónde está el Maven Wrapper?

El Maven Wrapper está **incluido** en el proyecto en:

```
InazumaGo/
├── .mvn/
│   └── wrapper/
│       ├── maven-wrapper.jar     ← Aquí está (NO tocar)
│       └── maven-wrapper.properties
├── mvnw.cmd                       ← Script Windows (NO tocar)
├── mvnw                           ← Script Linux/Mac (NO tocar)
└── pom.xml
```

**No necesitas modificar nada del Maven Wrapper.** El script `run-tests.ps1` lo usa automáticamente.

### Solucionar problemas de configuración

#### Problema: "No compiler is provided"

**Causa:** JDK no encontrado, es JRE, o es versión diferente a 21

**Solución:**
```powershell
# 1. Verifica que tienes JDK versión 21
java -version

# 2. Si ves versión diferente, desinstala y descarga JDK 21
# Descarga desde: https://www.microsoft.com/openjdk

# 3. Coloca en: C:\Users\[tu-usuario]\.jdks\jdk-21
# 4. Vuelve a ejecutar:
.\scripts\run-tests.ps1
```

#### Problema: Tengo JDK versión 17 u otra versión instalada

**Causa:** El proyecto requiere específicamente JDK 21

**Solución:**
```powershell
# Opción 1: Desinstalar la otra versión
# Panel de Control > Programas > Desinstalar programa
# Busca "Java" y desinstala cualquier versión que no sea 21

# Opción 2: Si quieres mantener ambas versiones
# Descarga JDK 21 en una carpeta separada
# Coloca en: C:\Users\[usuario]\.jdks\jdk-21
# El script usará la versión 21 automáticamente
```

#### Problema: Script no encuentra JDK versión 21

**Causa:** JDK 21 en ubicación no estándar

**Solución:**
```powershell
# Verifica dónde está tu JDK 21
dir "C:\Users\$env:USERNAME\.jdks"
dir "C:\Program Files\Java"

# Si lo encontraste en otra ubicación, muévelo a:
Move-Item "C:\Tu-Ruta\jdk-21" "C:\Users\$env:USERNAME\.jdks\jdk-21"

# Luego ejecuta:
.\scripts\run-tests.ps1
```
