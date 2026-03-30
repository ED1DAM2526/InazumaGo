# CI/CD — Guía de Integración Continua

**Versión:** 1.0  
**Fecha:** 2026-03-30  
**Responsable:** DevOps, Motor

## Tabla de contenidos

1. [Introducción](#introducción)
2. [Ejecución local de tests](#ejecución-local-de-tests)
3. [Pipeline CI (genérica)](#pipeline-ci-genérica)
4. [Pasos de la pipeline](#pasos-de-la-pipeline)
5. [Troubleshooting](#troubleshooting)

## Introducción

Este documento describe cómo ejecutar la pipeline de CI para InazumaGo tanto localmente como en un servidor CI/CD (GitHub Actions, GitLab CI, etc.).

**Objetivo:** Garantizar que cada commit/PR tenga tests verdes, build reproducible y sin errores de compilación.

---

## Ejecución local de tests

### Prerequisitos

- JDK 21 instalado
- Maven 3.6+

### Comando rápido

```powershell
# En PowerShell (Windows)
mvn clean test
```

### Paso a paso

```powershell
# 1. Limpiar build anterior
mvn clean

# 2. Compilar código fuente
mvn compile

# 3. Ejecutar tests unitarios
mvn test

# 4. Ver resultados
# Los reportes de Surefire están en:
# target/surefire-reports/index.html
```

### Con script de JDK local

Si usas `doc/ia/user-prompt.md` para JDK:

```powershell
# Configurar JDK y ejecutar tests
.\scripts\use-user-jdk.ps1 -RunMaven
```

### Empaquetar después de tests

```powershell
# Compilar + tests + empaquetar
mvn clean package

# El JAR estará en:
# target/InazumaGo-1.0-SNAPSHOT.jar
```

---

## Pipeline CI (genérica)

### Diagrama de flujo

```
┌─────────────────┐
│ Commit/Push     │
└────────┬────────┘
         │
         ▼
┌─────────────────────┐
│ Checkout código     │
└────────┬────────────┘
         │
         ▼
┌─────────────────────┐
│ Setup JDK 21        │
└────────┬────────────┘
         │
         ▼
┌─────────────────────┐
│ mvn clean compile   │
└────────┬────────────┘
         │
         ▼
┌─────────────────────┐
│ mvn test            │
└────────┬────────────┘
         │
    ┌────┴────┐
    │          │
  PASS       FAIL
    │          │
    ▼          ▼
┌──────┐  ┌──────────────┐
│ Cont.│  │ Error report │
└──┬───┘  └──────────────┘
   │
   ▼
┌─────────────────────┐
│ mvn package         │
└────────┬────────────┘
         │
    ┌────┴────┐
    │          │
  PASS       FAIL
    │          │
    ▼          ▼
┌──────────┐ ┌──────┐
│ Artifacts│ │Fail  │
└──────────┘ └──────┘
```

---

## Pasos de la pipeline

### Paso 1: Checkout

**Acción:** Descargar código del repositorio

```yaml
# Ejemplo GitHub Actions
- uses: actions/checkout@v3
```

**Verificación:** Código debe estar disponible en el workspace

---

### Paso 2: Setup JDK

**Acción:** Instalar y configurar JDK 21

```yaml
# Ejemplo GitHub Actions
- uses: actions/setup-java@v3
  with:
    java-version: '21'
    distribution: 'temurin'
    cache: maven
```

**Verificación:** `java -version` devuelve 21.x

---

### Paso 3: Compilación

**Acción:** Compilar código fuente

```bash
mvn clean compile
```

**Verificación:**
- No hay errores de compilación
- `target/classes/` contiene `.class` compilados

**En caso de fallo:**
- Revisar errores de sintaxis
- Verificar que todas las dependencias están disponibles (pom.xml)

---

### Paso 4: Ejecución de Tests

**Acción:** Ejecutar tests unitarios

```bash
mvn -DskipTests=false test
```

**Verificación:**
- Todos los tests pasan (BUILD SUCCESS)
- Reportes en `target/surefire-reports/`

**En caso de fallo:**
- Revisar test logs en output
- Ejecutar test fallido localmente: `mvn test -Dtest=NombreTestFallido`

---

### Paso 5: Empaquetado

**Acción:** Generar JAR ejecutable

```bash
mvn package
```

**Verificación:**
- JAR generado en `target/InazumaGo-1.0-SNAPSHOT.jar`
- Tamaño razonable (>1 MB típicamente)

**En caso de fallo:**
- Revisar que pom.xml es válido
- Verificar que tests pasaron antes

---

### Paso 6: Artefactos (opcional)

**Acción:** Guardar JAR generado como artefacto

```yaml
# Ejemplo GitHub Actions
- uses: actions/upload-artifact@v3
  with:
    name: InazumaGo-build
    path: target/InazumaGo-1.0-SNAPSHOT.jar
```

**Verificación:** Artefacto descargable desde UI de GitHub Actions

---

## Ejemplo de pipeline completa (GitHub Actions)

Archivo: `.github/workflows/ci.yml`

```yaml
name: CI

on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 21
      uses: actions/setup-java@v3
      with:
        java-version: '21'
        distribution: 'temurin'
        cache: maven
    
    - name: Compile
      run: mvn clean compile
    
    - name: Run tests
      run: mvn -DskipTests=false test
    
    - name: Package
      run: mvn package
    
    - name: Upload artifacts
      uses: actions/upload-artifact@v3
      if: success()
      with:
        name: InazumaGo-build
        path: target/InazumaGo-1.0-SNAPSHOT.jar
    
    - name: Publish test results
      uses: EnricoMi/publish-unit-test-result-action@v2
      if: always()
      with:
        files: target/surefire-reports/TEST-*.xml
```

---

## Pasos de la pipeline en Windows (PowerShell)

Si usas CI local con PowerShell:

```powershell
# Archivo: ci/run-ci.ps1

param(
    [switch]$SkipTests
)

$ErrorActionPreference = "Stop"

Write-Host "=== InazumaGo CI Pipeline ==="
Write-Host ""

# Paso 1: Checkout (ya existe)
Write-Host "[1/5] Código disponible"

# Paso 2: Setup JDK
Write-Host "[2/5] Setup JDK..."
.\scripts\use-user-jdk.ps1 -RunMaven

# Paso 3: Compilación
Write-Host "[3/5] Compilar..."
mvn clean compile
if ($LASTEXITCODE -ne 0) { throw "Compilación falló" }

# Paso 4: Tests
if (-not $SkipTests) {
    Write-Host "[4/5] Ejecutar tests..."
    mvn -DskipTests=false test
    if ($LASTEXITCODE -ne 0) { throw "Tests fallaron" }
} else {
    Write-Host "[4/5] Saltando tests (--SkipTests)"
}

# Paso 5: Empaquetado
Write-Host "[5/5] Empaquetar..."
mvn package
if ($LASTEXITCODE -ne 0) { throw "Packaging falló" }

Write-Host ""
Write-Host "✅ Pipeline completada exitosamente"
Write-Host "Artefacto: target/InazumaGo-1.0-SNAPSHOT.jar"
```

Uso:

```powershell
# Ejecutar pipeline completa
.\ci\run-ci.ps1

# Saltar tests
.\ci\run-ci.ps1 -SkipTests
```

---

## Troubleshooting

### Problema: "mvn: command not found"

**Causa:** Maven no está en PATH

**Solución:**
- Instala Maven: https://maven.apache.org/download.cgi
- O usa el script: `.\scripts\use-user-jdk.ps1 -RunMaven`

---

### Problema: "javac: command not found"

**Causa:** JDK no está en PATH

**Solución:**
- Instala JDK 21: https://www.oracle.com/java/technologies/downloads/
- O configura `JAVA_HOME` en `doc/ia/user-prompt.md`

---

### Problema: Tests fallan solo en CI

**Causa:** Diferencias entre máquina local y CI (JDK version, path separators, etc.)

**Solución:**
- Usa contenedores Docker en CI (garantiza entorno consistente)
- Usa rutas relativas en tests, no absolutas
- Verifica versión de JDK en CI vs local

---

### Problema: Build SUCCESS pero tests no corren

**Causa:** `pom.xml` tiene `<skipTests>true</skipTests>` o no hay tests detectados

**Solución:**
```bash
# Forzar ejecución de tests
mvn clean test -DskipTests=false

# Verificar que hay tests
mvn test -DtestFailureIgnore=true
```

---

### Problema: Out of memory en CI

**Causa:** CI tiene poca memoria para JVM

**Solución:**
```bash
export MAVEN_OPTS="-Xmx512m"
mvn clean test
```

---

## Mejores prácticas

1. **Ejecuta tests localmente antes de push:**
   ```powershell
   mvn clean test
   ```

2. **Revisa reportes de tests:**
   ```
   target/surefire-reports/index.html
   ```

3. **No commits sin tests verdes:**
   - Fuerza tests en pre-commit hooks (opcional)

4. **Documenta cambios en CI:**
   - Si modificas `.github/workflows/` o `pom.xml`, describe cambios en PR

5. **Mantén CI rápido:**
   - Cache de Maven (ver arriba)
   - Evita downloads innecesarios
   - Paraleliza tests si es posible

---

## Próximos pasos

- [ ] Configurar CI en GitHub (crear `.github/workflows/ci.yml`)
- [ ] Añadir SonarQube o linting (análisis de código)
- [ ] Configurar artefactos para descargar desde CI
- [ ] Automatizar releases desde CI

---

**Contacto:** Equipo DevOps  
**Última actualización:** 2026-03-30

