# 🎯 INSTRUCCIONES PASO-A-PASO: Resolver Error 401

## Estado Actual

✅ **Código** está CORRECTO  
✅ **firebase-admin** está ELIMINADO  
✅ **Compilación** es EXITOSA  
❌ **Error 401** persiste porque Firebase Rules no están configuradas

---

## 🔧 SOLUCIÓN EN 5 MINUTOS

### Paso 1: Ir a Firebase Console (1 minuto)

1. Abre en navegador: **https://console.firebase.google.com**
2. Login con tu cuenta Google (la misma que creó el proyecto)
3. Selecciona el proyecto **"inazumago"** (o el nombre que uses)

### Paso 2: Acceder a Realtime Database (1 minuto)

1. En el menú izquierdo, haz clic en **"Build"** (si no lo ves, expande)
2. Haz clic en **"Realtime Database"**
3. Verás tu base de datos: `inazumago-default-rtdb` (o similar)
4. Haz clic en ella para abrirla

### Paso 3: Acceder a las Reglas (1 minuto)

1. Verás tres pestañas en la parte superior: **Data**, **Rules**, **Backups**
2. Haz clic en la pestaña **"Rules"**
3. Verás un editor JSON con las reglas actuales (probablemente vacío o muy restrictivo)

### Paso 4: Copiar y Pegar las NUEVAS Reglas (1 minuto)

**BORRA TODO en el editor** (Ctrl+A, Delete)

**COPIA Y PEGA exactamente esto:**

```json
{
  "rules": {
    ".read": "auth != null",
    ".write": "auth != null"
  }
}
```

### Paso 5: PUBLICAR las Reglas (1 minuto)

1. Verás un botón **"Publish"** en la esquina inferior derecha del editor
2. Haz clic en **"Publish"**
3. Espera a que aparezca el mensaje: **"Rules published successfully"**
4. Verás un timestamp actualizado (ej: "Last published on May 22, 11:59 PM")

---

## ✅ VERIFICACIÓN

Si ves esto en Firebase Console:

```
✓ Pestaña "Rules" muestra tu JSON
✓ Campo de edición muestra exactamente:
  {
    "rules": {
      ".read": "auth != null",
      ".write": "auth != null"
    }
  }
✓ Verdes sin errores (no hay líneas rojas)
✓ Botón "Publish" está disponible
✓ Después de publicar, ves "Rules published successfully"
✓ Timestamp actualizado
```

---

## 🔄 EN INTELLIJ IDEA

Después de publicar las rules:

### Paso 1: Invalidar Cache (20 segundos)

1. **File** → **Invalidate Caches**
2. Selecciona **"Invalidate and Restart"**
3. IntelliJ se reiniciará

### Paso 2: Clean Build (30 segundos)

Después del reinicio:
1. **Build** → **Clean** (espera a que termine)
2. **Build** → **Clean Build** (espera a que termine)

### Paso 3: Ejecutar Aplicación (10 segundos)

1. **Run** → **Run 'MainGUI'** (o busca en Run Configurations)
2. Espera a que la app abra

---

## 🧪 PRUEBA LA APLICACIÓN

1. **Login**: Usa `prueba1@gmail.com` y tu contraseña de Firebase
2. **Mira los logs**: Deberías ver:
   ```
   ✓ Login exitoso: prueba1@gmail.com
   ✓ Token guardado en AppState: SÍ
   ✓ Token configurado en Firebase Repository
   ✓ Navegado a pantalla de emparejamiento
   ✓ Buscando partida en Firebase...
   ```
3. **Si no ves 401**: ¡ÉXITO! El problema está resuelto

---

## ⚠️ SI SIGUES VIENDO 401

### Síntoma: Sigues viendo "Error al crear game: 401"

**Causas posibles** (en orden de probabilidad):

#### 1. No esperaste lo suficiente después de publicar
- **Solución**: Espera 1-2 minutos
- **Por qué**: Firebase toma tiempo en propagar las reglas

#### 2. Firebase Console muestra rules antiguas
- **Comprueba**: Mira el timestamp en la esquina (¿es reciente?)
- **Solución**: F5 para refrescar la página de Firebase Console

#### 3. No publicaste correctamente
- **Comprueba**: ¿Viste "Rules published successfully"?
- **Solución**: Vuelve a hacer click en "Publish"

#### 4. Estás en el RTDB incorrecto
- **Comprueba**: ¿La URL dice `inazumago-default-rtdb`? (o tu nombre exacto)
- **Solución**: Verifica que en tu app `MatchingScreenController` usa la misma URL

#### 5. La RTDB está desactivada
- **Comprueba**: En Realtime Database, ¿dice "Disabled" o similar?
- **Solución**: Haz clic en "Create Database" si está desactivada

---

## 📞 Checklist Final ANTES de ejecutar

- [ ] Publiqué Rules en Firebase Console
- [ ] Vi el mensaje "Rules published successfully"
- [ ] Invalidé el cache en IntelliJ (File → Invalidate Caches → Restart)
- [ ] Hice Clean Build (Build → Clean → Build → Clean Build)
- [ ] Esperé al menos 30 segundos después de publicar

---

## 🎉 CUANDO FUNCIONE

Verás en los logs:

```
may 22, 2026 11:51:21 P. M. es.iesquevedo.controller.LoginController onLoginClicked
INFORMACIÓN: Botón login clickeado
may 22, 2026 11:51:22 P. M. es.iesquevedo.service.impl.AuthServiceImpl lambda$login$1
INFORMACIÓN: Login exitoso: prueba1@gmail.com
may 22, 2026 11:51:22 P. M. es.iesquevedo.ui.MatchingScreenController startMatching
INFORMACIÓN: Token configurado en Firebase Repository
may 22, 2026 11:51:24 P. M. es.iesquevedo.ui.MatchingScreenController createNewGame
INFORMACIÓN: Juego creado en Firebase: game_xxxxx
```

**¡SIN 401! ✅**

---

## 🚀 Resumen Ultra-Rápido

1. Firebase Console → Realtime Database → Reglas
2. Copiar/pegar reglas que mostré arriba
3. Publish
4. IntelliJ → Invalidate Caches → Restart
5. Build → Clean Build
6. Run
7. ¡Listo!

---

**¿Necesitas ayuda con algún paso? Déjame saber el número del paso donde te atascas.**

