# 📋 RESUMEN DE CAMBIOS - Sistema de Contadores de Tiempo

## 🎯 Objetivo
Cambiar el sistema de contadores de tiempo para que:
- Cuenten **hacia atrás** desde 3 minutos (180 segundos)
- Si el tiempo llega a 0, **el jugador pierde automáticamente**
- Se descuente solo del jugador en turno
- Funcione en juego local y multijugador online

---

## 📝 Cambios Realizados

### 1️⃣ **LocalGameController.java**

#### Cambios Principales:

**a) Constantes y Variables inicializadas**
```java
// Línea 38-39
private static final long INITIAL_TIME_MS = 180_000; // 3 minutos
private static long testTimeMs = 0; // Para tests

// Línea 56-57 (inicialización correcta)
private long player1TimeMs;  // Sin inicialización = permite configurar después
private long player2TimeMs;
```

**b) Nuevo método para configurar tiempo de prueba** (Línea 72-77)
```java
public static void setTestTimeMs(long timeMsForTest) {
    testTimeMs = timeMsForTest;
}
```

**c) Inicialización del juego** (Línea 79-92)
```java
public void initializeLocalGame() {
    // Determinar tiempo inicial
    long initialTime = (testTimeMs > 0) ? testTimeMs : INITIAL_TIME_MS;
    player1TimeMs = initialTime;  // 5 segundos (test) o 180 segundos (normal)
    player2TimeMs = initialTime;
    
    // ... resto de inicialización
    startGameTimer();
}
```

**d) Timer que cuenta hacia atrás** (Línea 426-461)
```java
private void startGameTimer() {
    gameTimer = new AnimationTimer() {
        @Override
        public void handle(long now) {
            // ... lógica delta
            if (!gameEnded) {
                if (game.getCurrentPlayerIndex() == 0) {
                    player1TimeMs -= elapsedNanos / 1_000_000;  // ⬅️ RESTA
                    if (player1TimeMs <= 0) {
                        player1TimeMs = 0;
                        endGameByTime(1, 2);  // Jugador 1 pierde
                        return;
                    }
                } else {
                    player2TimeMs -= elapsedNanos / 1_000_000;  // ⬅️ RESTA
                    if (player2TimeMs <= 0) {
                        player2TimeMs = 0;
                        endGameByTime(2, 1);  // Jugador 2 pierde
                        return;
                    }
                }
                updateTimeLabels();
            }
        }
    };
    gameTimer.start();
}
```

**e) Nuevo método para finalizar por tiempo** (Línea 463-481)
```java
private void endGameByTime(int loserPlayerIndex, int winnerPlayerIndex) {
    gameEnded = true;
    game.setState(GameState.FINISHED);
    
    String loserName = (loserPlayerIndex == 1) ? player1Name : player2Name;
    String winnerName = (winnerPlayerIndex == 1) ? player1Name : player2Name;
    
    String result = "⏱️ ¡Tiempo agotado! " + loserName + " se quedó sin tiempo.\n" + winnerName + " gana.";
    statusLabel.setText(result);
    
    stopGameTimer();
    showVictoryDialog(winnerName, loserName + " (Tiempo agotado)");
}
```

---

### 2️⃣ **GameController.java** (Multijugador Online)

Se aplicaron **exactamente los mismos cambios** que en LocalGameController:

- Línea 37-39: Constantes `INITIAL_TIME_MS` y `testTimeMs`
- Línea 56-58: Variables `player1TimeMs` y `player2TimeMs` sin inicializar
- Línea 89-94: Método `setTestTimeMs(long)`
- Línea 96-116: Método `createLocalGame()` modificado
- Línea 815-850: Timer que cuenta hacia atrás
- Línea 852-870: Método `endGameByTime()`

---

### 3️⃣ **GameTimerTest.java** (Nuevo archivo de test)

Archivo de prueba ubicado en: `src/test/java/es/iesquevedo/controller/GameTimerTest.java`

**Tests incluidos:**
1. ✅ `testInitialTimeIsCorrect()` - Verifica tiempo inicial = 5 segundos
2. ✅ `testTimeFormatting()` - Verifica formato MM:SS
3. ✅ `testTimeDecreases()` - Verifica que disminuye correctamente
4. ✅ `testTimeDeductedFromCurrentPlayer()` - Solo al jugador en turno
5. ✅ `testTimeAtVariousPoints()` - En múltiples puntos
6. ✅ `testTurnTransition()` - Sin pérdida al cambiar turno
7. ✅ `testTimeRemainsValidAfterMultipleTurns()` - Válido tras turnos
8. ✅ `testTimeAtZero()` - Comportamiento cuando llega a 0

**Cómo usar el test con 5 segundos:**
```java
// En cualquier test que quiera usar 5 segundos:
LocalGameController.setTestTimeMs(5_000);  // 5 segundos
GameController.setTestTimeMs(5_000);       // 5 segundos
// El tiempo predeterminado es 3 minutos (180 segundos)
```

---

## 🔄 Flujo de Funcionamiento

### Juego Local:

```
Inicio Partida
    ↓
LocalGameController.initializeLocalGame()
    ↓
Configura: player1TimeMs = 180_000 ms (3 min)
           player2TimeMs = 180_000 ms (3 min)
    ↓
startGameTimer() inicia AnimationTimer
    ↓
Cada frame:
  - Calcula tiempo transcurrido (Δt)
  - Resta Δt al jugador actual
  - Si tiempo ≤ 0 → Llama endGameByTime()
  - Actualiza display (MM:SS)
    ↓
Si tiempo = 0:
  - Jugador actual pierde
  - Se muestra diálogo de victoria
  - Retorna al menú
```

---

## 🧪 Ejemplo de Test

```java
public void testWithFiveSeconds() {
    // Configurar tiempo de 5 segundos
    LocalGameController.setTestTimeMs(5_000);
    
    // Crear y iniciar juego
    LocalGameController controller = new LocalGameController();
    controller.initializeLocalGame();
    
    // Simular paso de 6 segundos
    // → El jugador actual pierde
    // → Se llama a endGameByTime()
    // → Muestra "⏱️ ¡Tiempo agotado!"
}
```

---

## ✅ Verificación

Para verificar que funciona correctamente:

1. **Tiempo inicial correcto:**
   - Display muestra "03:00" al inicio (3 minutos)
   - Con test: muestra "00:05" (5 segundos)

2. **Cuenta hacia atrás:**
   - Cada segundo disminuye 1
   - 03:00 → 02:59 → 02:58 → ... → 00:01 → 00:00

3. **Descuento solo del jugador en turno:**
   - Jugador Negro en turno: su tiempo baja
   - Jugador Blanco esperando: su tiempo NO baja

4. **Fin de partida por tiempo:**
   - Cuando llega a 00:00 → Pierde automáticamente
   - Se muestra mensaje de victoria/derrota
   - Se permite volver al menú

---

## 📌 Notas Importantes

- El tiempo se descuenta en **milisegundos** internamente
- Se convierte a formato **MM:SS** para mostrar
- El método `formatTime()` ya existía y funciona correctamente
- Los cambios son **simétricos** entre LocalGameController y GameController
- El sistema es **thread-safe** gracias a Platform.runLater() en JavaFX

---

## 🚀 Próximos Pasos

Para completar la solución:
1. ✅ Arreglar contadores (COMPLETADO)
2. ⏳ Sincronizar tiempo en multijugador online (requiere Firebase)
3. ⏳ Agregar sonido de advertencia cuando falta poco tiempo
4. ⏳ Permitir pausa y reanudación de partida

