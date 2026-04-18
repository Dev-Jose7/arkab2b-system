---
title: "Requisitos funcionales"
linkTitle: "2. Requisitos funcionales"
weight: 2
url: "/mvp/producto/requisitos-funcionales/"
---

## Propósito del archivo
Formalizar los requisitos funcionales del ciclo `MVP` a partir de los casos de
uso funcionales identificados en el bloque de definición funcional.

Esta sección reemplaza la antigua subsección de capacidades funcionales y deja
explícita la traducción:
- escenarios de negocio -> casos de uso funcionales -> requisitos funcionales

## Requisitos funcionales identificados
| ID | Requisito funcional | Caso(s) de uso de origen | Descripcion |
|---|---|---|---|
| FR-001 | `Consultar catalogo vendible` | CUF01 | El sistema debe permitir al comprador B2B consultar la oferta comercial disponible para compra. |
| FR-002 | `Filtrar y revisar productos y variantes` | CUF01 | El sistema debe permitir identificar productos, variantes y condiciones basicas de vendibilidad dentro del catalogo. |
| FR-003 | `Consultar disponibilidad comprometible` | CUF02 | El sistema debe informar la disponibilidad aplicable antes de la confirmacion de compra. |
| FR-004 | `Validar disponibilidad previa a confirmacion` | CUF02 | El sistema debe validar si la oferta seleccionada puede ser comprometida comercialmente antes de formalizar el pedido. |
| FR-005 | `Gestionar carrito de compra` | CUF03 | El sistema debe permitir crear y mantener un carrito de compra antes de la generacion del pedido. |
| FR-006 | `Agregar y ajustar productos en carrito` | CUF03 | El sistema debe permitir agregar, actualizar o retirar productos del carrito mientras no haya sido convertido en pedido. |
| FR-007 | `Crear pedido desde carrito validado` | CUF04 | El sistema debe permitir formalizar un pedido a partir de un carrito valido. |
| FR-008 | `Registrar pedido trazable` | CUF04 | El sistema debe registrar el pedido con informacion suficiente para su seguimiento posterior. |
| FR-009 | `Ajustar pedido antes de cierre` | CUF05 | El sistema debe permitir modificar un pedido dentro de la ventana permitida antes de su cierre operativo o contractual. |
| FR-010 | `Revalidar consistencia tras ajuste de pedido` | CUF05 | El sistema debe recalcular y validar la consistencia del pedido cuando este sea ajustado antes de cierre. |
| FR-011 | `Consultar estado del pedido` | CUF06 | El sistema debe permitir al comprador B2B consultar el estado vigente de su pedido. |
| FR-012 | `Exponer estado vigente con contexto operativo` | CUF06 | El sistema debe mostrar el estado actual del pedido con datos de ultima actualizacion para lectura entendible. |
| FR-013 | `Actualizar estado operativo del pedido` | CUF07 | El sistema debe permitir a la operacion registrar cambios de estado sobre el pedido durante su ciclo operativo. |
| FR-014 | `Mantener trazabilidad de cambios operativos` | CUF07, CUF11 | El sistema debe conservar historial verificable y cronologico de los cambios relevantes aplicados al pedido. |
| FR-015 | `Registrar pago manual asociado a pedido` | CUF08 | El sistema debe permitir registrar pagos manuales vinculados a un pedido. |
| FR-016 | `Mantener evidencia del pago manual` | CUF08, CUF09 | El sistema debe conservar evidencia trazable del registro y estado del pago manual. |
| FR-017 | `Consultar estado financiero del pedido` | CUF09 | El sistema debe permitir consultar el estado financiero/manual del pedido. |
| FR-018 | `Emitir notificaciones por cambios relevantes` | CUF10 | El sistema debe emitir comunicaciones derivadas cuando ocurran cambios relevantes en el ciclo comercial o del pedido. |
| FR-019 | `Registrar entrega o emisión de notificaciones` | CUF10 | El sistema debe conservar registro de las notificaciones emitidas o intentadas. |
| FR-020 | `Consultar historial de cambios relevantes` | CUF11 | El sistema debe permitir consultar el historial de cambios relevantes ocurridos sobre un pedido. |
| FR-021 | `Actualizar stock operativo` | CUF12 | El sistema debe permitir registrar cambios de stock derivados de la operacion. |
| FR-022 | `Mantener stock alineado con la realidad operativa` | CUF12 | El sistema debe conservar una representacion actualizada del stock operativo. |
| FR-023 | `Recalcular disponibilidad comprometible` | CUF13 | El sistema debe recalcular la disponibilidad comprometible cuando cambien stock, reservas o condiciones equivalentes. |
| FR-024 | `Proteger la promesa comercial mediante disponibilidad coherente` | CUF13 | El sistema debe reflejar disponibilidad coherente con el stock y las reservas vigentes. |
| FR-025 | `Generar reporte semanal de ventas` | CUF14 | El sistema debe generar una vista semanal consolidada del desempeno comercial. |
| FR-026 | `Generar reporte de abastecimiento o reposicion` | CUF15 | El sistema debe generar una vista operativa de necesidades de reposicion o abastecimiento. |
| FR-027 | `Configurar reglas operativas regionales` | CUF16 | El sistema debe permitir definir reglas operativas aplicables por pais o region. |
| FR-028 | `Aplicar reglas regionales en la operacion` | CUF17 | El sistema debe ejecutar comportamiento condicionado por la politica regional vigente, sin alterar el flujo core de compra B2B. |

## Relación entre casos de uso funcionales y requisitos funcionales
| Caso de uso funcional | Requisitos funcionales derivados |
|---|---|
| CUF01 `Consultar catalogo vendible` | FR-001, FR-002 |
| CUF02 `Consultar disponibilidad antes de confirmar compra` | FR-003, FR-004 |
| CUF03 `Gestionar carrito de compra` | FR-005, FR-006 |
| CUF04 `Crear pedido a partir del carrito` | FR-007, FR-008 |
| CUF05 `Ajustar pedido antes de cierre` | FR-009, FR-010 |
| CUF06 `Consultar estado del pedido` | FR-011, FR-012 |
| CUF07 `Actualizar estado operativo del pedido` | FR-013, FR-014 |
| CUF08 `Registrar pago manual` | FR-015, FR-016 |
| CUF09 `Consultar estado financiero del pedido` | FR-016, FR-017 |
| CUF10 `Emitir notificacion por cambio relevante` | FR-018, FR-019 |
| CUF11 `Consultar historial de cambios relevantes` | FR-014, FR-020 |
| CUF12 `Actualizar stock operativo` | FR-021, FR-022 |
| CUF13 `Recalcular disponibilidad comprometible` | FR-023, FR-024 |
| CUF14 `Generar reporte semanal de ventas` | FR-025 |
| CUF15 `Generar reporte de abastecimiento o reposicion` | FR-026 |
| CUF16 `Configurar politica regional aplicable` | FR-027 |
| CUF17 `Aplicar reglas regionales en la operacion` | FR-028 |

## Resumen por prioridad
| Prioridad | IDs |
|---|---|
| Must | FR-001, FR-002, FR-004, FR-005, FR-009, FR-011 |
| Should | FR-003, FR-006, FR-007, FR-008, FR-010, FR-012, FR-013, FR-014, FR-015, FR-016, FR-017, FR-018, FR-019, FR-020, FR-021, FR-022, FR-023, FR-024, FR-025, FR-026, FR-027, FR-028 |
| Could | N/A |

## Separacion semantica aplicada en requisitos del pedido
| Capacidad | Requisitos asociados | Delimitacion aplicada |
|---|---|---|
| `Visibilidad de estado` | FR-011, FR-012 | consulta del estado vigente; no reemplaza historial ni notificacion |
| `Historial / trazabilidad` | FR-014, FR-020 | evidencia cronologica de cambios confirmados |
| `Notificacion derivada` | FR-018, FR-019 | comunicacion y registro de entrega de cambios relevantes |

## Lectura metodológica

| Aspecto | Lectura correcta |
|---|---|
| Qué son estos requisitos funcionales | Formalizan las respuestas obligatorias que el sistema debe ofrecer frente a los casos de uso funcionales ya identificados. |
| Qué no son | No son diseño técnico, componentes de arquitectura, agregados del dominio, comandos de aplicacion ni contratos API. |
| Qué viene despues | A partir de estos requisitos funcionales, el pilar de Dominio puede formalizar lenguaje, conceptos, reglas, comportamiento, eventos y modelo táctico. |

La cadena correcta queda así:

**Problema de negocio → escenario de negocio → caso de uso funcional → requisito funcional**
