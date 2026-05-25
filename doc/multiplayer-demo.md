# 🎮 Demo Local: Flujo Completo de Partida E3-US3

## Resumen Ejecutivo

Esta guía describe cómo ejecutar una **partida completa de dos jugadores** con **sincronización de estado, turnos alternos, validación de reglas y manejo de errores**.

**Requisitos Previos:**
- ✅ Maven 3.9+
- ✅ JDK 21+
- ✅ Firebase Realtime Database configurada (opcional para demo local)
- ✅ WireMock integrado en tests

---

## 📋 Flujo Manual de la Aplicación (Demo Local)

### Opción 1: Demo Interactiva con JavaFX (Recomendado)

#### Paso 1: Compilar el Proyecto
```bash
cd C:\Users\madrid\Documents\InazumaGo
mvn clean compile
```

**Resultado esperado:**
```
BUILD SUCCESS
```

#### Paso 2: Ejecutar la Aplicación
```bash
mvn javafx:run -f pom.xml
```

O desde el IDE:
- Abre `src/main/java/es/iesquevedo/MainApp.java`
- Clic derecho → "Run 'MainApp.main()'"

#### Paso 3: Pantalla de Login

1. Verás la pantalla de **Login**
2. Ingresa credenciales (cualquier email/password vale para demo):
   - Email: `jugador1@inazuma.go`
   - Contraseña: `password123`
3. Haz clic en "Iniciar sesión"

**Resultado esperado:**
- ✅ Navegación a pantalla principal

#### Paso 4: Pantalla Principal (MainScreen)

1. Verás botones: "Crear Partida", "Unirse", "Salir"
2. Haz clic en **"Crear Partida"**

**Resultado esperado:**
- ✅ Transición a pantalla de emparejamiento

#### Paso 5: Pantalla de Emparejamiento (MatchingScreen)

La aplicación busca oponente automáticamente:
- Label: "Buscando jugador disponible..."
- Timer: Contador de tiempo de espera
- ProgressIndicator: Animación de carga

**Tiempo de espera:** 3-8 segundos (simulado)

**Resultado esperado:**
- ✅ Estado: "¡Oponente encontrado!"
- ✅ Transición automática a pantalla de partida

#### Paso 6: Pantalla de Partida (GameScreen)

Ahora ves el **tablero 19x19** con dos jugadores:

```
┌─────────────────────────────────┐
│  Jugador1 (Negro) - Turno       │
│  Puntos: 0 | Tiempo: 00:00      │
│                                 │
│           TABLERO 19x19          │
│      (click para colocar)        │
│                                 │
│  Jugador2 (Blanco)              │
│  Puntos: 0 | Tiempo: 00:00      │
└─────────────────────────────────┘
```

#### Paso 7: Ejecutar Secuencia de Turnos

**Turno 1 - Jugador 1 (Negro):**
1. Haz clic en una intersección, ej: (5, 5)
2. Verás una piedra negra
3. Puntos Jugador1: +1
4. Turno cambia a "Jugador2 (Blanco)"

**Turno 2 - Jugador 2 (Blanco):**
1. Haz clic en otra intersección, ej: (3, 3)
2. Verás una piedra blanca
3. Puntos Jugador2: +1
4. Turno cambia a "Jugador1 (Negro)"

**Turnos 3+ - Repetir:**

Ejecuta 6-8 movimientos totales para ver sincronización:

```
Turno 1 (Jugador1): (5, 5)     -> Negro
Turno 2 (Jugador2): (3, 3)     -> Blanco
Turno 3 (Jugador1): (7, 7)     -> Negro
Turno 4 (Jugador2): (9, 9)     -> Blanco
Turno 5 (Jugador1): (10, 10)   -> Negro
Turno 6 (Jugador2): (15, 15)   -> Blanco
Turno 7 (Jugador1): (12, 12)   -> Negro
Turno 8 (Jugador2): (8, 8)     -> Blanco
```

**Verificaciones en cada turno:**
- ✅ Color correcto (negro/blanco alterna)
- ✅ Puntuación incrementa
- ✅ Timer sigue avanzando
- ✅ Turno se muestra correctamente

#### Paso 8: Probar Botones de Control

**Botón: "Pasar Turno"**
1. Haz clic en "Pasar Turno"
2. Turno cambia sin colocar piedra

**Botón: "Rendirse"**
1. Haz clic en "Rendirse"
2. Aparece mensaje: "[Ganador] ganó. [Perdedor] se rindió"
3. Partida finaliza

**Botón: "Volver"**
1. Haz clic en "Volver"
2. Regresa a MainScreen

---

## 🧪 Demo Automática con Tests (Recomendado para CI/CD)

### Test E2E Happy Path: Flujo Completo

```bash
mvn test -Dtest=GameE2ESimpleTest
```

**Qué verifica:**
```
✅ testCreateAndStartGame
   • Crear partida
   • Unirse segundo jugador
   • Iniciar partida (WAITING → IN_PROGRESS)

✅ testExecuteMove
   • Player1 ejecuta movimiento válido
   • Turno cambia a Player2
   • Estado sincronizado

✅ testNextTurn
   • Player1 turno inicial
   • nextTurn() cambia a Player2

✅ testFinishGame
   • Partida en curso
   • finishGame() → FINISHED
   • Winner registrado
```

**Resultado esperado:**
```
[INFO] Running es.iesquevedo.integration.GameE2ESimpleTest
[INFO] Tests run: 4, Failures: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

### Test E2E con Rechazo y Rollback

```bash
mvn test -Dtest=GameE2EWireMockTest
```

**Qué verifica:**
```
✅ testMoveRejectionWith403RollsBack
   • Player1 coloca en (5, 5)
   • Player2 coloca en (3, 3)
   • Player1 intenta en (5, 5) -> RECHAZO (InvalidMoveException)
   • Rollback: turno y contador no cambian
   • Reintento en (7, 7) -> ÉXITO
   • Estado sincronizado nuevamente

✅ testMultipleAlternatingMovesWithRuleValidation
   • 8 movimientos alternos (4 cada jugador)
   • Validación de reglas en cada movimiento
   • Alternancia correcta de turnos
   • Contador de turnos incrementa correctamente

✅ testRetryAfterInterruptionMaintainsConsistency
   • Movimiento exitoso de Player1
   • Intento fallido de Player2 en posición ocupada
   • Verificar consistencia (turno, contador)
   • Reintento exitoso con posición diferente
   • Estado consistente todo el tiempo

✅ testGameFinishStateSync
   • Múltiples movimientos
   • Fin de partida
   • Estado FINISHED y winners sincronizados

✅ testGameAbandonSync
   • Abandono durante partida
   • Estado ABANDONED
   • Turnos registrados correctamente
```

**Resultado esperado:**
```
[INFO] Running es.iesquevedo.integration.GameE2EWireMockTest
[INFO] Tests run: 5, Failures: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

### Ejecutar Todos los Tests de Integración

```bash
mvn test -Dtest=GameE2E*
```

**Resultado esperado:**
```
[INFO] Running es.iesquevedo.integration.GameE2ESimpleTest
[INFO] Running es.iesquevedo.integration.GameE2EWireMockTest
[INFO] Tests run: 9, Failures: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

## 📊 Verificación de Criterios E3-US3

### Criterio 1: Crear/Unir y Sincronizar Estado

✅ **Implementado:**
- `GameService.createGame()` - Crea partida con jugador 1
- `GameService.joinGame()` - Jugador 2 se une
- `GameService.startGame()` - Inicia (WAITING → IN_PROGRESS)
- `GameEventService.notifyGameStart()` - Sincroniza en Firebase

**Verificación Manual:**
```bash
mvn test -Dtest=GameE2ESimpleTest::testCreateAndStartGame
```

---

### Criterio 2: Flujo de Turnos Alternos hasta Fin

✅ **Implementado:**
- `GameService.executeMove()` - Ejecuta movimiento y cambia turno
- `Game.nextTurn()` - Alterna jugadores (currentPlayerIndex)
- `GameService.finishGame()` - Finaliza con ganador
- `GameService.abandonGame()` - Abandono de partida

**Verificación Manual:**
```bash
mvn test -Dtest=GameE2ESimpleTest::testNextTurn
mvn test -Dtest=GameE2EWireMockTest::testMultipleAlternatingMovesWithRuleValidation
```

---

### Criterio 3: Rechazo + Rollback + Feedback

✅ **Implementado:**
- `InazumaGoMoveValidator` - Valida movimientos según reglas
- `InvalidMoveException` (código 403 simulado) - Rechaza movimiento inválido
- Rollback automático: turno y contador NO cambian
- Mensaje de error en UI (GameController)

**Verificación Manual:**
```bash
mvn test -Dtest=GameE2EWireMockTest::testMoveRejectionWith403RollsBack
mvn test -Dtest=GameE2EWireMockTest::testRetryAfterInterruptionMaintainsConsistency
```

---

### Criterio 4: Test E2E + Error/Reintento

✅ **Implementado:**
- **Happy Path:** `GameE2ESimpleTest` (4 tests)
  - Crear, unir, iniciar, mover, fin
  
- **Con Errores:** `GameE2EWireMockTest` (5 tests)
  - Rechazo 403
  - Rollback
  - Reintento exitoso
  - Múltiples turnos
  - Interrupciones

```bash
mvn test -Dtest=GameE2E*
```

**Resultado esperado:** 9 tests, 0 fallos ✅

---

### Criterio 5: Guía de Demo Local

✅ **Este documento (multiplayer-demo.md)**
- Demo interactiva con JavaFX (Opción 1)
- Demo automática con tests (Opción 2)
- Verificación de cada criterio
- Comandos copy-paste listos

---

## 🔍 Troubleshooting

### Error: "Build failed"

```bash
# Limpiar y recompilar
mvn clean compile
```

### Error: "Test failed"

```bash
# Ejecutar con verbosidad
mvn test -X -Dtest=GameE2ESimpleTest
```

### Puerto 8080/8081 en uso

Los tests usan puertos dinámicos (8080 y 8081). Si hay conflicto:

```bash
# Cerrar aplicación que ocupa el puerto
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

---

## 📈 Checklist de Verificación

| Item | Estado | Comando |
|------|--------|---------|
| Compilación | ✅ | `mvn clean compile` |
| Tests Happy Path | ✅ | `mvn test -Dtest=GameE2ESimpleTest` |
| Tests Error/Rollback | ✅ | `mvn test -Dtest=GameE2EWireMockTest` |
| Todos los tests | ✅ | `mvn test -Dtest=GameE2E*` |
| App JavaFX | ✅ | `mvn javafx:run` |
| Matchmaking UI | ✅ | Ejecutar app → Click "Crear Partida" |
| Game UI | ✅ | De matchmaking avanza automáticamente |
| Firebase Sync | ✅ | Ver logs en `application.properties` |

---

## 🎯 Resultado Final

**E3-US3 - COMPLETADO 100%:**

```
✅ Crear/Unir partida + Sincronización de estado inicial
✅ Flujo de turnos alternos hasta condición de fin (victoria/abandono)
✅ Rechazo de movimiento con rollback sin desincronizar
✅ Tests E2E: happy path + error scenarios + reintento
✅ Guía de demo local documentada (este archivo)
✅ Aplicación ejecutable (JavaFX)
✅ Pantalla de emparejamiento (Matchmaking)
```

**BUILD SUCCESS** ✅

---

## 📞 Referencias

- **Repositorio:** `src/test/java/es/iesquevedo/integration/GameE2E*.java`
- **Servicio:** `src/main/java/es/iesquevedo/service/impl/GameServiceImpl.java`
- **Modelo:** `src/main/java/es/iesquevedo/model/Game.java`
- **UI:** `src/main/resources/fxml/Game.fxml`, `MatchingScreen.fxml`
- **Eventos:** `src/main/java/es/iesquevedo/service/GameEventService.java`

---

**Fecha:** 25/05/2026
**Versión:** 1.0
**Estado:** ✅ COMPLETADO
**Equipo:** UI, Motor, Red, QA

