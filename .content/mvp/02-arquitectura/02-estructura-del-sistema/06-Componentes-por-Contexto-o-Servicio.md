---
title: "Componentes por contexto o servicio"
linkTitle: "6. Componentes por contexto / servicio"
weight: 6
url: "/mvp/arquitectura/estructura-del-sistema/componentes-por-contexto-servicio/"
aliases:
  - "/mvp/arquitectura/servicios/"
---

## Proposito de la seccion
Describir la organizacion interna de cada servicio por componentes y capas,
mostrando como se implementa cada contexto sin mezclar verdades.

## Patron interno comun por servicio
| Capa/componente | Responsabilidad |
|---|---|
| `adapter-in` | expone API HTTP y/o listeners de eventos |
| `application` | orquesta comandos, idempotencia y coordinacion de puertos |
| `domain` | protege reglas, politicas y consistencia del contexto |
| `adapter-out` | implementa repositorios, clientes a otros servicios y publicacion de eventos |
| `observability/security` | telemetria, manejo de errores y enforcement transversal |

## Componentes dominantes por servicio
| Servicio | Componentes funcionales principales | Interfaces expuestas | Integraciones dominantes |
|---|---|---|---|
| `directory-service` | gestion de contexto organizacional y politica regional | API de organizacion/politica regional, eventos regionales | consumo de legitimidad de actor, publicacion a broker |
| `catalog-service` | gestion de oferta vendible y precio vigente | API de oferta vendible, eventos de oferta | publicacion a broker, lectura de contexto regional cuando aplica |
| `inventory-service` | control de stock, reserva y disponibilidad comprometible | API de disponibilidad/reserva, eventos de stock/disponibilidad | sync con `order`, publish a broker |
| `order-service` | carrito, checkout, pedido, estado operativo y pago manual | API de carrito/pedido/pago/estado, eventos de pedido y pago | sync con `directory`/`catalog`/`inventory`, async hacia `notification`/`reporting` |
| `notification-service` | emision de comunicacion derivada y registro de entrega | API interna de request/dispatch/retry/consulta, eventos de resultado | consumo de eventos `Core`, integracion con proveedor externo |
| `reporting-service` | ingesta de hechos, proyecciones y reportes semanales | API read-only de reportes + API interna de rebuild/generacion | consumo multi-contexto, salida derivada |

## Delimitacion de responsabilidad por servicio
| Servicio | Que implementa | Que no implementa |
|---|---|---|
| `directory-service` | verdad organizacional/regional | autenticacion o ciclo comercial del pedido |
| `catalog-service` | verdad de oferta comercial | stock, reserva o estados de pedido |
| `inventory-service` | verdad de stock/reserva/disponibilidad | precio comercial y pago |
| `order-service` | verdad de compromiso comercial y pago manual | ownership de oferta, stock o politica regional |
| `notification-service` | comunicacion derivada | mutaciones transaccionales de `Core` |
| `reporting-service` | lectura/snapshot consolidado | correccion de estados transaccionales |

## Relacion con `identity-access`
`identity-access` no se implementa como componente de dominio interno en estos
servicios. Se consume como capacidad transversal para legitimidad de actor y
controles de borde/servicio.

## Diagramas de componentes por contenedor

| Servicio | Explicacion compacta por componentes | Diagrama |
|---|---|---|
| `directory-service` | Resuelve contexto organizacional y politica regional con capa de aplicacion orientada a comandos, politicas de aislamiento, repositorios propios y publicacion de hechos por outbox. | <a href="#" class="R-tree-diagram-link" data-diagram-source="diagram-components-directory-service" data-diagram-title="Componentes - directory-service" onclick="window.R_openMermaidStoredDiagram && window.R_openMermaidStoredDiagram('diagram-components-directory-service','Componentes - directory-service'); return false;">Ver diagrama</a> |
| `catalog-service` | Gestiona oferta vendible y precio mediante handlers de aplicacion, politicas de vendibilidad/precio, persistencia de catalogo y salida de eventos de oferta. | <a href="#" class="R-tree-diagram-link" data-diagram-source="diagram-components-catalog-service" data-diagram-title="Componentes - catalog-service" onclick="window.R_openMermaidStoredDiagram && window.R_openMermaidStoredDiagram('diagram-components-catalog-service','Componentes - catalog-service'); return false;">Ver diagrama</a> |
| `inventory-service` | Implementa mutacion de stock, reservas y recalculo de disponibilidad comprometible con consistencia local, repositorios de inventario y publicacion asincrona. | <a href="#" class="R-tree-diagram-link" data-diagram-source="diagram-components-inventory-service" data-diagram-title="Componentes - inventory-service" onclick="window.R_openMermaidStoredDiagram && window.R_openMermaidStoredDiagram('diagram-components-inventory-service','Componentes - inventory-service'); return false;">Ver diagrama</a> |
| `order-service` | Orquesta carrito, checkout, pedido y pago manual; integra validaciones `Core` sincronas y preserva trazabilidad con repositorios propios y outbox. | <a href="#" class="R-tree-diagram-link" data-diagram-source="diagram-components-order-service" data-diagram-title="Componentes - order-service" onclick="window.R_openMermaidStoredDiagram && window.R_openMermaidStoredDiagram('diagram-components-order-service','Componentes - order-service'); return false;">Ver diagrama</a> |
| `notification-service` | Consume hechos confirmados, decide ruteo/reintento y registra entrega como salida derivada, integrando proveedor externo sin mutar verdad transaccional `Core`. | <a href="#" class="R-tree-diagram-link" data-diagram-source="diagram-components-notification-service" data-diagram-title="Componentes - notification-service" onclick="window.R_openMermaidStoredDiagram && window.R_openMermaidStoredDiagram('diagram-components-notification-service','Componentes - notification-service'); return false;">Ver diagrama</a> |
| `reporting-service` | Ingiere hechos multi-contexto y construye proyecciones/snapshots read-only para reportes semanales, con deduplicacion, checkpoints y artefactos derivados. | <a href="#" class="R-tree-diagram-link" data-diagram-source="diagram-components-reporting-service" data-diagram-title="Componentes - reporting-service" onclick="window.R_openMermaidStoredDiagram && window.R_openMermaidStoredDiagram('diagram-components-reporting-service','Componentes - reporting-service'); return false;">Ver diagrama</a> |

<div class="diagram-source-bank" aria-hidden="true" style="position:absolute; left:-10000px; top:0; width:1px; height:1px; overflow:hidden; opacity:0; pointer-events:none;">
  <div id="diagram-components-directory-service" class="diagram-source">
{{< mermaid >}}
flowchart TB
  EXT["identity-access (capacidad transversal)"]
  BR["event-broker"]

  subgraph C1["Componente: GestionPoliticaRegional"]
    C1IN["adapter-in: OrganizationPolicyController"]
    C1APP["application: ConfigureRegionalPolicyHandler / ApplyRegionalPolicyToOperationHandler"]
    C1DOM["domain: OrganizationContext / CountryPolicy / OrganizationIsolationPolicy"]
    C1OUT["adapter-out: OrganizationRepository / CountryPolicyRepository / DomainEventPublisher"]
    C1IN --> C1APP --> C1DOM --> C1OUT
  end

  subgraph C2["Componente: ConsultaContextoOrganizacional"]
    C2IN["adapter-in: OrganizationQueryController"]
    C2APP["application: ResolveOrganizationContextQuery"]
    C2DOM["domain: OrganizationContext"]
    C2OUT["adapter-out: OrganizationRepository"]
    C2IN --> C2APP --> C2DOM --> C2OUT
  end

  EXT --> C1IN
  EXT --> C2IN
  C1OUT --> BR
{{< /mermaid >}}
  </div>

  <div id="diagram-components-catalog-service" class="diagram-source">
{{< mermaid >}}
flowchart TB
  DIR["directory-service"]
  BR["event-broker"]

  subgraph C1["Componente: GestionOfertaVendible"]
    C1IN["adapter-in: CatalogOfferCommandController"]
    C1APP["application: PublishCatalogOfferHandler / UpdateCatalogOfferHandler"]
    C1DOM["domain: CatalogOffer / SellabilityPolicy / PricePolicy"]
    C1OUT["adapter-out: CatalogOfferRepository / PriceRepository / DomainEventPublisher"]
    C1IN --> C1APP --> C1DOM --> C1OUT
  end

  subgraph C2["Componente: ConsultaOfertaVendible"]
    C2IN["adapter-in: CatalogOfferQueryController"]
    C2APP["application: ResolveSellableOfferQuery"]
    C2DOM["domain: CatalogOffer"]
    C2OUT["adapter-out: CatalogOfferRepository / PriceRepository"]
    C2IN --> C2APP --> C2DOM --> C2OUT
  end

  C2APP -. contexto regional .-> DIR
  C1OUT --> BR
{{< /mermaid >}}
  </div>

  <div id="diagram-components-inventory-service" class="diagram-source">
{{< mermaid >}}
flowchart TB
  ORD["order-service"]
  BR["event-broker"]

  subgraph C1["Componente: GestionStockOperativo"]
    C1IN["adapter-in: InventoryCommandController"]
    C1APP["application: UpdateOperationalStockHandler"]
    C1DOM["domain: InventoryBalance / OversellGuardPolicy"]
    C1OUT["adapter-out: StockRepository / MovementRepository / DomainEventPublisher"]
    C1IN --> C1APP --> C1DOM --> C1OUT
  end

  subgraph C2["Componente: GestionDisponibilidadComprometible"]
    C2IN["adapter-in: AvailabilityCommandController"]
    C2APP["application: RecalculateCommitableAvailabilityHandler"]
    C2DOM["domain: InventoryBalance / ReservationPolicy"]
    C2OUT["adapter-out: ReservationRepository / StockRepository / DomainEventPublisher"]
    C2IN --> C2APP --> C2DOM --> C2OUT
  end

  subgraph C3["Componente: ValidacionDisponibilidadCheckout"]
    C3IN["adapter-in: AvailabilityQueryController"]
    C3APP["application: ValidateCheckoutAvailabilityQuery"]
    C3DOM["domain: InventoryBalance"]
    C3OUT["adapter-out: StockRepository / ReservationRepository"]
    C3IN --> C3APP --> C3DOM --> C3OUT
  end

  ORD --> C3IN
  C1OUT --> BR
  C2OUT --> BR
{{< /mermaid >}}
  </div>

  <div id="diagram-components-order-service" class="diagram-source">
{{< mermaid >}}
flowchart TB
  DIR["directory-service"]
  CAT["catalog-service"]
  INV["inventory-service"]
  BR["event-broker"]

  subgraph C1["Componente: GestionCarrito"]
    C1IN["adapter-in: CartController"]
    C1APP["application: CreateCartHandler / AdjustCartItemsHandler"]
    C1DOM["domain: Cart"]
    C1OUT["adapter-out: CartRepository"]
    C1IN --> C1APP --> C1DOM --> C1OUT
  end

  subgraph C2["Componente: GestionPedido"]
    C2IN["adapter-in: CheckoutOrderController"]
    C2APP["application: ValidateCheckoutAvailabilityHandler / CreateOrderFromCartHandler / AdjustOrderBeforeCloseHandler / RevalidateOrderConsistencyAfterAdjustmentHandler / UpdateOrderOperationalStatusHandler"]
    C2DOM["domain: Order / CheckoutPolicies"]
    C2OUT["adapter-out: OrderRepository / OrderStatusHistoryRepository / DomainEventPublisher"]
    C2IN --> C2APP --> C2DOM --> C2OUT
  end

  subgraph C3["Componente: GestionPagoManual"]
    C3IN["adapter-in: ManualPaymentController"]
    C3APP["application: RegisterManualPaymentHandler"]
    C3DOM["domain: Order (ManualPayment interno)"]
    C3OUT["adapter-out: PaymentRecordRepository / DomainEventPublisher"]
    C3IN --> C3APP --> C3DOM --> C3OUT
  end

  C2APP --> DIR
  C2APP --> CAT
  C2APP --> INV
  C2OUT --> BR
  C3OUT --> BR
{{< /mermaid >}}
  </div>

  <div id="diagram-components-notification-service" class="diagram-source">
{{< mermaid >}}
flowchart TB
  BR["event-broker"]
  PROV["Proveedor externo de notificacion"]

  subgraph C1["Componente: EmisionNotificacionCambioRelevante"]
    C1IN["adapter-in: RelevantChangeEventListener"]
    C1APP["application: EmitRelevantChangeNotificationHandler"]
    C1DOM["domain: NotificationDispatch / RoutingPolicy"]
    C1OUT["adapter-out: NotificationRequestRepository / NotificationProviderGateway"]
    C1IN --> C1APP --> C1DOM --> C1OUT
  end

  subgraph C2["Componente: RegistroEntregaNotificacion"]
    C2IN["adapter-in: ProviderCallbackController"]
    C2APP["application: RecordNotificationDeliveryHandler"]
    C2DOM["domain: NotificationDispatch / RetryPolicy"]
    C2OUT["adapter-out: NotificationAttemptRepository / ProviderCallbackRepository / DomainEventPublisher"]
    C2IN --> C2APP --> C2DOM --> C2OUT
  end

  BR --> C1IN
  C1OUT --> PROV
  PROV --> C2IN
  C2OUT --> BR
{{< /mermaid >}}
  </div>

  <div id="diagram-components-reporting-service" class="diagram-source">
{{< mermaid >}}
flowchart TB
  BR["event-broker"]
  API["Read API de reportes"]

  subgraph C1["Componente: GeneracionReporteSemanalVentas"]
    C1IN["adapter-in: WeeklySalesReportController / SalesEventListener"]
    C1APP["application: GenerateWeeklySalesReportHandler"]
    C1DOM["domain: ProjectionPolicy / FactDedupPolicy"]
    C1OUT["adapter-out: AnalyticFactRepository / SalesProjectionRepository / ReportArtifactRepository"]
    C1IN --> C1APP --> C1DOM --> C1OUT
  end

  subgraph C2["Componente: GeneracionReporteSemanalReposicion"]
    C2IN["adapter-in: WeeklyReplenishmentReportController / InventoryEventListener"]
    C2APP["application: GenerateWeeklyReplenishmentReportHandler"]
    C2DOM["domain: ProjectionPolicy / FactDedupPolicy"]
    C2OUT["adapter-out: AnalyticFactRepository / ReplenishmentProjectionRepository / WeeklyReportExecutionRepository"]
    C2IN --> C2APP --> C2DOM --> C2OUT
  end

  BR --> C1IN
  BR --> C2IN
  C1OUT --> API
  C2OUT --> API
{{< /mermaid >}}
  </div>
</div>

## Comandos implementados

### `directory-service`
| Comando de dominio | Componente responsable (`Handler`) | Agregado / parte del dominio que toca | Evento(s) que puede producir |
|---|---|---|---|
| `ConfigurarPoliticaRegional` | `ConfigureRegionalPolicyHandler` | `CountryPolicy` / `OrganizationContext` | `RegionalPolicyConfigured` |
| `AplicarPoliticaRegionalEnOperacion` | `ApplyRegionalPolicyToOperationHandler` | `CountryPolicy` | `RegionalPolicyAppliedInOperation` |

### `catalog-service`
| Comando de dominio | Componente responsable (`Handler`) | Agregado / parte del dominio que toca | Evento(s) que puede producir |
|---|---|---|---|
| `PublicarOfertaDeCatalogo` | `PublishCatalogOfferHandler` | `CatalogOffer` | `CatalogOfferPublished` |
| `ActualizarOfertaDeCatalogo` | `UpdateCatalogOfferHandler` | `CatalogOffer` | `CatalogOfferUpdated` |

### `inventory-service`
| Comando de dominio | Componente responsable (`Handler`) | Agregado / parte del dominio que toca | Evento(s) que puede producir |
|---|---|---|---|
| `ActualizarStockOperativo` | `UpdateOperationalStockHandler` | `InventoryBalance` | `StockUpdated` |
| `RecalcularDisponibilidadComprometible` | `RecalculateCommitableAvailabilityHandler` | `InventoryBalance` | `CommitableAvailabilityRecalculated` |

### `order-service`
| Comando de dominio | Componente responsable (`Handler`) | Agregado / parte del dominio que toca | Evento(s) que puede producir |
|---|---|---|---|
| `CrearCarrito` | `CreateCartHandler` | `Cart` | `CartCreated` |
| `AjustarItemsDeCarrito` | `AdjustCartItemsHandler` | `Cart` | `CartItemsAdjusted` |
| `ValidarDisponibilidadEnCheckout` | `ValidateCheckoutAvailabilityHandler` | `Order` (precondicion de checkout) | `CheckoutAvailabilityValidated` |
| `CrearPedidoDesdeCarrito` | `CreateOrderFromCartHandler` | `Order` | `OrderCreatedFromValidatedCart` |
| `AjustarPedidoAntesDeCierre` | `AdjustOrderBeforeCloseHandler` | `Order` | `OrderAdjustedBeforeClose` |
| `RevalidarConsistenciaDePedidoTrasAjuste` | `RevalidateOrderConsistencyAfterAdjustmentHandler` | `Order` | `OrderConsistencyRevalidated` |
| `ActualizarEstadoOperativoDePedido` | `UpdateOrderOperationalStatusHandler` | `Order` | `OrderOperationalStatusUpdated` |
| `RegistrarPagoManual` | `RegisterManualPaymentHandler` | `Order` (`ManualPayment` interno al agregado) | `ManualPaymentRegistered`, `OrderFinancialStatusUpdated` |

### `notification-service`
| Comando de dominio | Componente responsable (`Handler`) | Agregado / parte del dominio que toca | Evento(s) que puede producir |
|---|---|---|---|
| `EmitirNotificacionDeCambioRelevante` | `EmitRelevantChangeNotificationHandler` | `NotificationDispatch` | `RelevantChangeNotificationEmitted` |
| `RegistrarEntregaDeNotificacion` | `RecordNotificationDeliveryHandler` | `NotificationDispatch` | `NotificationDeliveryRecorded` |

### `reporting-service`
| Comando de dominio | Componente responsable (`Handler`) | Agregado / parte del dominio que toca | Evento(s) que puede producir |
|---|---|---|---|
| `GenerarReporteSemanalDeVentas` | `GenerateWeeklySalesReportHandler` | proyeccion/snapshot semanal derivado | `WeeklySalesReportGenerated` |
| `GenerarReporteSemanalDeReposicion` | `GenerateWeeklyReplenishmentReportHandler` | proyeccion/snapshot semanal derivado | `WeeklyReplenishmentReportGenerated` |

## Arquitectura interna por servicio (detalle tecnico rescatado de `legacy`)

### `directory-service` (`Core`)
| Aspecto | Definicion arquitectonica |
|---|---|
| Arquitectura interna (`C4` L3 equivalente) | `OrganizationPolicyController` -> `ConfigureRegionalPolicyHandler` / `ApplyRegionalPolicyToOperationHandler` -> `OrganizationContext`, `CountryPolicy`, `OrganizationIsolationPolicy` -> `OrganizationRepository`, `CountryPolicyRepository`, `DirectoryAuditRepository`, `OutboxPersistenceAdapter`, `KafkaDomainEventPublisherAdapter` |
| Componentes principales | resolucion de contexto organizacional, resolucion de politica regional vigente, validacion de aislamiento por organizacion, relay de outbox |
| Flujo critico de ejecucion | `AplicarPoliticaRegionalEnOperacion` resuelve politica vigente por `organizationId+countryCode`, valida vigencia/version y retorna snapshot consumible por `order` y `reporting` |
| Contratos relevantes | API de resumen organizacional, API de resolucion de politica regional, eventos `RegionalPolicyConfigured` y `RegionalPolicyAppliedInOperation` |
| Persistencia dominante | `organization`, `organization_country_policy`, `directory_audit`, `outbox_event`, `processed_event` |
| Consideraciones de seguridad | aislamiento estricto por organizacion operante, actor tecnico confiable para resolucion runtime, masking de datos institucionales en logs |
| Presupuesto base de rendimiento | `p95` lectura de politica regional <= `120 ms`; `p95` validacion de contexto <= `180 ms`; outbox `commit -> ack` <= `2.0 s` |

### `catalog-service` (`Core`)
| Aspecto | Definicion arquitectonica |
|---|---|
| Arquitectura interna (`C4` L3 equivalente) | `CatalogOfferController` -> `PublishCatalogOfferHandler` / `UpdateCatalogOfferHandler` -> `CatalogOffer`, `SellabilityPolicy`, `PricePolicy` -> `CatalogOfferRepository`, `PriceRepository`, `CatalogAuditRepository`, `OutboxPersistenceAdapter` |
| Componentes principales | publicacion/actualizacion de oferta vendible, resolucion de variante para checkout, gestion de precio vigente/programado |
| Flujo critico de ejecucion | `PublicarOfertaDeCatalogo` valida vendibilidad y precio vigente, persiste oferta/variantes y publica `CatalogOfferPublished` para consumidores `order`, `inventory` y `reporting` |
| Contratos relevantes | API de busqueda/detalle de oferta, API de resolucion de variante para checkout, eventos de oferta y precio |
| Persistencia dominante | `product`, `variant`, `price`, `variant_attribute`, `catalog_audit`, `outbox_event`, `processed_event` |
| Consideraciones de seguridad | RBAC de operacion comercial para mutaciones, controles de idempotencia en cambios masivos de precio, validacion de `tenant` en query y write |
| Presupuesto base de rendimiento | `p95` resolve variant <= `180 ms`; `p95` search <= `320 ms`; upsert de precio <= `350 ms`; outbox publish <= `250 ms` |

### `inventory-service` (`Core`)
| Aspecto | Definicion arquitectonica |
|---|---|
| Arquitectura interna (`C4` L3 equivalente) | `InventoryCommandController` -> `UpdateOperationalStockHandler` / `RecalculateCommitableAvailabilityHandler` -> `InventoryBalance`, `ReservationPolicy`, `OversellGuardPolicy` -> `StockRepository`, `ReservationRepository`, `MovementRepository`, `OutboxPersistenceAdapter` |
| Componentes principales | mutacion de stock, manejo de reservas con TTL, recalculo de disponibilidad comprometible, ledger de movimientos |
| Flujo critico de ejecucion | `RecalcularDisponibilidadComprometible` aplica cambios de stock/reserva en transaccion local, recalcula `available = physical - reserved` y publica evento para `order`/`reporting` |
| Contratos relevantes | API de disponibilidad/reserva/confirmacion/liberacion, API interna de validacion de reservas en checkout, eventos de stock/reserva |
| Persistencia dominante | `stock_item`, `stock_reservation`, `stock_movement`, `idempotency_record`, `inventory_audit`, `outbox_event`, `processed_event` |
| Consideraciones de seguridad | permisos granulares para ajustes de stock, uso exclusivo de identidad tecnica para confirmaciones de checkout, bloqueo de acceso cruzado por tenant |
| Presupuesto base de rendimiento | `p95` crear reserva <= `120 ms`; `p95` confirmar reserva <= `150 ms`; `p95` disponibilidad <= `80 ms`; publish outbox <= `250 ms` |

### `order-service` (`Core`)
| Aspecto | Definicion arquitectonica |
|---|---|
| Arquitectura interna (`C4` L3 equivalente) | `Cart/Checkout/OrderController` -> handlers de carrito, checkout, pedido y pago manual -> `Cart`, `Order`, `ManualPayment` + politicas de checkout -> repositorios de carrito/pedido/pago/historial + clientes `directory`, `catalog`, `inventory` + outbox |
| Componentes principales | orquestacion sync de precondiciones `Core`, consistencia de pedido y pago manual, trazabilidad de transiciones |
| Flujo critico de ejecucion | `CrearPedidoDesdeCarrito` valida contexto regional en `directory`, oferta en `catalog`, disponibilidad en `inventory`; luego confirma mutacion local de `Order` y publica hecho para `notification`/`reporting` |
| Contratos relevantes | API de carrito, checkout, pedido, estado operativo y pago manual; eventos de pedido, estado y pago |
| Persistencia dominante | `cart`, `cart_item`, `checkout_attempt`, `purchase_order`, `order_line`, `payment_record`, `order_status_history`, `order_audit`, `idempotency_record`, `outbox_event` |
| Consideraciones de seguridad | aislamiento por organizacion operante en todos los comandos, validacion de ownership en consultas, evidencia obligatoria para pago manual |
| Presupuesto base de rendimiento | `p95` upsert carrito <= `140 ms`; `p95` validacion checkout <= `200 ms`; `p95` confirmar pedido <= `350 ms`; `p95` registrar pago manual <= `180 ms` |

### `notification-service` (`Generic`, densidad minima suficiente)
| Aspecto | Definicion arquitectonica |
|---|---|
| Arquitectura interna (`C4` L3 equivalente) | listeners de eventos `Core`/controlador interno -> `EmitRelevantChangeNotificationHandler` / `RecordNotificationDeliveryHandler` -> `NotificationDispatch`, `RetryPolicy`, `RoutingPolicy` -> repositorios de solicitud/intento/callback/auditoria + cliente de proveedor + outbox |
| Componentes principales | request de notificacion derivada, dispatch, retry y registro de entrega sin mutar estado transaccional `Core` |
| Flujo critico de ejecucion | `EmitirNotificacionDeCambioRelevante` consume hecho confirmado, persiste solicitud en `PENDING`, ejecuta envio por proveedor y registra resultado para publicacion derivada |
| Contratos relevantes | API interna de `request/dispatch/retry/discard`, callbacks de proveedor, eventos `RelevantChangeNotificationEmitted` y `NotificationDeliveryRecorded` |
| Persistencia dominante | `notification_request`, `notification_attempt`, `provider_callback`, `notification_audit`, `outbox_event`, `processed_event` |
| Consideraciones de seguridad | scopes m2m por operacion, validacion de firma/origen en callback, masking de payload sensible |
| Presupuesto base de rendimiento | `p95` crear solicitud <= `180 ms`; `p95` dispatch interno <= `220 ms`; dispatch total con proveedor <= `1500 ms`; callback <= `140 ms` |

### `reporting-service` (`Generic`, densidad minima suficiente)
| Aspecto | Definicion arquitectonica |
|---|---|
| Arquitectura interna (`C4` L3 equivalente) | listeners de eventos + API read-only + API interna de jobs -> `GenerateWeeklySalesReportHandler` / `GenerateWeeklyReplenishmentReportHandler` y handlers de ingesta -> `ProjectionPolicy`, `FactDedupPolicy` -> repositorios de hechos/proyecciones/artefactos/checkpoints + outbox |
| Componentes principales | ingesta idempotente de hechos, proyecciones materializadas, generacion semanal de reportes y consulta read-only |
| Flujo critico de ejecucion | `GenerarReporteSemanalDeVentas` consolida snapshot semanal por tenant, persiste metadata de artefacto y publica `WeeklySalesReportGenerated` |
| Contratos relevantes | API read-only de reportes semanales/KPI, API interna de rebuild y generacion manual, eventos de reporte semanal |
| Persistencia dominante | `analytic_fact`, `sales_projection`, `replenishment_projection`, `operations_kpi_projection`, `weekly_report_execution`, `report_artifact`, `consumer_checkpoint`, `outbox_event`, `processed_event` |
| Consideraciones de seguridad | consulta y artefactos siempre filtrados por tenant owner, ops internas solo con scope `reporting.ops`, validacion de schema/version en eventos consumidos |
| Presupuesto base de rendimiento | ingesta de hecho <= `220 ms` p95; consultas semanales <= `550-600 ms` p95; reporte semanal <= `15 min`; rebuild 10k hechos <= `8 min` |
