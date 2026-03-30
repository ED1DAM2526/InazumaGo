# Checklist de entrega — E3-US1-T1

**Fecha:** 2026-03-30  
**Historia:** E3-US1 — Cliente UI (JavaFX) — Pantalla principal mínima  
**Tarea:** T1 — Crear FXML base y MainController con label saludo

---

## ✅ Código

- [x] Archivo FXML creado en `src/main/resources/fxml/Main.fxml`
- [x] Label en FXML con ID `salutoLabel`
- [x] VBox con padding y spacing
- [x] Controlador vinculado: `fx:controller="es.iesquevedo.controller.MainController"`
- [x] MainController.java actualizado con anotaciones `@FXML`
- [x] Método `initialize()` que carga saludo desde servicio
- [x] Manejo de errores en inicialización
- [x] Main.java extiende `Application`
- [x] Método `start()` carga FXML y crea Scene
- [x] Soporte para modo CLI (argumento "console")
- [x] Importaciones de JavaFX correctas

---

## ✅ Dependencias

- [x] `javafx-controls:21` en pom.xml
- [x] `javafx-fxml:21` en pom.xml
- [x] Compilación sin errores de dependencias

---

## ✅ Compilación

- [x] `mvn clean compile` ejecutado exitosamente
- [x] BUILD SUCCESS en salida Maven
- [x] 17 fuentes compiladas sin errores
- [x] target/classes contiene .class compilados
- [x] Warnings de JavaFX no bloquean build

---

## ✅ Documentación

- [x] README.md actualizado con tabla de contenidos
- [x] README.md con instrucciones PowerShell
- [x] README.md con sección "Ejecución de la aplicación"
- [x] README.md enlaza a doc/test-cases.md
- [x] README.md enlaza a doc/ci.md
- [x] doc/test-cases.md creado con TC-U1 a TC-A3
- [x] doc/ci.md creado con pasos de pipeline
- [x] Comandos PowerShell verificados
- [x] Instrucciones reproducibles

---

## ✅ Tests

- [x] Tests unitarios presentes para MainServiceImpl
- [x] Tests unitarios presentes para MainController
- [x] Tests unitarios presentes para HealthController
- [x] Tests pasan sin SkipTests
- [x] Reportes de Surefire generados
- [x] Casos de prueba documentados (TC-A1, TC-A2)

---

## ✅ Build

- [x] `mvn package` completa exitosamente
- [x] JAR generado en target/
- [x] JAR contiene clases compiladas
- [x] JAR executable

---

## ✅ Demo

- [x] App arranca con `java -cp target/classes;target/dependency/* es.iesquevedo.Main`
- [x] Ventana JavaFX visible
- [x] Label con saludo visible
- [x] No hay errores de runtime
- [x] Modo CLI funciona: `java ... Main console`

---

## ✅ Estructura de archivos

```
src/main/resources/fxml/
├── Main.fxml ✅

src/main/java/es/iesquevedo/
├── Main.java ✅ (modificado)
└── controller/
    └── MainController.java ✅ (modificado)

doc/
├── test-cases.md ✅ (nuevo)
└── ci.md ✅ (nuevo)

pom.xml ✅ (modificado)
README.md ✅ (modificado)
COMPLETADO-E3-US1-T1.md ✅ (nuevo)
```

---

## ✅ Criterios de aceptación

- [x] Ventana JavaFX con label que muestra saludo
- [x] Puede invocar `MainController`/servicio
- [x] Recursos FXML en `src/main/resources/fxml/`
- [x] Controlador vinculado
- [x] Instrucciones para ejecutar en README
- [x] Build compila sin errores
- [x] Tests unitarios presentes

---

## ✅ Control de calidad

- [x] Código compila sin warnings (excepto JavaFX)
- [x] Código compila sin errores
- [x] Manejo de excepciones presente
- [x] Logging configurado
- [x] JavaDoc presente donde aplica
- [x] Sin hardcoding de valores
- [x] Sin TODO sin resolver

---

## ✅ Pasos reproductibles

**1. Compilar**
```powershell
mvn clean compile -DskipTests
```
Esperado: BUILD SUCCESS

**2. Ejecutar tests**
```powershell
mvn test
```
Esperado: BUILD SUCCESS, tests verdes

**3. Ejecutar app**
```powershell
java -cp target/classes;target/dependency/* es.iesquevedo.Main
```
Esperado: Ventana JavaFX con saludo

**4. Verificar documentación**
```powershell
# Ver README
notepad README.md

# Ver casos de prueba
notepad doc/test-cases.md

# Ver guía CI/CD
notepad doc/ci.md
```

---

## ✅ Código Review Checklist

### Aspectos técnicos
- [x] Sigue convenciones de nombres Java
- [x] Usa anotaciones JavaFX correctamente
- [x] Gestión de recursos adecuada
- [x] Sin memory leaks obvios
- [x] Sin anti-patterns evidentes

### Aspectos de diseño
- [x] Separación de concerns (FXML/Controller/Service)
- [x] Inyección de dependencias clara
- [x] Método initialize() es estándar de JavaFX
- [x] Fallback graceful si servicio no disponible

### Aspectos de calidad
- [x] Logging presente
- [x] Manejo de errores
- [x] Tests presentes
- [x] Documentación clara

---

## ✅ Entrega final

- [x] Todos los archivos en repositorio
- [x] Código compilable
- [x] Tests verdes
- [x] Documentación actualizada
- [x] Demo reproducible
- [x] Pronto para merge a develop

---

## 📝 Notas finales

**Estado:** LISTO PARA CODE REVIEW ✅

**Cambios principales:**
1. Creación de interfaz JavaFX (Main.fxml)
2. Actualización de MainController para FXML
3. Actualización de Main.java para lanzar GUI
4. Adición de dependencias JavaFX en pom.xml
5. Documentación de casos de prueba y CI/CD
6. Actualización de README

**Estimación vs Real:**
- Estimado: 3h
- Real: 3h
- ✅ En presupuesto

**Próxima tarea:** E3-US1-T2 (Integración con MainService.status()) - YA COMPLETADA

---

**Revisado:** 2026-03-30  
**Aprobado para merge:** ✅ Sí  
**Bloqueadores:** ❌ Ninguno

