---
title: "Supuestos y Decisiones Base"
linkTitle: "3. Supuestos y decisiones base"
weight: 3
url: "/mvp/producto/supuestos-decisiones-base/"
---

## Propósito del archivo
Explicitar que condiciones se dan por sentadas y que decisiones del baseline se
congelan para reducir ambigüedad en el modelado posterior.

## Supuestos y simplificaciones del `MVP`
| Tipo           | Definicion                                                                                   | Impacto en el ciclo                                                |
|----------------|----------------------------------------------------------------------------------------------|--------------------------------------------------------------------|
| Supuesto       | el canal manual coexistira temporalmente como contingencia                                   | la salida digital no elimina de inmediato la operacion actual      |
| Supuesto       | el `MVP` prioriza confiabilidad del flujo de compra sobre amplitud funcional                 | se favorece estabilidad del core antes que expansion de features   |
| Supuesto       | baseline y metas iniciales se apoyan en documentos del reto                                  | los adjuntos siguen siendo fuente primaria del pilar               |
| Simplificacion | el producto se centra en backend y no incluye desarrollo de frontend                         | reduce alcance de implementacion a la verdad funcional del backend |
| Simplificacion | no se incorpora pasarela online ni conciliacion automatica bancaria en esta fase             | el pago opera como registro manual controlado                      |
| Simplificacion | las capacidades clasificadas como evolucion posterior no bloquean el cierre del ciclo actual | se evita inflacion del `MVP`                                       |

## Decisiones y restricciones de partida
| Tipo                   | Definicion                                                                                                                                                   | Como condiciona el producto                                       |
|------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------|
| Decision congelada     | el alcance aprobado en requisitos funcionales y no funcionales queda congelado para este ciclo                                                               | evita reabrir semantica core durante la implementacion            |
| Decision congelada     | no se reabre la semantica core de carrito, checkout, pedido, pago manual, stock, reserva, catalogo vendible ni aislamiento organizacional por legitimidad de actor | dominio y arquitectura deben consumir esa verdad sin reinventarla |
| Decision congelada     | la regionalizacion operativa se resuelve por `countryCode` y no admite fallback global implicito en operaciones criticas                                     | obliga a resolver operaciones criticas con configuracion por pais |
| Decision congelada     | el empaquetado tecnico y la ejecucion reproducible por servicio forman parte del baseline de entrega                                                         | calidad, arquitectura y operacion deben sostener reproducibilidad |
| Restriccion de partida | aislamiento por organizacion y legitimidad de actor en toda mutacion                                                                                         | impide operaciones cruzadas entre organizaciones                  |
| Restriccion de partida | trazabilidad auditable de cambios criticos (stock, pedido, pago)                                                                                             | obliga evidencia operativa en mutaciones del flujo comercial      |
| Restriccion de partida | retencion y borrado/anonimizacion segun politica vigente por pais                                                                                            | condiciona tratamiento y ciclo de vida de datos operativos        |

## Reglas base de cumplimiento del producto
| Regla                                                                        | Alcance                                               |
|------------------------------------------------------------------------------|-------------------------------------------------------|
| trazabilidad auditable de cambios en stock, estado de pedido y pagos         | toda mutacion critica debe dejar evidencia operativa  |
| retencion y borrado o anonimizacion de datos segun politica vigente por pais | la operacion regional condiciona tratamiento de datos |
| segregacion de datos por organizacion en toda consulta y mutacion            | el aislamiento por organizacion y legitimidad de actor es parte del baseline |
