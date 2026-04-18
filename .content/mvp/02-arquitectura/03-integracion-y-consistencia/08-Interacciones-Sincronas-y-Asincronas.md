---
title: "Interacciones sincronas y asincronas"
linkTitle: "8. Interacciones sincronas y asincronas"
weight: 8
url: "/mvp/arquitectura/integracion-y-consistencia/interacciones-sincronas-y-asincronas/"
aliases:
  - "/mvp/arquitectura/arc42/vista-ejecucion/"
---

## Proposito de la seccion
Definir que interacciones requieren respuesta inmediata y cuales deben
resolverse de forma desacoplada por eventos.

## Criterio de decision sync vs async
| Criterio | Sync | Async |
|---|---|---|
| necesidad de respuesta inmediata de negocio | si | no |
| proteccion de invariante en camino critico | si | no |
| side effects derivados no bloqueantes | no | si |
| tolerancia a latencia/retardo | baja | media/alta |
| acoplamiento temporal aceptable | alto | bajo |

## Interacciones sincronas obligatorias
| Flujo | Llamadas sincronas requeridas | Motivo |
|---|---|---|
| checkout y creacion de pedido | `order -> directory`, `order -> catalog`, `order -> inventory` | validar precondiciones criticas antes de mutar pedido |
| ajuste de pedido antes de cierre | `order -> inventory` (revalidacion) y segun caso `directory/catalog` | mantener coherencia comercial luego del ajuste |
| consultas operativas inmediatas | API del servicio owner | respuesta directa del contexto owner |
| validacion de legitimidad de actor en borde | `api-gateway/services -> identity-access` (por token/caches/politica) | control de acceso transversal previo a ejecutar comando |

## Interacciones asincronas recomendadas
| Flujo | Publicador | Consumidor | Resultado esperado |
|---|---|---|---|
| cambios de pedido/pago | `order` | `notification`, `reporting` | comunicacion y lectura derivada sin bloquear `order` |
| cambios de stock/disponibilidad | `inventory` | `notification`, `reporting` | alertas operativas y consolidacion de abastecimiento |
| cambios de oferta | `catalog` | `reporting` | coherencia de lectura comercial derivada |
| cambios regionales | `directory` | `reporting` (y otros que apliquen) | consolidacion semanal con contexto regional correcto |
| resultados de notificacion | `notification` | `reporting` | lectura de efectividad de comunicacion |

## Dependencias temporales y respuesta esperada
| Tipo de flujo | Dependencia temporal |
|---|---|
| mutacion `Core` con precondiciones | fuerte: se confirma antes de aceptar el comando |
| propagacion a `notification`/`reporting` | eventual: se acepta retraso sin romper verdad transaccional |
| lectura de reportes | eventual: snapshot con corte definido |

## Reglas operativas de interaccion
- no convertir flujos derivados en dependencias sincronas del camino critico;
- no usar `reporting` para validar transacciones de `Core`;
- no usar `notification` como fuente de verdad de estado de pedido;
- definir timeouts/retries/circuit breaker para llamadas sync del `Core`.

## Flujos tecnicos anclados en comandos de dominio
| Comando de dominio | Servicio que recibe el comando | Dependencias sincronas | Evento(s) publicado(s) | Reaccion asincrona esperada |
|---|---|---|---|---|
| `CrearPedidoDesdeCarrito` | `order-service` (`CreateOrderFromCartHandler`) | `directory-service`, `catalog-service`, `inventory-service` | `OrderCreatedFromValidatedCart` | `notification-service` y `reporting-service` consumen hecho de pedido |
| `ValidarDisponibilidadEnCheckout` | `order-service` (`ValidateCheckoutAvailabilityHandler`) | `catalog-service`, `inventory-service`, `directory-service` | `CheckoutAvailabilityValidated` | uso posterior por `CreateOrderFromCart` y seguimiento en `reporting` |
| `RegistrarPagoManual` | `order-service` (`RegisterManualPaymentHandler`) | validacion local del `Order` y reglas financieras del contexto | `ManualPaymentRegistered`, `OrderFinancialStatusUpdated` | notificacion de cambio relevante y actualizacion de proyecciones |
| `EmitirNotificacionDeCambioRelevante` | `notification-service` (`EmitRelevantChangeNotificationHandler`) | no requiere sync con `Core` (consume hecho confirmado) | `RelevantChangeNotificationEmitted` | registro de entrega y consumo por `reporting-service` |
| `GenerarReporteSemanalDeVentas` | `reporting-service` (`GenerateWeeklySalesReportHandler`) | no aplica sync en camino critico | `WeeklySalesReportGenerated` | distribucion/consulta operativa derivada |
| `GenerarReporteSemanalDeReposicion` | `reporting-service` (`GenerateWeeklyReplenishmentReportHandler`) | no aplica sync en camino critico | `WeeklyReplenishmentReportGenerated` | distribucion/consulta operativa derivada |

## Secuencia resumida por comando critico
| Comando | Secuencia tecnica resumida |
|---|---|
| `CrearPedidoDesdeCarrito` | `order` valida precondiciones sync (`directory/catalog/inventory`) -> muta `Order` -> publica evento -> reaccionan `notification` y `reporting` |
| `RegistrarPagoManual` | `order` valida evidencia y estado permitido -> muta estado financiero -> publica evento financiero -> reaccionan `notification` y `reporting` |
| `ActualizarStockOperativo` | `inventory` muta stock local -> publica `StockUpdated` -> reaccionan `notification` y `reporting`; `order` usa disponibilidad en validaciones posteriores |
| `AplicarPoliticaRegionalEnOperacion` | `directory` resuelve politica vigente -> consumidores `order/reporting` aplican contrato regional en sus comandos |

## Secuencias de ejecucion tecnica (rescatadas y adaptadas de `legacy`)
| Flujo tecnico | Secuencia de interaccion | Resultado semantico esperado |
|---|---|---|
| checkout completo | `order` recibe comando -> consulta `directory` (politica/contexto) -> consulta `catalog` (oferta/precio) -> consulta `inventory` (disponibilidad/reserva) -> confirma pedido -> publica evento | pedido confirmado sin sobreventa y con contexto regional valido |
| actualizacion de oferta con propagacion | `catalog` muta oferta/precio -> registra outbox -> publica evento -> `order`/`inventory`/`reporting` actualizan lectura local | oferta coherente en consumidores sin compartir base de datos |
| stock caliente con reintentos | `inventory` procesa ajuste/reserva con retry acotado por contencion -> publica evento -> `order` revalida en siguientes comandos | disponibilidad comprometible coherente bajo concurrencia |
| notificacion derivada | `notification` consume hecho confirmado de `order`/`inventory` -> crea solicitud -> intenta entrega -> registra resultado -> publica evento de entrega | comunicacion desacoplada, trazable y sin mutar `Core` |
| reporte semanal | `reporting` consume hechos de semana -> consolida proyeccion -> genera artefacto -> publica evento de reporte generado | snapshot semanal derivado disponible para operacion |

## Presupuesto de dependencia para flujos criticos
| Flujo | Dependencia | Timeout objetivo | Regla de degradacion |
|---|---|---|---|
| `CrearPedidoDesdeCarrito` | `directory-service` | <= `300 ms` | sin politica regional vigente, bloquear confirmacion de pedido |
| `CrearPedidoDesdeCarrito` | `catalog-service` | <= `300 ms` | sin oferta vendible vigente, rechazar checkout |
| `CrearPedidoDesdeCarrito` | `inventory-service` | <= `350-500 ms` | sin disponibilidad/reserva valida, no formalizar pedido |
| `EmitirNotificacionDeCambioRelevante` | proveedor externo | <= `1500-2000 ms` | marcar fallo `retryable` y reintentar sin bloquear `Core` |
| `GenerarReporteSemanal*` | storage de artefactos | <= `2 s` por upload | job `FAILED` con retry idempotente y sin alterar hechos fuente |
