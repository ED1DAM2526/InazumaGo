# Scripts de Pruebas - InazumaGo

## Descripción General

Este directorio contiene scripts PowerShell para facilitar la ejecución de tests y compilación del proyecto InazumaGo.

## Scripts Disponibles

### 1. `run-tests.ps1` - Tests Unitarios

Ejecuta los tests unitarios del proyecto usando Maven.

**Uso**:
```powershell
.\scripts\run-tests.ps1
.\scripts\run-tests.ps1 -Verbose
```

**Opciones**:
- Sin parámetros: Ejecución normal
- `-Verbose`: Salida detallada de Maven

**Requisitos**:
- Maven 3.9+ o Maven Wrapper (mvnw.cmd)
- Java 21+
- Archivo `doc/ia/user-prompt.md` configurado (recomendado)

**Ejemplo salida**:
```
===============================================
Ejecutando tests de InazumaGo (usando Maven Wrapper)
===============================================

[*] Compilando con Maven Wrapper (mvnw.cmd)...
Tests ejecutados exitosamente!
Reportes en: C:\...\target\surefire-reports
===============================================
```

---

### 2. `run-integration-tests.ps1` - Tests de Integración (WireMock)

Ejecuta los tests de integración con WireMock para stubbing de APIs HTTP.

**Uso**:
```powershell
# Todos los tests (unitarios + integración)
.\scripts\run-integration-tests.ps1

# Solo tests de integración
.\scripts\run-integration-tests.ps1 -SkipUnitTests

# Con salida detallada
.\scripts\run-integration-tests.ps1 -Verbose
```

**Opciones**:
- Sin parámetros: Tests completos
- `-SkipUnitTests`: Solo tests de integración (excluye unitarios)
- `-Verbose`: Salida detallada de Maven

**Requisitos**:
- Maven 3.9+ o Maven Wrapper
- Java 21+
- WireMock 2.35.0+ (declarado en pom.xml)
- Puertos 8080+ disponibles (para WireMock)

**Ejemplo salida**:
```
===============================================
Ejecutando Tests de Integración (WireMock)
===============================================

[*] Limpiando compilación anterior...
[*] Compilación completada.

[*] Ejecutando tests de integración con WireMock...
Modo: Tests completos (unitarios + integración)

Tests de integración completados exitosamente!
Reportes en: C:\...\target\surefire-reports

Resumen de tests:
  - WireMockIntegrationTest
  - MainControllerIntegrationTest
  - FirebaseIntegrationTest
===============================================
```

---

### 3. `use-user-jdk.ps1` - Configurar JDK Local

Aplica la configuración de JDK desde el archivo `doc/ia/user-prompt.md` a la sesión actual.

**Uso**:
```powershell
# Aplicar JDK configurado
.\scripts\use-user-jdk.ps1

# Compilar y ejecutar tests con ese JDK
.\scripts\use-user-jdk.ps1 -RunMaven

# Compilar, tests y lanzar app
.\scripts\use-user-jdk.ps1 -RunMaven -RunMain

# Ejecutar solo la aplicación
.\scripts\use-user-jdk.ps1 -RunMain
```

**Configuración**:
1. Edita `doc/ia/user-prompt.md`
2. Agrega una línea como:
   ```powershell
   $env:JAVA_HOME = 'C:\ruta\a\tu\jdk21'
   ```

**Requisitos**:
- Archivo `doc/ia/user-prompt.md` debe existir
- Archivo debe contener `$env:JAVA_HOME` configurado

---

## Flujo Recomendado de Trabajo

### Desarrollo Local

1. **Primera vez - Configurar JDK** (opcional pero recomendado):
   ```powershell
   # Editar doc/ia/user-prompt.md y agregar ruta del JDK
   notepad doc/ia/user-prompt.md
   ```

2. **Ejecutar tests unitarios**:
   ```powershell
   .\scripts\run-tests.ps1
   ```

3. **Ejecutar tests de integración**:
   ```powershell
   .\scripts\run-integration-tests.ps1
   ```

4. **Si hay errores, ver detalles**:
   ```powershell
   .\scripts\run-integration-tests.ps1 -Verbose
   ```

### Antes de hacer Commit

```powershell
# Ejecutar todos los tests
.\scripts\run-integration-tests.ps1

# Verificar que todo pase
# Si sale código 0, está listo para commit
echo $LASTEXITCODE
```

### En el Pipeline CI/CD

El archivo `ci/pipeline.yml` ejecuta automáticamente:
1. Tests unitarios
2. Tests de integración
3. Empaquetado de la aplicación

No necesitas hacer nada especial; el CI/CD hace todo automáticamente.

---

## Interpretar Códigos de Salida

- **0**: Éxito ✓
- **1**: Error en tests
- **2**: Error en compilación
- **3**: Error en setup/inicialización
- **4**: Maven Wrapper no encontrado
- **5**: Script auxiliar no encontrado

**Verificar código**:
```powershell
.\scripts\run-integration-tests.ps1
$exitCode = $LASTEXITCODE
Write-Host "Código de salida: $exitCode"
```

---

## Reportes de Tests

### Ubicación de Reportes

```
target/
  surefire-reports/              # Reportes XML
    TEST-*.xml                   # Resultados por clase
    TEST-*.txt                   # Resumen legible
  site/
    jacoco/                      # Cobertura de código
      index.html                 # Dashboard interactivo
```

### Ver Reportes

```powershell
# Abrir reportes de cobertura en navegador
Start-Process "target/site/jacoco/index.html"

# Ver reportes XML (en editor de texto)
notepad "target/surefire-reports/TEST-es.iesquevedo.service.MainServiceTest.txt"
```

---

## Troubleshooting

### Problema: "No se puede ejecutar scripts"

**Error**: `File cannot be loaded because running scripts is disabled on this system`

**Solución**:
```powershell
# Permitir scripts solo en sesión actual
Set-ExecutionPolicy -ExecutionPolicy Bypass -Scope Process

# Luego ejecutar script
.\scripts\run-integration-tests.ps1
```

### Problema: "Maven no encontrado"

**Error**: `./mvnw: command not found`

**Solución**:
```powershell
# Verificar que exista mvnw.cmd
ls mvnw.cmd

# Si no existe, regenerar Maven Wrapper
mvn -N io.takari:maven:wrapper
```

### Problema: "Puerto 8080 ya en uso"

**Error**: `Address already in use`

**Solución**:
```powershell
# Cambiar puerto en WireMockIntegrationTest.java (línea 33)
# De: new WireMockServer(8080)
# A:  new WireMockServer(9999)  # o puerto disponible
```

### Problema: "Tests de integración fallan localmente"

**Pasos de debug**:
1. Ejecutar con `-Verbose`:
   ```powershell
   .\scripts\run-integration-tests.ps1 -Verbose
   ```

2. Ver logs detallados:
   ```powershell
   cat target/surefire-reports/TEST-*.txt
   ```

3. Verificar que WireMock está disponible:
   ```powershell
   grep -i wiremock pom.xml
   ```

---

## Variables de Entorno

### `JAVA_HOME`
Ruta al JDK. Configurar en `doc/ia/user-prompt.md`:
```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21'
```

### `MAVEN_OPTS`
Opciones de JVM para Maven (opcional):
```powershell
$env:MAVEN_OPTS = '-Xmx1024m'
```

### `FIREBASE_URL`
URL de Firebase Realtime (para tests que lo requieran):
```powershell
$env:FIREBASE_URL = 'https://tu-proyecto.firebaseio.com'
```

---

## Información Adicional

- **Documentación de WireMock**: [wiremock.org](http://wiremock.org/)
- **Maven Surefire**: [maven.apache.org/surefire](https://maven.apache.org/surefire/maven-surefire-plugin/)
- **Guía de CI/CD**: Ver `doc/ci.md`
- **Integración Pipeline**: Ver `doc/WIREMOCK_INTEGRATION_PIPELINE.md`

---

## Contacto/Soporte

Si encuentras problemas:
1. Revisa la sección **Troubleshooting** arriba
2. Consulta la documentación en `doc/`
3. Abre un issue en el repositorio

