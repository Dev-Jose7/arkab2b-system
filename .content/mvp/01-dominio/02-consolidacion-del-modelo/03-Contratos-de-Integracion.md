---
title: "Contratos de integracion"
linkTitle: "3. Contratos de integracion"
weight: 3
url: "/mvp/dominio/contratos-de-integracion/"
aliases:
  - "/mvp/dominio/contratos-integracion/"
---

## Proposito de la seccion
Formalizar como se conectan los `contextos delimitados` (`bounded contexts`)
entre si y con capacidades externas, respetando propiedad semantica y sin
mezclar detalles de infraestructura.

## Contratos semanticos entre contextos delimitados (`bounded contexts`)
| Contrato semantico | Publicado por | Consumido por | Que expone | Frontera que debe respetarse |
|---|---|---|---|---|
| `Contexto organizacional y regional` | `directorio` (`directory`) | `catalogo` (`catalog`), `pedidos` (`order`), `reporteria` (`reporting`) | organizacion valida y politica regional aplicable | los consumidores no redefinen politica regional |
| `Oferta vendible` | `catalogo` (`catalog`) | `pedidos` (`order`), `inventario` (`inventory`), `reporteria` (`reporting`) | oferta publicada para compra: `OfertaDeCatalogo` (`CatalogOffer`) | consumidores no redefinen condiciones comerciales |
| `Disponibilidad comprometible` | `inventario` (`inventory`) | `pedidos` (`order`), `reporteria` (`reporting`) | disponibilidad comprometible para compra | `pedidos` (`order`) no recalcula propiedad semantica de inventario |
| `Compromiso comercial de pedido` | `pedidos` (`order`) | `notificaciones` (`notification`), `reporteria` (`reporting`) | estado y cambios relevantes del pedido | consumidores derivados no mutan estado de pedido |
| `Comunicacion derivada` | `notificaciones` (`notification`) | `reporteria` (`reporting`), operacion | emision y resultado de entrega de notificacion | no se usa para corregir transacciones de nucleo ni para reemplazar historial |
| `Lectura semanal consolidada` | `reporteria` (`reporting`) | negocio, ventas, inventario, operacion | reportes semanales versionados | no reemplaza verdad transaccional de nucleo |

## Publicacion y consumo de eventos por contexto
| Contexto | Publica | Consume |
|---|---|---|
| `directorio` (`directory`) | `PoliticaRegionalConfigurada` (`RegionalPolicyConfigured`), `PoliticaRegionalAplicadaEnOperacion` (`RegionalPolicyAppliedInOperation`) | legitimidad de actor publicada por capacidad transversal |
| `catalogo` (`catalog`) | `OfertaDeCatalogoPublicada` (`CatalogOfferPublished`), `OfertaDeCatalogoActualizada` (`CatalogOfferUpdated`) | `PoliticaRegionalAplicadaEnOperacion` (`RegionalPolicyAppliedInOperation`) |
| `inventario` (`inventory`) | `StockActualizado` (`StockUpdated`), `DisponibilidadComprometibleRecalculada` (`CommitableAvailabilityRecalculated`) | cambios de oferta que afectan promesa comercial |
| `pedidos` (`order`) | `CarritoCreado` (`CartCreated`), `ItemsDeCarritoAjustados` (`CartItemsAdjusted`), `DisponibilidadDeCheckoutValidada` (`CheckoutAvailabilityValidated`), `PedidoCreadoDesdeCarritoValidado` (`OrderCreatedFromValidatedCart`), `EstadoOperativoDePedidoActualizado` (`OrderOperationalStatusUpdated`), `PagoManualRegistrado` (`ManualPaymentRegistered`), `EstadoFinancieroDePedidoActualizado` (`OrderFinancialStatusUpdated`) | politica regional, oferta publicada, disponibilidad comprometible |
| `notificaciones` (`notification`) | `NotificacionDeCambioRelevanteEmitida` (`RelevantChangeNotificationEmitted`), `EntregaDeNotificacionRegistrada` (`NotificationDeliveryRecorded`) | cambios relevantes de `pedidos` (`order`) |
| `reporteria` (`reporting`) | `ReporteSemanalDeVentasGenerado` (`WeeklySalesReportGenerated`), `ReporteSemanalDeReposicionGenerado` (`WeeklyReplenishmentReportGenerated`) | hechos publicados por `directorio` (`directory`), `catalogo` (`catalog`), `inventario` (`inventory`), `pedidos` (`order`), `notificaciones` (`notification`) |

## Contratos con capacidad tecnica transversal
| Capacidad externa | Contextos consumidores | Decision que habilita | Limite de integracion |
|---|---|---|---|
| `identidad-acceso` (`identity-access`) | `directorio` (`directory`), `pedidos` (`order`), `notificaciones` (`notification`) | validar legitimidad de actor para operar o consultar | no transfiere propiedad semantica de negocio |

## Regla de anti-corrupcion por tipo de subdominio
| Tipo | Regla de proteccion |
|---|---|
| `Nucleo` (`Core`) | no aceptar que un consumidor externo reinterprete su verdad primaria |
| `Generico` (`Generic`) | consumir eventos de nucleo y publicar salida derivada minima sin mutacion inversa |
| `Tecnico transversal` | tratar autenticacion/autorizacion como contrato externo, no como modelo interno |

## Regla de integracion para arquitectura
La implementacion tecnica debe respetar estos contratos semanticos como entrada
obligatoria para APIs, eventos, colas y adaptadores.
