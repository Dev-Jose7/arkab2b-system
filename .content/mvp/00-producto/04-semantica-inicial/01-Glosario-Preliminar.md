---
title: "Glosario preliminar"
linkTitle: "1. Glosario preliminar"
weight: 1
url: "/mvp/producto/glosario-preliminar/"
aliases:
  - "/mvp/producto/glosario/"
---

## Proposito del archivo
Definir lenguaje preliminar y canonico de producto para reducir ambiguedad en
los requisitos, en la lectura del baseline y en el modelado posterior.

## Cobertura de la seccion
| Tema requerido | Cobertura |
|---|---|
| terminos relevantes del negocio | se consolidan en `Terminos` con nombre canonico |
| significados preliminares | cada termino declara definicion operacional para `MVP` |
| expresiones usadas por producto | se explicitan en `Expresion de uso` y reglas de lenguaje |
| candidatos a modelado posterior | se marca por termino en `Candidato` con motivo |

## Terminos del producto
| Termino | Definicion preliminar | Expresion de uso | Candidato | Motivo |
|---|---|---|---|---|
| Organizacion | Cliente B2B con identidad comercial propia dentro del sistema. | "El pedido pertenece a la organizacion ORG-001." | Si | Define frontera de operacion y aislamiento por contexto organizacional. |
| Comprador B2B | Persona legitimada y asociada a una organizacion que participa en el ciclo comercial de compra. | "El comprador B2B confirma el pedido." | Si | Participa en la validacion, confirmacion y seguimiento de la compra. |
| Legitimidad de actor (capacidad transversal) | Condicion de uso permitido del sistema validada por una capacidad tecnica externa al dominio de negocio. | "La operacion se ejecuta con actor legitimado." | Si | Condiciona acceso operativo y aislamiento sin modelar un dominio interno de IAM. |
| Producto | Agrupador comercial de variantes vendibles. | "El producto SSD NVMe tiene varias variantes." | Si | Concepto comercial central para catalogo y oferta vendible. |
| Variante (SKU) | Unidad vendible concreta con atributos definidos. | "SKU SSD-1TB-NVME-980PRO" | Si | Unidad vendible con reglas de identificacion y consistencia. |
| Stock fisico | Unidades realmente existentes en inventario. | "El stock fisico del SKU es 120." | Si | Base para calculo de disponibilidad y control de inventario. |
| Disponibilidad | Unidades vendibles calculadas como stock fisico menos reservas activas. | "Disponibilidad actual: 35 unidades." | Si | Regla operativa clave para no sobreventa y promesa comercial. |
| Reserva | Apartado temporal de unidades para un carrito o checkout. | "La reserva vence en 20 minutos." | Si | Comportamiento temporal que condiciona carrito/checkout. |
| Carrito | Intencion de compra editable previa a confirmar pedido. | "El carrito contiene 5 lineas." | Si | Intencion de compra previa al pedido con reglas de evolucion. |
| Carrito abandonado | Carrito sin conversion a pedido dentro de la ventana definida. | "Se detecto carrito abandonado para recordatorio." | Si | Evento operacional para seguimiento y recuperacion comercial. |
| Pedido | Entidad de compra creada desde carrito que se materializa en `PENDING_APPROVAL` y evoluciona por estados contractuales del MVP (`PENDING_APPROVAL`, `CONFIRMED`, `CANCELLED`); `CREATED` es interno y los estados de fulfillment quedan reservados. | "Pedido ARKA-CO-2026-000184 en estado PENDING_APPROVAL." | Si | Centro del flujo transaccional y ciclo de estados del MVP. |
| Estado de pedido | Etapa oficial del ciclo de vida de un pedido. | "Estado de pedido: CONFIRMED." | Si | Expresa transiciones oficiales y control de proceso. |
| Estado de pago | Situacion agregada de pago del pedido. | "Estado de pago: PENDING." | Si | Resume situacion financiera del pedido en el ciclo MVP. |
| Sobreventa | Confirmacion de pedido sin disponibilidad real suficiente. | "La meta es mantener sobreventa <= 1.0%." | Si | Restriccion de negocio con impacto semantico y operativo. |
| Reporte de abastecimiento | Reporte de SKU con riesgo de faltante para reponer inventario o abastecer la operacion. | "Se genero el reporte de abastecimiento semanal." | Si | Soporta decision operativa de reposicion y continuidad comercial. |

## Sinonimos no permitidos
| Termino canonico | Sinonimos prohibidos |
|---|---|
| Organizacion | cliente global, cuenta global |
| Comprador B2B | usuario global, usuario libre |
| Legitimidad de actor | admin total, permiso libre, rol de negocio |
| Producto | item maestro |
| Variante (SKU) | item suelto, referencia ambigua |
| Stock fisico | stock comercial |
| Disponibilidad | stock disponible comercial |
| Reserva | bloqueo indefinido |
| Carrito | pedido borrador final |
| Carrito abandonado | carrito perdido |
| Pedido | orden finalizada |
| Estado de pedido | estado final |
| Estado de pago | estado financiero libre |
| Sobreventa | falta normal de stock |
| Reporte de abastecimiento | reporte de compras suelto |

## Acronimos
| Acronimo | Significado |
|---|---|
| API | Application Programming Interface |
| BC | Bounded Context |
| MVP | Minimum Viable Product |
| SKU | Stock Keeping Unit |
| SLA | Service Level Agreement |
| KPI | Key Performance Indicator |
| NFR | Non-Functional Requirement |
| FR | Functional Requirement |

## Reglas de lenguaje
- Un concepto tiene un solo nombre canonico.
- Si se crea un termino nuevo, tambien debe actualizarse el pilar de dominio
  cuando esa semantica se formalice.
- Si en este ciclo se materializan contratos, deben usar los nombres canonicos
  definidos aqui.
- Eventos de contrato usan naming en ingles `UpperCamelCase` y envelope comun con
  `eventType` y `eventVersion`.
- Las tablas de FR y NFR deben usar los mismos terminos definidos aqui.
