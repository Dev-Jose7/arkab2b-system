---
title: "Persistencia y gestion de datos"
linkTitle: "10. Persistencia y gestion de datos"
weight: 10
url: "/mvp/arquitectura/realizacion-tecnica/persistencia-y-gestion-de-datos/"
---

## Proposito de la seccion
Definir ownership de datos, almacenamiento por contexto y reglas de aislamiento
para sostener consistencia semantica en arquitectura.

## Principios de persistencia
| Principio | Aplicacion |
|---|---|
| `database-per-service` | cada contexto persiste su verdad en su propia base y schema |
| ownership unico de dato transaccional | solo el contexto owner puede mutar su verdad transaccional |
| lectura derivada por eventos | `notification` y `reporting` reconstruyen estado desde hechos publicados |
| sin FK ni joins cross-service | integracion de datos solo por API/eventos versionados |
| idempotencia en write path | mutaciones HTTP/eventos usan llaves de dedupe en tablas `idempotency/processed_event` |

## Modelo logico por contexto/servicio
| Servicio | Modelo logico dominante | Invariantes de persistencia | Datos transaccionales owner | Datos derivados que publica |
|---|---|---|---|---|
| `directory-service` | `organization`, `organization_country_policy`, `organization_contact`, `address` | politica activa unica por `organizationId+countryCode`; default de direccion/contacto unico por tipo activo | contexto organizacional y politica regional vigente | resumen organizacional y hechos regionales |
| `catalog-service` | `product`, `variant`, `price`, `variant_attribute` | SKU unico por tenant; precio vigente resoluble sin traslape temporal | oferta vendible y precio de catalogo | vistas de catalogo consultable |
| `inventory-service` | `stock_item`, `stock_reservation`, `stock_movement` | `physical_qty >= 0`; `reserved_qty <= physical_qty`; reserva solo si hay disponibilidad | stock, reservas y disponibilidad comprometible | disponibilidad por SKU y alertas derivadas |
| `order-service` | `cart`, `purchase_order`, `order_line`, `payment_record`, `order_status_history` | pedido solo desde checkout validado; pago manual con referencia trazable; transicion de estado valida | compromiso comercial de pedido y estado financiero manual | historial consultable y hechos de ciclo de pedido |
| `notification-service` (`Generic`) | `notification_request`, `notification_attempt`, `provider_callback` | estados terminales (`SENT`/`DISCARDED`) no mutables; dedupe por `eventId+recipient+channel` | estado de comunicacion derivada | eventos de emision/entrega de notificacion |
| `reporting-service` (`Generic`) | `analytic_fact`, `sales_projection`, `replenishment_projection`, `weekly_report_execution`, `report_artifact` | dedupe por `sourceEventId`; unicidad por `tenant+week+reportType` en ejecucion semanal | snapshots y proyecciones derivadas | reportes semanales y KPI de lectura |

## Modelo fisico e indices criticos por servicio
| Servicio | Tablas fisicas principales | Constraints/indices criticos (rescatado y adaptado) | Retencion/particionado recomendado |
|---|---|---|---|
| `directory-service` | `organization`, `organization_country_policy`, `organization_contact`, `address`, `directory_audit`, `outbox_event`, `processed_event` | `ux_country_policy_active` (politica activa unica), `ux_org_contact_primary`, `ux_address_default_by_type`, `idx_country_policy_resolution`, `ux_processed_event_consumer` | `directory_audit` mensual si crece; `outbox` 30 dias; `processed_event` 30-60 dias |
| `catalog-service` | `products`, `variants`, `prices`, `price_schedules`, `catalog_audits`, `outbox_events`, `processed_events` | `ux_variants_tenant_sku`, indice de precio vigente por `variant+effective_from`, indice de facetas `attribute_code+normalized_value`, `idx_outbox_pending_occurred` | auditoria extendida; `outbox` 30 dias; crecimiento controlado de atributos |
| `inventory-service` | `stock_items`, `stock_reservations`, `stock_movements`, `idempotency_records`, `inventory_audit`, `outbox_event`, `processed_event` | checks de no-negativo/consistencia reservada, indice de contencion `tenant+warehouse+sku`, dedupe por idempotency key, `ux_processed_event_consumer` | `stock_movement` particion temporal; `processed_event` corto para dedupe |
| `order-service` | `carts`, `cart_items`, `checkout_attempts`, `purchase_orders`, `order_lines`, `payment_records`, `order_status_history`, `order_audit`, `idempotency_records`, `outbox_event` | unicidad de `checkout_correlation_id` por tenant, idempotencia por `tenant+operation+key`, indices por estado de pedido y fecha, historial ordenado por `occurred_at` | `order_status_history` y `order_audit` con archivado mensual |
| `notification-service` (`Generic`) | `notification_requests`, `notification_attempts`, `provider_callbacks`, `notification_audits`, `channel_policies`, `outbox_events`, `processed_events` | `ux_requests_tenant_notification_key`, `ux_attempts_notification_number`, dedupe de callback `provider+providerRef+callbackEventId`, `idx_outbox_status_occurred` | `notification_attempts` mensual; callbacks 6 meses; auditoria 24 meses |
| `reporting-service` (`Generic`) | `analytic_facts`, `sales_projections`, `replenishment_projections`, `operations_kpi_projections`, `weekly_report_executions`, `report_artifacts`, `consumer_checkpoints`, `outbox_events`, `processed_events` | `ux_facts_tenant_source_event`, `ux_sales_tenant_period`, `ux_supply_tenant_period_sku`, `ux_weekly_exec_tenant_week_type`, `ux_artifacts_tenant_week_type_format` | `analytic_facts` particion mensual; artifacts 12 meses; `processed_events` 60 dias |

## Ownership de datos transaccionales vs derivados
| Contexto/servicio | Datos transaccionales owner | Datos derivados |
|---|---|---|
| `directory-service` | organizacion, membresia operativa, direcciones operativas, politica regional vigente | vistas de consulta organizacional |
| `catalog-service` | productos, variantes, condiciones de vendibilidad y precio de catalogo | vistas de catalogo consultable |
| `inventory-service` | stock operativo, reservas, disponibilidad comprometible | vistas de disponibilidad por SKU/organizacion |
| `order-service` | carrito, pedido, estado operativo y financiero, pago manual | historial de cambios y consultas operativas |
| `notification-service` | solicitud/intento/resultado de notificacion | paneles operativos de entrega |
| `reporting-service` | snapshots semanales y proyecciones versionadas | reportes de ventas y reposicion |

## Estrategia de almacenamiento
| Componente | Estrategia |
|---|---|
| `Core` services | `PostgreSQL` por servicio |
| `notification-service` | almacenamiento propio de request/attempt/callback para trazabilidad de entrega |
| `reporting-service` | almacenamiento read-side para hechos, proyecciones y artefactos |
| cache tecnica | `Redis` para cache selectiva, idempotencia corta y checkpoints operativos |
| broker/eventos | topicos con `DLQ` y retry controlado para integracion asincrona |

## Artefactos de modelo complementario
| Artefacto | Uso |
|---|---|
| `legacy/02-arquitectura/02-servicios/02-servicio-directorio/data/directory-current.dbml` | referencia de modelo fisico de `directory` reutilizada para aterrizar claves/indices vigentes |
| modelos logico/fisico de `legacy` por servicio (`data/01-Modelo-Logico.md`, `data/02-Modelo-Fisico.md`) | fuente secundaria de detalle tecnico incorporada en este documento |

## Regla de aislamiento semantico de datos
- sin joins ni `FK` cross-service;
- sin consultas directas a base de otro servicio;
- integracion de datos solo por API/eventos versionados;
- ningun servicio derivado corrige tablas transaccionales del `Core`.

## Soporte a lectura derivada
| Necesidad | Realizacion |
|---|---|
| seguimiento semanal de ventas | proyeccion en `reporting-service` desde eventos de `order` |
| seguimiento semanal de reposicion | proyeccion en `reporting-service` desde eventos de `inventory` |
| efectividad de comunicacion | consolidacion en `reporting-service` desde eventos de `notification` |

## Politica de retencion y minimizacion
Retencion por clase de dato y minimizacion de payload sensible se gobiernan por
las decisiones arquitectonicas activas del ciclo y por los `NFR` de
trazabilidad, seguridad y cumplimiento.
