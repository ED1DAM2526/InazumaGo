# Guía de Debugging para Error 401 en Firebase

## 🔴 Error 401 = "No Autorizado"

Este error significa que Firebase rechazó la petición porque:
1. **No hay token** → `idToken` es NULL
2. **Token caducó** → El token tiene > 1 hora
3. **Token inválido** → Malformado o incorrecto
4. **Permisos insuficientes** → Security Rules denegan acceso
5. **Mezcla de autenticación** → Usando tanto REST como Admin SDK simultáneamente

## ✅ Paso 1: Verificar que el Token se Obtiene

### Edita `LoginController.java` línea 107:
```java
// Log para debuguear
LOGGER.log(Level.INFO, "Token guardado en AppState: " + (token != null ? "SÍ" : "NO"));
+ LOGGER.log(Level.INFO, "Token (primeros 50 chars): " + (token != null ? token.substring(0, Math.min(50, token.length())) : "NULL"));
```

### Lo que deberías ver en los logs:
```
Login exitoso para: usuario@ejemplo.com
Token guardado en AppState: SÍ
Token (primeros 50 chars): eyJhbGciOiJSUzI1NiIsImtpZCI6IjEyMzQ1Njc4OTAiLCJ...
```

## ✅ Paso 2: Verificar que el Token se Configura en el Repositorio

### Ya está en `MatchingScreenController.java` línea 70-71:
```java
if (token != null) {
    firebaseRepository.setIdToken(token);
    LOGGER.log(java.util.logging.Level.INFO, "Token configurado en Firebase Repository");
} else {
    LOGGER.log(java.util.logging.Level.WARNING, "⚠️ Token es NULL en AppState");
}
```

### Lo que deberías ver:
```
Token en AppState: eyJhbGciOiJSUzI1NiIsImtpZCI6IjEyMzQ1Njc4OTAiLCJ...
Token configurado en Firebase Repository
```

O si no:
```
⚠️ Token es NULL en AppState
```

## ✅ Paso 3: Verificar que la URL Incluye el Token

### Edita `FirebaseMainRepository.java` línea 183:

```java
// Después de construir la URL
if (idToken != null) {
    url += "?auth=" + idToken;
}

// Agrega:
LOGGER.log(Level.INFO, "URL de petición listGames: " + 
    (url.contains("?auth=") ? 
        url.substring(0, url.lastIndexOf("?auth=")) + "?auth=***" 
        : url));
```

### Lo que deberías ver:
```
URL de petición listGames: https://inazumago-default-rtdb.firebaseio.com/games.json?auth=***
```

Si ves sin `?auth=`:
```
URL de petición listGames: https://inazumago-default-rtdb.firebaseio.com/games.json
```

**PROBLEMA**: El token no se está añadiendo.

## ✅ Paso 4: Verificar Security Rules

### En Firebase Console:
1. Ve a **Realtime Database** → **Rules**
2. Deberías tener algo como:
```json
{
  "rules": {
    ".read": "auth != null",
    ".write": "auth != null",
    "games": {
      ".read": "auth != null",
      ".write": "auth != null"
    }
  }
}
```

### Si ves esto (demasiado permisivo):
```json
{
  "rules": {
    ".read": true,
    ".write": true
  }
}
```

**NO es problema para 401, pero es un riesgo de seguridad**. La base de datos sería pública.

## 🧪 Test Manual: cURL para Verificar Token

```bash
# 1. Obtén un token real (login)
# Copia el token que ves en los logs de tu app

# 2. Prueba la petición manualmente
curl -X GET "https://inazumago-default-rtdb.firebaseio.com/games.json?auth=TU_TOKEN_AQUI"

# Si funciona:
{"game1": {...}, "game2": {...}}

# Si da 401:
{"error": "Permission denied"}
```

## 🔧 Soluciones por Causa

### Causa 1: Token es NULL
**Síntomas**:
- Logs muestran "Token es NULL en AppState"
- No puedes logearte

**Soluciones**:
- [ ] Verifica que `AuthServiceImpl.login()` devuelva un token válido
- [ ] Verifica que `LoginController.onLoginClicked()` guarde el token en AppState
- [ ] Comprueba que Firebase Auth REST API está disponible (no bloqueada)

### Causa 2: Token ha caducado
**Síntomas**:
- Token aparece en logs al login
- Después de esperar > 1 hora, falla 401

**Soluciones**:
- [ ] `AuthServiceImpl.isTokenExpiring()` verifica solo cada 5 minutos
- [ ] `AuthServiceImpl.getCurrentToken()` llama a `refreshAccessToken()` si es necesario
- [ ] Verifica que `refreshToken` se guardó en login

### Causa 3: Token no se incluye en URL
**Síntomas**:
- Logs muestran URL sin `?auth=`

**Soluciones**:
- [ ] Verifica que `setIdToken()` fue llamado
- [ ] Verifica que el constructor no sobrescribe `idToken` a null
- [ ] En `FirebaseMainRepository.listGames()`, el token debe estar en URL

### Causa 4: Firebase Security Rules denegan acceso
**Síntomas**:
- curl devuelve 401 incluso con token válido
- Token es válido y se incluye en URL

**Soluciones**:
- [ ] Actualiza las Security Rules en Firebase Console
- [ ] Verifica que `"auth != null"` permita lectura/escritura
- [ ] Prueba con `{".read": true, ".write": true}` temporalmente para confirmar

### Causa 5: Mezcla de autenticación (firebase-admin + REST)
**Síntomas**:
- Conflictos extraños en classpath
- A veces funciona, a veces no

**Soluciones**:
- [ ] ✅ **YA RESUELTO**: `firebase-admin` eliminado del pom.xml
- [ ] Ejecuta `mvn dependency:tree` para confirmar
- [ ] Busca `firebase-admin` en output

```bash
mvn dependency:tree | grep -i firebase
```

Debería mostrar solo:
- `com.google.code.gson` (Gson)
- `com.squareup.okhttp3` (OkHttp)

NO debería mostrar:
- ❌ `com.google.firebase:firebase-admin`
- ❌ `com.google.firebase:firebase-database`

## 📋 Checklist Completo

- [ ] Proyecto compila sin errores
- [ ] Logs muestran "Token guardado en AppState: SÍ"
- [ ] Logs muestran "Token configurado en Firebase Repository"
- [ ] URL contiene `?auth=...`
- [ ] Firebase Security Rules permiten lectura con `auth != null`
- [ ] `mvn dependency:tree` no muestra `firebase-admin`
- [ ] Token se refresca automáticamente (no caducado)

## 🆘 Si Aún No Funciona

1. **Captura todos los logs**:
   ```bash
   # En la salida de consola de tu JavaFX app
   # Copia TODOS los mensajes desde login hasta el error 401
   ```

2. **Usa el debugging de Nivel FINE**:
   - Edita `src/main/resources/logging.properties`
   - Cambia `.level=INFO` a `.level=FINE`

3. **Test con WireMock locally**:
   ```bash
   # Si tienes WireMock configurado para tests
   mvn test
   ```

4. **Verifica Firebase Console**:
   - [Firebase Console](https://console.firebase.google.com)
   - Selecciona tu proyecto
   - Realtime Database → Rules → Current
   - Verifica que permite `auth != null`

## 📞 Información que Necesitas para Debugging

Si aún falla, recopila:
1. **URL de Firebase**: `https://inazumago-default-rtdb.firebaseio.com`
2. **Primeros 50 caracteres del token**: De los logs
3. **Resultado de curl**: `curl -X GET "..."`
4. **Security Rules actuales**: De Firebase Console
5. **Salida de**: `mvn dependency:tree | grep firebase`
6. **Versión Java**: `java -version`
7. **Versión Maven**: `mvn --version`

