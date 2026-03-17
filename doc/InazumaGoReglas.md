## 1. ¿Qué es el Go?

## El Go es un juego de mesa para 2 jugadores.

- Un jugador usa piedras **negras**.
- El otro usa piedras **blancas**.
- Se juega en un tablero con líneas que forman muchos puntos donde se pueden poner las piedras.

El objetivo es **tener más territorio que tu rival** al final del juego.

## 2. Cómo jugar

1. **Turnos:**
   - Los jugadores colocan **una piedra en un punto vacío** del tablero.
   - El jugador con piedras negras empieza.
   - No se pueden mover las piedras ya colocadas, excepto si se capturan en cuyo caso se sacan del tablero.
2. **Pasar turno:**
   - Si no quieres jugar en un turno, puedes **pasar**.
   - Cuando los **dos jugadores pasan seguidos**, termina el juego.

## 3. Libertades y cadenas

- Cada piedra tiene **libertades**: puntos vacíos a los ejes de ella de ella (arriba, abajo, izquierda, derecha).
- Si varias piedras del mismo color están juntas, forman una **cadena** y comparten libertades.

## 4. Capturar piedras

- Si cubres la **última libertad** de una piedra o cadena del rival, se **captura** y se retira del tablero.
- Una piedra **sin libertades no puede quedarse** en el tablero (esto se llama suicidio si la pones ahí tú mismo y no está permitido).

## 5. La regla del Go

- No puedes repetir **exactamente la misma posición** que había antes de tu turno.
- Esto evita que el juego se quede atrapado en capturas sin fin.

## 6. Ganar puntos

- El juego termina cuando **los dos pasan seguidos**.
- Para ganar, se cuentan los **puntos**:
  - Las piedras propias en el tablero.
  - Los puntos vacíos que están **rodeados por tus piedras**.
- Gana quien tenga **más puntos**.

## 7. Ideas importantes

- **Ojos:** un grupo de piedras con espacios libres dentro ayuda a que no puedan ser capturadas.
- **Piedras muertas:** piedras que ya no pueden escapar del rival se retiran del tablero.
- **Territorio neutral:** puntos que nadie controla no cuentan para nadie.

## 8. Tablero

- El tablero  tiene unas dimensiones de 9×9.

## 9. Video tutorial del Go.

- Video corto explicando las reglas del Go: <https://www.youtube.com/watch?v=gOvG5ACfeL4&pp=ygUQY29tbyBqdWdhciBhbCBnbw%3D%3D>