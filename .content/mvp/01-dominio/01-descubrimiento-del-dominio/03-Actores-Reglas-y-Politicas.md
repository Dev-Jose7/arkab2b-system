---
title: "Actores, reglas y politicas"
linkTitle: "3. Actores, reglas y politicas"
weight: 3
url: "/mvp/dominio/actores-reglas-y-politicas/"
aliases:
  - "/mvp/dominio/reglas-e-invariantes/"
  - "/mvp/dominio/reglas-invariantes/"
  - "/mvp/dominio/comportamiento-global/"
---

## Proposito de la seccion
Definir quien participa en el dominio y bajo que reglas y politicas se valida
el comportamiento descubierto para ArkaB2B.

## Actores del dominio y su participacion
| Actor | Rol de negocio/operacion | Comandos en los que participa | Resultado que habilita |
|---|---|---|---|
| `Comprador B2B` | ejecuta el flujo comercial digital | `CrearCarrito` (`CreateCart`), `AjustarItemsDeCarrito` (`AdjustCartItems`), `CrearPedidoDesdeCarrito` (`CreateOrderFromCart`), `AjustarPedidoAntesDeCierre` (`AdjustOrderBeforeClose`) | convierte intencion de compra en pedido trazable |
| `Administrador de cuenta B2B` | gobierna operacion de su organizacion | participa en condiciones de aislamiento y resolucion de contexto organizacional para comandos de compra y consulta | evita operaciones fuera de organizacion |
| `Operacion Arka` | controla avance operativo y financiero | `ActualizarEstadoOperativoDePedido` (`UpdateOrderOperationalStatus`), `RegistrarPagoManual` (`RegisterManualPayment`), `ActualizarStockOperativo` (`UpdateOperationalStock`), `ConfigurarPoliticaRegional` (`ConfigureRegionalPolicy`) | asegura continuidad operativa y evidencia verificable |
| `Sistema` | ejecuta coordinacion y consolidacion automatizada | `AplicarPoliticaRegionalEnOperacion` (`ApplyRegionalPolicyToOperation`), `ValidarDisponibilidadEnCheckout` (`ValidateCheckoutAvailability`), `RecalcularDisponibilidadComprometible` (`RecalculateCommitableAvailability`), `EmitirNotificacionDeCambioRelevante` (`EmitRelevantChangeNotification`), `GenerarReporteSemanalDeVentas` (`GenerateWeeklySalesReport`), `GenerarReporteSemanalDeReposicion` (`GenerateWeeklyReplenishmentReport`) | sostiene coherencia del flujo y salida operativa periodica |
| `identidad-acceso` (`identity-access`) (capacidad transversal) | valida legitimidad de actor | condicion transversal de legitimidad para comandos del dominio | habilita o bloquea ejecucion; no posee verdad de negocio interna |

## Reglas de negocio que gobiernan el flujo
| Regla | Fuente en Producto | Comandos / eventos condicionados | Decision que impone |
|---|---|---|---|
| `Aislamiento obligatorio de operaciones de negocio por organizacion operante` | NFR-005, supuestos y decisiones base | comandos de compra, seguimiento y pago | ninguna mutacion o consulta de negocio opera fuera del contexto organizacional resuelto |
| `Coherencia obligatoria entre promesa comercial y disponibilidad comprometible` | FR-003, FR-004, FR-023, FR-024, NFR-004 | `ValidarDisponibilidadEnCheckout` (`ValidateCheckoutAvailability`), `CrearPedidoDesdeCarrito` (`CreateOrderFromCart`), `RecalcularDisponibilidadComprometible` (`RecalculateCommitableAvailability`) | ningun pedido se formaliza si la disponibilidad comprometible no respalda la promesa comercial |
| `Trazabilidad verificable obligatoria del pedido durante todo su ciclo operativo` | FR-008, FR-012, FR-014, FR-020, NFR-006 | `CrearPedidoDesdeCarrito` (`CreateOrderFromCart`), `ActualizarEstadoOperativoDePedido` (`UpdateOrderOperationalStatus`) | todo cambio operativo confirmado del pedido deja trazabilidad verificable y consultable |
| `Validez del pago manual sujeta a soporte verificable asociado al pedido` | FR-015, FR-016, FR-017, NFR-006 | `RegistrarPagoManual` (`RegisterManualPayment`) y actualizacion financiera de pedido | no existe pago manual valido sin soporte verificable asociado al pedido |
| `Ejecucion de operaciones sensibles a regionalizacion con politica regional vigente y resoluble` | FR-027, FR-028, NFR-011, decisiones base por `countryCode` | `ConfigurarPoliticaRegional` (`ConfigureRegionalPolicy`), `AplicarPoliticaRegionalEnOperacion` (`ApplyRegionalPolicyToOperation`) | toda operacion sensible a regionalizacion se ejecuta con una politica regional vigente y resoluble |

## Politicas de delimitacion semantica del modelo
| Politica | Fuente en Producto | Capacidades condicionadas | Decision que impone |
|---|---|---|---|
| `Comunicacion derivada de hechos confirmados sin mutacion de la verdad transaccional del nucleo` | FR-018, FR-019, alcance/no alcance | `EmitirNotificacionDeCambioRelevante` (`EmitRelevantChangeNotification`), `RegistrarEntregaDeNotificacion` (`RecordNotificationDelivery`) | la notificacion comunica hechos del negocio ya ocurridos, pero no modifica la verdad transaccional del nucleo |
| `Publicacion de reporteria derivada sin correccion ni redefinicion de transacciones del nucleo` | FR-025, FR-026, NFR-002 | `GenerarReporteSemanalDeVentas` (`GenerateWeeklySalesReport`), `GenerarReporteSemanalDeReposicion` (`GenerateWeeklyReplenishmentReport`) | la reporteria publica lectura derivada y no corrige ni redefine estados transaccionales del nucleo |
| `Separacion semantica obligatoria entre visibilidad de estado, evidencia historica y comunicacion derivada` | EN03, EN04, EN06, FR-011, FR-012, FR-014, FR-018, FR-019, FR-020 | consulta de estado vigente, consulta de historial y comunicacion derivada | la consulta del estado vigente, la evidencia historica y la comunicacion derivada deben resolverse como capacidades distintas |

## Politicas de dominio transversales
| Politica | Sobre que conceptos opera | Que decision encapsula | Resultado o restriccion |
|---|---|---|---|
| `PoliticaDeValidacionDeCheckout` (`CheckoutValidationPolicy`) | oferta vendible, disponibilidad, contexto organizacional | si un carrito puede formalizarse como pedido | habilita o bloquea creacion de pedido |
| `PoliticaDeVentanaDeAjusteDePedido` (`OrderAdjustmentWindowPolicy`) | pedido y estado operativo | si un pedido puede ajustarse antes de cierre | evita ajustes fuera de ventana valida |
| `PoliticaDeEvidenciaDePagoManual` (`ManualPaymentEvidencePolicy`) | pedido y pago manual | que evidencia minima valida un registro manual | impide estado financiero ambiguo |
| `PoliticaDeOperacionRegional` (`RegionalOperationPolicy`) | politica regional y operacion por pais | que regla aplica a cada operacion critica | evita fallback global implicito |
| `PoliticaDeEmisionDeNotificacion` (`NotificationEmissionPolicy`) | cambios relevantes y plan de envio | cuando notificar, a quien y bajo que control de duplicidad | reduce ruido operativo y mantiene trazabilidad |
| `PoliticaDeCorteSemanal` (`WeeklyCutoffPolicy`) | hechos semanales de ventas/abastecimiento | cuando puede cerrarse y publicarse un reporte semanal | asegura cortes repetibles y auditables |

## Condiciones de validez por flujo
| Flujo | Condiciones obligatorias | Resultado valido |
|---|---|---|
| `Compra B2B confiable` | contexto organizacional resuelto + aislamiento organizacional + oferta vendible + disponibilidad comprometible | pedido creado sin sobreventa |
| `Ajuste de pedido antes de cierre` | pedido en ventana ajustable + revalidacion de consistencia | pedido corregido sin romper promesa comercial |
| `Pago manual trazable` | pedido existente + evidencia verificable + referencia consistente | estado financiero actualizado con soporte auditable |
| `Notificacion por cambio relevante` | evento confirmado + politica de emision valida | notificacion emitida y entrega registrada |
| `Seguimiento semanal de control` | ventana semanal cerrada + datos consistentes por organizacion/pais | reportes semanales publicables para decision |

## Regla de orden metodologico
La asignacion de estas reglas y politicas a `contextos delimitados`
(`bounded contexts`) se define en la seccion 5.
