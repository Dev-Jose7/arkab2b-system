---
title: "Prioridades del MVP / roadmap inmediato"
linkTitle: "1. Prioridades del MVP / roadmap inmediato"
weight: 1
url: "/mvp/producto/prioridades-mvp-roadmap-inmediato/"
---

## Proposito del archivo
Explicitar que capacidades y restricciones son prioritarias en el `MVP`, que
puede esperar y que secuencia inmediata tiene sentido segun dependencias ya
aprobadas.

## 1. Que va primero
| Orden | Bloque de avance |
|---|---|
| 1 | legitimidad operativa y aislamiento |
| 2 | catalogo y stock |
| 3 | pedido y pago manual |
| 4 | readiness regional |
| 5 | capacidades derivadas |

## 2. Que es critico para el MVP
| Tipo | Requisitos criticos |
|---|---|
| FR (Must) | FR-001, FR-002, FR-004, FR-005, FR-009, FR-011 |
| NFR (Must) | NFR-001, NFR-003, NFR-004, NFR-005, NFR-006, NFR-007, NFR-010, NFR-011 |
| Areas de foco inmediato | Legitimidad de actor y aislamiento por organizacion; Catalogo vendible y stock confiable; Checkout, pedido y pago manual; Parametros por pais; Trazabilidad y observabilidad minima |

## 3. Que puede esperar
| Diferido | Motivo |
|---|---|
| recordatorio de carrito abandonado | es valioso, pero no bloquea la salida del flujo core |
| analitica avanzada y forecasting | quedaron fuera del alcance del `MVP` |
| fulfillment extendido y estados operativos posteriores | el baseline actual se concentra en la confirmacion comercial inicial |
| federacion enterprise y permisos hipergranulares | son hardening o evolucion posterior, no condicion del ciclo actual |

## 4. Que secuencia de avance tiene sentido
| Paso | Bloque | Dependencias base |
|---|---|---|
| 1 | legitimidad operativa y aislamiento | FR-009, NFR-005, NFR-006, NFR-007 |
| 2 | catalogo y stock | FR-001, FR-002, NFR-004 |
| 3 | pedido y pago manual | FR-004, FR-005, FR-010, NFR-001, NFR-008 |
| 4 | readiness regional | FR-011, NFR-010, NFR-011 |
| 5 | capacidades derivadas | FR-003, FR-006, FR-007, FR-008, NFR-002, NFR-003, NFR-009 |

## 5. Que areas requieren mayor profundidad inmediata
| Area | Profundidad requerida ahora |
|---|---|
| Legitimidad de actor y aislamiento por organizacion | definir y asegurar restriccion operacional desde el inicio mediante capacidad transversal |
| Catalogo vendible y stock confiable | estabilizar oferta valida y disponibilidad coherente |
| Checkout, pedido y pago manual | consolidar flujo transaccional principal del ciclo |
| Parametros por pais | habilitar operacion regional sin reabrir el core |
| Trazabilidad y observabilidad minima | asegurar control operativo y evidencia del baseline |
