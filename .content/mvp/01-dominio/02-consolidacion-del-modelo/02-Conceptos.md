---
title: "Conceptos"
linkTitle: "2. Conceptos"
weight: 2
url: "/mvp/dominio/conceptos/"
---

## Proposito de la seccion
Consolidar los conceptos nucleares del dominio ya descubierto, definiendo su
identidad semantica, su papel en la solucion del problema y su diferencia frente
a otros conceptos.

## Conceptos centrales consolidados
| Concepto | BC propietario | Que es | Papel en el dominio | Diferencia clave |
|---|---|---|---|---|
| `Organizacion` (`Organization`) | `directorio` (`directory`) | unidad empresarial que compra y opera en ArkaB2B | delimita aislamiento de datos y decisiones | no equivale a `Cuenta` (`Account`) |
| `PoliticaPorPais` (`CountryPolicy`) | `directorio` (`directory`) | regla operativa regional por organizacion y pais | habilita crecimiento regional sin romper el nucleo | no es parametro tecnico global |
| `OfertaDeCatalogo` (`CatalogOffer`) | `catalogo` (`catalog`) | oferta comercial publicada para compra | hace consultable lo vendible en el canal B2B | no define stock ni estado de pedido |
| `Producto` (`Product`) | `catalogo` (`catalog`) | identidad comercial base | organiza la oferta para consulta y seleccion | no es unidad de inventario |
| `Variante` (`Variant`) | `catalogo` (`catalog`) | unidad vendible concreta derivada de producto | conecta catalogo con inventario y pedido | no es por si misma disponibilidad |
| `StockOperativo` (`OperationalStock`) | `inventario` (`inventory`) | existencia operativa registrada | base para promesa comercial y reposicion | no expresa vendibilidad |
| `DisponibilidadComprometible` (`CommitableAvailability`) | `inventario` (`inventory`) | capacidad real de compromiso comercial | evita sobreventa en checkout y ajuste de pedido | no es precio ni estado contractual |
| `Carrito` (`Cart`) | `pedidos` (`order`) | intencion de compra editable | permite preparar compra antes de formalizar | no es compromiso comercial |
| `CheckoutValidado` (`ValidatedCheckout`) | `pedidos` (`order`) | decision de consistencia previa a crear pedido | protege promesa comercial antes de formalizacion | no es interfaz tecnica |
| `Pedido` (`Order`) | `pedidos` (`order`) | compromiso comercial trazable | concentra verdad transaccional del ciclo comercial | no es reserva ni notificacion |
| `EstadoDePedido` (`OrderStatus`) | `pedidos` (`order`) | etapa visible y operativa del pedido | habilita seguimiento para comprador y operacion | no es estado de entrega de mensaje |
| `PagoManual` (`ManualPayment`) | `pedidos` (`order`) | registro manual de pago como parte del ciclo del pedido | cierra trazabilidad financiera del MVP sin pasarela dentro de la misma frontera de consistencia de `Pedido` (`Order`) | no es conciliacion automatica |
| `NotificacionDeCambioRelevante` (`RelevantChangeNotification`) | `notificaciones` (`notification`) | comunicacion derivada de un cambio confirmado | reduce friccion de seguimiento manual | no muta estado de nucleo |
| `ReporteSemanalDeVentas` (`WeeklySalesReport`) | `reporteria` (`reporting`) | `snapshot` semanal consolidado comercial | soporta decision de ventas | no corrige pedidos |
| `ReporteSemanalDeReposicion` (`WeeklyReplenishmentReport`) | `reporteria` (`reporting`) | `snapshot` semanal de necesidades de reposicion | soporta decision de abastecimiento | no corrige stock operativo |

## Relaciones semanticas entre conceptos
| Relacion | Conceptos implicados | Sentido de la relacion |
|---|---|---|
| `Compra confiable` | `OfertaDeCatalogo` (`CatalogOffer`) -> `DisponibilidadComprometible` (`CommitableAvailability`) -> `Carrito` (`Cart`) -> `CheckoutValidado` (`ValidatedCheckout`) -> `Pedido` (`Order`) | transforma consulta en compromiso comercial con promesa protegida |
| `Seguimiento trazable` | `Pedido` (`Order`) -> `EstadoDePedido` (`OrderStatus`) -> `NotificacionDeCambioRelevante` (`RelevantChangeNotification`) | hace visible el ciclo del pedido para actor externo e interno |
| `Control financiero MVP` | `Pedido` (`Order`) -> `PagoManual` (`ManualPayment`) | mantiene el estado financiero como verdad del pedido, no como agregado separado |
| `Control semanal` | hechos de `pedidos` (`order`) / `inventario` (`inventory`) -> `ReporteSemanalDeVentas` (`WeeklySalesReport`) / `ReporteSemanalDeReposicion` (`WeeklyReplenishmentReport`) | produce lectura derivada para decision operativa sin mutar verdad transaccional |
| `Regionalizacion` | `Organizacion` (`Organization`) -> `PoliticaPorPais` (`CountryPolicy`) | condiciona la operacion por pais sin bifurcar el modelo de nucleo |

## Diferenciaciones criticas del modelo
| No confundir | Motivo |
|---|---|
| `Organizacion` (`Organization`) con `Cuenta` (`Account`) | `Organizacion` (`Organization`) es dominio de negocio; `Cuenta` (`Account`) es capacidad transversal |
| `OfertaDeCatalogo` (`CatalogOffer`) con `DisponibilidadComprometible` (`CommitableAvailability`) | oferta define que se vende; disponibilidad define cuanto puede comprometerse |
| `Carrito` (`Cart`) con `Pedido` (`Order`) | carrito expresa intencion; pedido expresa compromiso |
| `EstadoDePedido` (`OrderStatus`) con `EntregaDeNotificacion` (`NotificationDelivery`) | uno es ciclo comercial; el otro es resultado de comunicacion |
| `ReporteSemanal` (`WeeklyReport`) con verdad transaccional | reporte consolida lectura; no es propietario del estado operacional |
