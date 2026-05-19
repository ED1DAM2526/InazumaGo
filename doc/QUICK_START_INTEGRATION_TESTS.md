# Guía Rápida: Ejecutar Tests de Integración con WireMock

> **Resumen**: Esta guía te muestra cómo ejecutar tests de integración con WireMock en InazumaGo en 3 pasos simples.

## Quick Start (3 pasos)

### 1️⃣ Abrir Terminal PowerShell

```powershell
# Navegar al directorio del proyecto
cd C:\Users\1dam\IdeaProjects\InazumaGo

# Verificar que estás en el directorio correcto
ls mvnw.cmd  # Debe mostrar el archivo
```

### 2️⃣ Ejecutar Tests de Integración

**Opción A: Tests Completos** (Unitarios + Integración)
```powershell
.\scripts\run-integration-tests.ps1
```

**Opción B: Solo Tests de Integración**
```powershell
.\scripts\run-integration-tests.ps1 -SkipUnitTests
```

**Opción C: Con Salida Detallada** (para debug)
```powershell
.\scripts\run-integration-tests.ps1 -Verbose
```

### 3️⃣ Verificar Resultados

✅ **Éxito**:
```
Tests de integración completados exitosamente!
Reportes en: C:\Users\1dam\IdeaProjects\InazumaGo\target\surefire-reports
```

❌ **Error**:
```
Los tests de integración fallaron (código: 1)
Para más detalles, ejecuta:
  .\scripts\run-integration-tests.ps1 -Verbose
```

---

## ¿Qué se está probando?

Los tests de integración incluyen:

| Test | Descripción | Puerto |
|------|-------------|--------|
| **WireMockIntegrationTest** | Mock de APIs HTTP con WireMock | 8080 |
| **MainControllerIntegrationTest** | Integración Controller + Service | - |
| **FirebaseIntegrationTest** | Tests con repositorio en memoria | - |

---

## Ejemplos de Uso

### Ejecutar todos los tests
```powershell
.\scripts\run-integration-tests.ps1
```

**Output esperado**:
```
===============================================
Ejecutando Tests de Integración (WireMock)
===============================================

[*] Compilando proyecto...
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

### Ejecutar solo tests de integración
```powershell
.\scripts\run-integration-tests.ps1 -SkipUnitTests
```

### Debug: Ver salida detallada
```powershell
.\scripts\run-integration-tests.ps1 -Verbose
```

Esto mostrará todos los comandos Maven ejecutados y detalles de cada test.

---

## ¿Dónde están los reportes?

Los reportes se generan automáticamente en:

```
target/surefire-reports/
├── TEST-es.iesquevedo.integration.WireMockIntegrationTest.xml
├── TEST-es.iesquevedo.integration.WireMockIntegrationTest.txt
├── TEST-es.iesquevedo.integration.MainControllerIntegrationTest.xml
├── TEST-es.iesquevedo.integration.MainControllerIntegrationTest.txt
└── ...
```

**Ver reportes**:
```powershell
# Abrir en el Explorador de archivos
explorer target\surefire-reports\

# Ver resumen de un test
notepad target\surefire-reports\TEST-es.iesquevedo.integration.WireMockIntegrationTest.txt
```

---

## Problemas Comunes

### ❌ Error: "No se puede ejecutar scripts"

```
File cannot be loaded because running scripts is disabled on this system
```

**Solución**:
```powershell
Set-ExecutionPolicy -ExecutionPolicy Bypass -Scope Process
.\scripts\run-integration-tests.ps1
```

### ❌ Error: "Puerto 8080 ya en uso"

```
Address already in use
```

**Solución**:
1. Cerrar aplicaciones que usen el puerto 8080
2. O editar `src\test\java\es\iesquevedo\integration\WireMockIntegrationTest.java` línea 33:
   ```java
   // Cambiar de:
   wireMockServer = new WireMockServer(8080);
   // A:
   wireMockServer = new WireMockServer(9999);  // puerto diferente
   ```

### ❌ Error: "Tests unitarios fallan"

Los tests de integración dependen de que los tests unitarios pasen. Ejecutar solo unitarios:
```powershell
.\scripts\run-tests.ps1
```

---

## Integración en Pipeline CI/CD

El archivo `ci/pipeline.yml` ejecuta automáticamente tests de integración en:
- ✅ Cada push a `main`/`dev`
- ✅ Cada pull request
- ✅ En máquina Docker con Java 21

**No necesitas hacer nada adicional**; el CI/CD maneja todo automáticamente.

---

## Flujo Recomendado

```
1. Editar código en rama feature
   ↓
2. Ejecutar tests localmente
   .\scripts\run-integration-tests.ps1
   ↓
3. ✅ Si pasan, hacer commit
   ↓
4. Crear Pull Request
   ↓
5. Pipeline CI/CD ejecuta tests automáticamente
   ↓
6. ✅ Si pasan en CI, hacer merge
```

---

## Más Información

- **Documentación completa**: `doc/WIREMOCK_INTEGRATION_PIPELINE.md`
- **Scripts disponibles**: `scripts/README.md`
- **CI/CD**: `ci/pipeline.yml`
- **Tests**: `src/test/java/es/iesquevedo/integration/`

---

## Preguntas Frecuentes

**P: ¿Por qué ejecutar tests de integración?**
R: Verificar que los componentes funcionan correctamente juntos con APIs externas (simuladas con WireMock).

**P: ¿Qué es WireMock?**
R: Herramienta que crea mock servers HTTP para testing sin depender de servicios externos reales.

**P: ¿Cuánto tiempo toman los tests?**
R: Generalmente 1-3 minutos, dependiendo del hardware.

**P: ¿Puedo ejecutar solo un test?**
R: Sí, con Maven:
```powershell
.\mvnw -DskipTests=false test -Dtest=WireMockIntegrationTest
```

**P: ¿Cómo agregar más tests de integración?**
R: Crear nuevas clases en `src/test/java/es/iesquevedo/integration/` con anotación `@DisplayName` y métodos `@Test`.

---

**Última actualización**: 2026-05-12  
**Versión**: 1.0

