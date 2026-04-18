---
title: "Contextos delimitados"
linkTitle: "5. Contextos delimitados"
weight: 5
url: "/mvp/dominio/bounded-contexts/"
aliases:
  - "/mvp/dominio/resumen-del-dominio/"
  - "/mvp/dominio/mapa-de-contexto/"
  - "/mvp/dominio/mapa-contexto/"
  - "/mvp/dominio/contextos-delimitados/"
  - "/mvp/dominio/contextos-delimitados/identidad-acceso/"
  - "/mvp/dominio/contextos-delimitados/directorio/"
  - "/mvp/dominio/contextos-delimitados/catalogo/"
  - "/mvp/dominio/contextos-delimitados/inventario/"
  - "/mvp/dominio/contextos-delimitados/pedidos/"
  - "/mvp/dominio/contextos-delimitados/notificaciones/"
  - "/mvp/dominio/contextos-delimitados/reporteria/"
  - "/mvp/dominio/tactico/core-domain/"
  - "/mvp/dominio/tactico/generic-subdomain/"
  - "/mvp/dominio/tactico/directory/"
  - "/mvp/dominio/tactico/catalog/"
  - "/mvp/dominio/tactico/inventory/"
  - "/mvp/dominio/tactico/order/"
  - "/mvp/dominio/tactico/notification/"
  - "/mvp/dominio/tactico/reporting/"
---

## Proposito de la seccion
Consolidar las fronteras semanticas del dominio descubiertas desde Producto:
que contextos existen, que verdad posee cada uno y como se relacionan.

## Clasificacion oficial de subdominios
| Tipo | Contextos | Lectura metodologica |
|---|---|---|
| `Nucleo` (`Core`) | `directorio` (`directory`), `catalogo` (`catalog`), `inventario` (`inventory`), `pedidos` (`order`) | concentran verdad primaria del negocio de compra B2B |
| `Generico` (`Generic`) | `notificaciones` (`notification`), `reporteria` (`reporting`) | soportan capacidades necesarias sin apropiarse de verdad de nucleo |
| Tecnico transversal | `identidad-acceso` (`identity-access`) | capacidad externa de legitimidad; no es BC interno del negocio |

## Contextos internos del dominio de ArkaB2B
| BC | Tipo | Que comportamiento le pertenece | Lenguaje que publica | Verdad que posee | Que no le pertenece |
|---|---|---|---|---|---|
| `directorio` (`directory`) | `Nucleo` (`Core`) | configuracion regional y contexto organizacional de operacion | `Organizacion` (`Organization`), `PoliticaPorPais` (`CountryPolicy`) | politica regional vigente por organizacion/pais | oferta comercial, stock, ciclo transaccional de pedido |
| `catalogo` (`catalog`) | `Nucleo` (`Core`) | publicacion de oferta vendible consultable | `OfertaDeCatalogo` (`CatalogOffer`), `Producto` (`Product`), `Variante` (`Variant`) | condiciones comerciales de oferta publicada | disponibilidad fisica, estado de pedido, pago |
| `inventario` (`inventory`) | `Nucleo` (`Core`) | actualizacion de stock y recalculo de disponibilidad comprometible | `StockOperativo` (`OperationalStock`), `DisponibilidadComprometible` (`CommitableAvailability`) | stock operativo y promesa de disponibilidad | precio, flujo de pago, estado contractual del pedido |
| `pedidos` (`order`) | `Nucleo` (`Core`) | carrito, checkout, pedido, estado operativo y pago manual | `Carrito` (`Cart`), `Pedido` (`Order`), `EstadoDePedido` (`OrderStatus`), `PagoManual` (`ManualPayment`) | compromiso comercial y estado transaccional del pedido | propiedad semantica de oferta, stock y politica regional |
| `notificaciones` (`notification`) | `Generico` (`Generic`) | comunicacion derivada por cambios relevantes confirmados | `NotificacionDeCambioRelevante` (`RelevantChangeNotification`), `EntregaDeNotificacion` (`NotificationDelivery`) | salida de comunicacion emitida/entregada | mutacion transaccional de contextos de nucleo |
| `reporteria` (`reporting`) | `Generico` (`Generic`) | consolidacion semanal de lectura comercial y de abastecimiento | `ReporteSemanalDeVentas` (`WeeklySalesReport`), `ReporteSemanalDeReposicion` (`WeeklyReplenishmentReport`) | `snapshots` semanales para decision | correccion de estados transaccionales de nucleo |

## Relaciones de contexto
| Relacion | Upstream | Downstream | Lenguaje publicado | Traduccion o anti-corrupcion requerida |
|---|---|---|---|---|
| Contexto regional de operacion | `directorio` (`directory`) | `catalogo` (`catalog`), `pedidos` (`order`), `reporteria` (`reporting`) | `PoliticaPorPais` (`CountryPolicy`) aplicable | los consumidores aplican politica sin redefinir propiedad semantica |
| Oferta vendible para compra | `catalogo` (`catalog`) | `pedidos` (`order`), `inventario` (`inventory`), `reporteria` (`reporting`) | oferta publicada y condiciones comerciales | los consumidores no recalculan reglas de oferta |
| Promesa comercial comprometible | `inventario` (`inventory`) | `pedidos` (`order`), `reporteria` (`reporting`) | disponibilidad comprometible | `pedidos` (`order`) consume disponibilidad como contrato publicado |
| Compromiso comercial y ciclo de estado | `pedidos` (`order`) | `notificaciones` (`notification`), `reporteria` (`reporting`) | cambios relevantes de pedido y pago manual | consumidores derivados no mutan estado de pedido |
| Comunicacion derivada | `notificaciones` (`notification`) | `reporteria` (`reporting`) (lectura) | emision y resultado de entrega de notificaciones | `reporteria` (`reporting`) consume como hecho derivado, no como control transaccional |

## Capacidad tecnica transversal asociada
| Capacidad | Tipo | Contextos consumidores | Que aporta | Restriccion |
|---|---|---|---|---|
| `identidad-acceso` (`identity-access`) | tecnica transversal | `directorio` (`directory`), `pedidos` (`order`), `notificaciones` (`notification`) | legitimidad de actor para ejecutar comandos y consultas | no define lenguaje ni verdad de negocio de ArkaB2B |

## Densidad esperada por tipo de subdominio
| Tipo | Densidad esperada | Criterio |
|---|---|---|
| `Nucleo` (`Core`) | alta | protege invariantes primarios del negocio y concentra mayor volumen de reglas |
| `Generico` (`Generic`) | minima suficiente | soporta valor real como capacidad derivada sin sobredimensionar modelo transaccional |
| `Tecnico transversal` | fuera del dominio interno | se trata en integracion/arquitectura, no como BC de negocio |
