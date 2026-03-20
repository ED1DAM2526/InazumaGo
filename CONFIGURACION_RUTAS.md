# 📍 Guía Rápida: Configuración de Rutas (JDK versión 21 y Maven Wrapper)

## 🎯 Resumen ejecutivo

El proyecto InazumaGo incluye todo lo necesario para ejecutar tests automáticamente. Solo necesitas:

1. **Tener JDK versión 21 instalado** (SOLO versión 21, no otra)
2. **Colocarlo en una ruta estándar** (el script lo buscará automáticamente)
3. **Ejecutar el script**: `.\scripts\run-tests.ps1`

---

## ⚠️ IMPORTANTE: SOLO JDK versión 21

**Este proyecto SOLO funciona con JDK versión 21.**

❌ **NO USAR**: Versiones 17, 20, 22, 11, etc.

✅ **USAR**: Versión 21 (21.0.1, 21.0.10, etc.)

**Razones:**
- Compatibilidad con dependencias del proyecto
- Evitar problemas de compilación
- Garantizar funcionamiento correcto de tests

---

## 📂 Dónde colocar JDK versión 21

El script busca automáticamente en este orden:

### 1️⃣ RECOMENDADO: Carpeta `.jdks` del usuario

```
C:\Users\[tu-usuario]\.jdks\
├── jdk-21/                    ← VERSIÓN 21 REQUERIDA
│   ├── bin/ → java.exe aquí
│   └── lib/
└── ms-21.0.10/               ← VERSIÓN 21 REQUERIDA
    ├── bin/ → java.exe aquí
    └── lib/
```

**VÁLIDO:**
```
C:\Users\1dam\.jdks\jdk-21\
C:\Users\1dam\.jdks\ms-21.0.10\
C:\Users\1dam\.jdks\openjdk-21\
```

**NO VÁLIDO:**
```
❌ C:\Users\1dam\.jdks\jdk-17\       ← Versión 17 (NO usar)
❌ C:\Users\1dam\.jdks\jdk-20\       ← Versión 20 (NO usar)
❌ C:\Users\1dam\.jdks\jdk-22\       ← Versión 22 (NO usar)
```

**Pasos:**

```powershell
# 1. Crea la carpeta
mkdir C:\Users\$env:USERNAME\.jdks

# 2. Descarga SOLO JDK versión 21 desde:
# https://www.microsoft.com/openjdk    ← Microsoft JDK 21
# https://www.oracle.com/java/technologies/downloads/  ← Oracle JDK 21
# https://adoptium.net/                ← Eclipse Temurin 21

# 3. Extrae en la carpeta .jdks
# Debe quedar: C:\Users\1dam\.jdks\jdk-21

# 4. Verifica
Test-Path "C:\Users\1dam\.jdks\jdk-21\bin\java.exe"

# 5. El script lo encontrará automáticamente
.\scripts\run-tests.ps1
```

### 2️⃣ Alternativa: Program Files (Instalación estándar)

```
C:\Program Files\Java\
├── jdk-21/                    ← VERSIÓN 21 REQUERIDA
│   ├── bin/ → java.exe aquí
│   └── lib/
└── NO desinstales otras versiones si no las necesitas
```

**VÁLIDO:**
```
C:\Program Files\Java\jdk-21\
C:\Program Files\Java\openjdk-21\
```

**NO VÁLIDO:**
```
❌ C:\Program Files\Java\jdk-17\      ← Versión 17 (NO usar)
❌ C:\Program Files\Java\jdk-20\      ← Versión 20 (NO usar)
```

**Pasos:**

```powershell
# 1. Descarga SOLO JDK versión 21
# https://www.oracle.com/java/technologies/downloads/

# 2. Instala normalmente

# 3. Verifica que es versión 21
Test-Path "C:\Program Files\Java\jdk-21\bin\java.exe"

# 4. Ejecuta el script
.\scripts\run-tests.ps1
```

### 3️⃣ Ubicación personalizada (NO RECOMENDADO)

Si tienes JDK versión 21 en otra ruta:

```powershell
# Opción A: Mover a ruta estándar
Move-Item "C:\Mi-JDK\jdk-21" "C:\Users\$env:USERNAME\.jdks\jdk-21"
.\scripts\run-tests.ps1

# Opción B: Configurar JAVA_HOME temporalmente
$env:JAVA_HOME = 'C:\Mi-Ruta\jdk-21'
.\scripts\run-tests.ps1
```

---

## ✅ Verificar que tienes JDK versión 21

Abre PowerShell y ejecuta:

```powershell
java -version
```

**Resultado CORRECTO:**

```
openjdk version "21.0.10" 2026-01-20 LTS
OpenJDK Runtime Environment Microsoft-13106404 (build 21.0.10+7-LTS)
OpenJDK 64-Bit Server VM Microsoft-13106404 (build 21.0.10+7-LTS, mixed mode, sharing)
```

↑ Si ves "21" en la primera línea, ¡está bien!

**Resultado INCORRECTO (versión diferente):**

```
openjdk version "17.0.5" 2022-10-18
OpenJDK Runtime Environment (build 17.0.5+8-LTS)
OpenJDK 64-Bit Server VM (build 17.0.5+8-LTS, mixed mode, sharing)
```

↑ Si ves "17", "20", "22", etc., debes descargar JDK versión 21

---

## 📦 Dónde está el Maven Wrapper

**NO necesitas hacer nada.** El Maven Wrapper está incluido:

```
InazumaGo/
├── .mvn/
│   └── wrapper/
│       ├── maven-wrapper.jar      ✓ Presente (automático)
│       └── maven-wrapper.properties
├── mvnw.cmd                        ✓ Script Windows (automático)
├── mvnw                            ✓ Script Linux/Mac (automático)
└── pom.xml
```

El script `run-tests.ps1` lo usa automáticamente.

---

## 🚀 Ejecutar tests

### Una vez configurado JDK versión 21:

```powershell
cd C:\Users\1dam\IdeaProjects\InazumaGo
.\scripts\run-tests.ps1
```

### Resultado esperado:

```
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0

[INFO] BUILD SUCCESS
[INFO] Total time: 19.429 s
```

---

## 🔧 Solucionar problemas comunes

### Error: "No compiler is provided"

```
[ERROR] No compiler is provided in this environment. Perhaps you are running on a JRE rather than a JDK?
```

**Causa principal:** 
1. No tienes JDK (tienes JRE)
2. Tienes JDK pero versión diferente a 21

**Solución:**
```powershell
# 1. Verifica qué versión tienes
java -version

# 2. Si no ves "21", descarga JDK versión 21:
# https://www.microsoft.com/openjdk

# 3. Coloca en: C:\Users\[usuario]\.jdks\jdk-21

# 4. Vuelve a ejecutar
.\scripts\run-tests.ps1
```

---

### Error: "Tengo JDK 17 (u otra versión) instalada"

```
openjdk version "17.0.5" 2022-10-18
```

**Causa:** Proyecto requiere versión 21

**Solución:**

```powershell
# Opción 1: Desinstalar versión anterior
# Panel de Control > Programas > Desinstalar programa
# Busca "Java 17" y desinstala

# Opción 2: Instalar JDK 21 junto a la otra
# Descarga JDK 21 desde: https://www.microsoft.com/openjdk
# Coloca en: C:\Users\[usuario]\.jdks\jdk-21
# El script usará versión 21 automáticamente
```

---

### Error: "Cannot find mvnw.cmd"

```
El archivo mvnw.cmd no existe
```

**Causa:** No estás en la carpeta correcta

**Solución:**
```powershell
# Navega a la raíz del proyecto
cd C:\Users\1dam\IdeaProjects\InazumaGo

# Verifica que existe
ls mvnw.cmd

# Ejecuta
.\scripts\run-tests.ps1
```

---

### Error: "JDK not found"

```
[*] Buscando JDK...
[ERROR] JDK no encontrado
```

**Causa:** JDK versión 21 no en ubicación estándar

**Solución:**

1. Coloca JDK 21 en:
   ```
   C:\Users\1dam\.jdks\jdk-21\
   ```

2. O configura manualmente:
   ```powershell
   $env:JAVA_HOME = 'C:\Tu-Ruta\jdk-21'
   $env:Path = "C:\Tu-Ruta\jdk-21\bin;$env:Path"
   .\scripts\run-tests.ps1
   ```

---

## 📊 Resumen de ubicaciones

| Componente | Ubicación | Versión | Automático |
|-----------|-----------|---------|-----------|
| JDK | `C:\Users\[usuario]\.jdks\jdk-21\` | **21 REQUERIDA** | ✓ Buscado |
| JDK (alternativa) | `C:\Program Files\Java\jdk-21\` | **21 REQUERIDA** | ✓ Buscado |
| Maven Wrapper JAR | `.mvn/wrapper/maven-wrapper.jar` | Incluido | ✓ Presente |
| mvnw.cmd | `./mvnw.cmd` | Incluido | ✓ Presente |
| mvnw | `./mvnw` | Incluido | ✓ Presente |
| Test Script | `./scripts/run-tests.ps1` | Incluido | ✓ Presente |

---

## 📚 Más información

- Ver: `README.md` - Documentación completa
- Ver: `TESTS_SETUP.md` - Detalles técnicos
- Ejecutar: `.\scripts\run-tests.ps1 -Verbose` - Modo debug

---

## 📂 Dónde colocar el JDK

El script busca automáticamente en este orden:

### 1️⃣ RECOMENDADO: Carpeta `.jdks` del usuario

```
C:\Users\[tu-usuario]\.jdks\
├── jdk-21/
│   ├── bin/ → java.exe aquí
│   └── lib/
└── ms-21.0.10/
    ├── bin/ → java.exe aquí
    └── lib/
```

**Pasos:**

```powershell
# 1. Crea la carpeta
mkdir C:\Users\$env:USERNAME\.jdks

# 2. Descarga JDK desde:
# https://www.microsoft.com/openjdk
# https://www.oracle.com/java/technologies/downloads/
# https://adoptium.net/

# 3. Extrae en la carpeta .jdks
# Ejemplo: C:\Users\1dam\.jdks\jdk-21

# 4. El script lo encontrará automáticamente
.\scripts\run-tests.ps1
```

### 2️⃣ Alternativa: Program Files (Instalación estándar)

```
C:\Program Files\Java\
├── jdk-21/
│   ├── bin/ → java.exe aquí
│   └── lib/
└── jdk-17/
    ├── bin/ → java.exe aquí
    └── lib/
```

**Pasos:**

```powershell
# 1. Descarga e instala normalmente
# https://www.oracle.com/java/technologies/downloads/

# 2. Se instalará en Program Files automáticamente

# 3. Verifica
Test-Path "C:\Program Files\Java\jdk-21\bin\java.exe"

# 4. Ejecuta el script
.\scripts\run-tests.ps1
```

### 3️⃣ Ubicación personalizada (manual)

Si tienes JDK en otra ruta:

```powershell
# Opción A: Mover a ruta estándar
Move-Item "C:\Mi-JDK\jdk-21" "C:\Users\$env:USERNAME\.jdks\jdk-21"
.\scripts\run-tests.ps1

# Opción B: Configurar JAVA_HOME temporalmente
$env:JAVA_HOME = 'C:\Mi-Ruta\jdk-21'
.\scripts\run-tests.ps1

# Opción C: Usar el script de JDK
powershell -ExecutionPolicy Bypass -File .\scripts\use-user-jdk.ps1 -JdkPath 'C:\Mi-Ruta\jdk-21'
```

---

## ✅ Verificar que el JDK está bien instalado

Abre PowerShell y ejecuta:

```powershell
java -version
```

**Resultado esperado:**

```
openjdk version "21.0.10" 2026-01-20 LTS
OpenJDK Runtime Environment Microsoft-13106404 (build 21.0.10+7-LTS)
OpenJDK 64-Bit Server VM Microsoft-13106404 (build 21.0.10+7-LTS, mixed mode, sharing)
```

**Resultado incorrecto (JRE, no JDK):**

```
java version "21.0.10" 2026-01-20 LTS
Java(TM) SE Runtime Environment (build 21.0.10+7)
Java HotSpot(TM) 64-Bit Server VM (build 21.0.10+7, mixed mode, sharing)
```

↑ **Si ves esto, necesitas descargar JDK, no JRE**

---

## 📦 Dónde está el Maven Wrapper

**NO necesitas hacer nada.** El Maven Wrapper está incluido:

```
InazumaGo/
├── .mvn/
│   └── wrapper/
│       ├── maven-wrapper.jar      ✓ Presente (automático)
│       └── maven-wrapper.properties
├── mvnw.cmd                        ✓ Script Windows (automático)
├── mvnw                            ✓ Script Linux/Mac (automático)
└── pom.xml
```

El script `run-tests.ps1` lo usa automáticamente.

---

## 🚀 Ejecutar tests

### Una vez configurado el JDK:

```powershell
cd C:\Users\1dam\IdeaProjects\InazumaGo
.\scripts\run-tests.ps1
```

### Resultado esperado:

```
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0

[INFO] BUILD SUCCESS
[INFO] Total time: 19.429 s
```

---

## 🔧 Solucionar problemas comunes

### Error: "No compiler is provided"

```
[ERROR] No compiler is provided in this environment. Perhaps you are running on a JRE rather than a JDK?
```

**Causa:** Tienes JRE en lugar de JDK

**Solución:**
1. Descarga JDK desde: https://www.microsoft.com/openjdk
2. Coloca en: `C:\Users\[usuario]\.jdks\jdk-21`
3. Vuelve a ejecutar: `.\scripts\run-tests.ps1`

---

### Error: "Cannot find mvnw.cmd"

```
El archivo mvnw.cmd no existe
```

**Causa:** No estás en la carpeta correcta

**Solución:**
```powershell
# Navega a la raíz del proyecto
cd C:\Users\1dam\IdeaProjects\InazumaGo

# Verifica que existe
ls mvnw.cmd

# Ejecuta
.\scripts\run-tests.ps1
```

---

### Error: "JDK not found"

```
[*] Buscando JDK...
[ERROR] JDK no encontrado
```

**Causa:** JDK no en ubicación estándar

**Solución:**

Opción 1: Mover JDK
```powershell
# Descomprime aquí
C:\Users\1dam\.jdks\jdk-21\
```

Opción 2: Configurar manualmente
```powershell
$env:JAVA_HOME = 'C:\Tu-Ruta\jdk-21'
$env:Path = "C:\Tu-Ruta\jdk-21\bin;$env:Path"
.\scripts\run-tests.ps1
```

---

## 📊 Resumen de ubicaciones

| Componente | Ubicación | Automático |
|-----------|-----------|-----------|
| JDK | `C:\Users\[usuario]\.jdks\jdk-21\` | ✓ Buscado |
| JDK (alternativa) | `C:\Program Files\Java\jdk-21\` | ✓ Buscado |
| Maven Wrapper JAR | `.mvn/wrapper/maven-wrapper.jar` | ✓ Incluido |
| mvnw.cmd | `./mvnw.cmd` | ✓ Incluido |
| mvnw | `./mvnw` | ✓ Incluido |
| Test Script | `./scripts/run-tests.ps1` | ✓ Incluido |

---

## 📚 Más información

- Ver: `README.md` - Documentación completa
- Ver: `TESTS_SETUP.md` - Detalles técnicos
- Ejecutar: `.\scripts\run-tests.ps1 -Verbose` - Modo debug


