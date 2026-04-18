---
title: "Requisitos no funcionales"
linkTitle: "3. Requisitos no funcionales"
weight: 3
url: "/mvp/producto/definicion-funcional/requisitos-no-funcionales/"
---

## Proposito del archivo
Formalizar los requisitos no funcionales del ciclo `MVP` a partir del
problema de negocio, los actores del sistema, los escenarios de negocio, los
supuestos base, los objetivos del sistema y el contexto operativo esperado.

Esta seccion deja explicita la traduccion:
- problema de negocio -> escenarios de negocio -> capacidades condicionadas -> requisitos no funcionales (`NFR`)

## Requisitos no funcionales identificados
| ID | Restriccion no funcional | Que condiciona | Fuente principal | Escenarios / capacidades impactadas |
|---|---|---|---|---|
| NFR-001 | `Tiempo de respuesta de APIs core` | el flujo de lectura y escritura del catalogo, carrito y pedido | problema de negocio, objetivos del sistema, escenarios de negocio | `Compra B2B confiable con validacion de disponibilidad`; `Confirmacion de pedido con promesa comercial protegida`; consultar catalogo, validar disponibilidad, crear pedido |
| NFR-002 | `Tiempo de generacion de reportes semanales` | la produccion de reportes operativos de ventas y abastecimiento | escenarios de negocio, objetivos del sistema, contexto operativo | `Seguimiento semanal de ventas y abastecimiento`; generar reporte semanal de ventas; generar reporte de abastecimiento |
| NFR-003 | `Disponibilidad del backend en horario operativo` | la continuidad del canal digital durante la operacion del negocio | problema de negocio, objetivos del sistema, contexto operativo | consulta de catalogo, carrito, pedido, seguimiento y reportes |
| NFR-004 | `Confiabilidad de disponibilidad comercial` | la coherencia entre stock, reserva y promesa de venta | problema de negocio, escenarios de negocio, supuestos base | `Compra B2B confiable con validacion de disponibilidad`; `Confirmacion de pedido con promesa comercial protegida`; validar disponibilidad, confirmar pedido |
| NFR-005 | `Aislamiento por organizacion y legitimidad de actor` | quien puede ver y operar informacion dentro del sistema | actores del sistema, supuestos base, objetivos del sistema | operacion por organizacion, legitimidad de actor consumida como capacidad transversal, seguimiento por cuenta |
| NFR-006 | `Trazabilidad de cambios criticos` | el registro verificable de stock, pedido, pago y estados | problema de negocio, actores del sistema, escenarios de negocio | `Registro y control trazable del pago manual`; `Seguimiento visible del estado del pedido`; `Seguimiento operativo interno del ciclo del pedido` |
| NFR-007 | `Observabilidad operacional minima` | el seguimiento de la salud y comportamiento del backend | problema de negocio, objetivos del sistema | operacion general, seguimiento de fallas, evidencia de ejecucion |
| NFR-008 | `Escalamiento en picos de demanda` | el comportamiento del sistema bajo carga alta o variable | contexto operativo / crecimiento esperado, objetivos del sistema | compra B2B, validacion de disponibilidad, reporte semanal |
| NFR-009 | `Calidad minima de entrega continua` | que los cambios lleguen sin romper el comportamiento esperado | contexto operativo / crecimiento esperado, supuestos base | evolucion controlada de las capacidades del MVP |
| NFR-010 | `Retencion y borrado / anonimizacion de datos` | el ciclo de vida de la informacion sensible y operativa | supuestos y decisiones base, contexto operativo, objetivos del sistema | pago, pedido, auditoria, historiales operativos |
| NFR-011 | `Localizacion operativa y cumplimiento configurable por pais` | el comportamiento operativo que cambia por pais o region sin alterar el flujo core | contexto operativo / crecimiento esperado, objetivos del sistema | `Operacion preparada para crecimiento regional`; reglas operativas por pais, reportes y retencion regional |

## Lectura regional operativa
| Aspecto | Delimitacion |
|---|---|
| Que cambia por pais o region | politicas operativas aplicadas por `countryCode`, retencion y reglas de tratamiento de datos |
| Que no cambia por pais o region | flujo core de catalogo, disponibilidad, pedido y pago manual en su semantica transaccional base |

## Relacion entre fuentes metodologicas y NFR
| Fuente metodologica | Lectura correcta en NFR |
|---|---|
| Problema de negocio | aporta tensiones, riesgos, impactos y fricciones que exigen cualidades operativas |
| Actores del sistema | aporta necesidades de visibilidad, legitimidad operativa, trazabilidad y confiabilidad |
| Escenarios de negocio | muestra condiciones reales en las que el sistema debe sostener esas cualidades |
| Supuestos y decisiones base | congela restricciones que no deben reinterpretarse en el MVP |
| Objetivos del sistema | convierte expectativas de negocio en cualidades operativas obligatorias |
| Contexto operativo / crecimiento esperado | define presiones de escala, disponibilidad, regionalizacion operativa y evolucion controlada |

## Resumen por prioridad
| Prioridad | IDs |
|---|---|
| Must | NFR-001, NFR-003, NFR-004, NFR-005, NFR-006, NFR-007, NFR-010, NFR-011 |
| Should | NFR-002, NFR-008, NFR-009 |
| Could | N/A |
