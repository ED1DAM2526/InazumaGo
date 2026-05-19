# Integración de Tests de Integración con WireMock en Pipeline CI/CD

## Descripción General

Este documento describe cómo se integran los tests de integración con WireMock en el pipeline CI/CD del proyecto InazumaGo.

## ¿Qué es WireMock?

**WireMock** es una herramienta que permite:
- Crear **mock servers HTTP** en ambiente de pruebas
- Hacer **stub de endpoints** de terceros (Firebase, APIs externas)
- Simular **respuestas y errores** sin depender de servicios externos
- Validar **llamadas HTTP** en tests de integración

Dependencia Maven:
```xml
<dependency>
    <groupId>com.github.tomakehurst</groupId>
    <artifactId>wiremock-jre8</artifactId>
    <version>2.35.0</version>
    <scope>test</scope>
</dependency>
```

## Estructura del Pipeline CI/CD

El pipeline tiene **3 jobs en paralelo/secuencial**:

### 1. **Unit Tests** (Tests Unitarios)
- **ID**: `unit-tests`
- **Propósito**: Ejecutar tests unitarios aislados
- **Comandos**:
  ```bash
  ./mvnw -B clean compile -q
  ./mvnw -B -DskipTests=false -Dgroups=\!integration test
  ```
- **Artefactos**: Reportes de Surefire, cobertura JaCoCo

### 2. **Integration Tests with WireMock** (Tests de Integración)
- **ID**: `integration-tests`
- **Propósito**: Ejecutar tests de integración con WireMock
- **Dependencia**: Requiere que `unit-tests` pase
- **Comandos**:
  ```bash
  ./mvnw -B clean compile -q
  ./mvnw -B -DskipTests=false -Dgroups=integration test
  ```
- **Artefactos**: Reportes de integración, cobertura de WireMock

### 3. **Build Package** (Empaquetado)
- **ID**: `build`
- **Propósito**: Generar JAR/ZIP de la aplicación
- **Dependencia**: Requiere que `unit-tests` e `integration-tests` pasen
- **Comandos**:
  ```bash
  ./mvnw -B clean package -DskipTests -q
  ```
- **Artefactos**: JAR compilado, ZIP de distribución

## Ejecutar Tests Localmente

### Opción 1: Tests Completos (Unitarios + Integración)
```powershell
# Ejecutar todos los tests
./scripts/run-integration-tests.ps1

# Con salida detallada
./scripts/run-integration-tests.ps1 -Verbose
```

### Opción 2: Solo Tests de Integración
```powershell
# Ejecutar solo tests de integración
./scripts/run-integration-tests.ps1 -SkipUnitTests
```

### Opción 3: Usando Maven directamente
```powershell
# Compilar
./mvnw clean compile

# Tests unitarios
./mvnw -DskipTests=false -Dgroups=\!integration test

# Tests de integración
./mvnw -DskipTests=false -Dgroups=integration test

# Todos los tests
./mvnw -DskipTests=false test
```

## Ejemplo: Test de Integración con WireMock

```java
@DisplayName("Tests de Integración con WireMock")
class WireMockIntegrationTest {

    private WireMockServer wireMockServer;

    @BeforeEach
    void setUp() {
        // Inicializar servidor WireMock en puerto 8080
        wireMockServer = new WireMockServer(8080);
        wireMockServer.start();
        configureFor("localhost", 8080);
    }

    @AfterEach
    void tearDown() {
        // Detener y resetear
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
            wireMockServer.resetAll();
        }
    }

    @Test
    void testApiCall() {
        // Hacer stub de un endpoint
        stubFor(get(urlEqualTo("/api/players/1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("{\"id\": 1, \"name\": \"Player1\"}")));

        // El servidor está listo en http://localhost:8080
        // Tus tests pueden hacer llamadas HTTP a ese servidor
    }
}
```

## Configuración del pom.xml

El `pom.xml` incluye:

```xml
<!-- WireMock para tests de integración -->
<dependency>
    <groupId>com.github.tomakehurst</groupId>
    <artifactId>wiremock-jre8</artifactId>
    <version>2.35.0</version>
    <scope>test</scope>
</dependency>

<!-- JaCoCo para cobertura de tests -->
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.12</version>
    <!-- Configuración automática -->
</plugin>

<!-- Surefire para ejecutar tests -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>3.1.2</version>
</plugin>
```

## Scripts PowerShell Disponibles

### `run-integration-tests.ps1`
Ejecuta tests de integración con WireMock. Opciones:
- **Sin parámetros**: Tests completos (unitarios + integración)
- `-Verbose`: Salida detallada de Maven
- `-SkipUnitTests`: Solo tests de integración

**Uso**:
```powershell
.\scripts\run-integration-tests.ps1
.\scripts\run-integration-tests.ps1 -Verbose
.\scripts\run-integration-tests.ps1 -SkipUnitTests
```

### `run-tests.ps1` (Existente)
Ejecuta solo tests unitarios con Maven Wrapper.

## Reportes y Resultados

Los reportes se generan en:

```
target/
  surefire-reports/          # Reportes XML de tests
    TEST-*.xml               # Resultados por test class
    TEST-*.txt               # Resumen por test
  site/
    jacoco/                  # Cobertura de código JaCoCo
      index.html             # Dashboard de cobertura
```

## Flujo de CI/CD Completo

```
┌─────────────────┐
│  Push/Pull      │
│  Request        │
└────────┬────────┘
         │
         ▼
┌─────────────────────────────┐
│ 1. Unit Tests (Paralelo)    │
│    - Compilar               │
│    - Tests unitarios        │
│    - Reportes               │
└────────┬────────────────────┘
         │ ✓ Pasar
         ▼
┌─────────────────────────────┐
│ 2. Integration Tests        │
│    (Paralelo, depende de 1) │
│    - Tests de integración   │
│    - WireMock stubs         │
│    - Cobertura JaCoCo       │
└────────┬────────────────────┘
         │ ✓ Pasar
         ▼
┌─────────────────────────────┐
│ 3. Build Package            │
│    (Paralelo, depende de 1,2)
│    - Empaquetar JAR         │
│    - Generar ZIP            │
│    - Artefactos finales     │
└────────┬────────────────────┘
         │ ✓ Completado
         ▼
     ✓ Éxito
```

## Configuración de Puertos en WireMock

WireMock puede usar diferentes puertos:

```java
// Puerto 8080 (default en nuestro ejemplo)
WireMockServer wireMockServer = new WireMockServer(8080);

// Puerto específico
WireMockServer wireMockServer = new WireMockServer(9999);

// Puerto dinámico (asignado automáticamente)
WireMockServer wireMockServer = new WireMockServer();
int port = wireMockServer.port();
```

## Troubleshooting

### Problema: "Puerto ya en uso"
**Solución**: Cambiar puerto o asegurar que WireMock se detiene en `@AfterEach`

### Problema: "Tests de integración fallan localmente pero pasan en CI"
**Solución**: Verificar que `@BeforeEach` inicializa correctamente WireMock

### Problema: "WireMock server no inicia"
**Solución**: 
- Verificar que el puerto no esté ocupado
- Verificar dependencia `wiremock-jre8` en pom.xml
- Ver logs con `-Verbose` en el script

## Próximos Pasos

1. **Agregar más tests de integración** en `src/test/java/es/iesquevedo/integration/`
2. **Configurar Firebase Realtime** con WireMock stubs
3. **Implementar reportes** de cobertura en dashboard de CI
4. **Añadir validación** de endpoints antes de deploy

## Enlaces Útiles

- [WireMock Documentation](http://wiremock.org/)
- [Maven Surefire Plugin](https://maven.apache.org/surefire/maven-surefire-plugin/)
- [JaCoCo Coverage](https://www.jacoco.org/)
- [JUnit 5 Tags](https://junit.org/junit5/docs/current/user-guide/#writing-tests-tagging-and-filtering)

