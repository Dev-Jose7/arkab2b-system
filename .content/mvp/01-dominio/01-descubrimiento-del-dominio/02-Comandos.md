---
title: "Comandos"
linkTitle: "2. Comandos"
weight: 2
url: "/mvp/dominio/comandos/"
---

## Proposito de la seccion
Identificar las intenciones de negocio que provocan hechos de dominio,
explicitando quien las ejecuta, que resultado buscan y que precondiciones deben
cumplirse.

## Comandos derivados de escenarios, CUF y FR
| ID | Comando | Actor que lo ejecuta | Escenario(s) / CUF / FR | Resultado que busca | Precondiciones de negocio | Evento(s) esperados |
|---|---|---|---|---|---|---|
| CMD-01 | `ConfigurarPoliticaRegional` (`ConfigureRegionalPolicy`) | Negocio / Operacion | EN08, CUF16, FR-027 | definir regla operativa por pais o region | organizacion valida y alcance regional definido | `PoliticaRegionalConfigurada` (`RegionalPolicyConfigured`) |
| CMD-02 | `AplicarPoliticaRegionalEnOperacion` (`ApplyRegionalPolicyToOperation`) | Sistema | EN08, CUF17, FR-028 | aplicar comportamiento condicionado por politica vigente | politica vigente para el `countryCode` de la operacion | `PoliticaRegionalAplicadaEnOperacion` (`RegionalPolicyAppliedInOperation`) |
| CMD-03 | `PublicarOfertaDeCatalogo` (`PublishCatalogOffer`) | Operacion comercial | EN01, EN02, CUF01, FR-001 | publicar oferta vendible para consulta | politica regional aplicable y configuracion de oferta valida | `OfertaDeCatalogoPublicada` (`CatalogOfferPublished`) |
| CMD-04 | `ActualizarOfertaDeCatalogo` (`UpdateCatalogOffer`) | Operacion comercial | EN01, EN02, CUF01, FR-002 | actualizar condiciones de oferta/variante | oferta existente y reglas de vendibilidad vigentes | `OfertaDeCatalogoActualizada` (`CatalogOfferUpdated`) |
| CMD-05 | `ActualizarStockOperativo` (`UpdateOperationalStock`) | Operacion / Inventario | EN07, CUF12, FR-021, FR-022 | alinear stock operativo con realidad fisica | SKU existente y causa operativa valida | `StockActualizado` (`StockUpdated`) |
| CMD-06 | `RecalcularDisponibilidadComprometible` (`RecalculateCommitableAvailability`) | Sistema / Inventario | EN01, EN02, EN07, CUF13, FR-023, FR-024 | recalcular promesa comercial comprometible | cambio previo en stock o reserva | `DisponibilidadComprometibleRecalculada` (`CommitableAvailabilityRecalculated`) |
| CMD-07 | `CrearCarrito` (`CreateCart`) | Comprador B2B | EN01, CUF03, FR-005 | abrir intencion de compra editable | actor legitimado y aislamiento por organizacion valido | `CarritoCreado` (`CartCreated`) |
| CMD-08 | `AjustarItemsDeCarrito` (`AdjustCartItems`) | Comprador B2B | EN01, CUF03, FR-006 | agregar, actualizar o retirar lineas del carrito | carrito activo y oferta vigente | `ItemsDeCarritoAjustados` (`CartItemsAdjusted`) |
| CMD-09 | `ValidarDisponibilidadEnCheckout` (`ValidateCheckoutAvailability`) | Sistema | EN01, EN02, CUF02, FR-004 | validar si la compra puede comprometerse | oferta vendible + disponibilidad comprometible + contexto organizacional valido | `DisponibilidadDeCheckoutValidada` (`CheckoutAvailabilityValidated`) |
| CMD-10 | `CrearPedidoDesdeCarrito` (`CreateOrderFromCart`) | Comprador B2B / Sistema | EN01, EN02, CUF04, FR-007, FR-008 | formalizar pedido desde carrito validado | checkout aprobado y consistencia vigente | `PedidoCreadoDesdeCarritoValidado` (`OrderCreatedFromValidatedCart`) |
| CMD-11 | `AjustarPedidoAntesDeCierre` (`AdjustOrderBeforeClose`) | Comprador B2B / Operacion comercial | EN02, CUF05, FR-009 | corregir pedido dentro de ventana permitida | pedido ajustable segun estado y ventana | `PedidoAjustadoAntesDeCierre` (`OrderAdjustedBeforeClose`) |
| CMD-12 | `RevalidarConsistenciaDePedidoTrasAjuste` (`RevalidateOrderConsistencyAfterAdjustment`) | Sistema | EN02, CUF05, FR-010 | asegurar consistencia posterior al ajuste | ajuste aplicado y fuentes de validacion disponibles | `ConsistenciaDePedidoRevalidada` (`OrderConsistencyRevalidated`) |
| CMD-13 | `ActualizarEstadoOperativoDePedido` (`UpdateOrderOperationalStatus`) | Operacion Arka | EN04, CUF07, FR-013, FR-014 | reflejar avance operativo real del pedido | transicion de estado permitida | `EstadoOperativoDePedidoActualizado` (`OrderOperationalStatusUpdated`) |
| CMD-14 | `RegistrarPagoManual` (`RegisterManualPayment`) | Operacion comercial / financiera | EN05, CUF08, CUF09, FR-015, FR-016, FR-017 | registrar pago manual y su impacto financiero | pedido existente, referencia valida y evidencia trazable | `PagoManualRegistrado` (`ManualPaymentRegistered`), `EstadoFinancieroDePedidoActualizado` (`OrderFinancialStatusUpdated`) |
| CMD-15 | `EmitirNotificacionDeCambioRelevante` (`EmitRelevantChangeNotification`) | Sistema | EN06, CUF10, FR-018 | informar cambio relevante a actores interesados | evento de negocio relevante confirmado | `NotificacionDeCambioRelevanteEmitida` (`RelevantChangeNotificationEmitted`) |
| CMD-16 | `RegistrarEntregaDeNotificacion` (`RecordNotificationDelivery`) | Sistema | EN06, CUF10, FR-019 | registrar resultado de envio | intento de entrega ejecutado | `EntregaDeNotificacionRegistrada` (`NotificationDeliveryRecorded`) |
| CMD-17 | `GenerarReporteSemanalDeVentas` (`GenerateWeeklySalesReport`) | Sistema | EN07, CUF14, FR-025 | publicar consolidado semanal comercial | ventana semanal cerrada y datos consistentes | `ReporteSemanalDeVentasGenerado` (`WeeklySalesReportGenerated`) |
| CMD-18 | `GenerarReporteSemanalDeReposicion` (`GenerateWeeklyReplenishmentReport`) | Sistema | EN07, CUF15, FR-026 | publicar consolidado semanal de reposicion | ventana semanal cerrada y datos de stock/pedido consistentes | `ReporteSemanalDeReposicionGenerado` (`WeeklyReplenishmentReportGenerated`) |

## Capacidades de consulta de Producto (no son comandos de mutacion)
| FR de consulta | Tratamiento en dominio |
|---|---|
| FR-001, FR-002 | se soportan con lectura de oferta publicada |
| FR-003 | se soporta con lectura de disponibilidad comprometible publicada |
| FR-011, FR-012 | se soportan con lectura de estado vigente del pedido |
| FR-017 | se soporta con lectura de estado financiero del pedido |
| FR-020 | se soporta con lectura derivada de historial de cambios relevantes |

## Lectura metodologica
Los comandos expresan intenciones de negocio u operacion del sistema y no
endpoints tecnicos. Las consultas se modelan como lectura de verdad publicada y
no como mutaciones.

La asignacion de comandos a `contextos delimitados` (`bounded contexts`) se
formaliza en la seccion 5.
