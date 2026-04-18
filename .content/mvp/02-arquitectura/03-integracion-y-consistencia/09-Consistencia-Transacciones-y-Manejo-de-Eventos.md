---
title: "Consistencia, transacciones y manejo de eventos"
linkTitle: "9. Consistencia, transacciones y manejo de eventos"
weight: 9
url: "/mvp/arquitectura/integracion-y-consistencia/consistencia-transacciones-manejo-eventos/"
aliases:
  - "/mvp/arquitectura/arc42/conceptos-transversales/"
---

## Proposito de la seccion
Definir fronteras de transaccion local, estrategia de consistencia distribuida y
manejo confiable de eventos.

## Fronteras de transaccion local
| Contexto | Donde termina la transaccion local | Garantia |
|---|---|---|
| `directory` | mutacion de organizacion/politica regional + registro local de evento | consistencia fuerte de configuracion regional |
| `catalog` | mutacion de oferta + registro local de evento | consistencia fuerte de oferta publicada |
| `inventory` | mutacion de stock/reserva/disponibilidad + registro local de evento | coherencia fuerte de promesa comprometible |
| `order` | mutacion de carrito/pedido/pago manual + registro local de evento | consistencia fuerte del compromiso comercial |
| `notification` | estado de intento/entrega + registro local de evento | coherencia local de comunicacion derivada |
| `reporting` | actualizacion de proyeccion/snapshot + marca de procesamiento | coherencia local de lectura derivada |

## Consistencia dentro y fuera de fronteras
| Ambito | Modelo de consistencia |
|---|---|
| dentro de cada contexto | fuerte/transaccional local |
| entre contextos `Core` en camino critico | coordinacion sync con precondiciones explicitas |
| entre `Core` y `Generic` | eventual por eventos |
| entre capacidades transversales y negocio | contrato tecnico controlado (sin ceder ownership semantico) |

## Publicacion y consumo de eventos
| Tema | Decision |
|---|---|
| publicacion | `outbox` transaccional en productores de eventos |
| consumo | dedupe por `eventId + consumerRef` e `inbox/processed_event` cuando aplique |
| orden y particion | particion por clave de negocio para preservar orden relativo donde importa |
| DLQ | obligatoria para mensajes no recuperables |
| replay/reproceso | controlado y auditable, sin reescribir decisiones de dominio ya materializadas |

## Reintentos, duplicados y fallos de propagacion
| Escenario | Manejo requerido |
|---|---|
| timeout/fallo transitorio en consumidor | retry con backoff y limite |
| evento duplicado | `noop` idempotente |
| fallo no recuperable de contrato/payload | enrutar a DLQ + alerta operativa |
| broker degradado | preservar transaccion local y drenar outbox cuando se recupere |
| fallo de servicio derivado | no revertir estado `Core` ya confirmado |

## Preservacion de verdad por contexto
| Regla | Efecto |
|---|---|
| `reporting` no corrige estados transaccionales | evita contaminacion semantica del `Core` |
| `notification` no muta pedido ni inventario | mantiene comunicacion como capacidad derivada |
| `order` no redefine disponibilidad ni oferta | respeta ownership de `inventory` y `catalog` |
| `inventory` no redefine estado comercial del pedido | respeta ownership de `order` |

## Criterio de aceptacion para arquitectura
La consistencia queda aceptada cuando:
- cada mutacion se cierra en su frontera local,
- toda propagacion cross-context es trazable,
- y los fallos de integracion no deforman la verdad del contexto owner.
