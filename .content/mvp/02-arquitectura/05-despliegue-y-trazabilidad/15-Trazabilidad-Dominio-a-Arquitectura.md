---
title: "Trazabilidad dominio a arquitectura"
linkTitle: "15. Trazabilidad dominio a arquitectura"
weight: 15
url: "/mvp/arquitectura/despliegue-y-trazabilidad/trazabilidad-dominio-arquitectura/"
aliases:
  - "/mvp/arquitectura/trazabilidad/"
---

## Proposito de la seccion
Demostrar que la arquitectura implementa el dominio vigente sin reinterpretar
sus limites, contratos ni clasificacion de subdominios.

## Mapeo de contextos de dominio a realizacion tecnica
| Contexto de dominio | Servicio owner | Evidencia arquitectonica principal |
|---|---|---|
| `directory` | `directory-service` | secciones 05, 06, 07, 10 |
| `catalog` | `catalog-service` | secciones 05, 06, 07, 10 |
| `inventory` | `inventory-service` | secciones 05, 06, 08, 09, 10 |
| `order` | `order-service` | secciones 05, 06, 08, 09, 10 |
| `notification` (`Generic`) | `notification-service` | secciones 05, 06, 08, 09 |
| `reporting` (`Generic`) | `reporting-service` | secciones 05, 06, 08, 09, 10 |
| `identity-access` (tecnico transversal) | capacidad externa | secciones 11 y 12 |

## Mapeo de agregados/comportamientos a componentes
| Referente de dominio | Traduccion arquitectonica |
|---|---|
| `OrganizationContext` / `CountryPolicy` | componentes de resolucion organizacional y politica regional en `directory-service` |
| `CatalogOffer` | componentes de oferta vendible en `catalog-service` |
| `InventoryBalance` | componentes de stock, reserva y disponibilidad en `inventory-service` |
| `Cart` / `Order` (+ `ManualPayment`) | componentes de checkout, pedido y pago manual en `order-service` |
| `NotificationDispatch` | componentes de emision/registro de entrega en `notification-service` |
| snapshot semanal derivado | componentes de proyeccion/reporte en `reporting-service` |

## Mapeo de contratos de dominio a integraciones tecnicas
| Contrato de dominio | Traduccion tecnica |
|---|---|
| contexto organizacional/regional | API `directory` + eventos regionales |
| oferta vendible | API `catalog` + eventos de oferta |
| disponibilidad comprometible | API `inventory` + eventos de stock/disponibilidad |
| compromiso comercial del pedido | API `order` + eventos de pedido/pago |
| comunicacion derivada | consumidor/publicador de `notification` |
| lectura semanal consolidada | consumidores/proyecciones de `reporting` + API read-only |

## Reglas de negocio con impacto arquitectonico
| Regla de dominio | Impacto en arquitectura |
|---|---|
| aislamiento por organizacion operante | validaciones de contexto organizacional en borde y servicio owner |
| promesa comercial coherente | flujo sync `order`-`catalog`-`inventory` antes de mutar pedido |
| pedido trazable en su ciclo | metadata obligatoria en mutaciones/eventos |
| pago manual verificable | componentes de evidencia y estados financieros en `order` |
| operacion regional parametrizada | dependencia obligatoria de politica regional vigente |

## Criterio de preservacion semantica
Arquitectura se considera alineada cuando:
- no hay reasignacion de ownership por conveniencia tecnica,
- `notification` y `reporting` permanecen derivados,
- y `identity-access` se mantiene como capacidad transversal fuera del dominio
  interno.

## Trazabilidad explicita de comandos de dominio en arquitectura
| Comando de dominio | Bounded Context | Servicio | Componente arquitectonico | Integraciones implicadas | Evento(s) producido(s) |
|---|---|---|---|---|---|
| `ConfigurarPoliticaRegional` | `directory` | `directory-service` | `ConfigureRegionalPolicyHandler` | validacion de contexto organizacional + publicacion a broker | `RegionalPolicyConfigured` |
| `AplicarPoliticaRegionalEnOperacion` | `directory` | `directory-service` | `ApplyRegionalPolicyToOperationHandler` | resolucion de politica regional consumida por `order`/`reporting` | `RegionalPolicyAppliedInOperation` |
| `PublicarOfertaDeCatalogo` | `catalog` | `catalog-service` | `PublishCatalogOfferHandler` | publicacion de oferta para `order`/`inventory`/`reporting` | `CatalogOfferPublished` |
| `ActualizarOfertaDeCatalogo` | `catalog` | `catalog-service` | `UpdateCatalogOfferHandler` | actualizacion de oferta para consumidores downstream | `CatalogOfferUpdated` |
| `ActualizarStockOperativo` | `inventory` | `inventory-service` | `UpdateOperationalStockHandler` | salida a `order` (via contrato de disponibilidad), `notification`, `reporting` | `StockUpdated` |
| `RecalcularDisponibilidadComprometible` | `inventory` | `inventory-service` | `RecalculateCommitableAvailabilityHandler` | disponibilidad consumida por `order` y `reporting` | `CommitableAvailabilityRecalculated` |
| `CrearCarrito` | `order` | `order-service` | `CreateCartHandler` | validacion de legitimidad de actor en borde/servicio | `CartCreated` |
| `AjustarItemsDeCarrito` | `order` | `order-service` | `AdjustCartItemsHandler` | consulta de oferta vigente (`catalog`) cuando aplique | `CartItemsAdjusted` |
| `ValidarDisponibilidadEnCheckout` | `order` | `order-service` | `ValidateCheckoutAvailabilityHandler` | `directory` + `catalog` + `inventory` (sync) | `CheckoutAvailabilityValidated` |
| `CrearPedidoDesdeCarrito` | `order` | `order-service` | `CreateOrderFromCartHandler` | `directory` + `catalog` + `inventory` (sync) + salida a `notification`/`reporting` | `OrderCreatedFromValidatedCart` |
| `AjustarPedidoAntesDeCierre` | `order` | `order-service` | `AdjustOrderBeforeCloseHandler` | revalidacion con `inventory` y segun caso `catalog`/`directory` | `OrderAdjustedBeforeClose` |
| `RevalidarConsistenciaDePedidoTrasAjuste` | `order` | `order-service` | `RevalidateOrderConsistencyAfterAdjustmentHandler` | validaciones sync con contratos de soporte core | `OrderConsistencyRevalidated` |
| `ActualizarEstadoOperativoDePedido` | `order` | `order-service` | `UpdateOrderOperationalStatusHandler` | salida a `notification`/`reporting` | `OrderOperationalStatusUpdated` |
| `RegistrarPagoManual` | `order` | `order-service` | `RegisterManualPaymentHandler` | validaciones internas de pedido + salida a `notification`/`reporting` | `ManualPaymentRegistered`, `OrderFinancialStatusUpdated` |
| `EmitirNotificacionDeCambioRelevante` | `notification` | `notification-service` | `EmitRelevantChangeNotificationHandler` | consumo de hechos de `order`/`inventory` | `RelevantChangeNotificationEmitted` |
| `RegistrarEntregaDeNotificacion` | `notification` | `notification-service` | `RecordNotificationDeliveryHandler` | proveedor externo de canal + salida a `reporting` | `NotificationDeliveryRecorded` |
| `GenerarReporteSemanalDeVentas` | `reporting` | `reporting-service` | `GenerateWeeklySalesReportHandler` | consumo de hechos `order/catalog/directory/notification` | `WeeklySalesReportGenerated` |
| `GenerarReporteSemanalDeReposicion` | `reporting` | `reporting-service` | `GenerateWeeklyReplenishmentReportHandler` | consumo de hechos `inventory/order/directory` | `WeeklyReplenishmentReportGenerated` |
