---
title: "Lenguaje ubicuo"
linkTitle: "1. Lenguaje ubicuo"
weight: 1
url: "/mvp/dominio/lenguaje-ubicuo/"
---

## Proposito de la seccion
Consolidar el vocabulario oficial del dominio a partir de Producto y del
bloque de descubrimiento, evitando ambiguedad entre negocio, analisis, diseno y
arquitectura.

## Terminos canonicos del dominio
| Termino canonico (ES/EN) | Significado oficial | Contexto propietario | No confundir con |
|---|---|---|---|
| `Organizacion` (`Organization`) | unidad empresarial que opera en ArkaB2B | `directorio` (`directory`) | `Cuenta` (`Account`) de capacidad transversal |
| `Politica regional` (`CountryPolicy`) | regla vigente por organizacion y pais para operar | `directorio` (`directory`) | configuracion tecnica global |
| `Oferta de catalogo` (`CatalogOffer`) | oferta vendible publicada para compra B2B | `catalogo` (`catalog`) | stock fisico |
| `Producto` (`Product`) | identidad comercial base de la oferta | `catalogo` (`catalog`) | pedido |
| `Variante` (`Variant`) | version vendible concreta de producto | `catalogo` (`catalog`) | `BalanceDeInventario` (`InventoryBalance`) |
| `Stock operativo` (`OperationalStock`) | cantidad fisica registrada para operacion | `inventario` (`inventory`) | disponibilidad comprometible |
| `Disponibilidad comprometible` (`CommitableAvailability`) | cantidad que puede prometerse comercialmente | `inventario` (`inventory`) | vendibilidad de oferta |
| `Carrito` (`Cart`) | intencion de compra editable previa al pedido | `pedidos` (`order`) | pedido confirmado |
| `Checkout validado` (`ValidatedCheckout`) | validacion previa de consistencia para crear pedido | `pedidos` (`order`) | endpoint tecnico |
| `Pedido` (`Order`) | compromiso comercial creado desde carrito validado | `pedidos` (`order`) | reserva de inventario |
| `Estado de pedido` (`OrderStatus`) | etapa visible y operativa del ciclo del pedido | `pedidos` (`order`) | estado de entrega de notificacion |
| `Pago manual` (`ManualPayment`) | registro manual de pago asociado al pedido | `pedidos` (`order`) | pasarela online |
| `Estado financiero` (`FinancialStatus`) | situacion de pago del pedido en MVP | `pedidos` (`order`) | estado logistico |
| `Historial de cambios` (`OrderChangeHistory`) | lectura derivada cronologica de cambios relevantes del pedido | `pedidos` (`order`) | log tecnico de infraestructura |
| `Notificacion de cambio relevante` (`RelevantChangeNotification`) | comunicacion derivada de hecho confirmado | `notificaciones` (`notification`) | comando de negocio de nucleo |
| `Registro de entrega` (`NotificationDelivery`) | resultado trazable de una comunicacion emitida | `notificaciones` (`notification`) | estado comercial del pedido |
| `Reporte semanal de ventas` (`WeeklySalesReport`) | consolidado semanal comercial para decision | `reporteria` (`reporting`) | verdad transaccional de pedido |
| `Reporte semanal de reposicion` (`WeeklyReplenishmentReport`) | consolidado semanal de abastecimiento | `reporteria` (`reporting`) | correccion de stock operativo |

## Lenguaje publicado por contexto delimitado (`bounded context`)
| Contexto delimitado (`bounded context`) | Lenguaje que publica | Lenguaje que consume |
|---|---|---|
| `directorio` (`directory`) | `Organizacion` (`Organization`), `PoliticaPorPais` (`CountryPolicy`) | legitimidad de actor desde `identidad-acceso` (`identity-access`) |
| `catalogo` (`catalog`) | `OfertaDeCatalogo` (`CatalogOffer`), `Producto` (`Product`), `Variante` (`Variant`) | `PoliticaPorPais` (`CountryPolicy`) aplicada |
| `inventario` (`inventory`) | `StockOperativo` (`OperationalStock`), `DisponibilidadComprometible` (`CommitableAvailability`) | `OfertaDeCatalogo` (`CatalogOffer`) publicada |
| `pedidos` (`order`) | `Carrito` (`Cart`), `CheckoutValidado` (`ValidatedCheckout`), `Pedido` (`Order`), `EstadoDePedido` (`OrderStatus`), `PagoManual` (`ManualPayment`), `EstadoFinanciero` (`FinancialStatus`), `HistorialDeCambios` (`OrderChangeHistory`) | `PoliticaPorPais` (`CountryPolicy`), `OfertaDeCatalogo` (`CatalogOffer`), `DisponibilidadComprometible` (`CommitableAvailability`), legitimidad de actor |
| `notificaciones` (`notification`) | `NotificacionDeCambioRelevante` (`RelevantChangeNotification`), `EntregaDeNotificacion` (`NotificationDelivery`) | hechos relevantes de `pedidos` (`order`) |
| `reporteria` (`reporting`) | `ReporteSemanalDeVentas` (`WeeklySalesReport`), `ReporteSemanalDeReposicion` (`WeeklyReplenishmentReport`) | hechos publicados por `directorio` (`directory`), `catalogo` (`catalog`), `inventario` (`inventory`), `pedidos` (`order`), `notificaciones` (`notification`) |

## Terminos reservados para capacidad transversal
| Termino | Propiedad semantica | Regla de uso en dominio |
|---|---|---|
| `Cuenta` (`Account`) | `identidad-acceso` (`identity-access`) | solo como referencia de actor legitimado |
| `Sesion` (`Session`) | `identidad-acceso` (`identity-access`) | solo como precondicion de operacion |
| `Rol` (`Role`) / `Permiso` (`Permission`) | `identidad-acceso` (`identity-access`) | condicion de autorizacion, no verdad de compra B2B |

## Regla editorial de lenguaje
Todo termino nuevo debe declarar propietario semantico, significado oficial y
frontera de uso.
Si no cumple estos tres puntos, no entra al lenguaje ubicuo canonico.
