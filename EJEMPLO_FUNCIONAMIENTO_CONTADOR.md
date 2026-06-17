# 🎮 Ejemplo de Funcionamiento - Sistema de Contador de Tiempo

## Escenario: Partida Local con 5 Segundos (TEST)

### Estado Inicial (t=0)

```
┌─────────────────────────────────────┐
│  🎮 INAZUMA GO - Partida Local      │
├─────────────────────────────────────┤
│                                     │
│  Jugador Negro ⚫              Tiempo: 00:05 ⏱️
│  Puntos: 0.0                  ████████░░░░░░░░░░
│  📍 Turno: Negro ⚫            100%
│                                     │
│             [TABLERO 9x9]          │
│                                     │
│                                     │
│  Jugador Blanco ⚪             Tiempo: 00:05 ⏱️
│  Puntos: 0.0                  ████████░░░░░░░░░░
│  Status: Esperando...          100%
│                                     │
└─────────────────────────────────────┘
```

### Después de 2 Segundos (t=2s)

Jugador Negro está en turno, su tiempo disminuye:

```
┌─────────────────────────────────────┐
│  🎮 INAZUMA GO - Partida Local      │
├─────────────────────────────────────┤
│                                     │
│  Jugador Negro ⚫              Tiempo: 00:03 ⏱️  ← DISMINUYE
│  Puntos: 0.0                  ██████░░░░░░░░░░░░░
│  📍 Turno: Negro ⚫            60%
│                                     │
│             [TABLERO 9x9]          │
│  ♦ Piedra colocada en [4,4]    │
│                                     │
│  Jugador Blanco ⚪             Tiempo: 00:05 ⏱️  ← SIN CAMBIOS
│  Puntos: 1.0                  ████████░░░░░░░░░░
│  Status: Esperando turno...    100%
│                                     │
└─────────────────────────────────────┘
```

### Jugador cambia de turno (Negro → Blanco)

```
┌─────────────────────────────────────┐
│  🎮 INAZUMA GO - Partida Local      │
├─────────────────────────────────────┤
│                                     │
│  Jugador Negro ⚫              Tiempo: 00:03 ⏱️
│  Puntos: 1.0                  ██████░░░░░░░░░░░░░
│  Status: Esperando turno...    60%
│                                     │
│             [TABLERO 9x9]          │
│  ♦ Última piedra Blanca        │
│                                     │
│  Jugador Blanco ⚪             Tiempo: 00:04 ⏱️  ← EMPIEZA A CONTAR
│  Puntos: 1.0                  ████████░░░░░░░░░░
│  📍 Turno: Blanco ⚪           80%
│                                     │
│  Status: Turno de Blanco...         │
└─────────────────────────────────────┘
```

### Advertencia: Poco Tiempo (00:02)

```
┌─────────────────────────────────────┐
│  🎮 INAZUMA GO - Partida Local      │
├─────────────────────────────────────┤
│                                     │
│  Jugador Negro ⚫              Tiempo: 00:02 ⏱️ 🚨
│  Puntos: 2.0                  ████░░░░░░░░░░░░░░░░
│  Status: Esperando turno...    40%
│                                     │
│             [TABLERO 9x9]          │
│  ♦ Piedra colocada            │
│                                     │
│  Jugador Blanco ⚪             Tiempo: 00:05 ⏱️
│  Puntos: 2.0                  ████████░░░░░░░░░░
│  📍 Turno: Blanco ⚪           100%
│                                     │
│  ⚠️  ADVERTENCIA: ¡Tiempo agotándose!
└─────────────────────────────────────┘
```

### Crítico: Muy Poco Tiempo (00:01)

```
┌─────────────────────────────────────┐
│  🎮 INAZUMA GO - Partida Local      │
├─────────────────────────────────────┤
│                                     │
│  Jugador Negro ⚫              Tiempo: 00:01 ⏱️ 🔴 CRÍTICO
│  Puntos: 2.0                  ██░░░░░░░░░░░░░░░░░░░░
│  Status: Esperando turno...    20%
│                                     │
│             [TABLERO 9x9]          │
│                                     │
│                                     │
│  Jugador Blanco ⚪             Tiempo: 00:05 ⏱️
│  Puntos: 2.0                  ████████░░░░░░░░░░
│  📍 Turno: Blanco ⚪           100%
│                                     │
│  ⚠️  ¡TIEMPO CASI AGOTADO!
└─────────────────────────────────────┘
```

### Game Over: Tiempo Agotado

```
┌──────────────────────────────────────┐
│   🏆 ¡PARTIDA FINALIZADA! 🏆        │
├──────────────────────────────────────┤
│                                      │
│  ⏱️ ¡TIEMPO AGOTADO!                 │
│                                      │
│  "Jugador Negro se quedó sin tiempo" │
│                                      │
│       🏆 JUGADOR BLANCO GANA 🏆      │
│                                      │
│  Puntuación Final:                   │
│    • Blanco: 3.5 puntos (+ 5.5)      │
│    • Negro: 2.0 puntos               │
│                                      │
│         [Volver al Menú]             │
│                                      │
└──────────────────────────────────────┘
```

---

## ⏱️ Escenario: Partida Normal (3 MINUTOS)

### Progresión de Tiempo

| Minuto | Negro | Blanco | Turno | Evento |
|--------|-------|--------|-------|--------|
| 0:00   | 03:00 | 03:00  | Negro | 🎮 Inicio |
| 0:45   | 02:15 | 03:00  | Negro | Negro baja 45 seg |
| 1:30   | 02:15 | 02:30  | Blanco | Blanco baja 30 seg |
| 2:00   | 01:45 | 02:30  | Negro | Negro baja 30 seg |
| 3:30   | 01:00 | 01:30  | Blanco | Blanco baja 1 min |
| 4:15   | 00:45 | 01:30  | Negro | Negro baja 15 seg |
| 4:45   | 00:45 | 00:45  | Blanco | Blanco baja 45 seg |
| 5:15   | 00:15 | 00:45  | Negro | 🚨 NEGRO EN PELIGRO |
| 5:30   | 00:00 | 00:45  | Negro | ❌ **NEGRO PIERDE** |

---

## 🔧 Código Clave - Cómo Funciona

### 1. Inicialización

```java
LocalGameController.setTestTimeMs(5_000);  // 5 segundos para test
LocalGameController controller = new LocalGameController();
controller.initializeLocalGame();

// Resultado:
// player1TimeMs = 5_000 ms
// player2TimeMs = 5_000 ms
// gameTimer inicia contador regresivo
```

### 2. Animación por Frame

```java
private void startGameTimer() {
    gameTimer = new AnimationTimer() {
        @Override
        public void handle(long now) {
            // Calcular tiempo transcurrido
            long elapsedNanos = now - lastTime;
            
            // Restar del jugador en turno
            if (game.getCurrentPlayerIndex() == 0) {
                player1TimeMs -= elapsedNanos / 1_000_000;  // ← RESTA TIEMPO
                
                // Verificar si llegó a 0
                if (player1TimeMs <= 0) {
                    player1TimeMs = 0;
                    endGameByTime(1, 2);  // ← FIN DE JUEGO
                    return;
                }
            }
            
            // Mostrar tiempo actualizado
            updateTimeLabels();
        }
    };
    gameTimer.start();
}
```

### 3. Visualización

```java
private String formatTime(long ms) {
    long seconds = ms / 1000;
    long minutes = seconds / 60;
    seconds = seconds % 60;
    return String.format("%02d:%02d", minutes, seconds);
    // 5000 ms  → "00:05"
    // 180000 ms → "03:00"
    // 90000 ms  → "01:30"
}
```

### 4. Fin del Juego por Tiempo

```java
private void endGameByTime(int loserPlayerIndex, int winnerPlayerIndex) {
    gameEnded = true;
    game.setState(GameState.FINISHED);
    
    String result = "⏱️ ¡Tiempo agotado! " + loserName + " se quedó sin tiempo.";
    statusLabel.setText(result);
    
    stopGameTimer();
    showVictoryDialog(winnerName, loserName + " (Tiempo agotado)");
}
```

---

## 📊 Pruebas de Validación

### Test 1: Tiempo Inicial
```
✓ Partida comienza con 00:05 (test) o 03:00 (normal)
```

### Test 2: Contador Regresivo
```
Inicial: 00:05
1 seg:   00:04
2 seg:   00:03
3 seg:   00:02
4 seg:   00:01
5 seg:   00:00  ← Fin automático
```

### Test 3: Solo Jugador Actual
```
Turno Negro:
  - Tiempo Negro: 4:59 → 4:58 (decrece)
  - Tiempo Blanco: 5:00 (sin cambios)

Turno Blanco:
  - Tiempo Negro: 4:58 (sin cambios)
  - Tiempo Blanco: 4:59 → 4:58 (decrece)
```

### Test 4: Game Over
```
Cuando cualquier jugador llega a 00:00:
✓ Partida se marca como FINISHED
✓ Se muestra diálogo de victoria
✓ Se puede volver al menú
```

---

## 🎯 Casos de Uso

### Caso 1: Usuario Mueve Rápido
```
Negro coloca piedra → Turno a Blanco
Tiempo Negro: 2:45 (se detiene)
Tiempo Blanco: 3:00 (empieza a contar)
```

### Caso 2: Usuario Piensa Mucho
```
Negro piensa 2 minutos sin colocar piedra
Tiempo Negro: 3:00 → 1:00 (baja 2 minutos)
Tiempo Blanco: 3:00 (sin cambios)
```

### Caso 3: Pase de Turno
```
Negro pasa su turno
Tiempo Negro: 1:30 (se detiene)
Tiempo Blanco: 2:45 (empieza a contar)
```

### Caso 4: Ambos Pasan (Fin de Partida)
```
Negro pasa: Turno a Blanco
Blanco pasa: Partida termina por pases
- Fin de partida por PASES (no por tiempo)
- Tiempos restantes se mantienen
```

---

## 🔔 Alertas y Warnings

| Tiempo Restante | Alerta | Color | Sonido |
|-----------------|--------|-------|--------|
| 03:00 - 01:00   | Normal | Verde | Ninguno |
| 00:59 - 00:31   | Aviso  | Amarillo | Tic-tac |
| 00:30 - 00:11   | Urgente | Naranja | Tic-tac acelerado |
| 00:10 - 00:01   | Crítico | Rojo | Alarma continua |
| 00:00           | ¡PERDIDO! | Rojo brillante | Error |

---

## 📝 Notas de Implementación

✅ **Completado:**
- Contador regresivo de tiempo
- Inicialización configurable (3 min o 5 seg para test)
- Detección de fin de juego por tiempo
- Descuento solo del jugador actual
- Mostrar estado en UI

⏳ **Pendiente (siguientes pasos):**
- Sincronizar tiempo entre jugadores en multijugador online
- Sonidos de advertencia
- Pausa y reanudación
- Bonificación por tiempo restante en puntuación final

