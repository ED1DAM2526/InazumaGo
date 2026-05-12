# Guía de Configuración: Firebase Realtime Database

## Propósito

Este documento describe cómo configurar los parámetros de conexión, timeout y reintentos hacia Firebase RTDB mediante `application.properties` y la clase `AppConfig`.

---

## 1. Parámetros en `application.properties`

### Firebase RTDB Endpoint

```properties
firebase.rtdb.url=https://your-project.firebaseio.com
```

- **Descripción**: URL base del proyecto Firebase RTDB.
- **Ejemplo**: `https://my-game-project.firebaseio.com`
- **Nota**: Obtén este valor de la consola de Firebase > Realtime Database > URL.

### Timeouts (en milisegundos)

```properties
firebase.rtdb.timeout.connect=10000
firebase.rtdb.timeout.read=30000
firebase.rtdb.timeout.write=30000
```

| Propiedad | Descripción | Valor Recomendado | Rango |
|---|---|---|---|
| `firebase.rtdb.timeout.connect` | Timeout de conexión inicial | 10000 ms (10 s) | 5000 – 20000 ms |
| `firebase.rtdb.timeout.read` | Timeout de lectura | 30000 ms (30 s) | 10000 – 60000 ms |
| `firebase.rtdb.timeout.write` | Timeout de escritura | 30000 ms (30 s) | 10000 – 60000 ms |

**Notas**:
- Tiempos más cortos reducen latencia pero pueden causar timeouts en conexiones lentas.
- Tiempos más largos son más robustos pero aumentan latencia percibida.

### Configuración de Reintentos

```properties
firebase.http.retry.max-attempts=3
firebase.http.retry.backoff-multiplier=2.0
firebase.http.retry.initial-delay-ms=500
```

| Propiedad | Descripción | Valor Recomendado |
|---|---|---|
| `firebase.http.retry.max-attempts` | Número máximo de reintentos | 3 – 5 |
| `firebase.http.retry.backoff-multiplier` | Multiplicador exponencial de delay | 2.0 |
| `firebase.http.retry.initial-delay-ms` | Delay inicial (ms) | 500 – 1000 |

**Ejemplo de cálculo de delays**:
- Intento 1: 500 ms
- Intento 2: 500 × 2.0 = 1000 ms
- Intento 3: 1000 × 2.0 = 2000 ms
- Total: ~3.5 segundos

---

## 2. Clase `AppConfig` - Métodos de Configuración

### Cargar OkHttpClient Configurado

```java
Properties props = new Properties();
props.load(new FileInputStream("application.properties"));

OkHttpClient httpClient = AppConfig.createFirebaseHttpClient(props);
```

El método `createFirebaseHttpClient(Properties)` automáticamente:
1. Lee los parámetros de timeout desde Properties.
2. Lee la configuración de reintentos.
3. Construye un `OkHttpClient` con los valores parseados.
4. Aplica valores por defecto si una propiedad está ausente.

### Cargar URL de Firebase

```java
Properties props = new Properties();
props.load(new FileInputStream("application.properties"));

String firebaseUrl = AppConfig.loadFirebaseUrl(props);
MainRepository repo = AppConfig.createMainRepository(firebaseUrl);
```

### Ejemplo Completo de Inicialización

```java
import java.io.IOException;
import java.util.Properties;

public class ApplicationBootstrap {
    public static void main(String[] args) throws IOException {
        // 1. Cargar propiedades
        Properties props = new Properties();
        props.load(ApplicationBootstrap.class.getResourceAsStream("/application.properties"));

        // 2. Obtener configuración de Firebase
        String firebaseUrl = AppConfig.loadFirebaseUrl(props);
        OkHttpClient httpClient = AppConfig.createFirebaseHttpClient(props);

        // 3. Crear repositorio y servicio
        MainRepository repo = AppConfig.createMainRepository(firebaseUrl);
        MainService service = new MainServiceImpl(repo);

        // Uso...
        System.out.println("Firebase URL: " + firebaseUrl);
        System.out.println("HTTP Client configurado con timeouts.");
    }
}
```

---

## 3. Valores Recomendados por Entorno

### Desarrollo Local

```properties
firebase.rtdb.url=https://your-dev-project.firebaseio.com
firebase.rtdb.timeout.connect=10000
firebase.rtdb.timeout.read=30000
firebase.rtdb.timeout.write=30000
firebase.http.retry.max-attempts=3
firebase.http.retry.backoff-multiplier=2.0
firebase.http.retry.initial-delay-ms=500
```

### Pruebas con Emulador Firebase

```properties
firebase.rtdb.url=http://localhost:9000
firebase.rtdb.timeout.connect=5000
firebase.rtdb.timeout.read=10000
firebase.rtdb.timeout.write=10000
firebase.http.retry.max-attempts=1
firebase.http.retry.backoff-multiplier=1.0
firebase.http.retry.initial-delay-ms=100
```

### Producción

```properties
firebase.rtdb.url=https://your-prod-project.firebaseio.com
firebase.rtdb.timeout.connect=15000
firebase.rtdb.timeout.read=45000
firebase.rtdb.timeout.write=45000
firebase.http.retry.max-attempts=5
firebase.http.retry.backoff-multiplier=2.0
firebase.http.retry.initial-delay-ms=500
```

---

## 4. Gestión de Tokens y Seguridad

### Variables de Entorno

Para evitar hardcodear tokens, usa variables de entorno:

```properties
firebase.auth.token=${FIREBASE_ID_TOKEN}
```

Luego, en tu aplicación:

```java
String token = System.getenv("FIREBASE_ID_TOKEN");
```

### Cloud Functions (Recomendado)

Para operaciones sensibles, delega a una Cloud Function backend:
- El cliente obtiene un `idToken` desde Firebase Authentication.
- El cliente invoca una Cloud Function con el `idToken`.
- La Cloud Function valida el token y ejecuta la operación con permisos elevados.

### Reglas de Seguridad

Siempre valida en las reglas de Firebase RTDB:

```json
{
  "rules": {
    "games": {
      "$gameId": {
        ".read": "auth != null && root.child('games').child($gameId).child('meta').child('players').child(auth.uid).exists()",
        ".write": "false",
        "moves": {
          "$moveId": {
            ".write": "auth != null && newData.child('playerId').val() == auth.uid"
          }
        }
      }
    }
  }
}
```

---

## 5. Configuración por Perfiles (application-{profile}.properties)

Spring Boot soporta perfiles de configuración:

### application-dev.properties
```properties
firebase.rtdb.url=https://dev-project.firebaseio.com
firebase.rtdb.timeout.connect=10000
firebase.rtdb.timeout.read=30000
firebase.rtdb.timeout.write=30000
```

### application-prod.properties
```properties
firebase.rtdb.url=https://prod-project.firebaseio.com
firebase.rtdb.timeout.connect=15000
firebase.rtdb.timeout.read=45000
firebase.rtdb.timeout.write=45000
```

### Activar Perfil

En la línea de comandos:
```bash
java -jar app.jar --spring.profiles.active=prod
```

---

## 6. Validación de Configuración

Implementa validación al arrancar la aplicación:

```java
public class ConfigValidator {
    public static void validate(Properties props) {
        String firebaseUrl = AppConfig.loadFirebaseUrl(props);
        if (firebaseUrl == null || firebaseUrl.isBlank()) {
            throw new IllegalArgumentException("firebase.rtdb.url no configurada");
        }
        
        long connectTimeout = parseLong(props, "firebase.rtdb.timeout.connect", 10000L);
        if (connectTimeout <= 0) {
            throw new IllegalArgumentException("firebase.rtdb.timeout.connect debe ser > 0");
        }
        
        System.out.println("✓ Configuración de Firebase validada correctamente.");
    }
}
```

---

## 7. Troubleshooting

| Problema | Causa Probable | Solución |
|---|---|---|
| `Connection timeout` | Timeout muy corto o red lenta | Aumentar `firebase.rtdb.timeout.connect` |
| `Read timeout` | Timeout muy corto para operación lenta | Aumentar `firebase.rtdb.timeout.read` |
| `Too many requests` | Límite de Firebase excedido | Implementar backoff exponencial |
| `401 Unauthorized` | Token expirado o inválido | Renovar token de autenticación |
| `403 Forbidden` | Reglas de seguridad deniegan acceso | Revisar reglas y permisos del usuario |

---

## 8. Referencias

- [Firebase Realtime Database - Documentation](https://firebase.google.com/docs/database)
- [OkHttp - Configuration](https://square.github.io/okhttp/)
- [Spring Boot - Externalized Configuration](https://spring.io/projects/spring-boot)
- [Firebase Security Rules](https://firebase.google.com/docs/database/security)

---

**Última actualización**: 2026-04-28
**Documento relacionado**: `doc/firebase-realtime-plan.md`

