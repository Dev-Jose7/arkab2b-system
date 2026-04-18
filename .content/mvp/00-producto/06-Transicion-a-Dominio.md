---
title: "Transición a Dominio"
linkTitle: "6. Transición a Dominio"
weight: 6
url: "/mvp/producto/transicion-a-dominio/"
---

## Propósito de la transición
Esta sección define la interfaz formal entre el pilar de Producto y el pilar de
Dominio en el ciclo `MVP`. Producto prepara el terreno con una salida de negocio
estructurada, y Dominio toma esa salida como entrada oficial para formalizar la
semántica del sistema.

Producto no modela todavía el dominio en términos tácticos. Su responsabilidad
es dejar una base clara, trazable y sin ambigüedad crítica para que Dominio
pueda iniciar su trabajo con foco metodológico y continuidad documental.

## Que entrega Producto a Dominio
Producto entrega a Dominio el siguiente contenido, de forma explícita y
trazable dentro del pilar:

| Entrega | Contenido listado                                      | Fuente en Producto                                           |
|---------|--------------------------------------------------------|--------------------------------------------------------------|
| 1       | proposito del sistema                                  | `01-estrategica/01-Resumen-del-Producto.md`                  |
| 2       | problema de negocio                                    | `01-estrategica/02-Problema-de-Negocio.md`                   |
| 3       | objetivos del sistema                                  | `01-estrategica/03-Objetivos-del-Sistema.md`                 |
| 4       | alcance y no alcance                                   | `01-estrategica/04-Alcance-No-Alcance.md`                    |
| 5       | actores del sistema                                    | `02-contexto-de-uso/01-Actores-del-Sistema.md`               |
| 6       | escenarios de negocio                                  | `02-contexto-de-uso/02-Escenarios-de-Negocio.md`             |
| 7       | supuestos y decisiones base                            | `02-contexto-de-uso/03-Supuestos-y-Decisiones-Base.md`       |
| 8       | casos de uso funcionales                               | `03-definicion-funcional/01-Casos-de-Uso-Funcionales.md`     |
| 9       | requisitos funcionales                                 | `03-definicion-funcional/02-Requisitos-Funcionales.md`       |
| 10      | requisitos no funcionales                              | `03-definicion-funcional/03-Requisitos-No-Funcionales.md`    |
| 11      | glosario preliminar                                    | `04-semantica-inicial/01-Glosario-Preliminar.md`             |
| 12      | prioridades del `MVP` / roadmap inmediato              | `05-direccion/01-Prioridades-del-MVP-Roadmap-Inmediato.md`   |

Esta salida representa la definición funcional, contextual y operativa del producto 
para el corte vigente, y constituye la base de entrada para el modelado
semántico posterior.

## Que se espera que Dominio haga con esta salida
Con esta entrada, el pilar de Dominio debe formalizar:
- el significado del negocio;
- su lenguaje;
- sus conceptos;
- sus reglas;
- su comportamiento;
- sus límites semánticos;
- y su modelo.

La expectativa metodológica es que Dominio transforme esta base en una
formalización semántica consistente, preservando trazabilidad con Producto y sin
reinterpretar arbitrariamente el alcance definido.

## Cierre
Producto define que resultado de negocio se quiere lograr y entrega una entrada
estructurada para que Dominio formalice la verdad del negocio de forma
controlada, verificable y alineada con el stack documental de ArkaB2B.
