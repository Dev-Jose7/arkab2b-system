---
title: "Traduccion del dominio"
linkTitle: "1. Traduccion del dominio"
weight: 1
url: "/mvp/arquitectura/traduccion-del-dominio/"
---

## Proposito del bloque
Este bloque define como el dominio consolidado se transforma en decisiones
arquitectonicas estructurales sin alterar su semantica. Aqui se fijan
principios, mapeo dominio -> arquitectura y criterios de particion del sistema.

## Preguntas que responde
- Que principios no pueden romperse al materializar el dominio.
- Donde se implementa cada `bounded context` y bajo que criterio.
- Que fronteras deben separarse y que agrupaciones son practicas en `MVP`.

## Secciones del bloque
| Seccion | Aporte principal |
|---|---|
| Principios arquitectonicos | define restricciones estructurales y criterios de fidelidad al dominio |
| Mapeo dominio -> arquitectura | traduce contextos, contratos y ownership semantico a servicios y modulos |
| Decisiones de particion del sistema | cierra que se separa, que se agrupa y por que |

## Criterio metodologico
Este bloque usa `Domain Mapping` y `Strategic-to-Structural Translation`: la
arquitectura nace desde Dominio y no redefine el modelo del negocio.
