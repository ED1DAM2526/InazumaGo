# 🚀 START HERE: Integration Tests con WireMock

> **¿Quieres ejecutar tests de integración con WireMock?** Comienza aquí.

# ⚡ Inicio Rápido (3 comandos)

```powershell
# 0. (Recomendado) Comprobar que tienes JDK y compilador (javac)
.\scripts\check-jdk.ps1

# 1. Abrir terminal PowerShell en la raíz del proyecto
cd C:\Users\1dam\IdeaProjects\InazumaGo

# 2. Ejecutar tests de integración (usa el wrapper de Maven)
# Nota: si no existe `scripts\run-integration-tests.ps1`, ejecuta directamente:
.\mvnw.cmd -DskipTests=false test

# 3. Ver resultado
# ✅ Tests de integración completados exitosamente!
```

---

## 📚 Documentación

| Documento | Descripción | Audiencia |
|-----------|-------------|-----------|
| **[QUICK_START_INTEGRATION_TESTS.md](doc/QUICK_START_INTEGRATION_TESTS.md)** | Guía de 3 pasos | 👥 Todos |
| **[WIREMOCK_INTEGRATION_PIPELINE.md](doc/WIREMOCK_INTEGRATION_PIPELINE.md)** | Documentación técnica | 👨‍💻 Desarrolladores |
| **[CHECKLIST_INTEGRATION_WIREMOCK.md](doc/CHECKLIST_INTEGRATION_WIREMOCK.md)** | Lista de verificación | ✅ QA/DevOps |
| **[RESUMEN_INTEGRACION_WIREMOCK_PIPELINE.md](RESUMEN_INTEGRACION_WIREMOCK_PIPELINE.md)** | Resumen ejecutivo | 📋 Gestores |

---

## 🎯 Opciones de Ejecución

### Opción 1: Tests Completos (Unitarios + Integración)
```powershell
.\scripts\run-integration-tests.ps1
```

### Opción 2: Solo Tests de Integración
```powershell
.\scripts\run-integration-tests.ps1 -SkipUnitTests
```

### Opción 3: Con Debug (Salida Detallada)
```powershell
.\scripts\run-integration-tests.ps1 -Verbose
```

### Opción 4: Solo Tests Unitarios
```powershell
.\scripts\run-tests.ps1
```

---

## 📊 ¿Qué se está probando?

El pipeline incluye:

- ✅ **WireMockIntegrationTest** — Mock HTTP en puerto 8080
- ✅ **MainControllerIntegrationTest** — Controller + Service
- ✅ **FirebaseIntegrationTest** — Repositorio en memoria

**Tiempo estimado**: 2-3 minutos

---

## 🔍 Ver Reportes

Los reportes se generan automáticamente en `target/surefire-reports/`:

```powershell
# Abrir carpeta de reportes
explorer target\surefire-reports\

# Abrir cobertura JaCoCo en navegador
Start-Process "target/site/jacoco/index.html"
```

---

## 🐛 ¿Problemas?

| Problema | Solución |
|----------|----------|
| Scripts no se ejecutan | `Set-ExecutionPolicy -ExecutionPolicy Bypass -Scope Process` |
| Puerto 8080 en uso | Cambiar puerto en `src/test/java/es/iesquevedo/integration/WireMockIntegrationTest.java` |
| Necesito detalles | Ejecutar con `-Verbose`: `.\scripts\run-integration-tests.ps1 -Verbose` |
| ¿Más ayuda? | Ver `doc/QUICK_START_INTEGRATION_TESTS.md` |

---

## 🏗️ Estructura

```
InazumaGo/
├── scripts/
│   ├── run-integration-tests.ps1  ← Ejecutar tests aquí
│   ├── run-tests.ps1
│   └── README.md
├── ci/
│   └── pipeline.yml               ← Pipeline CI/CD
├── src/test/java/es/iesquevedo/integration/
│   ├── WireMockIntegrationTest.java
│   ├── MainControllerIntegrationTest.java
│   └── FirebaseIntegrationTest.java
├── doc/
│   ├── QUICK_START_INTEGRATION_TESTS.md
│   ├── WIREMOCK_INTEGRATION_PIPELINE.md
│   └── CHECKLIST_INTEGRATION_WIREMOCK.md
└── RESUMEN_INTEGRACION_WIREMOCK_PIPELINE.md
```

---

## 💡 Flujo de Trabajo

```
1. Editar código
   ↓
2. .\scripts\run-integration-tests.ps1  ← Tests locales
   ↓
3. git push
   ↓
4. Pipeline CI ejecuta automáticamente
   ↓
5. Merge si ✅ pasan todos los tests
```

---

## 📖 Documentación Completa

### Para Empezar Rápido 👇
**→ [doc/QUICK_START_INTEGRATION_TESTS.md](doc/QUICK_START_INTEGRATION_TESTS.md)**

### Para Detalles Técnicos 👇
**→ [doc/WIREMOCK_INTEGRATION_PIPELINE.md](doc/WIREMOCK_INTEGRATION_PIPELINE.md)**

### Para Verificación 👇
**→ [doc/CHECKLIST_INTEGRATION_WIREMOCK.md](doc/CHECKLIST_INTEGRATION_WIREMOCK.md)**

### Para Gestión 👇
**→ [RESUMEN_INTEGRACION_WIREMOCK_PIPELINE.md](RESUMEN_INTEGRACION_WIREMOCK_PIPELINE.md)**

---

## ✨ Características

✅ Ejecución local simple  
✅ Pipeline CI/CD automatizado  
✅ WireMock para mock HTTP  
✅ Reportes de cobertura JaCoCo  
✅ Documentación completa  
✅ Ejemplos funcionales  

---

## 🎬 Comienza Ahora

```powershell
.\scripts\run-integration-tests.ps1
```

¿Listo? ¡Ve a `doc/QUICK_START_INTEGRATION_TESTS.md` para la guía completa!

---

**Última actualización**: 2026-05-12  
**Versión**: 1.0  
**Estado**: ✅ Listo para Usar

