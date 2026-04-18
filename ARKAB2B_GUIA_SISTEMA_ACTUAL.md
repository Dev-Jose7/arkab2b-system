# Guía Completa del Sistema ArkaB2B (Estado Actual)

## 1. Visión general del sistema

ArkaB2B es una plataforma B2B para operación comercial entre organizaciones: autenticación/acceso, contexto organizacional, catálogo, inventario, pedidos y derivados (notificación/reporting).

La unidad real del negocio en el estado actual es `organization`:

- el contexto operativo es `organizationId`,
- los actores ejecutan acciones “en nombre de una organización”,
- los flujos core (checkout/pedido) y derivados (notificación/reporting) se resuelven por organización.

Está dividido en:

- plano de plataforma/control: Config Server, Eureka, Gateway,
- plano core de negocio: IAM, Directory, Catalog, Inventory, Order,
- plano derivado/analítico: Notification, Reporting,
- infraestructura compartida: Postgres, Redis, Kafka.

Referencias clave:

- config-repo base
- gateway routes

---

## 2. Mapa general de servicios

### Infraestructura/plataforma

| Servicio | Propósito | Tipo | Dependencias principales |
|---|---|---|---|
| `config-server` | Servir configuración centralizada por `spring.application.name` y perfil | Plataforma | Config repo local, Eureka |
| `eureka-server` | Registro/descubrimiento de instancias | Plataforma | Ninguna externa crítica |
| `api-gateway` | Borde único, routing y seguridad externa JWT | Plataforma/Borde | Eureka, IAM JWKS |

### Servicios de negocio

| Servicio | Responsabilidad principal | Core/Derivado | Dependencias |
|---|---|---|---|
| `identity-access-service` | Login, refresh, registro fundador, JWKS, emisión de token S2S | Core transversal | Postgres, Redis, Kafka |
| `directory-service` | Organización, perfiles, contactos, direcciones, políticas por país | Core | Postgres, Redis, Kafka, IAM |
| `catalog-service` | Productos, variantes, precios, resolución para checkout | Core | Postgres, Redis, Kafka, Directory/IAM |
| `inventory-service` | Stock, reservas, disponibilidad comprometible | Core | Postgres, Redis, Kafka, Catalog/Directory/Order/IAM |
| `order-service` | Carrito, validación de checkout, creación/ajustes/cancelación de pedido | Core | Postgres, Redis, Kafka, Directory/Catalog/Inventory/IAM |
| `notification-service` | Consumir hechos, aplicar policy/template, crear request y dispatch | Derivado | Postgres, Redis, Kafka, Directory/Order/IAM, provider externo |
| `reporting-service` | Ingesta de hechos, normalize/apply/checkpoint y proyecciones | Derivado | Postgres, Redis, Kafka, Directory/Order/IAM |

### Infraestructura compartida

- Postgres: una instancia con base por servicio.
- Redis: cache/rate-limit/soporte operacional.
- Kafka: integración async por tópicos de dominio + DLQ.

Referencias:

- docker-compose
- topics bootstrap
- postgres init

---

## 3. Cómo está organizado el sistema a nivel de arquitectura

Patrón real implementado: estilo hexagonal/clean por servicio:

- `domain`: agregados, entidades, value objects, reglas,
- `application`: comandos/queries/use-cases/orquestación,
- `infrastructure`: adapters in/out, config, persistencia, integración HTTP/Kafka/seguridad.

Cada servicio mantiene bounded context y base de datos propia (aislamiento de datos + integración por APIs/eventos).

Roles técnicos:

- Config Server: centraliza configuración y evita drift por archivo local suelto.
- Eureka: discovery runtime; los clientes internos llaman por nombre lógico de servicio.
- Gateway: borde externo, JWT validation y routing.
- IAM: identidad/autorización + emisor de tokens técnicos S2S.
- Kafka: backbone async para hechos de dominio y servicios derivados.
- Redis: cache/rate-limit/soporte de consultas y operación.
- Postgres: persistencia transaccional por servicio.

Referencias:

- Order package layout
- Notification package layout
- Reporting package layout

---

## 4. Cómo se inicia el sistema en local

### Prerrequisitos reales

- Docker + Docker Compose.
- Java 21 (toolchain en servicios).
- `curl`.
- Keys locales IAM.

Referencias:

- stack README
- start script
- stop script
- smoke script

### Paso a paso

1. Generar llaves JWT locales:

```bash
cd /Users/jose/Development/Java/arkab2b
./scripts/generate-local-jwt-keys.sh
```

2. Levantar stack:

```bash
./scripts/start-integrated-local.sh
```

3. Smoke opcional automático:

```bash
ARKAB2B_RUN_SMOKE=true ./scripts/start-integrated-local.sh
```

4. Verificación manual básica:

- Config: `http://localhost:8888/actuator/health/readiness`
- Eureka: `http://localhost:8761/eureka/apps`
- Gateway: `http://localhost:8080/actuator/health/readiness`

5. Apagar stack:

```bash
./scripts/stop-integrated-local.sh
```

Con volúmenes:

```bash
ARKAB2B_REMOVE_VOLUMES=true ./scripts/stop-integrated-local.sh
```

### Orden de arranque real

1. Infra: Postgres, Redis, Kafka, init de topics.
2. Control plane: Eureka → Config.
3. Core/derivados: IAM, Directory, Catalog, Inventory, Order, Notification, Reporting.
4. Borde: API Gateway.

### Decisión de arranque: por qué Eureka va antes que Config Server

- `eureka-server` arranca primero de forma intencional para estabilizar el control plane.
- `config-server` también participa como cliente de discovery/registro y por eso conviene levantarlo después.
- Este orden evita un bootstrap frágil o circular (Config dependiendo de discovery antes de que discovery esté disponible).
- En ArkaB2B, la decisión operativa oficial es: `Eureka -> Config Server -> servicios de negocio -> Gateway`.
- No es mala práctica en este contexto; es una medida de robustez del arranque integrado.

### Puertos por defecto

- Infra: 55432 (Postgres), 56379 (Redis), 59092 (Kafka host).
- Plataforma: 8888 (Config), 8761 (Eureka), 8080 (Gateway).
- Negocio: 8081..8087 (IAM..Reporting).

---

## 5. Configuración distribuida y plataforma de control

Funcionamiento:

- cada servicio define `spring.application.name`,
- arranca con `spring.config.import=optional:configserver:...`,
- Config Server resuelve `application.yml`, `application-local.yml`, `<service>.yml`,
- Eureka registra instancias y clientes internos usan discovery.

Qué gobierna Config central:

- rutas de gateway,
- JWKS issuer/audience,
- S2S client/scopes,
- topics Kafka,
- external adapters (base URLs, paths, timeouts),
- observabilidad (headers, métricas),
- flags operativos de relay/consumers/schedulers.

Datos sensibles:

- secretos van por env vars/placeholders,
- llaves locales se referencian por path externo, no incrustadas.

### Flujo de resolución cuando Config Server está arriba

Cuando `config-server` está arriba, el flujo en ArkaB2B es así:

1. Arranca el microservicio y lee su `application.yml` local (bootstrap):
   - `spring.application.name` (ej. `order-service`)
   - `spring.config.import=optional:configserver:...`
   - perfil activo (`SPRING_PROFILES_ACTIVE`, normalmente `local`)
2. Con esos datos, Spring consulta Config Server para `/{application}/{profile}` (ej. `/order-service/local`).
3. Config Server arma el `Environment` desde `platform/config-repo` con este orden lógico:
   - `application.yml`
   - `application-local.yml`
   - `order-service.yml`
   - `order-service-local.yml` (si existe)
4. El microservicio fusiona propiedades con la precedencia normal de Spring (env vars del proceso suelen tener prioridad alta).
5. Con la configuración efectiva inicializa beans (JWT/S2S, URLs externas, topics Kafka, outbox, puertos, etc.) y luego se registra en Eureka.

En resumen:

- con Config Server arriba, la config efectiva viene principalmente del `config-repo`;
- los `application*.yml` internos del servicio quedan como bootstrap/fallback;
- si una variable crítica viene por env var (Docker/K8s), puede sobrescribir lo central según precedencia.

### Regla de perfiles en ArkaB2B (importante)

- El perfil activo real lo define Spring mediante `SPRING_PROFILES_ACTIVE` (o `--spring.profiles.active`).
- En el stack local oficial, los servicios se ejecutan con perfil `local`.
- `app.runtime.profile` (cuando aparece en algunos servicios) es una propiedad informativa; no activa perfiles.
- La activación de perfil no debe hardcodearse para producción dentro del código del servicio.
- En producción, el perfil y secretos deben inyectarse desde la plataforma de despliegue (compose/k8s/pipeline/secret manager).

Referencias:

- config-server app
- config repo README
- identity-access config

---

## 6. Seguridad del sistema

### Externa (usuario/borde)

- Gateway es resource server JWT (issuer + audience + jwks-uri).
- Permite públicos mínimos: login/refresh/register-founder/introspect + JWKS + health/docs.
- Todo lo demás autenticado en borde.
- Manejo explícito de `401` y `403` por handlers JSON.

Referencias:

- gateway security

### IAM

- Endpoints auth: `/api/v1/auth/register-founder`, `/login`, `/refresh`, `/logout`.
- JWKS en `/.well-known/jwks.json`.
- Emite token técnico S2S en `/api/v1/internal/auth/service-token`.

Referencias:

- AuthHttpController
- ServiceTokenHttpController

### Interna (S2S)

- Cada servicio obtiene token técnico desde IAM (`clientId/clientSecret/scopes/audience`).
- WebClient con `@LoadBalanced` + bearer auto + trace/correlation propagation.
- Endpoints internos exigen `ROLE_TRUSTED_SERVICE` y scopes.

Referencias:

- order ServiceToServiceTokenProvider
- order internal endpoints

### Contexto organizacional

- El dominio core opera por `organizationId`.
- En sync se valida aislamiento organizacional.
- En async se inyecta contexto técnico autenticado por consumidor/scheduler con organización resuelta.

### Flujo de seguridad end-to-end (explicación clave)

Este es el flujo real y vigente del sistema:

1. `identity-access-service` autentica al usuario, gestiona sesión (login/refresh/logout) y emite JWT.
2. `api-gateway` actúa como borde: valida JWT (firma/issuer/audience vía JWKS) y bloquea acceso inválido antes de entrar al core.
3. Cada microservicio de negocio vuelve a validar JWT localmente (resource server propio): `directory`, `catalog`, `inventory`, `order`, `notification`, `reporting`.
4. En cada servicio se reconstruye principal/contexto de autenticación (`sub`, roles/scopes, `organizationId`, país) desde claims.
5. Los endpoints se blindan con autorización explícita (`@PreAuthorize` + validaciones de aplicación) para evitar confianza implícita solo en gateway.
6. Para llamadas internas, el llamador solicita token técnico S2S a IAM y el receptor exige `ROLE_TRUSTED_SERVICE` + scopes técnicos.
7. En consumidores/schedulers async se usa actor técnico autenticado para ejecutar casos de uso sin depender de sesión HTTP.

Por qué esto es importante:

- deja defensa en profundidad (no depende de un único punto de control),
- evita bypass de seguridad si alguien alcanza un servicio por red interna,
- mantiene aislamiento organizacional de forma consistente en sync y async,
- preserva semántica HTTP correcta (`401`/`403`) tanto en borde como en servicios.

---

## 7. Cómo se comunican los servicios (sync)

Integraciones principales reales:

1. `order -> directory`
   - checkout resolution (dirección/política/región para checkout),
   - endpoint interno de directory con organization/address/country.

2. `order -> catalog`
   - resolución de variante/precio para checkout.

3. `order -> inventory`
   - validación de reserva/disponibilidad.

4. `notification -> directory`
   - resolución de destinatarios/contactos organizacionales.

5. `notification -> order`
   - lookup de organization context por `orderId/cartId` cuando evento no trae contexto completo.

6. `reporting -> directory`
   - contexto regional/políticas para operación analítica.

7. `reporting -> order`
   - lookup de organization context desde `orderId/cartId`.

Aspectos técnicos comunes:

- discovery vía Eureka (`lb://` + load balanced WebClient),
- token S2S automático,
- propagación de `X-Trace-Id` / `X-Correlation-Id`,
- mapeo explícito de errores 4xx/5xx por adapters + exception handlers.

Referencias:

- order DiscoveryWebClientConfig
- notification Directory adapter
- reporting Order lookup adapter

---

## 8. Cómo se comunican los servicios (async)

Plano async:

- productores core escriben `outbox_events`,
- relay scheduler publica a Kafka,
- consumidores derivados procesan y persisten `processed_events/resultados`,
- errores de consumo usan retry + DLQ (`.dlq`).

### Qué publica/consume

- `order-service` publica `order.events.v1` y `order.cart.events.v1`.
- `inventory/catalog/directory` publican sus mutaciones/estados.
- `notification-service` consume eventos relevantes (order/cart/inventory), crea `notification_requests` y publica eventos de notificación.
- `reporting-service` consume múltiples tópicos upstream, registra hecho (`analytic_facts`), aplica proyección y actualiza `consumer_checkpoints`.

### Trazabilidad/consistencia

- envelope de eventos incluye `eventId`, `eventType`, `organizationId`, `traceId`, `correlationId` (según contrato),
- dedupe por `processed_events`,
- checkpoint por topic/partition en reporting.

Referencias:

- kafka topics bootstrap
- notification kafka consumer
- reporting kafka consumer

---

## 9. Flujo de negocio principal end-to-end

Secuencia de compra protegida (modelo actual):

1. Cliente obtiene token (usuario o técnico según escenario de prueba).
2. Entra por Gateway a `order-service`.
3. `order-service` crea carrito.
4. Ajusta ítems del carrito.
5. Valida checkout:
   - llama sync a directory (contexto dirección/política),
   - llama sync a catalog (variante/precio),
   - llama sync a inventory (reserva/disponibilidad).
6. Crea pedido.
7. `order-service` persiste pedido y publica evento async.
8. `notification-service` consume y deriva request/notificación.
9. `reporting-service` consume hecho, normaliza, aplica y checkpoint.

Puntos de validación:

- `purchase_orders` (order DB),
- `notification_requests`/`notification_attempts` (notification DB),
- `analytic_facts` + `consumer_checkpoints` (reporting DB).

---

## 10. Notification-service explicado completamente

### Qué hace

- interpreta hechos relevantes,
- resuelve destinatario organizacional,
- aplica `ChannelPolicy`,
- selecciona `NotificationTemplate`,
- renderiza payload,
- crea `notification_request`,
- ejecuta dispatch (scheduler o endpoint),
- registra attempts/callbacks/auditoría,
- publica eventos de mutación de notificación.

### Qué NO hace

- no “es” el proveedor final de envío,
- llama un provider HTTP externo configurable.

### Cómo consume

- `OrderDomainEventKafkaConsumer` procesa eventos soportados y resuelve organization context.
- crea contexto técnico autenticado para que el use-case no dependa de sesión HTTP.

### Dispatch

- `NotificationDispatchScheduler` recorre pendientes y dispara `DispatchNotificationCommand`.
- `NotificationProviderHttpAdapter` hace POST al provider (`http://notification-provider:8088` por defecto).

### Estado core vs externo

- core de notificación (policy/template/request/attempt/audit) está dentro del servicio.
- la entrega final depende de provider externo.

Referencias:

- NotificationApplicationService
- NotificationDispatchScheduler
- NotificationProviderHttpAdapter

---

## 11. Reporting-service explicado completamente

### Qué consume

- hechos de directory, catalog, inventory, order y notification (topics upstream).

### Pipeline real

1. `register` del hecho en `analytic_facts` (normalización inicial).
2. `apply` del hecho sobre proyecciones (sales/replenishment/kpi).
3. `checkpoint` por consumidor/topic/partition.

### Qué guarda

- `analytic_facts`,
- proyecciones (`sales_projections`, `replenishment_projections`, `operations_kpi_projections`),
- `consumer_checkpoints`,
- auditoría y outbox.

### Por qué es derivado

- no controla la transacción core de compra,
- proyecta y consolida para operación/reportes.

### Cómo verificar que funciona

- crecimiento de `analytic_facts`,
- `fact_status` aplicado,
- offsets en `consumer_checkpoints`.

Referencias:

- UpstreamDomainEventKafkaConsumer
- ReportingApplicationService

---

## 12. Base de datos, seeds y baseline local

### Bases por servicio (en una instancia Postgres)

- `identity_access`
- `directory`
- `arkab2b_catalog`
- `inventory`
- `order`
- `arkab2b_notification`
- `arkab2b_reporting`

Referencia:

- postgres init script

### Datos base relevantes

- Organizaciones seed: `organization-demo` y `organization-phase6`.
- Directory: direcciones, contactos, `organization_country_policy`.
- Catalog: producto/variante vendible:
  - `variant-demo-coffee-500` / `SKU-DEMO-COFFEE-500`
  - `variant-phase6-coffee-500` / `SKU-PHASE6-COFFEE-500`
- Inventory: stock para ambas organizaciones.
- Notification: channel policies y templates para eventos core.
- Reporting: KPI seed inicial.

Referencias:

- directory seed
- catalog seed
- inventory seed
- notification seed

### Nota importante

Los IDs `organization-demo` y `organization-phase6` son IDs de baseline local/pruebas integradas, no “estados productivos”.

---

## 13. Calidad y operación actuales del sistema

### Calidad

- Cada servicio está configurado con `compile/test/jacoco`.
- Hay separación por capas y pruebas unit/integration por servicio.
- Contratos HTTP y exception handlers mapean 4xx/5xx de forma explícita.

### Operación

- Stack reproducible por scripts oficiales.
- Health/readiness/liveness configurados con probes.
- Smoke automatizado valida readiness, prometheus, config serving, eureka registry.
- Observabilidad mínima activa:
  - logs con `traceId/correlationId`,
  - métricas http/jvm/prometheus,
  - contadores async específicos (`arka.notification.kafka.order_events`, `arka.reporting.kafka.upstream_events`).
- Runbooks existentes para incidentes clave.

Referencias:

- observability baseline
- runbooks index

---

## 14. Cómo probar el sistema manualmente

### 1) Levantar stack

```bash
cd /Users/jose/Development/Java/arkab2b
./scripts/generate-local-jwt-keys.sh
./scripts/start-integrated-local.sh
./scripts/smoke-integrated-local.sh
```

### 2) Obtener token técnico con contexto organizacional

```bash
IAM=http://localhost:8081
TOKEN=$(curl -sS "$IAM/api/v1/internal/auth/service-token" \
  -H "Content-Type: application/json" \
  -d '{
    "clientId":"order-service",
    "clientSecret":"order-service-secret",
    "audience":"arka-b2b",
    "scopes":["order.read","order.write","directory.read","catalog.read","inventory.read","inventory.reserve","iam.permission.read"],
    "organizationId":"organization-phase6",
    "countryCode":"CO"
  }' | jq -r '.accessToken')
```

### 3) Compra protegida (gateway)

```bash
GW=http://localhost:8080
IDK=$(uuidgen)

CART=$(curl -sS "$GW/api/v1/carts" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Idempotency-Key: $IDK" \
  -H "Content-Type: application/json" \
  -d '{"userId":"owner-phase6"}')

CART_ID=$(echo "$CART" | jq -r '.cartId')
```

```bash
curl -sS -X PUT "$GW/api/v1/carts/$CART_ID/items" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Idempotency-Key: $(uuidgen)" \
  -H "Content-Type: application/json" \
  -d '{
    "items":[
      {
        "operation":"UPSERT",
        "variantId":"variant-phase6-coffee-500",
        "sku":"SKU-PHASE6-COFFEE-500",
        "qty":1,
        "unitPrice":19900,
        "currency":"COP"
      }
    ]
  }'
```

```bash
CHECKOUT_ID=$(uuidgen)
curl -sS "$GW/api/v1/carts/$CART_ID/checkout-validation" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Idempotency-Key: $(uuidgen)" \
  -H "Content-Type: application/json" \
  -d "{
    \"checkoutCorrelationId\":\"$CHECKOUT_ID\",
    \"addressId\":\"addr-organization-phase6-hq\",
    \"countryCode\":\"CO\"
  }"
```

```bash
ORDER=$(curl -sS "$GW/api/v1/orders" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Idempotency-Key: $(uuidgen)" \
  -H "Content-Type: application/json" \
  -d "{
    \"cartId\":\"$CART_ID\",
    \"checkoutCorrelationId\":\"$CHECKOUT_ID\",
    \"userId\":\"owner-phase6\"
  }")
echo "$ORDER" | jq
```

### 4) Verificar derivados en DB

```bash
docker exec -i arkab2b-postgres psql -U postgres -d arkab2b_notification -c \
"select notification_id,organization_id,source_event_type,status,attempt_count,created_at
 from notification_requests order by created_at desc limit 10;"
```

```bash
docker exec -i arkab2b-postgres psql -U postgres -d arkab2b_reporting -c \
"select fact_id,organization_id,source_event_type,fact_status,created_at
 from analytic_facts order by created_at desc limit 10;"
```

```bash
docker exec -i arkab2b-postgres psql -U postgres -d arkab2b_reporting -c \
"select consumer_name,topic,partition,last_processed_offset,updated_at
 from consumer_checkpoints order by updated_at desc limit 10;"
```

### 5) Apagar

```bash
./scripts/stop-integrated-local.sh
```

---

## 15. Problemas conocidos, límites y decisiones de diseño actuales

1. `notification-provider` no está implementado como módulo del repo/stack local oficial.
   - `notification-service` sí prepara request/attempt/política/render.
   - la entrega final depende del endpoint externo `http://notification-provider:8088` (configurable).

2. Seeds son baseline de integración local.
   - `organization-demo` y `organization-phase6` son IDs de prueba/operación local.
   - no representan catálogo ni maestros “productivos”.

3. Modelo actual es organization-centric con 1 organización activa por contexto de operación.
   - no está modelado un switch multi-organización por usuario como capacidad madura.

4. El stack está diseñado para operación local reproducible y validación de capacidades core/derivadas, no para hardening de producción enterprise (HA, rotación automática de secretos, etc.).

---

## 16. Resumen final para entenderlo rápido

ArkaB2B hoy es un sistema organization-centric de microservicios con control plane (`config-server`, `eureka`, `gateway`), core comercial (`iam`, `directory`, `catalog`, `inventory`, `order`) y derivados (`notification`, `reporting`) sobre Postgres/Redis/Kafka.

Se levanta con:

- `start-integrated-local.sh`,
- se valida con `smoke-integrated-local.sh`,
- se apaga con `stop-integrated-local.sh`.

Una compra pasa por Gateway + seguridad; order coordina sync con directory/catalog/inventory, persiste pedido y emite eventos; notification/reporting reaccionan async.

Se prueba manualmente con token técnico + endpoints de carts/checkout/orders, y se confirma por tablas de notification y reporting.

Estado práctico actual:

- core y derivados internos están estructurados y operables en local,
- la pieza final de entrega externa de notificaciones depende de provider fuera del stack oficial del repo.
