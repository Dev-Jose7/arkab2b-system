---
title: "Eventos de dominio"
linkTitle: "1. Eventos de dominio"
weight: 1
url: "/mvp/dominio/eventos-de-dominio/"
aliases:
  - "/mvp/dominio/eventos-dominio/"
---

## Proposito de la seccion
Identificar los hechos significativos que ocurren en el negocio de ArkaB2B,
partiendo de escenarios, casos de uso funcionales, `FR` y `NFR` del `MVP`.

## Eventos descubiertos desde Producto
| ID | Evento de dominio | Escenario(s) | CUF / FR de origen | Cambio que expresa |
|---|---|---|---|---|
| EV-01 | `PoliticaRegionalConfigurada` (`RegionalPolicyConfigured`) | EN08 | CUF16, FR-027 | queda definida una politica operativa por pais o region |
| EV-02 | `PoliticaRegionalAplicadaEnOperacion` (`RegionalPolicyAppliedInOperation`) | EN08 | CUF17, FR-028 | una operacion se ejecuta con politica regional vigente |
| EV-03 | `OfertaDeCatalogoPublicada` (`CatalogOfferPublished`) | EN01, EN02 | CUF01, FR-001, FR-002 | una oferta vendible queda publicada para consulta B2B |
| EV-04 | `OfertaDeCatalogoActualizada` (`CatalogOfferUpdated`) | EN01, EN02 | CUF01, FR-001, FR-002 | cambian condiciones de oferta consultable |
| EV-05 | `StockActualizado` (`StockUpdated`) | EN07, EN01, EN02 | CUF12, FR-021, FR-022 | cambia el stock operativo real |
| EV-06 | `DisponibilidadComprometibleRecalculada` (`CommitableAvailabilityRecalculated`) | EN01, EN02, EN07 | CUF13, FR-023, FR-024 | cambia la disponibilidad comprometible |
| EV-07 | `CarritoCreado` (`CartCreated`) | EN01 | CUF03, FR-005 | nace una intencion de compra editable |
| EV-08 | `ItemsDeCarritoAjustados` (`CartItemsAdjusted`) | EN01 | CUF03, FR-006 | se ajusta contenido del carrito |
| EV-09 | `DisponibilidadDeCheckoutValidada` (`CheckoutAvailabilityValidated`) | EN01, EN02 | CUF02, FR-004 | se confirma si la compra es comprometible |
| EV-10 | `PedidoCreadoDesdeCarritoValidado` (`OrderCreatedFromValidatedCart`) | EN01, EN02 | CUF04, FR-007 | la intencion validada se formaliza como pedido |
| EV-12 | `PedidoAjustadoAntesDeCierre` (`OrderAdjustedBeforeClose`) | EN02 | CUF05, FR-009 | se modifica pedido dentro de ventana permitida |
| EV-13 | `ConsistenciaDePedidoRevalidada` (`OrderConsistencyRevalidated`) | EN02 | CUF05, FR-010 | se valida consistencia despues de ajuste |
| EV-14 | `EstadoOperativoDePedidoActualizado` (`OrderOperationalStatusUpdated`) | EN04 | CUF07, FR-013, FR-014 | cambia el estado operativo del pedido |
| EV-16 | `PagoManualRegistrado` (`ManualPaymentRegistered`) | EN05 | CUF08, FR-015 | se registra pago manual vinculado al pedido |
| EV-18 | `EstadoFinancieroDePedidoActualizado` (`OrderFinancialStatusUpdated`) | EN05 | CUF09, FR-017 | cambia estado financiero del pedido |
| EV-19 | `NotificacionDeCambioRelevanteEmitida` (`RelevantChangeNotificationEmitted`) | EN06 | CUF10, FR-018 | se emite notificacion por cambio relevante |
| EV-20 | `EntregaDeNotificacionRegistrada` (`NotificationDeliveryRecorded`) | EN06 | CUF10, FR-019 | queda trazado el resultado de entrega de notificacion |
| EV-22 | `ReporteSemanalDeVentasGenerado` (`WeeklySalesReportGenerated`) | EN07 | CUF14, FR-025 | se publica reporte semanal de ventas |
| EV-23 | `ReporteSemanalDeReposicionGenerado` (`WeeklyReplenishmentReportGenerated`) | EN07 | CUF15, FR-026 | se publica reporte semanal de abastecimiento/reposicion |

## Densidad de eventos en subdominios genericos
| Subdominio | Regla aplicada |
|---|---|
| `notificaciones` (`notification`) | solo modela hechos de comunicacion derivada (emision y resultado de entrega) |
| `reporteria` (`reporting`) | solo modela hechos de publicacion de `snapshots` semanales |

## Cobertura de fricciones del problema de negocio
| Friccion de Producto | Eventos que la abordan |
|---|---|
| validaciones manuales de disponibilidad | `DisponibilidadDeCheckoutValidada` (`CheckoutAvailabilityValidated`), `DisponibilidadComprometibleRecalculada` (`CommitableAvailabilityRecalculated`), `PedidoCreadoDesdeCarritoValidado` (`OrderCreatedFromValidatedCart`) |
| stock no sincronizado con promesa comercial | `StockActualizado` (`StockUpdated`), `DisponibilidadComprometibleRecalculada` (`CommitableAvailabilityRecalculated`), `ConsistenciaDePedidoRevalidada` (`OrderConsistencyRevalidated`) |
| baja visibilidad del estado del pedido | `PedidoCreadoDesdeCarritoValidado` (`OrderCreatedFromValidatedCart`), `EstadoOperativoDePedidoActualizado` (`OrderOperationalStatusUpdated`) |
| soporte manual para seguimiento y pagos | `PagoManualRegistrado` (`ManualPaymentRegistered`), `EstadoFinancieroDePedidoActualizado` (`OrderFinancialStatusUpdated`) |
| baja preparacion para crecimiento regional | `PoliticaRegionalConfigurada` (`RegionalPolicyConfigured`), `PoliticaRegionalAplicadaEnOperacion` (`RegionalPolicyAppliedInOperation`) |

## Depuracion de pureza de eventos aplicada
| Elemento retirado del catalogo de eventos | Tratamiento correcto |
|---|---|
| `TrazaDePedidoRegistrada` (`OrderTraceRegistered`) | queda como evidencia derivada de `PedidoCreadoDesdeCarritoValidado` (`OrderCreatedFromValidatedCart`) |
| `VisibilidadDePedidoActualizada` (`OrderVisibilityUpdated`) | queda como lectura derivada para consulta de estado vigente |
| `EvidenciaDePagoManualRegistrada` (`ManualPaymentEvidenceRecorded`) | queda como evidencia derivada del registro de pago manual |
| `HistorialDeCambiosDePedidoRegistrado` (`OrderChangeHistoryRecorded`) | queda como lectura derivada de cambios operativos confirmados |

## Regla de orden metodologico
La delimitacion semantica, propiedad por contexto y clasificacion de subdominio
de estos eventos se formaliza recien en la seccion 5 `contextos delimitados`
(`bounded contexts`).

Un evento de dominio permanece solo si expresa un hecho significativo del
negocio; lecturas, historial, evidencia documental y proyecciones no se
modelan como eventos primarios.
