# Pull Request: E3-US1-T1 - Pantalla Principal JavaFX

**Branch:** `feature/E3-US1-T1-pantalla-principal-javafx` → `develop`  
**Tipo:** Feature  
**Estado:** Ready for Review  
**Prioridad:** High  
**Estimación:** 3h | **Real:** 3h  
**Sprint:** Sprint 1 - Fundación & Build verde

---

## 📋 Información de la Historia

**Historia:** E3-US1 - Como usuario, quiero una pantalla principal mínima (JavaFX) que muestre el saludo y el estado  
**Tarea:** T1 - Crear FXML base y `MainController` con label saludo  
**Responsable:** UI Team  
**Revisor:** QA Team, DevOps  
**Fecha de inicio:** 2026-03-30  
**Fecha de conclusión:** 2026-03-30

---

## 🎯 Objetivo

Implementar la pantalla principal de la aplicación InazumaGo usando JavaFX, mostrando un saludo obtenido desde el servicio backend.

### Criterios de aceptación ✅

- [x] Ventana JavaFX con label que muestra saludo (puede invocar `MainController`/servicio)
- [x] Recursos FXML en `src/main/resources/fxml/` y controlador vinculado
- [x] Instrucciones para ejecutar la app en README o scripts
- [x] Aplicación compila sin errores
- [x] Tests unitarios presentes y pasan

---

## 📦 Cambios Realizados

### 🆕 Archivos Nuevos (11)

| Archivo | Descripción | Líneas |
|---------|-------------|--------|
| `src/main/resources/fxml/Main.fxml` | Interfaz JavaFX con VBox y label saludo | 16 |
| `src/main/java/es/iesquevedo/MainGUI.java` | Clase Application para JavaFX | 49 |
| `src/main/java/es/iesquevedo/MainApp.java` | Lógica principal de aplicación | 49 |
| `doc/test-cases.md` | Casos de prueba (TC-U1 a TC-A3) | ~520 |
| `doc/ci.md` | Guía de CI/CD y pipeline | ~380 |
| `run-app-fixed.ps1` | Script PowerShell para ejecutar app | 75 |
| `run-app.ps1` | Script alternativo (puede eliminarse) | 75 |
| `COMPLETADO-E3-US1-T1.md` | Resumen detallado de trabajo | ~200 |
| `CHECKLIST-E3-US1-T1.md` | Checklist de entrega | ~150 |
| `RESUMEN-EJECUCION-E3-US1-T1.md` | Resumen de ejecución | ~100 |
| `PROBLEMA-FXML-RESUELTO.md` | Resolución de problemas | ~50 |

### 🔄 Archivos Modificados (5)

| Archivo | Cambios Principales |
|---------|-------------------|
| `pom.xml` | + Dependencias JavaFX 21 (controls, fxml) |
| `src/main/java/es/iesquevedo/Main.java` | Convertido en wrapper simple |
| `src/main/java/es/iesquevedo/controller/MainController.java` | + Anotaciones @FXML, método initialize() |
| `README.md` | + Tabla de contenidos, instrucciones PowerShell |
| `src/main/resources/fxml/Main.fxml` | Quitado fx:controller para evitar conflicto |

### 📊 Métricas del Commit

```
16 files changed, 2196 insertions(+), 65 deletions(-)
- 11 archivos nuevos
- 5 archivos modificados
- +2196 líneas añadidas
- -65 líneas eliminadas
```

---

## 🏗️ Arquitectura Implementada

### Estructura de Clases

```
es.iesquevedo/
├── Main.java .................... Wrapper para compatibilidad
├── MainApp.java ................ Lógica principal (CLI/GUI)
├── MainGUI.java ................ Aplicación JavaFX
└── controller/
    └── MainController.java ..... Controlador FXML
```

### Flujo de Ejecución

```
Main.main() → MainApp.main() → {
    args[0] == "console" ? runConsoleMode() : Application.launch(MainGUI.class)
}
```

### Dependencias Añadidas

```xml
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-controls</artifactId>
    <version>21</version>
</dependency>
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-fxml</artifactId>
    <version>21</version>
</dependency>
```

---

## 🧪 Verificación de Funcionalidad

### ✅ Compilación Exitosa

```bash
mvn clean compile -DskipTests
# BUILD SUCCESS - 19 sources compiled
```

### ✅ Tests Unitarios

```bash
mvn test
# BUILD SUCCESS - All tests pass
```

### ✅ Ejecución en Modo CLI

```powershell
powershell -ExecutionPolicy Bypass -File .\run-app-fixed.ps1 -Console
```

**Salida esperada:**
```
INFO: Hello, InazumaGoPrevio!
INFO: Health: OK
```

### ✅ Ejecución en Modo GUI

```powershell
powershell -ExecutionPolicy Bypass -File .\run-app-fixed.ps1
```

**Resultado:** Ventana JavaFX se abre con saludo visible

---

## 📚 Documentación Incluida

### README.md Actualizado
- Tabla de contenidos completa
- Instrucciones de inicio rápido
- Comandos PowerShell verificados
- Enlaces a documentación técnica

### Casos de Prueba (doc/test-cases.md)
- **TC-U1:** Salud de la aplicación
- **TC-U2:** MainService - Saludo básico
- **TC-I1:** MainController - Integración
- **TC-I2:** JavaFX UI - Renderización
- **TC-A1:** Demo local - Inicio de app ✅
- **TC-A2:** Demo local - Tests verdes ✅

### Guía CI/CD (doc/ci.md)
- Pipeline genérica con ejemplos
- Ejemplo completo de GitHub Actions
- Troubleshooting de problemas comunes
- Mejores prácticas

---

## 🔧 Problemas Resueltos

### Problema 1: Conflicto de Controlador FXML
**Error:** `Controller value already specified`
**Solución:** Quitado `fx:controller` del FXML, usado solo `setController()` en código

### Problema 2: JavaFX en Modo CLI
**Error:** `JavaFX runtime components are missing`
**Solución:** Separación de MainApp (sin Application) y MainGUI (con Application)

### Problema 3: Wildcard en PowerShell
**Error:** `target/dependency/*` no funciona en PowerShell
**Solución:** Script personalizado que construye classpath correctamente

---

## 🚀 Cómo Probar Localmente

### Preparación
```powershell
# Compilar
mvn clean compile

# Descargar dependencias
mvn dependency:copy-dependencies
```

### Modo CLI
```powershell
powershell -ExecutionPolicy Bypass -File .\run-app-fixed.ps1 -Console
```

### Modo GUI
```powershell
powershell -ExecutionPolicy Bypass -File .\run-app-fixed.ps1
```

---

## 📋 Checklist de Revisión

### Código
- [x] Compila sin errores
- [x] Tests pasan
- [x] Funciona en modo CLI
- [x] Funciona en modo GUI
- [x] Manejo de errores presente
- [x] Logging configurado

### Documentación
- [x] README actualizado
- [x] Casos de prueba documentados
- [x] Guía CI/CD incluida
- [x] Scripts de ejecución incluidos

### Arquitectura
- [x] Separación de responsabilidades
- [x] Inyección de dependencias
- [x] Patrón MVC respetado
- [x] Compatibilidad con modo CLI

---

## 🔗 Referencias

- **Historia:** E3-US1 - Pantalla principal mínima (JavaFX)
- **Tarea:** T1 - Crear FXML base y MainController con label saludo
- **Documentos relacionados:**
  - `doc/test-cases.md` - Casos de prueba
  - `doc/ci.md` - Guía CI/CD
  - `COMPLETADO-E3-US1-T1.md` - Resumen detallado

---

## 🎯 Próximas Tareas (Sprint 1)

Después de merge:
- [ ] E1-US1-T1: Health endpoint (Motor)
- [ ] E1-US2-T1: MainService interfaz (Motor)
- [ ] E2-US1-T1: InMemory repository (Motor)

---

## ✅ Estado del Pull Request

**Estado:** Ready for Review  
**Bloqueadores:** Ninguno  
**Riesgos:** Ninguno identificado  
**Tiempo de revisión estimado:** 30 minutos  

---

**Commit:** `1c2309c` - feat: E3-US1-T1 - Pantalla principal JavaFX con label saludo  
**Branch:** `feature/E3-US1-T1-pantalla-principal-javafx`  
**Target:** `develop`

---

## 👥 Revisores Asignados

- [ ] @qa-team - Verificar tests y funcionalidad
- [ ] @devops-team - Verificar CI/CD y scripts
- [ ] @motor-team - Verificar integración con servicios

---

**Fecha de creación del PR:** 2026-03-30  
**Responsable:** UI Team  
**Contacto:** ui-team@inazumago.com

