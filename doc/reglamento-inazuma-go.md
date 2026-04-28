---

# REGLAMENTO OFICIAL

# INAZUMA GO

## (Versión Blitz 3+2 – Sistema Autónomo)

---

## Introducción

Esta especificación define las reglas de Inazuma Go en términos claros y no ambiguos para su correcta interpretación por jugadores y desarrolladores. En esta versión se explican los conceptos fundamentales del juego (libertades, capturas, ojos, ko, desarrollo de la partida, seki, etc.) sin entrar todavía en detalles algorítmicos o de implementación, que se trabajarán más adelante.

---

## 1. Naturaleza del juego

Inazuma Go es una variante rápida y autoarbitrada del Go diseñada para partidas competitivas sin intervención externa, con resolución automática y control de tiempo tipo Ajedrez Blitz. El sistema gestiona tiempo, legalidad de jugadas y determinación final.

---

## 2. Tablero, notación y elementos básicos

2.1. El tablero es de 9x9 intersecciones numeradas de 1 a 9 en filas y columnas (internamente se puede usar 0..8). Una coordenada se representará como (fila, columna).

2.2. Vecindad ortogonal: los vecinos de una intersección (r,c) son (r-1,c), (r+1,c), (r,c-1) y (r,c+1) siempre que estén dentro del tablero.

2.3. Dos colores: Negro (juega primero) y Blanco.

2.4. Estado de una intersección: "vacía", "negra" o "blanca".

---

## 3. Conceptos fundamentales (definiciones)

3.1. Grupo (o cadena): conjunto de piedras del mismo color conectadas entre sí por adyacencia ortogonal. Un "grupo" se considera como una unidad para las nociones de vida, muerte y captura.

3.2. Libertad (grado de libertad): cualquier intersección vacía ortogonalmente adyacente a una piedra del grupo. El número de libertades de un grupo es la cantidad de estas intersecciones distintas. Las libertades determinan si un grupo puede seguir existiendo en el tablero sin ser capturado.

3.3. Captura: cuando un grupo pierde todas sus libertades (es decir, no tiene intersecciones vacías adyacentes), queda capturado y sus piedras son retiradas del tablero. La captura es el resultado natural de jugadas que cierran las libertades de un grupo enemigo.

3.4. Suicidio: acción de colocar una piedra en una intersección que deja directamente a ese grupo sin libertades y que no provoca, con esa misma jugada, la captura de piedras adversarias que restauren libertades. En Inazuma Go el suicidio está prohibido: una jugada que produzca la situación de cero libertades para el propio grupo, sin captura simultánea que lo salve, se considera ilegal.

3.5. Ojo: intersección vacía (o conjunto de intersecciones vacías) que están completamente rodeadas por piedras de un mismo color de modo que el adversario no puede ocuparlas sin ser inmediatamente capturado; en términos prácticos, un ojo contribuye a la seguridad de un grupo y a su posibilidad de supervivencia indefinida.

3.6. Seki: situación de vida mutua en la que dos (o más) grupos de distinto color comparten libertades de modo que ninguna de las partes puede capturar a la otra sin exponerse a una pérdida. En seki, las intersecciones compartidas no se consideran territorio de nadie al finalizar la partida.

3.7. Desarrollo de la partida: se entiende por desarrollo el conjunto ordenado de jugadas desde el primer movimiento hasta la finalización; incluye pases, capturas, cambios de control territorial y cualquier secuencia que afecte el estado global del tablero.

---

## 4. Ko y regla de repetición (adaptación de Inazuma Go)

4.1. Ko (concepto): un "ko" clásico es una situación concreta en la que una captura permite, en principio, la recaptura inmediata que recrearía la posición anterior. En reglas tradicionales del Go existe la prohibición de la recaptura inmediata (regla de ko simple) para evitar ciclos infinitos.

4.2. Adaptación de Inazuma Go (política aplicable):
- Inazuma Go permite la recaptura en situaciones de ko simple (es decir, no impone la prohibición tradicional de recaptura inmediata).
- Sin embargo, si una jugada provoca que la posición vuelva exactamente a la posición inmediatamente anterior (repetición consecutiva de la misma posición con el mismo jugador al turno), esa repetición actúa como detonante: la partida finaliza de forma inmediata y se procede al conteo y resolución final.

4.3. Repeticiones no consecutivas: otras repeticiones que no sean inmediatas (por ejemplo recrear una posición vista varios movimientos antes, pero no la inmediatamente anterior) no producen automáticamente la finalización según la política de ko aquí descrita; solo la repetición consecutiva (la recaptura que restaura la posición previa en un solo movimiento) detona el fin de la partida.

4.4. Motivación: esta regla mantiene la posibilidad táctica del ko clásico (recaptura) pero evita ciclos sencillos que prolonguen artificialmente la partida; además simplifica la resolución automática porque el sistema solo debe detectar comparaciones con la posición inmediatamente anterior para aplicar esta clausula.

---

## 5. Sistema de puntuación (puntuación por área — estilo chino simplificado)

5.1. Método de conteo al final de la partida (algoritmo determinista):
1) A partir del tablero final, ejecutar iterativamente la eliminación de cualquier grupo que tenga 0 libertades (esto captura grupos que quedaron sin libertades por error; en partidas reglamentarias no debería haber ninguno, pero el paso asegura consistencia). Repetir hasta que no existan grupos con 0 libertades.
2) Para cada intersección vacía, realizar un flood-fill (búsqueda en región) para determinar la región vacía contigua y el conjunto de colores de piedras que limitan esa región (colores vecinos ortogonales). Si la región está adyacente únicamente a piedras de un solo color, la región pertenece a ese color y cada intersección de la región cuenta como 1 punto de área para ese color. Si la región está adyacente a ambos colores, la región es neutral (dame/seki) y no puntúa para nadie.
3) El área total de cada jugador = número de piedras del jugador en el tablero + número de intersecciones vacías adjudicadas al jugador por la regla anterior.
4) Aplicar komi: Blanco recibe +5.5 puntos.
5) El jugador con mayor puntuación total gana. Los empates no son posibles debido al komi fraccionario.

5.2. Justificación: este método es determinista y fácil de implementar. Maneja seki correctamente: las intersecciones que forman parte de seki estarán, por definición, adyacentes a ambos colores y por tanto no serán contadas como territorio para ninguno.

---

## 6. Control de tiempo

6.1. Tiempo por jugador: 3 minutos iniciales + 2 segundos de incremento por jugada (3+2).

6.2. Si el tiempo de un jugador llega a cero, pierde automáticamente y la partida termina.

6.3. Precisión: el servidor/cliente debe considerar la hora del servidor o un árbitro cronológico único para evitar discrepancias entre clientes; si la app es local, usar el reloj del dispositivo.

6.4. Contador permanente de resultado provisional:

- Además de los contadores de tiempo, la aplicación mantendrá permanentemente un contador del resultado provisional de la partida "si terminase en este momento".
- Este contador se actualizará tras cada jugada (incluyendo pases y tras cualquier captura) y mostrará, de forma explícita y legible para los jugadores, la estimación actual: puntos de cada jugador según las reglas de puntuación aplicables (incluido el komi de Blanco).
- El resultado provisional sirve únicamente como información en tiempo real para los jugadores y para la interfaz (por ejemplo, indicar quién va por delante y por cuántos puntos). No sustituye al conteo oficial que se realizará cuando la partida finalice según las reglas (véase §5 y §7).
- Toda actualización del contador provisional quedará registrada en el historial de la partida para auditoría y trazabilidad.

---

## 7. Condiciones automáticas de finalización

7.1. La partida termina automáticamente en cualquiera de las siguientes condiciones:
- Doble paso consecutivo (ambos jugadores pasan en jugadas consecutivas): fin y conteo.
- Repetición completa detectada según §4: fin y conteo inmediato.
- 8 jugadas consecutivas sin captura después del movimiento 20 (ver nota de implementación más abajo): fin y conteo. Esto evita prolongaciones artificiales.
- Tiempo agotado de un jugador: pérdida por tiempo.

Implementación de la regla "8 jugadas sin captura": mantener un contador que se reinicia a 0 cada vez que se captura al menos una piedra; si el contador llega a 8 y se han jugado más de 20 jugadas en total, finalizar.

---

## 8. Determinación de grupos vivos y muertos (criterios conceptuales)

8.1. Principio general: la determinación de si un grupo está vivo o muerto, a efectos del conteo, debe ser coherente, reproducible y explicable. En esta versión se describen criterios conceptuales que el motor aplicará de forma determinista.

8.2. Criterio conservador de vida: un grupo que posee dos ojos inequívocos (es decir, dos zonas separadas e irremplazables de seguridad) se considera vivo de forma indiscutible. Un grupo sin ojos claros puede ser considerado muerto si carece de posibilidades razonables de generar ojos contra la defensa del adversario.

8.3. Seki y ambigüedades: las situaciones de seki se tratarán como vida mutua y las intersecciones compartidas no contarán como territorio para ninguno de los jugadores.

8.4. Resolución de controversias: cuando existan dudas no resueltas por criterios conservadores, la adjudicación seguirá reglas internas del motor diseñadas para consistencia (estas reglas concretas y los algoritmos asociados se documentarán en la fase de implementación).

---

## 9. Registro y consistencia (conceptual)

9.1. Historial de la partida: la aplicación mantendrá un registro ordenado de las jugadas y de las posiciones relevantes para permitir auditoría, detección de repeticiones relevantes y reconstrucción del desarrollo.

9.2. Transparencia: las decisiones importantes (por ejemplo, finalización por repetición consecutiva, eliminación de grupos sin libertades al final) serán registradas y accesibles en el historial de la partida para permitir revisión.

---

## 10. Resultado oficial

10.1. Posibles resultados mostrados por la app:
- "Negro gana por X puntos"
- "Blanco gana por X puntos"
- "Negro gana por tiempo"
- "Blanco gana por tiempo"

10.2. No hay empates (komi 5.5 asegura desempate).

---

## 11. Notas prácticas para desarrolladores (resumen sin algoritmos)

- El proyecto requerirá utilidades para gestionar el tablero, las jugadas, la detección de capturas y la evaluación final; en una fase posterior se introducirán las especificaciones algorítmicas detalladas.

---

## 12. Glosario técnico (resumen rápido)

- Intersección: punto del tablero (fila, columna).
- Vecino ortogonal: N/S/E/O adyacente.
- Grupo (cadena): piedras conectadas ortogonalmente del mismo color.
- Libertad (grado de libertad): intersección vacía ortogonalmente adyacente a un grupo.
- Ojo: intersección vacía que aporta seguridad a un grupo y ayuda a evitar su captura.
- Ko: situación de captura y posible recaptura inmediata que recrea una posición anterior.
- Seki: vida mutua entre grupos de distinto color.
- Dame: intersección vacía neutral adyacente a ambos colores al final de la partida.

---

# Principios fundamentales de Inazuma Go

- Rapidez estructural
- Simplicidad normativa
- Final automático
- Cero arbitraje humano
- Fidelidad al espíritu esencial del Go
- Formato competitivo digital
