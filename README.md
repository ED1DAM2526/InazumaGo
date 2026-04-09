# Inazuma Go

Proyecto InazumaGo: aplicación de escritorio JavaFX para jugar Inazuma Go con integración a Firebase Realtime Database.

## Tabla de contenidos

1. [Inicio rápido](#inicio-rápido)
2. [Documentación principal](#documentación-principal)
3. [Compilación y pruebas](#compilación-y-pruebas)
4. [Ejecución de la aplicación](#ejecución-de-la-aplicación)
5. [Configuración local](#configuración-local)
6. [Notas para desarrolladores](#notas-para-desarrolladores)

## Inicio rápido

Para ejecutar tests y compilar localmente en PowerShell (Windows):

```powershell
# Opción 1: Usar el script de JDK local (si tienes configurado doc/ia/user-prompt.md)
powershell -ExecutionPolicy Bypass -File .\scripts\use-user-jdk.ps1 -RunMaven

# Opción 2: Usar Maven directamente (si está en PATH)
mvn clean compile -DskipTests
mvn test
```

Para ejecutar la aplicación:

```powershell
# Opción 1: Con script de JDK local
powershell -ExecutionPolicy Bypass -File .\scripts\use-user-jdk.ps1 -RunMain

# Opción 2: Con Java directamente
java -cp target/classes;target/dependency/* es.iesquevedo.Main
```

## Documentación principal

Lee los siguientes documentos para entender el proyecto:

- **[`doc/manual-usuario.md`](doc/manual-usuario.md)** — Manual de usuario: instrucciones de uso, casos de uso, ejemplos paso a paso y FAQ.
- **[`doc/manual-tecnico.md`](doc/manual-tecnico.md)** — Manual técnico: arquitectura, contratos, configuración y guía de desarrollo.
- **[`doc/reglamento-inazumago.md`](doc/reglamento-inazumago.md)** — Reglamento del juego: reglas, validaciones y políticas.
- **[`doc/ci.md`](doc/ci.md)** — Guía de CI/CD y ejecución de tests automáticos.
- **[`doc/normas-trabajo-proyecto.md`](doc/normas-trabajo-proyecto.md)** — Normas de trabajo: flujo de ramas, PR y revisiones.

## Compilación y pruebas

### Ejecutar tests

```powershell
# Tests con Maven (recomendado)
mvn -DskipTests=false test

# Ver resultados
# Los reportes de Surefire estarán en target/surefire-reports/
```

### Empaquetar el proyecto

```powershell
mvn clean package
# El JAR se genera en target/InazumaGo-1.0-SNAPSHOT.jar
```

### Usar el script de JDK local

Si necesitas usar un JDK específico configurado en `doc/ia/user-prompt.md`:

```powershell
# Configurar ruta del JDK en la sesión actual
.\scripts\use-user-jdk.ps1

# Compilar y ejecutar tests
.\scripts\use-user-jdk.ps1 -RunMaven

# Compilar, ejecutar tests y lanzar la app
.\scripts\use-user-jdk.ps1 -RunMaven -RunMain
```

**Nota:** Para usar este script, edita `doc/ia/user-prompt.md` y añade una línea como:
```powershell
$env:JAVA_HOME = 'C:\ruta\a\tu\jdk'
```

## Ejecución de la aplicación

### Aplicación GUI (JavaFX)

```powershell
# Compilar primero
mvn clean compile

# Ejecutar la app (abre ventana gráfica)
java -cp target/classes;target/dependency/* es.iesquevedo.Main
```

### Modo consola

```powershell
# Ejecutar en modo CLI sin GUI
java -cp target/classes;target/dependency/* es.iesquevedo.Main console
```

### Con script

```powershell
.\scripts\use-user-jdk.ps1 -RunMain
```

## Configuración local

### Variables de entorno

Para usar Firebase Realtime Database en producción, configura:

```powershell
$env:FIREBASE_URL = 'https://tu-proyecto.firebaseio.com'
```

Si no está configurada, la app usa un repositorio en memoria para pruebas.

### Credenciales

**NO subas credenciales al repositorio.** Para desarrollo local:

1. Crea un archivo local `doc/ia/user-prompt.md` (no se sube a Git)
2. Configura ahí tus variables:
   ```powershell
   $env:JAVA_HOME = 'C:\ruta\jdk'
   $env:FIREBASE_URL = 'https://...'
   ```

Consulta `.gitignore` para archivos excluidos.

## Notas para desarrolladores

### Estructura del proyecto

```
src/
  main/
    java/es/iesquevedo/
      Main.java                    # Punto de entrada (GUI + CLI)
      controller/
        MainController.java        # Controlador FXML
      service/
        MainService.java           # Interfaz de servicios
        impl/MainServiceImpl.java   # Implementación
      repository/
        MainRepository.java        # Interfaz de persistencia
        inmemory/...              # Implementación en memoria
        firebase/...              # Implementación Firebase
    resources/
      fxml/
        Main.fxml                  # Interfaz principal JavaFX
      application.properties       # Configuración
  test/
    java/...                       # Tests unitarios e integración
```

### Dependencias principales

- **JavaFX 21** — Framework GUI
- **Maven** — Gestión de dependencias y build
- **JUnit 5** — Testing
- **Mockito** — Mocking en tests
- **WireMock** — Stubbing de HTTP en tests

### Próximos pasos

Para contribuir:

1. Lee [`doc/normas-trabajo-proyecto.md`](doc/normas-trabajo-proyecto.md)
2. Crea una rama desde `develop`: `git checkout -b feature/descripción`
3. Haz commit con mensajes descriptivos
4. Abre un PR y espera revisiones
5. Merge a `develop` una vez aprobado

### Más información

- [`doc/epicas-historias-sprints.md`](doc/epicas-historias-sprints.md) — Plan de sprints y historias de usuario
- [`scripts/`](scripts/) — Scripts útiles de PowerShell
- [`doc/ia/`](doc/ia/) — Prompts para IA (no editar sin consentimiento del equipo)
powershell -ExecutionPolicy Bypass -File .\scripts\use-user-jdk.ps1 -JdkPath 'C:\Program Files\Java\jdk-17' -RunMaven -RunMain
