# 📋 CASO DE PRUEBA MANUAL

**Título:** Verificar que la app inicia y `health()` responde  
**ID Caso de Prueba:** CP-US1-T4  
**Fecha Creación:** 2026-04-09  
**Estado:** ✅ ACTIVO  
**Versión:** 1.0

---

## 🎯 Objetivo General

Verificar que la aplicación InazumaGo inicia correctamente y que el método `health()` del controlador de salud responde adecuadamente, tanto en modo GUI como en modo consola.

---

## 📋 Alcance

| Aspecto | Detalle |
|---------|---------|
| **Componentes Probados** | `HealthController`, `MainGUI`, `MainApp`, `HealthControllerTest` |
| **Plataformas** | Windows, Linux, macOS (con Java 21+) |
| **Modos de Ejecución** | GUI y Consola |
| **Tipo de Prueba** | Manual + Automatizada (pruebas unitarias) |

---

## 🔧 Prerequisitos

| Requisito | Versión | Mandatorio |
|-----------|---------|-----------|
| **JDK** | 21 o superior | ✅ Sí |
| **Maven** | 3.6+ | ✅ Sí |
| **Git** | Cualquier versión reciente | ✅ Sí |
| **FIREBASE_URL** | Variable de entorno | ⚠️ Opcional* |
| **Navegador/Display** | Cualquiera | Para modo GUI |

*Si FIREBASE_URL no está definida, la app usa repositorio en memoria (InMemory).

### 📦 Validar Instalación

Ejecutar en PowerShell:

```powershell
# Verificar JDK
java -version

# Verificar Maven
mvn -version
```

---

## 🧪 PRUEBAS

### **PRUEBA 1: Compilación del Proyecto**

**ID:** CP-US1-T4-P1  
**Descripción:** Verificar que el proyecto compila sin errores

**Pasos:**
1. Abrir PowerShell
2. Navegar a: `C:\InazumaGo`
3. Ejecutar comando:
   ```powershell
   mvn clean compile
   ```

**Resultado Esperado:**
- ✅ Salida final: `[INFO] BUILD SUCCESS`
- ✅ No hay errores de compilación
- ✅ Carpeta `target/` se crea correctamente
- ✅ Tiempo de ejecución: < 30 segundos

**Evidencia a Capturar:**
- Screenshot de la consola mostrando `BUILD SUCCESS`

---

### **PRUEBA 2: Ejecución de Pruebas Unitarias**

**ID:** CP-US1-T4-P2  
**Descripción:** Verificar que la prueba unitaria `HealthControllerTest` pasa correctamente

**Pasos:**
1. Desde `C:\InazumaGo`, ejecutar:
   ```powershell
   mvn test
   ```

**Resultado Esperado:**
- ✅ Salida contiene: `Tests run: X, Failures: 0, Errors: 0`
- ✅ La prueba `HealthControllerTest.health_shouldReturnOK()` está marcada como PASSED
- ✅ No hay avisos o errores en ejecución
- ✅ Salida final: `[INFO] BUILD SUCCESS`

**Validaciones Específicas:**
```
[INFO] Running es.iesquevedo.controller.HealthControllerTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
```

**Evidencia a Capturar:**
- Screenshot de resultado de prueba
- Archivo `target/surefire-reports/es.iesquevedo.controller.HealthControllerTest.txt`

---

### **PRUEBA 3: Ejecución en Modo Consola**

**ID:** CP-US1-T4-P3  
**Descripción:** Verificar que la aplicación inicia en modo consola y `HealthController.health()` responde

**Pasos:**
1. Desde `C:\InazumaGo`, compilar empaquetado:
   ```powershell
   mvn clean package -DskipTests
   ```

2. Ejecutar aplicación en modo consola:
   ```powershell
   java -cp "target/InazumaGo-1.0-SNAPSHOT.jar;target/dependency/*" es.iesquevedo.MainApp console
   ```

**Resultado Esperado:**
- ✅ La aplicación inicia sin errores
- ✅ Se muestra menú interactivo en consola
- ✅ No hay excepciones o stacktraces
- ✅ El mensaje `[INFO] Aplicación iniciada en modo consola` aparece en logs

**Verificación del Health Endpoint:**
- En el menú consola, seleccionar opción relacionada con health
- ✅ Respuesta debe ser: `"OK"`
- ✅ No debe haber excepciones

**Evidencia a Capturar:**
- Screenshot de la consola mostrando el menú iniciado
- Screenshot mostrando respuesta de health(): "OK"

---

### **PRUEBA 4: Ejecución en Modo GUI**

**ID:** CP-US1-T4-P4  
**Descripción:** Verificar que la aplicación GUI inicia correctamente

**Requisitos Adicionales:**
- Display gráfico disponible (no en entorno headless)

**Pasos:**
1. Desde `C:\InazumaGo`, ejecutar:
   ```powershell
   mvn javafx:run
   ```
   
   O alternativamente, usar script PowerShell:
   ```powershell
   & "./scripts/run-with-maven-java.ps1"
   ```

**Resultado Esperado:**
- ✅ Ventana JavaFX se abre correctamente
- ✅ Título de la ventana: `"InazumaGo"`
- ✅ Dimensiones: 600x400 píxeles
- ✅ No hay excepciones en consola
- ✅ Logs muestran: `[INFO] Aplicación JavaFX iniciada exitosamente`
- ✅ Interfaz gráfica es interactiva

**Validaciones Visuales:**
- Ventana renderiza correctamente
- Componentes FXML se cargan sin errores
- No hay zonas grises o corrupción visual

**Evidencia a Capturar:**
- Screenshot de la ventana GUI con título "InazumaGo" visible
- Screenshot de la consola mostrando logs de inicialización
- Video (opcional) de la interacción con la GUI

---

## 🔍 Verificaciones Adicionales (QA Avanzado)

### **Verificación A: Logs de Inicialización**

**Comando:**
```powershell
mvn test 2>&1 | Select-String -Pattern "ERROR|SEVERE|Health"
```

**Esperado:** No debe haber líneas con ERROR o SEVERE

### **Verificación B: Validar Dependencias**

**Comando:**
```powershell
mvn dependency:tree | Select-Object -First 50
```

**Esperado:** Todas las dependencias (JUnit, JavaFX, Firebase) deben resolverse correctamente

### **Verificación C: Ejecución de Todas las Pruebas**

**Comando:**
```powershell
mvn test -Dgroups=unit
```

**Esperado:** Todas las pruebas unitarias deben pasar

---

## 📊 Matriz de Resultados

| ID Prueba | Descripción | Estado | Notas |
|-----------|-------------|--------|-------|
| P1 | Compilación | ⬜ Pendiente | Rellenar durante ejecución |
| P2 | Pruebas Unitarias | ⬜ Pendiente | Rellenar durante ejecución |
| P3 | Modo Consola | ⬜ Pendiente | Rellenar durante ejecución |
| P4 | Modo GUI | ⬜ Pendiente | Rellenar durante ejecución |

### Leyenda:
- ⬜ **Pendiente** - Aún no ejecutada
- ✅ **PASS** - Completada satisfactoriamente
- ❌ **FAIL** - Falló, necesita investigación
- ⚠️ **CONDITIONAL** - Pasa condicionalmente

---

## 🐛 Troubleshooting

### Problema: "BUILD FAILURE - Compilation Error"
**Solución:**
1. Verificar que JDK 21 esté en PATH: `java -version`
2. Limpiar: `mvn clean`
3. Recompilar: `mvn compile`

### Problema: "HealthControllerTest - FAILURE"
**Solución:**
1. Verificar que `HealthController.java` existe en `src/main/java/es/iesquevedo/controller/`
2. Verificar que método `health()` retorna exactamente `"OK"`
3. Ejecutar: `mvn test -Dtest=HealthControllerTest -v`

### Problema: GUI no inicia - "LoadException"
**Solución:**
1. Verificar que `Main.fxml` tiene atributo `fx:controller`
2. Verificar que `MainController` tiene constructor sin parámetros
3. Verificar logs completos: `mvn javafx:run -X`

### Problema: Modo consola - NoClassDefFoundError
**Solución:**
1. Generar JAR con dependencias: `mvn package -DskipTests`
2. Verificar que classpath incluye `target/dependency/*`

---

## 📝 Criterios de Aceptación

La prueba se considera **EXITOSA** si se cumplen TODOS estos criterios:

1. ✅ **Compilación:** Proyecto compila sin errores (`BUILD SUCCESS`)
2. ✅ **Pruebas Unitarias:** `HealthControllerTest.health_shouldReturnOK()` PASA
3. ✅ **Método Health:** `HealthController.health()` retorna exactamente `"OK"`
4. ✅ **Modo Consola:** Aplicación inicia sin excepciones
5. ✅ **Modo GUI:** Ventana se abre con título "InazumaGo"
6. ✅ **Logs:** No hay excepciones críticas (SEVERE o ERROR)
7. ✅ **Inicialización:** Mensaje `"Aplicación JavaFX iniciada exitosamente"` visible

---

## 📋 Casos Relacionados

- **E3-US1-T1:** Corregir `LoadException` en FXML ✅ (Ya completado)
- **E1-US1-T4:** Este documento (En progreso)
- **E1-US2-T1:** Futuras pruebas de gameplay

---

## 🔄 Historial de Cambios

| Versión | Fecha | Autor | Cambios |
|---------|-------|-------|---------|
| 1.0 | 2026-04-09 | AI Assistant | Creación inicial del caso de prueba |
| | | | - 4 pruebas principales |
| | | | - Verificaciones avanzadas de QA |
| | | | - Troubleshooting completo |

---

## ✅ Firmas de Aprobación

| Rol | Nombre | Firma | Fecha |
|-----|--------|-------|-------|
| **Desarrollador** | [Tu nombre] | ☐ | ☐ |
| **QA** | [QA Lead] | ☐ | ☐ |
| **Scrum Master** | [SM] | ☐ | ☐ |

---

## 📞 Contacto y Escalación

- **Problemas técnicos:** Revisar sección "Troubleshooting"
- **Dudas sobre el caso:** Abrir issue en GitHub con etiqueta `testing`
- **Solicitar cambios:** Crear PR con rama `feature/test-updates`

---

**Fin del Documento**  
*Este documento debe ser revisado y aprobado antes de incluirlo en Release oficial.*

