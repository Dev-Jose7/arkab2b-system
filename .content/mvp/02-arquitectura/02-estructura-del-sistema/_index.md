---
title: "Estructura del sistema"
linkTitle: "2. Estructura del sistema"
weight: 2
url: "/mvp/arquitectura/estructura-del-sistema/"
---

## Proposito del bloque
Este bloque define la forma estructural del sistema con metodologia `C4`:
contexto, contenedores y componentes por servicio/contexto.

## Preguntas que responde
- Cual es la frontera del sistema frente a actores y capacidades externas.
- Que contenedores existen y que responsabilidad semantica implementa cada uno.
- Como se organiza internamente cada servicio para materializar su contexto.

## Secciones del bloque
| Seccion | Aporte principal |
|---|---|
| Contexto del sistema | define limites del sistema y relacion con su entorno |
| Contenedores | describe servicios, stores, broker y dependencias entre contenedores |
| Componentes por contexto / servicio | aterriza organizacion interna de cada servicio por capas y modulos |

## Criterio metodologico
Este bloque mantiene la separacion entre `Core`, `Generic` y capacidad tecnica
transversal para evitar mezcla de ownership de verdad.
