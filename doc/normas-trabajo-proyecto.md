# Normas de trabajo del proyecto InazumaGoPrevio

Versión: 1.0  
Fecha: 2026-03-11

## Índice

- [1. Objetivo del documento](#1-objetivo-del-documento)
- [2. Principios de trabajo](#2-principios-de-trabajo)
- [3. Equipos y responsabilidades](#3-equipos-y-responsabilidades)
- [4. Modelo de ramas](#4-modelo-de-ramas)
- [5. Flujo de trabajo con ramas y desarrollo](#5-flujo-de-trabajo-con-ramas-y-desarrollo)
- [6. Normas para Pull Requests](#6-normas-para-pull-requests)
- [7. Política de revisiones](#7-política-de-revisiones)
- [8. Política de merge](#8-política-de-merge)
- [9. Gestión de incidencias](#9-gestión-de-incidencias)
- [10. Clasificación y prioridad de incidencias](#10-clasificación-y-prioridad-de-incidencias)
- [11. Procedimiento para hotfix](#11-procedimiento-para-hotfix)
- [12. Criterios de cierre de tareas, historias e incidencias](#12-criterios-de-cierre-de-tareas-historias-e-incidencias)
- [13. Normas de documentación y trazabilidad](#13-normas-de-documentación-y-trazabilidad)
- [14. Reglas de escalado y bloqueos](#14-reglas-de-escalado-y-bloqueos)
- [15. Valoración del modelo adoptado](#15-valoración-del-modelo-adoptado)

## 1. Objetivo del documento

Este documento establece las normas de trabajo del proyecto `InazumaGoPrevio` para garantizar un desarrollo ordenado, trazable y alineado con los equipos definidos en la planificación del proyecto.

Su objetivo principal es regular:

- la forma de trabajo con ramas,
- el proceso de revisión y aprobación de cambios,
- la gestión de incidencias,
- los criterios de integración y cierre,
- y las responsabilidades de cada equipo durante el ciclo de desarrollo.

## 2. Principios de trabajo

Las normas del proyecto se basan en los siguientes principios:

- Todo cambio debe ser trazable.
- Ningún cambio funcional se integrará sin revisión.
- La calidad del producto se controlará antes de integrar en ramas principales.
- Toda incidencia debe quedar registrada y gestionada formalmente.
- La rama `main` debe mantenerse siempre en estado estable.
- La rama `dev` será la base de integración del trabajo en curso.
- El trabajo debe estar coordinado entre los equipos `UI`, `Motor`, `Red`, `DevOps` y `QA`.

## 3. Equipos y responsabilidades

A efectos organizativos, el proyecto se divide en los siguientes equipos:

- `UI`: responsable de interfaz, experiencia de usuario, pantallas, controladores de presentación y recursos visuales.
- `Motor`: responsable de lógica de negocio, servicios, controladores de aplicación, validaciones internas y modelo funcional.
- `Red`: responsable de repositorios, integración con Firebase, comunicaciones, autenticación y flujos de intercambio de datos.
- `DevOps`: responsable de scripts, CI/CD, packaging, despliegue, automatización y soporte de infraestructura técnica.
- `QA`: responsable de validación funcional, pruebas, criterios de aceptación, control de calidad y supervisión de integración.

## 4. Modelo de ramas

El proyecto utilizará el siguiente modelo de ramas:

- `main`: rama principal y estable del proyecto.
- `dev`: rama de integración del desarrollo.
- ramas de trabajo temporales creadas a partir de `dev`.

### 4.1 Rama `main`

La rama `main` será la rama principal del proyecto y deberá permanecer siempre en estado estable.

Características:

- representa la versión principal consolidada del proyecto,
- solo debe contener cambios validados,
- su gestión funcional corresponde al equipo de `QA`,
- el control final de integración en esta rama será especialmente estricto.

### 4.2 Rama `dev`

La rama `dev` será la rama de integración del trabajo aprobado para desarrollo.

Características:

- servirá como base para crear nuevas ramas de trabajo,
- reunirá cambios ya revisados y aprobados,
- estará también gestionada bajo supervisión del equipo de `QA`,
- no debe utilizarse para desarrollar directamente sobre ella.

### 4.3 Ramas de trabajo

Cada actuación sobre el proyecto se realizará en una rama nueva creada desde `dev`.

Formato principal de nombre:

- `feat/funcionalidad`

Donde `funcionalidad` identificará de forma breve y clara el trabajo realizado.

Además, para mejorar la trazabilidad, se admitirán también los siguientes prefijos según el tipo de trabajo:

- `fix/incidencia`
- `hotfix/incidencia-critica`
- `docs/documentacion`
- `refactor/modulo`

Estas variantes no sustituyen la norma principal, sino que la amplían para reflejar con mayor precisión el tipo de cambio realizado.

## 5. Flujo de trabajo con ramas y desarrollo

El flujo de trabajo del proyecto será el siguiente:

1. El trabajo parte siempre de la rama `dev`.
2. Para cada tarea, historia o incidencia se crea una rama específica.
3. El desarrollo se realiza únicamente en esa rama.
4. Cuando el trabajo está listo, se abre una Pull Request contra `dev`.
5. La Pull Request debe superar el proceso de revisión establecido.
6. Una vez aprobada, podrá integrarse en `dev`.
7. Los cambios que deban consolidarse como versión estable pasarán posteriormente de `dev` a `main` mediante el procedimiento de integración definido.

### Reglas obligatorias

- No se desarrollará directamente sobre `main`.
- No se desarrollará directamente sobre `dev`.
- Todo cambio deberá llegar mediante Pull Request.
- Toda rama de trabajo deberá estar asociada a una tarea, historia o incidencia registrada.
- Antes de solicitar revisión, la rama deberá actualizarse con `dev` si fuera necesario para evitar conflictos o integraciones obsoletas.

## 6. Normas para Pull Requests

Toda Pull Request deberá incluir, como mínimo:

- título claro y descriptivo,
- referencia a la tarea, historia o incidencia correspondiente en Taiga,
- equipo responsable del cambio,
- breve descripción funcional y técnica del cambio,
- impacto esperado,
- pruebas realizadas,
- riesgos conocidos o puntos pendientes,
- capturas o evidencias visuales si se trata de cambios de interfaz.

### Requisitos previos a revisión

Antes de pedir revisión, el autor deberá comprobar que:

- el cambio compila correctamente,
- las pruebas aplicables pasan,
- no existen conflictos pendientes con `dev`,
- la rama está correctamente nombrada,
- la descripción del PR está completa,
- Taiga refleja el estado real del trabajo.

## 7. Política de revisiones

Cada Pull Request deberá ser revisada obligatoriamente por **2 revisores**:

- **1 revisor del mismo equipo que ha desarrollado el cambio**, pero que no sea la persona autora,
- **1 revisor perteneciente al equipo de QA**.

### Reglas de revisión

- La persona autora nunca podrá autoaprobar su PR.
- El revisor técnico del mismo equipo debe validar la corrección técnica del cambio.
- El revisor de `QA` debe validar la trazabilidad, el impacto funcional, los criterios de aceptación y la calidad del cambio.
- Si el cambio afecta claramente a varios equipos, podrá solicitarse revisión adicional del equipo impactado.
- En cambios críticos de build, packaging, despliegue o automatización, se podrá requerir también validación de `DevOps`.

## 8. Política de merge

### 8.1 Merge hacia `dev`

Los cambios podrán integrarse en `dev` cuando:

- el PR tenga las dos revisiones obligatorias aprobadas,
- no existan bloqueos funcionales o técnicos abiertos,
- las comprobaciones aplicables hayan finalizado correctamente,
- la información en Taiga esté actualizada.

### 8.2 Merge hacia `main`

La integración final en `main` deberá realizarse con mayor control.

Norma general:

- los merges finales a `main` los realizará el **jefe de equipo de QA**,
- con la **supervisión del jefe de DevOps**.

Esta norma busca garantizar que la rama principal solo reciba cambios consolidados, validados y compatibles con la calidad y el proceso de entrega.

## 9. Gestión de incidencias

Toda incidencia detectada durante el proyecto deberá registrarse en **Taiga**.

No se considerará válida ninguna incidencia comunicada únicamente de forma verbal o informal si no ha sido registrada.

### Flujo de actuación ante una incidencia

1. Se crea la incidencia en Taiga.
2. Se describe el problema con la mayor precisión posible.
3. La incidencia se asigna inicialmente al **jefe de equipo responsable del área afectada**.
4. El jefe de equipo analiza la incidencia.
5. El jefe de equipo decide una de estas acciones:
   - resolverla directamente,
   - asignarla a un miembro de su equipo,
   - coordinarla con otro equipo si se trata de una incidencia transversal,
   - derivarla a `DevOps` si la incidencia corresponde a infraestructura, pipeline, despliegue, automatización o entorno.
6. Una vez resuelta, la incidencia pasará a validación de `QA` cuando afecte al comportamiento funcional o a la calidad del producto.

### Nota organizativa

Aunque inicialmente se planteó derivar las incidencias a `DevOps`, se establece como norma más precisa que **la incidencia debe asignarse primero al equipo propietario del área afectada**. `DevOps` intervendrá cuando la naturaleza del problema sea realmente de su ámbito.

## 10. Clasificación y prioridad de incidencias

Toda incidencia registrada debería incluir, al menos, una clasificación y una prioridad.

### 10.1 Clasificación sugerida

- `UI`
- `Motor`
- `Red`
- `DevOps`
- `QA`
- `Transversal`

### 10.2 Prioridad sugerida

- **Crítica**: bloquea la ejecución principal, el build, la demo, el release o una funcionalidad esencial.
- **Alta**: afecta gravemente a una funcionalidad importante, aunque exista alternativa parcial.
- **Media**: produce un error relevante, pero no bloqueante.
- **Baja**: defecto menor, mejora o ajuste no crítico.

### 10.3 Información mínima de una incidencia

Cada incidencia en Taiga deberá incluir, como mínimo:

- título,
- descripción,
- pasos para reproducir,
- comportamiento actual,
- comportamiento esperado,
- módulo o equipo afectado,
- prioridad o severidad,
- evidencias disponibles: logs, capturas, mensajes de error o contexto.

## 11. Procedimiento para hotfix

Cuando se detecte una incidencia crítica que afecte a `main`, podrá utilizarse una rama `hotfix/...`.

### Reglas de hotfix

- solo se utilizará para incidencias críticas,
- deberá abrirse la correspondiente incidencia en Taiga,
- requerirá revisión acelerada,
- deberá quedar registrada la justificación del carácter urgente,
- tras integrarse en `main`, el cambio deberá replicarse también en `dev` para evitar divergencias.

### Revisión mínima del hotfix

Como mínimo, un hotfix deberá contar con:

- 1 revisión técnica,
- 1 revisión de `QA`,
- validación final del jefe de `QA`.

Cuando el hotfix afecte a build, despliegue o automatización, deberá contar además con supervisión de `DevOps`.

## 12. Criterios de cierre de tareas, historias e incidencias

Una tarea, historia o incidencia no se considerará cerrada únicamente porque su desarrollo haya finalizado.

Se considerará cerrada cuando:

- el trabajo esté implementado,
- el cambio esté integrado mediante PR,
- las revisiones obligatorias estén aprobadas,
- las pruebas aplicables hayan sido superadas,
- `QA` haya validado el resultado cuando corresponda,
- Taiga esté actualizada con el estado final.

## 13. Normas de documentación y trazabilidad

Para mantener la trazabilidad del proyecto:

- toda rama deberá poder relacionarse con una tarea o incidencia,
- todo PR deberá enlazar con su elemento en Taiga,
- toda incidencia deberá registrar su evolución,
- toda decisión relevante de proceso deberá quedar documentada si afecta al equipo.

Además:

- los cambios de comportamiento funcional deberán reflejarse en la documentación si procede,
- las normas y documentación del proyecto deberán mantenerse actualizadas en `README.md` o en `doc/`.

## 14. Reglas de escalado y bloqueos

Cuando un PR, incidencia o tarea quede bloqueado y no pueda avanzar en un plazo razonable, deberá escalarse.

### Criterios de escalado sugeridos

Se recomienda escalar cuando:

- exista un bloqueo técnico entre equipos,
- falte una revisión durante un tiempo excesivo,
- el cambio afecte a varios equipos y no haya acuerdo de responsabilidad,
- exista riesgo para la integración del sprint,
- se vea comprometido el build, la demo o el release.

### Escalado

- El primer nivel de escalado será el jefe de equipo correspondiente.
- Si el bloqueo continúa, se elevará a coordinación del proyecto.
- Si el bloqueo afecta al proceso de integración, despliegue o estabilidad del entorno, se involucrará también a `DevOps` y `QA`.

## 15. Valoración del modelo adoptado

El modelo adoptado en este proyecto se considera adecuado porque:

- establece una estructura clara de ramas,
- obliga a que todo cambio pase por revisión,
- incorpora control de calidad mediante participación de `QA`,
- protege la estabilidad de `main`,
- y aporta trazabilidad mediante el uso obligatorio de Taiga.

No obstante, se reconoce que una participación intensiva de `QA` en revisiones y merges puede generar cuellos de botella si no se gestiona adecuadamente. Por ello, se establece que el papel de `QA` debe centrarse en la validación de calidad, la supervisión de integración y el control de aceptación, evitando asumir cargas innecesarias de ejecución técnica que correspondan a otros equipos.

Del mismo modo, `DevOps` participará en la supervisión de integración final y en las incidencias de su ámbito, pero no se considerará destino automático de todas las incidencias del proyecto.

Con estas reglas, el proyecto dispone de un marco de trabajo común, controlado y coherente con la organización definida en la planificación general.

