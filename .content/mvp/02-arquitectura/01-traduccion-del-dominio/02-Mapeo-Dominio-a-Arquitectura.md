---
title: "Mapeo dominio a arquitectura"
linkTitle: "2. Mapeo dominio a arquitectura"
weight: 2
url: "/mvp/arquitectura/traduccion-del-dominio/mapeo-dominio-arquitectura/"
---

## Proposito de la seccion
Traducir limites, contratos y clasificacion del dominio a unidades
arquitectonicas concretas para implementacion.

## Mapeo de `bounded context` a servicios/contenedores
| Contexto de Dominio | Tipo | Realizacion arquitectonica | Justificacion semantica |
|---|---|---|---|
| `directory` | `Core` | `directory-service` | owner de contexto organizacional y politica regional de operacion |
| `catalog` | `Core` | `catalog-service` | owner de oferta vendible y condiciones comerciales publicadas |
| `inventory` | `Core` | `inventory-service` | owner de stock operativo, reserva y disponibilidad comprometible |
| `order` | `Core` | `order-service` | owner del compromiso comercial, estados del pedido y pago manual |
| `notification` | `Generic` | `notification-service` | comunicacion derivada de hechos confirmados sin mutar `Core` |
| `reporting` | `Generic` | `reporting-service` | lectura/snapshot derivado para seguimiento semanal y decision |
| `identity-access` | tecnico transversal | capacidad externa/plataforma de seguridad | legitimidad del actor y control de acceso fuera del dominio interno |

## Mapeo de agregados/comportamientos a componentes arquitectonicos
| Contexto | Agregado o comportamiento de referencia | Componentes tecnicos dominantes |
|---|---|---|
| `directory` | `OrganizationContext`, `CountryPolicy` | API organizacional/regional, aplicacion de politica por pais, repositorio de politicas |
| `catalog` | `CatalogOffer` | API de oferta, aplicacion de publicacion/actualizacion, repositorio de oferta |
| `inventory` | `InventoryBalance` | API de stock/reservas, servicio de disponibilidad, repositorio de balance |
| `order` | `Cart`, `Order` (incluye `ManualPayment`) | API de checkout/pedido/pago manual, orquestacion de validaciones core, repositorio de pedido |
| `notification` | `NotificationDispatch` | consumidor de eventos core, planificador de envio, registro de entrega |
| `reporting` | snapshot semanal derivado | consumidores de eventos, proyecciones semanales, API de lectura |

## Mapeo explicito de comandos de dominio a realizacion arquitectonica
| Comando de dominio | Bounded Context | Servicio / modulo | Componente responsable (`Handler`) |
|---|---|---|---|
| `ConfigurarPoliticaRegional` | `directory` | `directory-service` | `ConfigureRegionalPolicyHandler` |
| `AplicarPoliticaRegionalEnOperacion` | `directory` | `directory-service` | `ApplyRegionalPolicyToOperationHandler` |
| `PublicarOfertaDeCatalogo` | `catalog` | `catalog-service` | `PublishCatalogOfferHandler` |
| `ActualizarOfertaDeCatalogo` | `catalog` | `catalog-service` | `UpdateCatalogOfferHandler` |
| `ActualizarStockOperativo` | `inventory` | `inventory-service` | `UpdateOperationalStockHandler` |
| `RecalcularDisponibilidadComprometible` | `inventory` | `inventory-service` | `RecalculateCommitableAvailabilityHandler` |
| `CrearCarrito` | `order` | `order-service` | `CreateCartHandler` |
| `AjustarItemsDeCarrito` | `order` | `order-service` | `AdjustCartItemsHandler` |
| `ValidarDisponibilidadEnCheckout` | `order` | `order-service` | `ValidateCheckoutAvailabilityHandler` |
| `CrearPedidoDesdeCarrito` | `order` | `order-service` | `CreateOrderFromCartHandler` |
| `AjustarPedidoAntesDeCierre` | `order` | `order-service` | `AdjustOrderBeforeCloseHandler` |
| `RevalidarConsistenciaDePedidoTrasAjuste` | `order` | `order-service` | `RevalidateOrderConsistencyAfterAdjustmentHandler` |
| `ActualizarEstadoOperativoDePedido` | `order` | `order-service` | `UpdateOrderOperationalStatusHandler` |
| `RegistrarPagoManual` | `order` | `order-service` | `RegisterManualPaymentHandler` |
| `EmitirNotificacionDeCambioRelevante` | `notification` | `notification-service` | `EmitRelevantChangeNotificationHandler` |
| `RegistrarEntregaDeNotificacion` | `notification` | `notification-service` | `RecordNotificationDeliveryHandler` |
| `GenerarReporteSemanalDeVentas` | `reporting` | `reporting-service` | `GenerateWeeklySalesReportHandler` |
| `GenerarReporteSemanalDeReposicion` | `reporting` | `reporting-service` | `GenerateWeeklyReplenishmentReportHandler` |

Regla de uso:
- el comando mantiene su nombre de dominio;
- la arquitectura lo materializa en `Handler`/componente dentro del servicio
  owner del contexto;
- no se reemplaza por una taxonomia paralela de "casos de uso de arquitectura".

## Mapeo de contratos de dominio a interfaces de arquitectura
| Contrato de Dominio | Traduccion arquitectonica | Tipo de interaccion |
|---|---|---|
| contexto organizacional y regional (`directory`) | API de resolucion de organizacion/politica regional + eventos de cambios regionales | sync + async |
| oferta vendible (`catalog`) | API de consulta de oferta vendible + eventos de publicacion/actualizacion | sync + async |
| disponibilidad comprometible (`inventory`) | API de validacion/reserva/confirmacion + eventos de stock/disponibilidad | sync + async |
| compromiso comercial de pedido (`order`) | API de checkout/estado/pago manual + eventos de cambios relevantes | sync + async |
| comunicacion derivada (`notification`) | eventos de entrega/fallo y API de consulta operativa de envio | async + read |
| lectura semanal consolidada (`reporting`) | APIs read-only de reportes + eventos de reporte generado | async + read |

## Capacidades internas vs separaciones explicitas
| Capacidad | Tratamiento arquitectonico |
|---|---|
| logica de negocio por contexto | interna de cada servicio (`application` + `domain`) |
| integracion entre contextos | separacion explicita por contratos API/eventos versionables |
| seguridad de identidad/acceso | capacidad transversal externa consumida por borde y servicios |
| observabilidad, secretos, broker, cache | capacidades de plataforma transversales |

## Regla de consistencia con la clasificacion del dominio
- `Core`: mayor densidad de componentes, ownership y consistencia.
- `Generic`: realizacion minima suficiente, centrada en reacciones derivadas.
- tecnico transversal: fuera de la particion de dominio interno.
