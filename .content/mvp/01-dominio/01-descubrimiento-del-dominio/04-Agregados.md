---
title: "Agregados"
linkTitle: "4. Agregados"
weight: 4
url: "/mvp/dominio/agregados/"
---

## Proposito de la seccion
Definir donde vive la consistencia del dominio a partir de eventos, comandos,
reglas y politicas descubiertas desde Producto.

## Agregados propuestos por consistencia de negocio
| Agregado (raiz) | Grupo de cambios consistente | Invariantes que protege | Comandos que recibe | Eventos que emite | Fuera de su frontera | Referencias externas por identidad |
|---|---|---|---|---|---|---|
| `ContextoDeOrganizacion` (`OrganizationContext`) | estado operativo de organizacion y relacion con actor habilitado | una operacion solo existe para una organizacion valida y activa | `ConfigurarPoliticaRegional` (`ConfigureRegionalPolicy`) (en colaboracion), validaciones de aislamiento para comandos de nucleo | `PoliticaRegionalConfigurada` (`RegionalPolicyConfigured`) (en colaboracion con `PoliticaPorPais` (`CountryPolicy`)) | legitimidad de actor de capacidad transversal, stock, precio, estado de pedido | `organizationId`, `accountId` |
| `PoliticaPorPais` (`CountryPolicy`) | configuracion regional aplicable por pais | existe una politica vigente por organizacion y pais para operaciones criticas | `ConfigurarPoliticaRegional` (`ConfigureRegionalPolicy`), `AplicarPoliticaRegionalEnOperacion` (`ApplyRegionalPolicyToOperation`) | `PoliticaRegionalConfigurada` (`RegionalPolicyConfigured`), `PoliticaRegionalAplicadaEnOperacion` (`RegionalPolicyAppliedInOperation`) | logica de carrito/pedido/stock | `organizationId`, `countryCode`, `policyVersion` |
| `OfertaDeCatalogo` (`CatalogOffer`) | oferta vendible consultable por comprador B2B | una oferta publicada mantiene coherencia entre producto, variante y condiciones comerciales | `PublicarOfertaDeCatalogo` (`PublishCatalogOffer`), `ActualizarOfertaDeCatalogo` (`UpdateCatalogOffer`) | `OfertaDeCatalogoPublicada` (`CatalogOfferPublished`), `OfertaDeCatalogoActualizada` (`CatalogOfferUpdated`) | disponibilidad fisica, estados de pedido, pagos | `offerId`, `productId`, `variantId` |
| `BalanceDeInventario` (`InventoryBalance`) | stock operativo, reserva y disponibilidad comprometible | no se publica promesa comercial inconsistente con stock y reservas vigentes | `ActualizarStockOperativo` (`UpdateOperationalStock`), `RecalcularDisponibilidadComprometible` (`RecalculateCommitableAvailability`) | `StockActualizado` (`StockUpdated`), `DisponibilidadComprometibleRecalculada` (`CommitableAvailabilityRecalculated`) | pricing, estado comercial del pedido | `variantId`, `warehouseId`, `organizationId` |
| `Carrito` (`Cart`) | intencion de compra editable previa a confirmacion | carrito existe bajo aislamiento organizacional y actor habilitado | `CrearCarrito` (`CreateCart`), `AjustarItemsDeCarrito` (`AdjustCartItems`) | `CarritoCreado` (`CartCreated`), `ItemsDeCarritoAjustados` (`CartItemsAdjusted`) | definicion de oferta y disponibilidad externas | `cartId`, `organizationId`, `accountId` |
| `Pedido` (`Order`) | formalizacion del compromiso comercial, su ciclo de estado y pago manual MVP | pedido nace solo desde carrito validado, evoluciona por transiciones permitidas y no admite actualizacion financiera sin registro manual valido | `ValidarDisponibilidadEnCheckout` (`ValidateCheckoutAvailability`), `CrearPedidoDesdeCarrito` (`CreateOrderFromCart`), `AjustarPedidoAntesDeCierre` (`AdjustOrderBeforeClose`), `RevalidarConsistenciaDePedidoTrasAjuste` (`RevalidateOrderConsistencyAfterAdjustment`), `ActualizarEstadoOperativoDePedido` (`UpdateOrderOperationalStatus`), `RegistrarPagoManual` (`RegisterManualPayment`) | `DisponibilidadDeCheckoutValidada` (`CheckoutAvailabilityValidated`), `PedidoCreadoDesdeCarritoValidado` (`OrderCreatedFromValidatedCart`), `PedidoAjustadoAntesDeCierre` (`OrderAdjustedBeforeClose`), `ConsistenciaDePedidoRevalidada` (`OrderConsistencyRevalidated`), `EstadoOperativoDePedidoActualizado` (`OrderOperationalStatusUpdated`), `PagoManualRegistrado` (`ManualPaymentRegistered`), `EstadoFinancieroDePedidoActualizado` (`OrderFinancialStatusUpdated`) | mutaciones de stock, reglas regionales externas y entrega de notificaciones | `orderId`, `cartId`, `organizationId`, `accountId` |
| `DespachoDeNotificacion` (`NotificationDispatch`) | emision y registro de entrega de notificaciones derivadas | una entrega registrada siempre se asocia a cambio relevante confirmado | `EmitirNotificacionDeCambioRelevante` (`EmitRelevantChangeNotification`), `RegistrarEntregaDeNotificacion` (`RecordNotificationDelivery`) | `NotificacionDeCambioRelevanteEmitida` (`RelevantChangeNotificationEmitted`), `EntregaDeNotificacionRegistrada` (`NotificationDeliveryRecorded`) | verdad transaccional principal de compra, stock y pedido | `notificationId`, `orderId` |

## Decisiones tacticas cerradas
| Decision | Cierre aplicado | Criterio DDD usado |
|---|---|---|
| `PagoManual` (`ManualPayment`) | vive dentro del agregado `Pedido` (`Order`) | comparte ciclo de vida, invariantes y consistencia financiera con el pedido; no justifica agregado transaccional separado en `MVP` |
| `DisponibilidadComprometible` (`CommitableAvailability`) | vive dentro del agregado `BalanceDeInventario` (`InventoryBalance`) | la verdad de stock, reserva y disponibilidad debe cerrarse en una sola frontera de consistencia |
| `reporteria` (`reporting`) | se modela como `snapshot`/lectura derivada | publica consolidacion semanal y no corrige verdad transaccional de nucleo; no se endurece como agregado fuerte |

## Limites transaccionales del modelo
| Regla de limite | Implicacion |
|---|---|
| consistencia se cierra dentro de cada agregado | no se usa transaccion distribuida entre agregados |
| colaboracion entre agregados y fuentes externas ocurre por contratos semanticos | se evita acoplamiento directo de estados internos |
| capacidades derivadas no corrigen transacciones principales | reaccionan y publican salida derivada (`notificaciones` y `reporteria`) |
| capacidad transversal de identidad queda fuera de agregados internos | se consume solo para validar legitimidad de actor |

## Regla de orden metodologico
La asignacion de estos agregados a `contextos delimitados` (`bounded contexts`)
se formaliza recien en la seccion 5.
