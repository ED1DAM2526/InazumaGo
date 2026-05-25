# 🚨 SOLUCIÓN DEL ERROR 401 - PASOS INMEDIATOS

## EL PROBLEMA
Tu código está enviando **todo correctamente** pero Firebase **RTDB está rechazando con 401** porque las Security Rules son demasiado restrictivas o tienen validaciones incompatibles con tu estructura de datos.

## LA CAUSA
Las validaciones en las Rules que pusiste requieren campos exactos y estructura exacta:
```json
".validate": "newData.hasChildren(['id', 'blackPlayer', 'status', 'createdAt'])"
```

Pero tu `GameDto` tiene más campos (board, currentTurn, moves, etc.) que causan conflictos en la validación.

---

## ✅ SOLUCIÓN - PUBLICAR REGLAS SIMPLES

### **PASO 1: Abre Firebase Console**
1. Ve a: https://console.firebase.google.com
2. Selecciona proyecto **InazumaGo**
3. Ve a **Realtime Database**
4. Haz click en **Rules**

### **PASO 2: Reemplaza COMPLETAMENTE el contenido con:**

```json
{
  "rules": {
    ".read": "auth != null",
    ".write": "auth != null",

    "games": {
      "$gameId": {
        ".read": "auth != null",
        ".write": "auth != null"
      }
    },

    "users": {
      "$uid": {
        ".read": "auth.uid == $uid",
        ".write": "auth.uid == $uid"
      }
    }
  }
}
```

### **PASO 3: Haz click en PUBLISH**
⚠️ **IMPORTANTE**: Espera a que aparezca un mensaje verde: "Rules published successfully"

### **PASO 4: Vuelve a IntelliJ (SIN cerrar la app)**
- La app debería intentar crear el game nuevamente
- Deberías ver: ✅ **"Juego creado en Firebase"** en los logs

---

## 📋 Checklist Rápido

- [ ] Firebase Console abierta en Rules
- [ ] Contenido anterior eliminado completamente
- [ ] Reglas simples pegadas
- [ ] **PUBLISHED** (verifica el botón verde)
- [ ] La app SIGUE corriendo (no cierres)
- [ ] Haces click en "Buscar partida" de nuevo
- [ ] Revisa los logs de consola

---

## 🎯 DESPUÉS (Una vez que funcione)

Cuando see "Juego creado en Firebase" con estos logs simples, podremos agregar validaciones más específicas, pero primero necesitamos que la conexión básica funcione.

---

## ❌ Si SIGUE dando 401...

Entonces el token está realmente expirado o hay un problema con la base de datos. En ese caso:

1. Logout de la app
2. Login nuevamente
3. Intenta crear game

Esto te dará un token FRESCO.

