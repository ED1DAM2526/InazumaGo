# 🎮 Prueba de la Vista de Partida - InazumaGo

## Cómo ejecutar y probar la aplicación

### Opción 1: Ejecutar desde el IDE (JetBrains IntelliJ IDEA)

1. Abre el proyecto en JetBrains IDEA
2. Navega a `src/main/java/es/iesquevedo/MainGUI.java`
3. Haz clic derecho → "Run 'MainGUI.main()'"
4. La aplicación mostrará la pantalla de Login

### Opción 2: Ejecutar desde línea de comandos

```bash
cd C:\Users\Usuario\IdeaProjects\InazumaGo
mvn compile
java -cp target/classes es.iesquevedo.MainGUI
```

---

## Flujo de Prueba

### Paso 1: Pantalla de Login
- Verás la pantalla de Login con campos de Email y Contraseña
- Ingresa cualquier email (ej: `usuario@example.com`)
- Ingresa cualquier contraseña (ej: `password123`)
- Haz clic en "Iniciar sesión"

### Paso 2: Pantalla de Partida (Nueva Vista)
Tras login exitoso, verás la nueva vista de partida con:

#### 📊 Encabezado (Top)
- **Jugador 1 (Negro)**
  - Nombre: usuario@example.com
  - Puntos: 0
  - Tiempo: 00:00
  
- **VS** (separador visual)

- **Jugador 2 (Blanco)**
  - Nombre: Oponente
  - Puntos: 0
  - Tiempo: 00:00

- **Turno Actual**: Turno: usuario@example.com

#### 🎯 Centro (Canvas)
- Tablero 19x19 con líneas de cuadrícula
- Haz clic en cualquier intersección para colocar una piedra
- Las piedras aparecerán en negro (Jugador 1) o blanco (Jugador 2)

#### 🎮 Controles (Bottom)
- **Pasar turno**: Cambia de jugador
- **Deshacer**: No disponible en esta versión
- **Rendirse**: Marca fin de la partida con ganador
- **Volver al menú**: Cierra la ventana de partida

---

## Características Implementadas ✅

### Vista FXML (Game.fxml)
- ✅ Diseño BorderPane con 3 secciones (Top, Center, Bottom)
- ✅ Encabezado con información de jugadores
- ✅ Canvas para dibujar el tablero 19x19
- ✅ Botones de control con eventos onAction
- ✅ Labels dinámicos para puntuación y tiempo

### Controlador (GameController.java)
- ✅ Inicialización del tablero vacío (Stone[19][19])
- ✅ Dibujo del tablero con líneas de cuadrícula
- ✅ Detección de clics del ratón en el Canvas
- ✅ Colocación de piedras (negro/blanco)
- ✅ Control de turnos alternantes
- ✅ Cálculo de puntuación en tiempo real
- ✅ Temporizador con AnimationTimer que cuenta segundos/minutos
- ✅ Métodos para acciones (pasar turno, rendirse)
- ✅ Inyección de nombres de jugadores
- ✅ Inyección de puntuaciones iniciales

### Navegación (LoginController.java modificado)
- ✅ Carga automática de Game.fxml tras login exitoso
- ✅ Paso de nombres de jugadores al GameController
- ✅ Cambio del título de la ventana a "InazumaGo - Partida"

---

## Pruebas Recomendadas

### Prueba 1: Colocar piedras
1. Haz clic en varias intersecciones del tablero
2. Verifica que aparecen piedras negras (jugador 1) y blancas (jugador 2) alternadamente
3. Comprueba que no se pueden colocar dos piedras en la misma posición

**Resultado esperado**: Las piedras aparecen correctamente en color y posición

### Prueba 2: Cambio de turno
1. Coloca una piedra
2. Verifica que el turno cambió al otro jugador
3. Comprueba que el color de la siguiente piedra es diferente

**Resultado esperado**: Los turnos se alternan correctamente

### Prueba 3: Puntuación
1. Coloca varias piedras
2. Verifica que el contador de puntos aumenta para cada jugador

**Resultado esperado**: Cada piedra colocada suma 1 punto al jugador

### Prueba 4: Temporizador
1. Espera 10-20 segundos
2. Verifica que el tiempo avanza (HH:MM)
3. Coloca una piedra y mira que el temporizador del otro jugador comienza

**Resultado esperado**: El tiempo se actualiza correctamente por segundo

### Prueba 5: Pasar turno
1. Haz clic en "Pasar turno"
2. Verifica que cambia el jugador actual sin colocar piedra

**Resultado esperado**: El turno cambia, pero no hay nueva piedra

### Prueba 6: Rendirse
1. Haz clic en "Rendirse"
2. Verifica que aparece un mensaje de ganador

**Resultado esperado**: Se muestra "[Ganador] ganó. [Perdedor] se rindió"

---

## Archivos Generados

```
✅ src/main/resources/fxml/Game.fxml (4,188 bytes)
✅ src/main/java/es/iesquevedo/controller/GameController.java (8,447 bytes)
📝 src/main/java/es/iesquevedo/controller/LoginController.java (modificado)
```

## Estado de Compilación

```
✅ BUILD SUCCESS
✅ GameController.class compilado
✅ GameController$Stone.class compilado
✅ GameController$1.class compilado (clase interna anónima)
```

---

## Notas Técnicas

- **Framework**: JavaFX 12.0.1
- **Compilador**: Eclipse Compiler (ecj) para Java 8
- **Versión Java**: 8 (compilado y compatible)
- **Patrón de Diseño**: MVC (Model-View-Controller)
- **Threading**: AnimationTimer para actualización de UI en tiempo real


