
## 🔄 Actualización - 2026-04-14

### Nuevas tareas completadas

#### E1-US2-T4 - Test de integración con Mockito

**Cambios realizados:**
- Añadidas dependencias Mockito en `pom.xml`
    - `mockito-core:5.5.0`
    - `mockito-junit-jupiter:5.5.0`
- Creado `MainControllerIntegrationTest.java`
    - Verifica que `MainController` delega correctamente en `MainService`
    - Testea escenario con servicio mockeado
    - Testea escenario con servicio null
    - Testea inyección de servicio

**Validación:**
- ✅ `mvn test -Dtest=MainControllerIntegrationTest` → BUILD SUCCESS
- ✅ Tests run: 3, Failures: 0
- ✅ Reportes generados en `target/surefire-reports/`

**Archivos modificados/creados:**
- `pom.xml` (dependencias Mockito)
- `src/test/java/es/iesquevedo/integration/MainControllerIntegrationTest.java` (nuevo)

---

**Estado actual del proyecto:**
- ✅ Compilación: BUILD SUCCESS
- ✅ Tests unitarios: pasan
- ✅ Tests de integración: pasan
- ✅ Pipeline CI: configurado
- ✅ Documentación: actualizada