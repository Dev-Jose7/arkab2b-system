---
title: "Contratos de integracion"
linkTitle: "7. Contratos de integracion"
weight: 7
url: "/mvp/arquitectura/integracion-y-consistencia/contratos-de-integracion/"
---

## Proposito de la seccion
Formalizar interfaces y eventos que cruzan fronteras de contexto, preservando
ownership semantico y evitando mezcla de verdades.

## Politica contractual comun
| Regla | Aplicacion |
|---|---|
| versionado explicito | APIs por major (`/api/v1`) y eventos por `eventVersion` + compatibilidad backward en cambios no breaking |
| idempotencia en mutaciones | mutaciones HTTP con `Idempotency-Key`; consumidores de eventos con dedupe por `eventId + consumerRef` |
| envelope canonico de eventos | `eventId`, `eventType`, `eventVersion`, `occurredAt`, `producer`, `tenantId`, `traceId`, `correlationId`, `payload` |
| entrega asincrona confiable | productores con `outbox`; consumidores con retry/backoff y `DLQ` para no recuperables |
| contrato semantico primero | los contratos publican significado de negocio; no se exponen estructuras internas de tablas |

## Contratos API por contexto/servicio
| Servicio | Familias de API expuestas | Comandos de dominio soportados | Regla operacional del contrato |
|---|---|---|---|
| `directory-service` | `organizations`, `operational-country-settings`, `checkout-address-validations` | `ConfigurarPoliticaRegional`, `AplicarPoliticaRegionalEnOperacion` | `order` y `reporting` dependen de politica regional vigente; sin politica se bloquea operacion sensible |
| `catalog-service` | `catalog/search`, `products/variants`, `variants/resolve`, `prices` | `PublicarOfertaDeCatalogo`, `ActualizarOfertaDeCatalogo` | mutaciones de oferta/precio idempotentes; resolucion de variante estable para checkout |
| `inventory-service` | `stock`, `reservations`, `availability`, `checkout/validate-reservations` | `ActualizarStockOperativo`, `RecalcularDisponibilidadComprometible` | reserva/confirmacion con consistencia local e idempotencia write-side |
| `order-service` | `cart`, `checkout`, `orders`, `payments/manual`, `order-status` | `CrearCarrito`, `AjustarItemsDeCarrito`, `ValidarDisponibilidadEnCheckout`, `CrearPedidoDesdeCarrito`, `AjustarPedidoAntesDeCierre`, `RevalidarConsistenciaDePedidoTrasAjuste`, `ActualizarEstadoOperativoDePedido`, `RegistrarPagoManual` | comandos criticos validan precondiciones sync con `directory`, `catalog`, `inventory` |
| `notification-service` | API interna `notifications/requests/dispatch/retry/discard`, callbacks de proveedor, consultas operativas | `EmitirNotificacionDeCambioRelevante`, `RegistrarEntregaDeNotificacion` | solo materializa comunicacion derivada, sin mutar estado transaccional `Core` |
| `reporting-service` | API read-only `reports/*`, API interna `reports/rebuild` y `weekly/generate` | `GenerarReporteSemanalDeVentas`, `GenerarReporteSemanalDeReposicion` | consume hechos confirmados y publica snapshots; no corrige verdad `Core` |

## Contratos de eventos por contexto/servicio
| Servicio | Eventos publicados (familia semantica) | Eventos consumidos (familia semantica) | Clave de particion sugerida |
|---|---|---|---|
| `directory-service` | cambios de politica regional y contexto organizacional | n/a en camino funcional principal del `MVP` | `organizationId` / `organizationId:countryCode` |
| `catalog-service` | oferta publicada/actualizada y precio vigente actualizado | hechos de reconciliacion operativa desde `inventory` cuando aplica | `productId` / `variantId` |
| `inventory-service` | stock actualizado, reserva/confirmacion/liberacion, disponibilidad recalculada | cambios de oferta desde `catalog` para reconciliacion de SKU | `stockId` / `reservationId` / `sku` |
| `order-service` | carrito, pedido, estado operativo y pago manual actualizado | oferta de `catalog`, disponibilidad de `inventory`, politica regional de `directory` | `orderId` / `cartId` |
| `notification-service` | notificacion emitida, entrega registrada, entrega fallida/descartada | cambios relevantes publicados por `order` e `inventory` | `notificationId` |
| `reporting-service` | reporte semanal generado y estado de actualizacion de snapshot | hechos de `Core` y `notification` | `tenantId:weekId:reportType` |

## Contratos que soportan ejecucion de comandos de dominio
| Comando de dominio | Servicio handler | Contratos sync requeridos | Evento(s) de salida | Consumidores esperados |
|---|---|---|---|---|
| `ConfigurarPoliticaRegional` | `directory-service` | contrato interno de organizacion/politica regional | `RegionalPolicyConfigured` | `order`, `reporting` |
| `AplicarPoliticaRegionalEnOperacion` | `directory-service` (owner) | API de resolucion de politica regional | `RegionalPolicyAppliedInOperation` | `order`, `reporting` |
| `PublicarOfertaDeCatalogo` | `catalog-service` | contrato interno de catalogo | `CatalogOfferPublished` | `order`, `inventory`, `reporting` |
| `ActualizarOfertaDeCatalogo` | `catalog-service` | contrato interno de catalogo/precio | `CatalogOfferUpdated` | `order`, `inventory`, `reporting` |
| `ActualizarStockOperativo` | `inventory-service` | contrato interno de stock/movimiento | `StockUpdated` | `order`, `reporting`, `notification` (si hay alerta) |
| `RecalcularDisponibilidadComprometible` | `inventory-service` | contrato interno de stock+reserva | `CommitableAvailabilityRecalculated` | `order`, `reporting` |
| `CrearCarrito` | `order-service` | contrato local de carrito + legitimidad de actor transversal | `CartCreated` | `reporting` (derivado) |
| `AjustarItemsDeCarrito` | `order-service` | contrato de oferta vigente (`catalog`) + reserva (`inventory`) cuando aplica | `CartItemsAdjusted` | `reporting` (derivado) |
| `ValidarDisponibilidadEnCheckout` | `order-service` | `directory` + `catalog` + `inventory` | `CheckoutAvailabilityValidated` | `reporting` (derivado) |
| `CrearPedidoDesdeCarrito` | `order-service` | `directory` + `catalog` + `inventory` | `OrderCreatedFromValidatedCart` | `notification`, `reporting` |
| `AjustarPedidoAntesDeCierre` | `order-service` | `inventory` (y segun caso `catalog`/`directory`) | `OrderAdjustedBeforeClose` | `notification`, `reporting` |
| `RevalidarConsistenciaDePedidoTrasAjuste` | `order-service` | contratos de soporte `Core` para revalidacion | `OrderConsistencyRevalidated` | `notification`, `reporting` |
| `ActualizarEstadoOperativoDePedido` | `order-service` | contrato local de pedido/estado | `OrderOperationalStatusUpdated` | `notification`, `reporting` |
| `RegistrarPagoManual` | `order-service` | contrato local de pedido/pago manual | `ManualPaymentRegistered`, `OrderFinancialStatusUpdated` | `notification`, `reporting` |
| `EmitirNotificacionDeCambioRelevante` | `notification-service` | eventos de cambio relevante publicados por `order`/`inventory` | `RelevantChangeNotificationEmitted` | `reporting` |
| `RegistrarEntregaDeNotificacion` | `notification-service` | callback/resultado del proveedor externo + estado local | `NotificationDeliveryRecorded` | `reporting` |
| `GenerarReporteSemanalDeVentas` | `reporting-service` | hechos de `order`, `catalog`, `directory`, `notification` | `WeeklySalesReportGenerated` | operacion comercial / `notification` |
| `GenerarReporteSemanalDeReposicion` | `reporting-service` | hechos de `inventory`, `order`, `directory` | `WeeklyReplenishmentReportGenerated` | operacion / abastecimiento |

## Solicitudes cross-context y datos que viajan
| Relacion | Tipo | Datos que viajan | Datos que no deben cruzar |
|---|---|---|---|
| `order` -> `directory` | sync | `organizationId`, `countryCode`, version de politica aplicable | estructuras internas de perfiles/contactos no requeridas |
| `order` -> `catalog` | sync | `offerId`/`variantId`, condicion vendible y precio vigente | tablas internas de taxonomia y scheduling de catalogo |
| `order` -> `inventory` | sync | SKU, cantidad, disponibilidad/reserva resultante | ledger completo de movimientos de stock |
| `Core` -> `notification` | async | identificador de hecho relevante y contexto minimo de comunicacion | estado transaccional completo de agregados `Core` |
| `Core` + `notification` -> `reporting` | async | hechos confirmados para proyeccion/snapshot | comandos de correccion de estados transaccionales |

## Calidad de contratos y verificacion
| Practica | Cobertura minima |
|---|---|
| contract test de APIs | validacion de esquema, codigos de error y reglas de versionado por servicio owner |
| contract test de eventos | validacion de envelope canonico y campos minimos de payload por familia de evento |
| verificacion de idempotencia | misma llave + mismo payload no duplica efecto; misma llave + payload distinto produce conflicto |
| verificacion de resiliencia | retry/backoff, dedupe y `DLQ` operativos sin perdida de hechos confirmados |

## Contrato con capacidad transversal
`identity-access` se consume como contrato tecnico de legitimidad de actor y no
como contrato de dominio interno. Sus detalles viven en seguridad
arquitectonica y capacidades transversales.
