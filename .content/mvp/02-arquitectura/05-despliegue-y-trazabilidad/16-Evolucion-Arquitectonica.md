---
title: "Evolucion arquitectonica"
linkTitle: "16. Evolucion arquitectonica"
weight: 16
url: "/mvp/arquitectura/despliegue-y-trazabilidad/evolucion-arquitectonica/"
aliases:
  - "/mvp/arquitectura/evolucion/"
---

## Proposito de la seccion
Explicitar direccion de evolucion de la arquitectura distinguiendo partes
cerradas, partes provisionales y riesgos activos sin romper semantica.

## Partes cerradas en el corte actual
| Componente | Estado |
|---|---|
| particion por contextos `Core` y `Generic` | cerrada |
| tratamiento de `identity-access` como capacidad transversal | cerrado |
| estrategia `sync`/`async` con `outbox`, dedupe y `DLQ` | cerrada |
| `database-per-service` y ownership de datos | cerrado |
| separacion `catalog` vs `inventory` | cerrada |

## Partes provisionales o ajustables
| Area | Condicion de ajuste |
|---|---|
| topologia de despliegue por costo/capacidad | puede evolucionar sin romper limites semanticos |
| escalado fino por servicio | se calibra con trafico real por contexto |
| granularidad de observabilidad/alertas | se endurece segun evidencia operativa |
| tuning de consultas y proyecciones | permitido si no altera contratos ni ownership |

## Riesgos activos (rescatados de `legacy` y ajustados al estado actual)
| ID | Riesgo | Impacto | Mitigacion vigente |
|---|---|---|---|
| R-01 | errores de idempotencia en checkout/pago o consumo de eventos | duplicidad de efectos en `order` y/o sesgo en derivados | llaves idempotentes en mutaciones, dedupe por `processed_event`, pruebas de contrato |
| R-02 | backlog de broker o lag de consumidores | atraso en `notification` y `reporting` | particionado, autoscaling de consumidores, monitoreo `outbox.pending` y `DLQ` |
| R-03 | brechas en autorizacion contextual por organizacion | acceso cruzado o mutacion no autorizada | enforcement de aislamiento por organizacion en servicio owner + auditoria obligatoria |
| R-04 | dependencia sync excesiva en camino critico | aumento de latencia o rechazo por timeout en checkout | budget de timeout por dependencia + degradacion controlada + reintento acotado |
| R-05 | sobredimensionar `notification`/`reporting` con logica transaccional | contaminacion semantica de capacidades derivadas | mantenerlos read/derivados y sin ownership de verdad `Core` |
| R-06 | crecimiento acelerado de tablas de historial/proyecciones | costo operativo y degradacion de consultas | particionado temporal, archivado y politicas de retencion por servicio |

## Deuda tecnica aceptada
| ID | Deuda | Motivo | Plan de pago |
|---|---|---|---|
| TD-01 | tuning de rendimiento final por pais/tenant | falta telemetria de carga real | recalibrar presupuestos despues de pilotos operativos |
| TD-02 | hardening avanzado de seguridad por servicio | prioridad en cierre semantico y trazabilidad | endurecimiento incremental en operacion sin romper contratos |
| TD-03 | evolucion de reporteria avanzada | prioridad de consistencia `Core` en `MVP` | ampliar modelos analiticos por iteraciones controladas |
| TD-04 | automatizacion mas profunda de contract testing cross-service | cobertura actual suficiente para baseline | aumentar gates y suites en ciclos siguientes |

## Tensiones de rendimiento por servicio
| Servicio | Tension principal | Respuesta de evolucion recomendada |
|---|---|---|
| `directory-service` | latencia de resolucion regional en picos de checkout | cache regional por ventana y escalado por `p95` |
| `catalog-service` | cargas masivas de precio y facetas de alta cardinalidad | batch controlado + indices de atributos + invalidacion selectiva |
| `inventory-service` | contencion en SKU caliente y expiracion de reservas | orden estable de lock, retry con jitter y particion de lotes |
| `order-service` | dependencia sync en confirmacion de pedido | budgets por dependencia + fallback de degradacion funcional |
| `notification-service` | tormenta de reintentos por proveedor externo | politicas de backoff/circuit breaker y control de cola |
| `reporting-service` | lag de ingesta y costo de rebuild | autoscaling por lag + rebuild incremental por tenant/periodo |

## Direccion razonable de evolucion
- separar capacidad solo cuando haya nueva frontera semantica real;
- fortalecer hardening de seguridad sin convertir capacidad transversal en
  dominio interno;
- escalar `Core` por criticidad de comandos y mantener `Generic` con densidad
  minima suficiente;
- reforzar contract testing y trazabilidad de extremo a extremo sin duplicar
  modelos de negocio.

## Regla de cambio arquitectonico
Todo cambio que afecte fronteras, consistencia o contratos debe registrarse por
ADR y actualizar trazabilidad dominio -> arquitectura.
