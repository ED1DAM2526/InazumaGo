# ✅ CHECKLIST RÁPIDO

**Caso de Prueba:** CP-US1-T4  
**Objetivo:** Verificar que la app inicia y health() responde  
**Tester:** _________________  
**Fecha:** _________________  
**Resultado Final:** ☐ PASS | ☐ FAIL

---

## 🚀 PREPARACIÓN

- [ ] JDK 21+ instalado: `java -version`
- [ ] Maven instalado: `mvn -version`
- [ ] Repositorio clonado en `C:\InazumaGo`
- [ ] Acceso a PowerShell

---

## 🧪 PRUEBAS RÁPIDAS

### P1: Compilación
```powershell
mvn clean compile
```
- [ ] Resultado: `BUILD SUCCESS`
- [ ] Sin errores de compilación
- [ ] Tiempo: _____ segundos

### P2: Pruebas Unitarias
```powershell
mvn test
```
- [ ] `HealthControllerTest.health_shouldReturnOK()` → PASSED
- [ ] Resultado: `BUILD SUCCESS`
- [ ] Tests run: 1, Failures: 0

### P3: Modo Consola
```powershell
mvn clean package -DskipTests
java -cp "target/InazumaGo-1.0-SNAPSHOT.jar;target/dependency/*" es.iesquevedo.MainApp console
```
- [ ] App inicia sin excepciones
- [ ] health() retorna: `OK`
- [ ] Menú consola visible

### P4: Modo GUI
```powershell
mvn javafx:run
```
- [ ] Ventana se abre
- [ ] Título: "InazumaGo" ✅
- [ ] Dimensiones: 600x400 ✅
- [ ] Log: "Aplicación JavaFX iniciada exitosamente" ✅

---

## 🔍 VALIDACIONES CRÍTICAS

- [ ] `health()` retorna exactamente `"OK"`
- [ ] No hay `SEVERE` ni `ERROR` en logs
- [ ] No hay `NullPointerException`
- [ ] No hay `LoadException` en FXML
- [ ] HealthController accessible desde MainController

---

## 📊 RESULTADO FINAL

**Criterio de Aceptación:** Todas las pruebas deben ser ✅

| P1 | P2 | P3 | P4 | **RESULTADO** |
|----|----|----|----|----|
| ☐ ✅ | ☐ ✅ | ☐ ✅ | ☐ ✅ | **☐ PASS** |
| ☐ ❌ | ☐ ❌ | ☐ ❌ | ☐ ❌ | **☐ FAIL** |

---

## 📝 NOTAS / OBSERVACIONES

```
___________________________________________________________________

___________________________________________________________________

___________________________________________________________________
```

---

**Tester:** _________________ **Fecha:** _________________ **Hora:** _________________

**Aprobado por:** _________________ **Firma:** _________________

