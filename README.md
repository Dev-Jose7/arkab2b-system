# ArkaB2B

Backend de operación comercial B2B orientado a **organizaciones** (`organization-centric`) con arquitectura de microservicios, configuración distribuida y flujos síncronos + asíncronos.

## 1) ¿Qué es este sistema?

ArkaB2B implementa un flujo comercial B2B con estos pilares funcionales:

- autenticación, autorización y sesiones,
- contexto organizacional y políticas regionales,
- catálogo y variantes vendibles,
- inventario y disponibilidad comprometible,
- carrito y creación de pedidos,
- servicios derivados de notificación y reporting.

La unidad semántica del dominio es **Organization**.

- Las acciones operan sobre `organizationId`.
- Los actores ejecutan acciones en nombre de su organización.
- Los flujos core y derivados se resuelven por contexto organizacional.

## 2) Arquitectura y mapa de servicios

### Plataforma y borde

| Servicio | Puerto | Responsabilidad |
|---|---:|---|
| `config-server` | `8888` | Configuración distribuida por `spring.application.name` + perfil |
| `eureka-server` | `8761` | Registro y descubrimiento de servicios |
| `api-gateway` | `8080` | Borde HTTP, validación JWT, enrutamiento hacia servicios |

### Core de negocio

| Servicio | Puerto | Responsabilidad |
|---|---:|---|
| `identity-access-service` | `8081` | Login, refresh, logout, register-founder, JWKS y JWT con contexto organizacional |
| `directory-service` | `8082` | Organización, direcciones, contactos, política por país |
| `catalog-service` | `8083` | Productos, variantes, precios, resolución comercial |
| `inventory-service` | `8084` | Stock, reservas, disponibilidad comprometible |
| `order-service` | `8085` | Carritos, checkout validation, creación/ajuste/cancelación de pedido |

### Derivados

| Servicio | Puerto | Responsabilidad |
|---|---:|---|
| `notification-service` | `8086` | Consume eventos, aplica políticas/templates y genera dispatch requests |
| `reporting-service` | `8087` | Consume eventos upstream y materializa hechos/proyecciones |

### Infra compartida

| Componente | Puerto host por defecto | Uso |
|---|---:|---|
| PostgreSQL | `55432` | Persistencia por servicio (una base por contexto) |
| Redis | `56379` | Cache/soporte operativo |
| Kafka | `59092` | Integración asíncrona entre contextos |

## 3) Stack técnico

- Java 21
- Spring Boot 3.5.4
- Spring Cloud 2025.0.0
- WebFlux reactivo
- Spring Security + OAuth2 Resource Server + JOSE
- R2DBC + PostgreSQL
- Kafka
- Redis
- Micrometer + Actuator + Prometheus endpoint
- OpenAPI/Swagger (springdoc)

## 4) Estructura del repositorio

```text
.
├─ api-gateway/
├─ config-server/
├─ eureka-server/
├─ identity-access-service/
├─ directory-service/
├─ catalog-service/
├─ inventory-service/
├─ order-service/
├─ notification-service/
├─ reporting-service/
├─ infra/
│  ├─ postgres/init-arkab2b.sql
│  └─ kafka/bootstrap-topics.sh
├─ platform/
│  ├─ config-repo/
│  ├─ operations/
│  ├─ secrets/
│  └─ stack/
├─ scripts/
├─ docker-compose.yml
└─ docker-compose.cloud.yml
```

## 5) Configuración distribuida (cómo funciona)

Cada servicio arranca con:

- `spring.application.name`
- `spring.config.import=optional:configserver:...`
- `SPRING_PROFILES_ACTIVE` (`local` en stack local, `docker` en cloud compose)

Con Config Server activo, la resolución por servicio/perfil sigue este orden lógico:

1. `application.yml`
2. `application-<profile>.yml`
3. `<service>.yml`
4. `<service>-<profile>.yml`

`platform/config-repo` es la fuente central de configuración funcional/operativa.
Los `application*.yml` internos de cada servicio actúan como bootstrap/fallback.

## 6) Seguridad end-to-end

### Borde (Gateway)

- `api-gateway` valida JWT con JWKS de IAM.
- Expone públicos mínimos: login/refresh/register-founder/introspect + JWKS + health/docs.
- Rutas de negocio requieren autenticación.

### IAM

- Emite JWT de usuario.
- Publica JWKS en `/.well-known/jwks.json`.
- Incluye `organizationId` y `countryCode` cuando login/refresh reciben contexto organizacional.

### Servicios internos

- Validan JWT/JWKS y audiencias.
- Reconstruyen contexto de seguridad desde claims (`roles`, `permissions`, `scope/scp`, `organizationId` cuando aplica).
- Blindan endpoints con autorización explícita y reglas de aplicación.

### Sync interno

- Las integraciones HTTP internas propagan el bearer JWT ya autenticado desde el request entrante.
- IAM emite JWT de usuario con contexto organizacional cuando el login/refresh lo recibe.

### Async y schedulers

- Consumidores Kafka y schedulers crean contexto autenticado interno en proceso.
- Ese contexto no depende de un token emitido por IAM y solo existe para ejecutar casos de uso sin request HTTP.

## 7) Integración sync y async

### Sync clave

- `order -> directory` (resolución checkout y contexto regional)
- `order -> catalog` (resolución de variante/precio)
- `order -> inventory` (validación reserva/disponibilidad)
- `notification -> directory` (resolución de destinatarios)
- `notification -> order` (lookup de contexto por order/cart)
- `reporting -> directory` y `reporting -> order` (enriquecimiento contextual)

### Async (Kafka)

Productores core y derivados publican/consumen tópicos como:

- `order.events.v1`, `order.cart.events.v1`
- `inventory.*`
- `catalog.*`
- `directory.*`
- `notification.*`
- `reporting.*`

Infra incluye bootstrap de tópicos + DLQ (`*.dlq`) vía `infra/kafka/bootstrap-topics.sh`.

## 8) Baseline de datos local (seeds)

El stack local inicia con datos semilla útiles para pruebas integradas:

- organizaciones: `organization-demo`, `organization-phase6`
- direcciones: `addr-organization-demo-hq`, `addr-organization-phase6-hq`
- políticas de país por organización
- catálogo con variante vendible:
  - `variant-demo-coffee-500` (`SKU-DEMO-COFFEE-500`)
  - `variant-phase6-coffee-500` (`SKU-PHASE6-COFFEE-500`)
- inventario con stock para esos SKU
- políticas y templates de notificación para eventos de pedido/carrito/inventario

## 9) Levantar el sistema en local (recomendado)

### Prerrequisitos

- Docker + Docker Compose
- Java 21
- `curl`
- `openssl`

### Flujo oficial

```bash
./scripts/generate-local-jwt-keys.sh
./scripts/start-integrated-local.sh
./scripts/smoke-integrated-local.sh
```

Detener:

```bash
./scripts/stop-integrated-local.sh
```

Detener y borrar volúmenes:

```bash
ARKAB2B_REMOVE_VOLUMES=true ./scripts/stop-integrated-local.sh
```

### Checks rápidos

```bash
curl -sf http://localhost:8888/actuator/health/readiness
curl -sf http://localhost:8761/actuator/health/readiness
curl -sf http://localhost:8080/actuator/health/readiness
curl -s  http://localhost:8761/eureka/apps
curl -s  http://localhost:8888/order-service/local
```

## 10) Levantar con Docker Compose sin scripts

Referencia completa:

- `ARKAB2B_STACK_DOCKER_COMPOSE_SIN_SCRIPTS.md`

Resumen:

1. generar llaves JWT locales,
2. construir `bootJar` de los 10 servicios,
3. levantar `postgres redis kafka kafka-topic-init`,
4. levantar servicios en orden,
5. validar readiness/Eureka/Config.

## 11) Despliegue remoto (DigitalOcean)

Ya existe baseline cloud con override compose y scripts remotos:

- `docker-compose.yml` (base)
- `docker-compose.cloud.yml` (override cloud)
- `platform/stack/.env.cloud.example`
- `scripts/start-digitalocean-compose.sh`
- `scripts/stop-digitalocean-compose.sh`
- `platform/stack/DIGITALOCEAN_DEPLOYMENT.md`

Flujo rápido en Droplet:

```bash
cp platform/stack/.env.cloud.example platform/stack/.env.cloud
./scripts/start-digitalocean-compose.sh
```

En cloud debe exponerse públicamente solo `api-gateway` (más SSH).

## 12) OpenAPI / Swagger

Cada servicio de negocio expone:

- Swagger UI: `/swagger-ui.html`
- OpenAPI JSON: `/v1/api-docs`

URLs locales típicas:

- IAM: `http://localhost:8081/swagger-ui.html`
- Directory: `http://localhost:8082/swagger-ui.html`
- Catalog: `http://localhost:8083/swagger-ui.html`
- Inventory: `http://localhost:8084/swagger-ui.html`
- Order: `http://localhost:8085/swagger-ui.html`
- Notification: `http://localhost:8086/swagger-ui.html`
- Reporting: `http://localhost:8087/swagger-ui.html`

También existe un hub local por tabs:

- `ARKAB2B_SWAGGER_TABS.html`

Nota: para embeber Swagger en iframe, los servicios deshabilitan `X-Frame-Options` únicamente en perfil `local`.

## 13) Build y tests

No hay build raíz único; se ejecuta por servicio.

### Build de todos los servicios

```bash
for s in eureka-server config-server identity-access-service directory-service catalog-service inventory-service order-service notification-service reporting-service api-gateway; do
  (cd "$s" && ./gradlew --no-daemon bootJar)
done
```

### Tests por servicio

```bash
(cd identity-access-service && ./gradlew --no-daemon test jacocoTestReport)
(cd directory-service && ./gradlew --no-daemon test jacocoTestReport)
(cd catalog-service && ./gradlew --no-daemon test jacocoTestReport)
(cd inventory-service && ./gradlew --no-daemon test jacocoTestReport)
(cd order-service && ./gradlew --no-daemon test jacocoTestReport)
(cd notification-service && ./gradlew --no-daemon test jacocoTestReport)
(cd reporting-service && ./gradlew --no-daemon test jacocoTestReport)
(cd api-gateway && ./gradlew --no-daemon test)
(cd config-server && ./gradlew --no-daemon test)
(cd eureka-server && ./gradlew --no-daemon test)
```

## 14) Observabilidad y operación

Artefactos operativos:

- `platform/operations/observability/observability-baseline.md`
- `platform/operations/runbooks/*.md`

Incluye baseline de:

- health/readiness/liveness
- métricas prometheus
- trazabilidad (`traceId`/`correlationId`)
- runbooks para incidentes frecuentes

## 15) Documentación de negocio/dominio/arquitectura

La base metodológica del MVP está en:

- `.content/mvp/00-producto`
- `.content/mvp/01-dominio`
- `.content/mvp/02-arquitectura`
- `.content/mvp/03-calidad`
- `.content/mvp/04-operacion`

Documentación técnica consolidada del estado implementado:

- `ARKAB2B_GUIA_SISTEMA_ACTUAL.md`

## 16) Limitaciones conocidas

- `notification-service` está cerrado en core (policy/template/request/attempt), pero el envío final depende de provider externo HTTP (`APP_EXTERNAL_PROVIDER_BASE_URL`, default `http://notification-provider:8088`).
- El baseline local está orientado a integración y validación MVP, no a hardening enterprise completo.

---

Si quieres una guía operacional más profunda (auditoría, flujo E2E y diagnóstico), usa primero `ARKAB2B_GUIA_SISTEMA_ACTUAL.md` y luego los runbooks en `platform/operations/runbooks/`.
