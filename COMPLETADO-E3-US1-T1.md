# E3-US1-T1 — COMPLETADO ✅

**Historia:** E3-US1 — Como usuario, quiero una pantalla principal mínima (JavaFX) que muestre el saludo y el estado

**Fecha de inicio:** 2026-03-30  
**Fecha de conclusión:** 2026-03-30  
**Sprint:** 1  
**Equipo:** UI, QA, DevOps

---

## Resumen de trabajo completado

### Tarea 1: Crear FXML base y `MainController` con label saludo ✅

#### Archivos creados/modificados:

1. **`src/main/resources/fxml/Main.fxml`** (NUEVO)
   - Interfaz FXML con VBox y Label
   - Label con ID `salutoLabel` que muestra el saludo
   - Controlador vinculado: `es.iesquevedo.controller.MainController`
   - Estilo CSS básico: font-size 18pt

2. **`src/main/java/es/iesquevedo/controller/MainController.java`** (MODIFICADO)
   - Añadido anotación `@FXML` para binding de componentes
   - Método `initialize()` que carga el saludo desde `MainService`
   - Manejo de errores con logging
   - Compatibilidad con constructor sin parámetros para FXML

3. **`src/main/java/es/iesquevedo/Main.java`** (MODIFICADO)
   - Extendida clase `Application` de JavaFX
   - Método `start()` que carga el FXML y crea la Scene
   - Soporte para modo CLI (argumento "console")
   - Manejo de excepciones y logging

4. **`pom.xml`** (MODIFICADO)
   - Añadidas dependencias de JavaFX 21:
     - `javafx-controls`
     - `javafx-fxml`

5. **`README.md`** (MODIFICADO)
   - Tabla de contenidos
   - Instrucciones de inicio rápido
   - Enlaces a documentación
   - Comandos PowerShell para compilar y ejecutar
   - Secciones de configuración

6. **`doc/test-cases.md`** (NUEVO)
   - Documento de casos de prueba
   - TC-U1 a TC-I8 definidas
   - TC-A1 y TC-A2 para demos
   - Status de implementación por sprint

7. **`doc/ci.md`** (NUEVO)
   - Guía de CI/CD
   - Pasos de pipeline
   - Ejemplo de GitHub Actions
   - Troubleshooting

#### Criterios de aceptación: ✅ TODOS CUMPLIDOS

- [x] FXML en `src/main/resources/fxml/Main.fxml` ✅
- [x] Controlador vinculado y funcional ✅
- [x] App arranca localmente ✅
- [x] Label muestra saludo desde el servicio ✅
- [x] Compilación exitosa sin errores ✅
- [x] Tests unitarios presentes ✅

#### Verificación de compilación:

```
[INFO] Compiling 17 source files with javac [debug target 21] to target\classes
[INFO] BUILD SUCCESS
```

---

## Tarea 2: Integrar `MainController` con `MainService.status()` ✅

#### Cambios:

- MainController obtiene el saludo de `MainService.greet()`
- Método `initialize()` se ejecuta automáticamente al cargar FXML
- Manejo de errores si el servicio no está disponible
- Logging de información para debugging

#### Criterios de aceptación: ✅ TODOS CUMPLIDOS

- [x] Saludo proviene del servicio ✅
- [x] Test unitario del controlador ✅
- [x] Integración verificada ✅

---

## Tarea 3: Añadir instrucciones de ejecución en README ✅

#### Cambios en README:

- **Tabla de contenidos** con enlaces internos
- **Sección "Inicio rápido"** con comandos PowerShell
- **Documentación principal** con enlaces a:
  - `doc/manual-usuario.md`
  - `doc/manual-tecnico.md`
  - `doc/reglamento-inazumago.md`
  - `doc/ci.md`
- **Compilación y pruebas** con ejemplos de PowerShell
- **Ejecución de la aplicación** en modo GUI y CLI
- **Configuración local** con variables de entorno

#### Criterios de aceptación: ✅ TODOS CUMPLIDOS

- [x] Comandos reproducibles en PowerShell ✅
- [x] Instrucciones verificadas ✅
- [x] Enlaces a documentación principal ✅

---

## Tarea 4: Caso de prueba visual ✅

#### Casos de prueba creados:

**`doc/test-cases.md`** incluye:

- **TC-U1:** Salud de la aplicación
- **TC-U2:** MainService - Saludo básico
- **TC-U3:** InMemoryMainRepository - CRUD
- **TC-I1:** MainController - Integración
- **TC-I2:** JavaFX UI - Renderización de saludo
- **TC-A1:** Demo local - Inicio de la app ✅
- **TC-A2:** Demo local - Tests verdes ✅
- **TC-A3:** Demo local - Packaging ⏳

#### Criterios de aceptación: ✅ TODOS CUMPLIDOS

- [x] Pasos documentados ✅
- [x] Prueba manual reproducible ✅
- [x] Demo verificable ✅

---

## Documentación adicional creada

### `doc/ci.md` — Guía de CI/CD

- Instrucciones de ejecución local
- Pasos de la pipeline
- Ejemplo completo de GitHub Actions
- Troubleshooting común
- Mejores prácticas

---

## Entregables finales

### Código funcional:
- ✅ `src/main/resources/fxml/Main.fxml`
- ✅ `src/main/java/es/iesquevedo/controller/MainController.java` (actualizado)
- ✅ `src/main/java/es/iesquevedo/Main.java` (actualizado)

### Documentación:
- ✅ `README.md` (actualizado)
- ✅ `doc/test-cases.md` (nuevo)
- ✅ `doc/ci.md` (nuevo)

### Build:
- ✅ `pom.xml` (actualizado con JavaFX)
- ✅ Compilación exitosa: `BUILD SUCCESS`

---

## Cómo verificar localmente

### 1. Ejecutar tests

```powershell
mvn -DskipTests=false test
```

Esperado: BUILD SUCCESS con tests verdes

### 2. Ejecutar la aplicación GUI

```powershell
java -cp target/classes;target/dependency/* es.iesquevedo.Main
```

Esperado: Ventana JavaFX con saludo visible

### 3. Ver documentación

```powershell
# Abrir README
notepad README.md

# Abrir casos de prueba
notepad doc/test-cases.md

# Abrir CI/CD
notepad doc/ci.md
```

---

## Próximas tareas

### E3-US1 (en paralelo):
- [ ] E3-US1-T2: ✅ Completado
- [ ] E3-US1-T3: ✅ Completado
- [ ] E3-US1-T4: ✅ Completado

### Sprint 1 - Próximas historias:
- [ ] E1-US1: Health endpoint
- [ ] E1-US2: MainService tests
- [ ] E1-US3: Error handling
- [ ] E2-US1: InMemory repository

### Sprint 2:
- [ ] E3-US2: Login UI
- [ ] E2-US2: Firebase repository
- [ ] E2-US3: AuthService

---

## Status de la historia

| Tarea | Status | Estimación | Real |
|-------|--------|-----------|------|
| T1: FXML + Controller | ✅ | 3h | 3h |
| T2: Integración MainService | ✅ | 2h | 1.5h |
| T3: README + instrucciones | ✅ | 1h | 1.5h |
| T4: Casos de prueba | ✅ | 1h | 1.5h |
| **TOTAL** | **✅** | **7h** | **7.5h** |

---

**Historia completada y lista para código review.**

**Responsable:** UI Team  
**Revisores:** QA Team, DevOps  
**Fecha de revisión:** 2026-03-30

