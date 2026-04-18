---
title: "Trazabilidad"
linkTitle: "4. Trazabilidad"
weight: 4
url: "/mvp/dominio/trazabilidad/"
---

## Proposito de la seccion
Hacer explicita la relacion entre Producto y Dominio para responder de forma
verificable por que existe cada evento, comando, regla, agregado y contexto
delimitado (`bounded context`).

## Cadena metodologica aplicada
| Eslabon | Artefacto fuente | Salida en Dominio |
|---|---|---|
| 1 | problema de negocio | fricciones y prioridades de modelado |
| 2 | escenarios de negocio (`EN01`..`EN08`) | hechos de negocio y relaciones entre contextos |
| 3 | casos de uso funcionales (`CUF01`..`CUF17`) | comandos y colaboraciones entre contextos |
| 4 | requisitos funcionales (`FR-001`..`FR-028`) | obligaciones de comportamiento, eventos y contratos |
| 5 | requisitos no funcionales (`NFR-001`..`NFR-011`) | reglas de validez, politicas y restricciones de integracion |
| 6 | dominio consolidado | lenguaje, conceptos, agregados, contextos delimitados (`bounded contexts`), evolucion |

## Problema de negocio -> respuesta de dominio
| Friccion de Producto | Respuesta principal en dominio | Contextos responsables |
|---|---|---|
| validaciones manuales de disponibilidad | `DisponibilidadDeCheckoutValidada` (`CheckoutAvailabilityValidated`) + `DisponibilidadComprometibleRecalculada` (`CommitableAvailabilityRecalculated`) | `pedidos` (`order`), `inventario` (`inventory`) |
| stock no sincronizado con promesa comercial | `StockActualizado` (`StockUpdated`) + recalculo comprometible + revalidacion de pedido | `inventario` (`inventory`), `pedidos` (`order`) |
| baja visibilidad del estado del pedido | hechos de creacion/actualizacion de pedido + lectura derivada de estado vigente | `pedidos` (`order`) |
| soporte manual para seguimiento y pagos | `PagoManualRegistrado` (`ManualPaymentRegistered`) + `EstadoFinancieroDePedidoActualizado` (`OrderFinancialStatusUpdated`) | `pedidos` (`order`) |
| baja preparacion para crecimiento regional | politica regional configurada/aplicada por organizacion y pais | `directorio` (`directory`) |

## Escenario -> eventos -> comandos -> contextos
| Escenario | Eventos de dominio asociados | Comandos asociados | Contextos principales |
|---|---|---|---|
| `EN01` Compra B2B confiable | `OfertaDeCatalogoPublicada` (`CatalogOfferPublished`), `CarritoCreado` (`CartCreated`), `ItemsDeCarritoAjustados` (`CartItemsAdjusted`), `DisponibilidadDeCheckoutValidada` (`CheckoutAvailabilityValidated`) | `PublicarOfertaDeCatalogo` (`PublishCatalogOffer`), `CrearCarrito` (`CreateCart`), `AjustarItemsDeCarrito` (`AdjustCartItems`), `ValidarDisponibilidadEnCheckout` (`ValidateCheckoutAvailability`) | `catalogo` (`catalog`), `pedidos` (`order`), `inventario` (`inventory`) |
| `EN02` Confirmacion con promesa protegida | `PedidoCreadoDesdeCarritoValidado` (`OrderCreatedFromValidatedCart`), `PedidoAjustadoAntesDeCierre` (`OrderAdjustedBeforeClose`), `ConsistenciaDePedidoRevalidada` (`OrderConsistencyRevalidated`) | `CrearPedidoDesdeCarrito` (`CreateOrderFromCart`), `AjustarPedidoAntesDeCierre` (`AdjustOrderBeforeClose`), `RevalidarConsistenciaDePedidoTrasAjuste` (`RevalidateOrderConsistencyAfterAdjustment`) | `pedidos` (`order`), `inventario` (`inventory`) |
| `EN03` Seguimiento visible | `PedidoCreadoDesdeCarritoValidado` (`OrderCreatedFromValidatedCart`), `EstadoOperativoDePedidoActualizado` (`OrderOperationalStatusUpdated`) | consulta derivada de estado vigente (sin comando de mutacion adicional) | `pedidos` (`order`) |
| `EN04` Seguimiento operativo interno | `EstadoOperativoDePedidoActualizado` (`OrderOperationalStatusUpdated`) | `ActualizarEstadoOperativoDePedido` (`UpdateOrderOperationalStatus`) | `pedidos` (`order`) |
| `EN05` Control de pago manual | `PagoManualRegistrado` (`ManualPaymentRegistered`), `EstadoFinancieroDePedidoActualizado` (`OrderFinancialStatusUpdated`) | `RegistrarPagoManual` (`RegisterManualPayment`) | `pedidos` (`order`) |
| `EN06` Coordinacion por notificaciones | `NotificacionDeCambioRelevanteEmitida` (`RelevantChangeNotificationEmitted`), `EntregaDeNotificacionRegistrada` (`NotificationDeliveryRecorded`) | `EmitirNotificacionDeCambioRelevante` (`EmitRelevantChangeNotification`), `RegistrarEntregaDeNotificacion` (`RecordNotificationDelivery`) | `notificaciones` (`notification`) |
| `EN07` Seguimiento semanal ventas/abastecimiento | `ReporteSemanalDeVentasGenerado` (`WeeklySalesReportGenerated`), `ReporteSemanalDeReposicionGenerado` (`WeeklyReplenishmentReportGenerated`) | `GenerarReporteSemanalDeVentas` (`GenerateWeeklySalesReport`), `GenerarReporteSemanalDeReposicion` (`GenerateWeeklyReplenishmentReport`) | `reporteria` (`reporting`) |
| `EN08` Operacion regional preparada | `PoliticaRegionalConfigurada` (`RegionalPolicyConfigured`), `PoliticaRegionalAplicadaEnOperacion` (`RegionalPolicyAppliedInOperation`) | `ConfigurarPoliticaRegional` (`ConfigureRegionalPolicy`), `AplicarPoliticaRegionalEnOperacion` (`ApplyRegionalPolicyToOperation`) | `directorio` (`directory`) |

## FR -> artefactos de dominio
| FR | Comandos y eventos derivados | Agregados / BC involucrados |
|---|---|---|
| `FR-001`..`FR-002` | `PublicarOfertaDeCatalogo` (`PublishCatalogOffer`), `ActualizarOfertaDeCatalogo` (`UpdateCatalogOffer`) -> `OfertaDeCatalogoPublicada` (`CatalogOfferPublished`), `OfertaDeCatalogoActualizada` (`CatalogOfferUpdated`) | `OfertaDeCatalogo` (`CatalogOffer`) / `catalogo` (`catalog`) |
| `FR-003`..`FR-004` | `ValidarDisponibilidadEnCheckout` (`ValidateCheckoutAvailability`) -> `DisponibilidadDeCheckoutValidada` (`CheckoutAvailabilityValidated`) | `Pedido` (`Order`), `BalanceDeInventario` (`InventoryBalance`) / `pedidos` (`order`), `inventario` (`inventory`) |
| `FR-005`..`FR-006` | `CrearCarrito` (`CreateCart`), `AjustarItemsDeCarrito` (`AdjustCartItems`) -> `CarritoCreado` (`CartCreated`), `ItemsDeCarritoAjustados` (`CartItemsAdjusted`) | `Carrito` (`Cart`) / `pedidos` (`order`) |
| `FR-007`..`FR-010` | `CrearPedidoDesdeCarrito` (`CreateOrderFromCart`), `AjustarPedidoAntesDeCierre` (`AdjustOrderBeforeClose`), `RevalidarConsistenciaDePedidoTrasAjuste` (`RevalidateOrderConsistencyAfterAdjustment`) | `Pedido` (`Order`) / `pedidos` (`order`) |
| `FR-011`..`FR-014`, `FR-020` | `ActualizarEstadoOperativoDePedido` (`UpdateOrderOperationalStatus`) + lecturas derivadas de estado/historial | `Pedido` (`Order`) / `pedidos` (`order`) |
| `FR-015`..`FR-017` | `RegistrarPagoManual` (`RegisterManualPayment`) -> eventos financieros de pedido | `Pedido` (`Order`) / `pedidos` (`order`) |
| `FR-018`..`FR-019` | `EmitirNotificacionDeCambioRelevante` (`EmitRelevantChangeNotification`), `RegistrarEntregaDeNotificacion` (`RecordNotificationDelivery`) | `DespachoDeNotificacion` (`NotificationDispatch`) / `notificaciones` (`notification`) |
| `FR-021`..`FR-024` | `ActualizarStockOperativo` (`UpdateOperationalStock`), `RecalcularDisponibilidadComprometible` (`RecalculateCommitableAvailability`) | `BalanceDeInventario` (`InventoryBalance`) / `inventario` (`inventory`) |
| `FR-025`..`FR-026` | `GenerarReporteSemanalDeVentas` (`GenerateWeeklySalesReport`), `GenerarReporteSemanalDeReposicion` (`GenerateWeeklyReplenishmentReport`) | `snapshots` semanales derivados / `reporteria` (`reporting`) |
| `FR-027`..`FR-028` | `ConfigurarPoliticaRegional` (`ConfigureRegionalPolicy`), `AplicarPoliticaRegionalEnOperacion` (`ApplyRegionalPolicyToOperation`) | `PoliticaPorPais` (`CountryPolicy`) / `directorio` (`directory`) |

## NFR -> reglas, politicas y contratos
| NFR | Regla/politica condicionada | Contextos impactados |
|---|---|---|
| `NFR-001`, `NFR-003` | continuidad y respuesta operativa en flujo de nucleo | `catalogo` (`catalog`), `inventario` (`inventory`), `pedidos` (`order`) |
| `NFR-004` | coherencia de promesa comercial | `inventario` (`inventory`), `pedidos` (`order`), `catalogo` (`catalog`) |
| `NFR-005` | aislamiento por organizacion operante | `directorio` (`directory`), `pedidos` (`order`) + capacidad transversal |
| `NFR-006` | trazabilidad auditable de cambios criticos | `pedidos` (`order`), `inventario` (`inventory`), `notificaciones` (`notification`) |
| `NFR-002` | cierre semanal consistente de reportes | `reporteria` (`reporting`) |
| `NFR-010`, `NFR-011` | reglas regionales de retencion/operacion por pais | `directorio` (`directory`) (propietario semantico), consumidores nucleo y generico |

## Trazabilidad de clasificacion de subdominios
| Clasificacion | Justificacion trazable a Producto |
|---|---|
| `Nucleo` (`Core`): `directorio` (`directory`), `catalogo` (`catalog`), `inventario` (`inventory`), `pedidos` (`order`) | concentran el flujo de valor principal del problema y de los FR de compra B2B |
| `Generico` (`Generic`): `notificaciones` (`notification`), `reporteria` (`reporting`) | responden a FR reales (notificar/reportar) como capacidades derivadas |
| Tecnico transversal: `identidad-acceso` (`identity-access`) | habilita acceso/legitimidad, pero no define verdad semantica del negocio interno |

## Cierres tacticos incorporados
| Decision cerrada | Aplicacion en trazabilidad |
|---|---|
| `PagoManual` (`ManualPayment`) dentro de `Pedido` (`Order`) | FR-015..FR-017 trazan sobre el mismo agregado transaccional |
| `DisponibilidadComprometible` (`CommitableAvailability`) dentro de `BalanceDeInventario` (`InventoryBalance`) | FR-003..FR-004 y FR-023..FR-024 trazan sobre una sola frontera de consistencia en `inventario` (`inventory`) |
| `reporteria` (`reporting`) como lectura derivada | FR-025..FR-026 trazan sobre `snapshots` semanales, no sobre agregados transaccionales fuertes |
