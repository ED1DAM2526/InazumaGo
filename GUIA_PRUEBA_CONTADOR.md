# 🧪 GUÍA DE PRUEBA - Sistema de Contador de Tiempo

## 📋 Resumen de lo que se hizo

Se han realizado cambios en dos archivos principales para implementar un sistema de contador de tiempo **regresivo**:

1. **LocalGameController.java** - Para partidas locales
2. **GameController.java** - Para partidas multijugador online

Además se creó:
3. **GameTimerTest.java** - Suite de tests para validar el comportamiento

---

## 🚀 Cómo Probar Localmente

### Opción 1: Compilar y Ejecutar Manualmente

```bash
cd C:\Users\Stiven\InazumaGo

# Limpiar y compilar
.\mvnw clean compile

# Si JDK está disponible
.\mvnw clean package
java -jar target/InazumaGo-1.0-SNAPSHOT.jar
```

### Opción 2: Ejecutar Tests Unitarios

```bash
# Ejecutar todos los tests de timer
.\mvnw test -Dtest=GameTimerTest

# Ejecutar test específico
.\mvnw test -Dtest=GameTimerTest#testInitialTimeIsCorrect

# Ejecutar con cobertura
.\mvnw test jacoco:report
```

### Opción 3: En el IDE (IntelliJ IDEA)

1. Abrir el proyecto en IntelliJ
2. Click derecho en `LocalGameController.java` → "Run tests"
3. O click en el play verde junto a `startGameTimer()`
4. Verificar la consola para ver logs

---

## 🎯 Pruebas Manuales en la GUI

### Test 1: Partida Local con Tiempo Normal (3 minutos)

**Pasos:**
1. Abrir la aplicación
2. Ir a "Partida Local"
3. Observar los contadores al inicio

**Verificar:**
- ✅ Ambos jugadores muestran "03:00"
- ✅ El tiempo empieza en 3 minutos (180 segundos)
- ✅ Al hacer un movimiento, el turno cambia

**Observar durante el juego:**
- ✅ El tiempo del jugador en turno disminuye (03:00 → 02:59 → 02:58...)
- ✅ El tiempo del jugador esperando no cambia
- ✅ Al cambiar de turno, el otro comienza a contar

---

### Test 2: Simular Fin por Tiempo (OPTIONAL - necesita modificación)

Para probar con 5 segundos sin esperar 3 minutos:

**En LocalGameController.java (línea ~75), modificar:**

```java
// TEMPORAL: Descomentar para test
LocalGameController.setTestTimeMs(5_000);  // 5 segundos en lugar de 3 minutos
```

**Pasos:**
1. Hacer la modificación arriba
2. Compilar y ejecutar
3. Iniciar partida local
4. Esperar 5 segundos sin hacer nada

**Verificar:**
- ✅ Después de 5 segundos exactos: "00:00"
- ✅ Se muestra mensaje: "⏱️ ¡Tiempo agotado! Jugador Negro se quedó sin tiempo"
- ✅ Se determina ganador automáticamente
- ✅ Se muestra diálogo de victoria
- ✅ Se puede volver al menú

---

### Test 3: Partida Multijugador (si Firebase está disponible)

**Pasos:**
1. Abrir aplicación
2. Ir a "Multijugador"
3. Crear o unirse a partida
4. Observar contadores

**Verificar:**
- ✅ Ambos jugadores ven "03:00" inicial
- ✅ Solo el jugador en turno ve su tiempo decrecer
- ✅ El otro jugador ve el conteo del rival disminuir en tiempo real

---

## 📊 Validación de Cambios

### Checklist de Verificación

#### ✅ LocalGameController.java

- [ ] Línea ~38: `INITIAL_TIME_MS = 180_000`
- [ ] Línea ~39: `testTimeMs = 0`
- [ ] Línea ~56-57: `player1TimeMs` y `player2TimeMs` SIN inicializar a 0
- [ ] Línea ~72: Método `setTestTimeMs(long timeMsForTest)`
- [ ] Línea ~79-92: `initializeLocalGame()` asigna tiempos
- [ ] Línea ~426-461: `startGameTimer()` RESTA tiempo (-)
- [ ] Línea ~426-461: Verifica `if (player1TimeMs <= 0)` → `endGameByTime(1, 2)`
- [ ] Línea ~426-461: Verifica `if (player2TimeMs <= 0)` → `endGameByTime(2, 1)`
- [ ] Línea ~463-481: Método `endGameByTime()` existe y llama a `showVictoryDialog()`

#### ✅ GameController.java

- [ ] Línea ~37-39: Mismo cambio que LocalGameController
- [ ] Línea ~56-58: Variables de tiempo sin inicializar
- [ ] Línea ~89: Método `setTestTimeMs(long)`
- [ ] Línea ~96-116: `createLocalGame()` modificado
- [ ] Línea ~815-850: `startGameTimer()` implementado correctamente
- [ ] Línea ~852-870: `endGameByTime()` existe

#### ✅ GameTimerTest.java

- [ ] Existe en `src/test/java/es/iesquevedo/controller/`
- [ ] Contiene 8 tests (testInitialTimeIsCorrect, etc.)
- [ ] Cada test tiene `@Test` y `@Timeout`

---

## 🔍 Qué Buscar en Logs

Al ejecutar la aplicación, buscar en los logs:

```
INFO: LocalGameController inicializado
INFO: Iniciando partida local
INFO: Tiempo inicial para ambos jugadores: 180 segundos   ← Correcto
INFO: Piedra colocada en [4,4]
INFO: Partida finalizada por tiempo: ⏱️ ¡Tiempo agotado! Jugador Negro...
```

---

## 🐛 Posibles Problemas y Soluciones

### Problema 1: Tiempo no cambia / está congelado

**Causa:** El `AnimationTimer` no se inició o se pausó  
**Solución:**
```java
// Verificar en LocalGameController.java línea ~92
startGameTimer();  // Debe estar aquí en initializeLocalGame()
```

### Problema 2: Tiempo va hacia adelante en lugar de atrás

**Causa:** Operación con `+=` en lugar de `-=`  
**Solución:**
```java
// MALO ❌
player1TimeMs += elapsedNanos / 1_000_000;

// CORRECTO ✅
player1TimeMs -= elapsedNanos / 1_000_000;
```

### Problema 3: Ambos jugadores pierden tiempo al mismo tiempo

**Causa:** No se está verificando `game.getCurrentPlayerIndex()`  
**Solución:**
```java
if (game.getCurrentPlayerIndex() == 0) {
    player1TimeMs -= ...;  // Solo jugador 1
} else {
    player2TimeMs -= ...;  // Solo jugador 2
}
```

### Problema 4: El juego no termina cuando llega a 00:00

**Causa:** Falta la condición de verificación  
**Solución:**
```java
if (player1TimeMs <= 0) {
    player1TimeMs = 0;
    endGameByTime(1, 2);  // Debe existir este método
    return;
}
```

### Problema 5: Compilación fallida "JRE not JDK"

**Causa:** Usar JRE en lugar de JDK  
**Solución:**
```bash
# Verificar Java version
java -version

# Instalar JDK 21 (o compatible)
# https://www.oracle.com/java/technologies/downloads/

# O usar una versión anterior de Java en pom.xml
```

---

## 📝 Archivo de Configuración

El tiempo inicial se puede cambiar en cualquier momento:

**Para tests (5 segundos):**
```java
LocalGameController.setTestTimeMs(5_000);
GameController.setTestTimeMs(5_000);
```

**Para normal (3 minutos):**
```java
// No hacer nada, usa valor por defecto
// INITIAL_TIME_MS = 180_000
```

**Para otro valor personalizado:**
```java
LocalGameController.setTestTimeMs(60_000);  // 1 minuto
LocalGameController.setTestTimeMs(300_000); // 5 minutos
```

---

## 🎮 Casos de Prueba Recomendados

### Caso 1: Usuario Lento
```
Acción: Esperar 1 minuto sin mover
Resultado:
  - Tiempo Negro: 03:00 → 02:00
  - Tiempo Blanco: 03:00 (sin cambios)
  - El jugador en turno pierde 60 segundos
```

### Caso 2: Cambio Rápido
```
Acción: Colocar piedra al instante
Resultado:
  - Turno cambia inmediatamente
  - Contador del otro jugador comienza a decrecer
  - Primer jugador deja de perder tiempo
```

### Caso 3: Múltiples Turnos
```
Acción: 10 movimientos rápidos
Resultado:
  - Ambos tiempos disminuyen progresivamente
  - Pero nunca simultáneamente (solo turno actual)
  - Cada uno pierde ~30 seg por movimiento
```

### Caso 4: Fin por Tiempo (con 5 seg)
```
Acción: No hacer nada por 5 segundos
Resultado:
  - Tiempo: 00:05 → 00:04 → 00:03 → 00:02 → 00:01 → 00:00
  - Al llegar a 00:00: endGameByTime() se ejecuta
  - Diálogo de victoria aparece
  - Se puede volver al menú
```

---

## 📈 Métricas a Verificar

- **Precisión temporal:** ±100ms de tolerancia
- **Descuento por turno:** Solo al jugador actual
- **Transición de turnos:** Sin saltos de tiempo
- **Fin de juego:** Automático al llegar a 0
- **Rendimiento:** Sin lag visual en contador

---

## 🔗 Referencias

- **LocalGameController.java:** L35-529
- **GameController.java:** L33-948
- **GameTimerTest.java:** Test suite completa
- **CAMBIOS_SISTEMA_TIEMPO.md:** Documentación técnica
- **EJEMPLO_FUNCIONAMIENTO_CONTADOR.md:** Ejemplos visuales

---

## ✨ Siguientes Pasos (Opcionales)

1. **Sincronización en multijugador:** Enviar tiempos a Firebase
2. **Sonidos de alerta:** Tic-tac cuando falta tiempo
3. **Pausa de partida:** Congelar contadores durante pausa
4. **Bonus de puntos:** Por tiempo sobrante en resultado final
5. **Configuración:** Permitir elegir duración inicial


