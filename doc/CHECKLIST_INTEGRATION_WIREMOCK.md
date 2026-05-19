# Checklist: Integración de Tests de Integración con WireMock en Pipeline

> Verifica que todo esté correctamente configurado para ejecutar tests de integración con WireMock.

## ✅ Requisitos del Sistema

- [ ] Java 21 o superior instalado
  ```powershell
  java -version
  ```
- [ ] Maven 3.9+ disponible (o Maven Wrapper presente)
  ```powershell
  mvn -version
  # o verificar
  ls mvnw.cmd
  ```
- [ ] PowerShell 5.1 o superior (Windows)
  ```powershell
  $PSVersionTable.PSVersion
  ```

## ✅ Archivos Creados/Actualizados

- [ ] **`scripts/run-integration-tests.ps1`**
  - Script PowerShell para ejecutar tests de integración
  - ```powershell
    .\scripts\run-integration-tests.ps1
    ```

- [ ] **`src/test/java/es/iesquevedo/integration/WireMockIntegrationTest.java`**
  - Tests de ejemplo con WireMock
  - Ejemplos de stubbing GET, POST, múltiples endpoints
  - Manejo de errores 404

- [ ] **`ci/pipeline.yml`** (Actualizado)
  - 3 jobs: Unit Tests, Integration Tests, Build Package
  - Caché de Maven
  - Artefactos de reportes

- [ ] **`pom.xml`** (Actualizado)
  - Configuración mejorada de Surefire para filtrar tests
  - Timeout aumentado para tests de integración

- [ ] **`doc/WIREMOCK_INTEGRATION_PIPELINE.md`**
  - Documentación completa del pipeline
  - Ejemplos de uso
  - Troubleshooting

- [ ] **`doc/QUICK_START_INTEGRATION_TESTS.md`**
  - Guía rápida (3 pasos)
  - Ejemplos prácticos
  - Preguntas frecuentes

- [ ] **`scripts/README.md`**
  - Documentación de scripts disponibles
  - Flujo de trabajo recomendado

## ✅ Dependencias en pom.xml

- [ ] WireMock 2.35.0 declarado
  ```xml
  <dependency>
      <groupId>com.github.tomakehurst</groupId>
      <artifactId>wiremock-jre8</artifactId>
      <version>2.35.0</version>
      <scope>test</scope>
  </dependency>
  ```

- [ ] JUnit 5 disponible
  ```xml
  <dependency>
      <groupId>org.junit.jupiter</groupId>
      <artifactId>junit-jupiter</artifactId>
      <version>5.10.0</version>
      <scope>test</scope>
  </dependency>
  ```

- [ ] Mockito disponible
  ```xml
  <dependency>
      <groupId>org.mockito</groupId>
      <artifactId>mockito-core</artifactId>
      <!-- ... -->
  </dependency>
  ```

- [ ] JaCoCo para cobertura
  ```xml
  <plugin>
      <groupId>org.jacoco</groupId>
      <artifactId>jacoco-maven-plugin</artifactId>
      <!-- ... -->
  </plugin>
  ```

## ✅ Ejecución Local

### Tests Unitarios
- [ ] Ejecutar tests unitarios
  ```powershell
  .\scripts\run-tests.ps1
  ```
- [ ] Verificar que todos pasen
  ```powershell
  $LASTEXITCODE -eq 0
  ```

### Tests de Integración
- [ ] Ejecutar tests de integración
  ```powershell
  .\scripts\run-integration-tests.ps1
  ```
- [ ] Verificar que todos pasen
  ```powershell
  $LASTEXITCODE -eq 0
  ```

### Tests Individuales
- [ ] Ejecutar solo WireMockIntegrationTest
  ```powershell
  .\mvnw -DskipTests=false test -Dtest=WireMockIntegrationTest
  ```
- [ ] Verificar que pase
  ```powershell
  $LASTEXITCODE -eq 0
  ```

- [ ] Ejecutar solo MainControllerIntegrationTest
  ```powershell
  .\mvnw -DskipTests=false test -Dtest=MainControllerIntegrationTest
  ```

- [ ] Ejecutar solo FirebaseIntegrationTest
  ```powershell
  .\mvnw -DskipTests=false test -Dtest=FirebaseIntegrationTest
  ```

## ✅ Reportes Generados

- [ ] Reportes en `target/surefire-reports/`
  ```powershell
  ls target\surefire-reports\
  ```

- [ ] Archivos XML para cada test
  - [ ] `TEST-es.iesquevedo.integration.WireMockIntegrationTest.xml`
  - [ ] `TEST-es.iesquevedo.integration.MainControllerIntegrationTest.xml`
  - [ ] `TEST-es.iesquevedo.integration.FirebaseIntegrationTest.xml`

- [ ] Archivos TXT legibles
  - [ ] `TEST-es.iesquevedo.integration.WireMockIntegrationTest.txt`
  - [ ] `TEST-es.iesquevedo.integration.MainControllerIntegrationTest.txt`
  - [ ] `TEST-es.iesquevedo.integration.FirebaseIntegrationTest.txt`

- [ ] Cobertura JaCoCo en `target/site/jacoco/`
  ```powershell
  ls target\site\jacoco\
  ```

## ✅ Pipeline CI/CD (ci/pipeline.yml)

### Estructura de Jobs
- [ ] **Job 1: unit-tests**
  - [ ] Ejecuta `mvnw clean compile -q`
  - [ ] Ejecuta `mvnw -DskipTests=false -Dgroups=\!integration test`
  - [ ] Recopila `target/surefire-reports/**`
  - [ ] Guarda caché de Maven

- [ ] **Job 2: integration-tests**
  - [ ] Depende de `unit-tests`
  - [ ] Ejecuta `mvnw clean compile -q`
  - [ ] Ejecuta `mvnw -DskipTests=false -Dgroups=integration test`
  - [ ] Ejecuta `mvnw jacoco:report`
  - [ ] Recopila reportes

- [ ] **Job 3: build**
  - [ ] Depende de `unit-tests` e `integration-tests`
  - [ ] Ejecuta `mvnw clean package -DskipTests -q`
  - [ ] Genera JAR y ZIP

### Configuración
- [ ] Variables de entorno: `JAVA_VERSION`, `MAVEN_OPTS`
- [ ] Triggers: `push`, `pull_request`
- [ ] Imagen Docker: `eclipse-temurin:21`

## ✅ Documentación Actualizada

- [ ] README.md menciona tests de integración
- [ ] doc/ci.md actualizado con comandos nuevos
- [ ] scripts/README.md documenta nuevos scripts

## ✅ Ejemplo de Uso End-to-End

1. [ ] Crear rama feature
   ```powershell
   git checkout -b feature/test-wiremock
   ```

2. [ ] Agregar cambio a un archivo de test
   ```powershell
   notepad src\test\java\es\iesquevedo\integration\WireMockIntegrationTest.java
   ```

3. [ ] Ejecutar tests localmente
   ```powershell
   .\scripts\run-integration-tests.ps1
   ```

4. [ ] Verificar que pasan (código 0)
   ```powershell
   echo $LASTEXITCODE  # Debe ser 0
   ```

5. [ ] Commit y push
   ```powershell
   git add -A
   git commit -m "Add WireMock integration tests"
   git push origin feature/test-wiremock
   ```

6. [ ] Abrir Pull Request
   - [ ] Pipeline CI/CD ejecuta automáticamente

7. [ ] Verificar que pipeline pasa
   - [ ] ✅ unit-tests
   - [ ] ✅ integration-tests
   - [ ] ✅ build

## ✅ Troubleshooting Verificado

- [ ] **Puerto 8080 disponible**
  ```powershell
  netstat -ano | findstr :8080
  # Si hay algo, cambiar puerto en WireMockIntegrationTest
  ```

- [ ] **Permisos de ejecución de scripts**
  ```powershell
  Get-ExecutionPolicy
  # Si es "Restricted", ejecutar:
  Set-ExecutionPolicy -ExecutionPolicy Bypass -Scope Process
  ```

- [ ] **Maven Wrapper presente**
  ```powershell
  ls mvnw.cmd
  # Debe existir
  ```

- [ ] **Dependencias descargadas**
  ```powershell
  # Maven descargará automáticamente al ejecutar tests
  .\mvnw dependency:resolve
  ```

## ✅ Validación Final

### Tests Locales
```powershell
# Ejecutar suite completa
.\scripts\run-integration-tests.ps1

# Debe mostrar:
# ✓ Tests de integración completados exitosamente!
# ✓ Código de salida: 0
```

### Compilación
```powershell
# Compilar sin tests
.\mvnw clean compile -q

# Verificar que no hay errores
$LASTEXITCODE -eq 0
```

### Empaquetado
```powershell
# Empaquetar aplicación
.\mvnw clean package -DskipTests -q

# Verificar JAR
ls target\*.jar
```

---

## 📋 Resumen de Cambios

| Archivo | Tipo | Acción |
|---------|------|--------|
| `scripts/run-integration-tests.ps1` | Nuevo | ✅ Creado |
| `src/test/java/.../WireMockIntegrationTest.java` | Nuevo | ✅ Creado |
| `ci/pipeline.yml` | Actualizado | ✅ Mejorado |
| `pom.xml` | Actualizado | ✅ Mejorado |
| `doc/WIREMOCK_INTEGRATION_PIPELINE.md` | Nuevo | ✅ Creado |
| `doc/QUICK_START_INTEGRATION_TESTS.md` | Nuevo | ✅ Creado |
| `scripts/README.md` | Nuevo | ✅ Creado |

---

## 📝 Notas

- WireMock inicia en puerto **8080** por defecto (personalizable)
- Tests de integración pueden tardar **1-3 minutos**
- Se requiere **conexión a internet solo la primera vez** (para descargar dependencias Maven)
- Los reportes se generan automáticamente en `target/surefire-reports/`

---

## ✉️ Siguiente Paso

1. Ejecuta: `.\scripts\run-integration-tests.ps1`
2. Verifica que todos los tests pasen
3. Revisa: `doc/QUICK_START_INTEGRATION_TESTS.md` para guía rápida
4. Lee: `doc/WIREMOCK_INTEGRATION_PIPELINE.md` para documentación completa

---

**Estado**: ✅ Integración Completada  
**Fecha**: 2026-05-12  
**Versión**: 1.0

