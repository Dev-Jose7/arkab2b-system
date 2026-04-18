# ArkaB2B Integrated Stack (Phase 4 Baseline)

## Scope
This baseline provides a reproducible local integrated stack for:

- `config-server`
- `eureka-server`
- `api-gateway`
- `identity-access-service`
- `directory-service`
- `catalog-service`
- `inventory-service`
- `order-service`
- `notification-service`
- `reporting-service`
- Shared infrastructure: PostgreSQL, Redis, Kafka

## Official commands

From repo root:

```bash
./scripts/generate-local-jwt-keys.sh
./scripts/start-integrated-local.sh
./scripts/smoke-integrated-local.sh
./scripts/stop-integrated-local.sh
```

## DigitalOcean / Remote Compose (Droplet or Coolify host)

This repository now includes a cloud override compose file to avoid exposing
internal services publicly and to run service profiles as `docker`.

Files:

- `docker-compose.yml` (base stack)
- `docker-compose.cloud.yml` (cloud override)
- `platform/stack/.env.cloud.example` (remote env template)
- `scripts/start-digitalocean-compose.sh`
- `scripts/stop-digitalocean-compose.sh`

Recommended flow in a remote Linux host:

```bash
cp platform/stack/.env.cloud.example platform/stack/.env.cloud
./scripts/start-digitalocean-compose.sh
```

Shutdown:

```bash
./scripts/stop-digitalocean-compose.sh
```

Important:

- Expose only `api-gateway` publicly.
- Set `ARKAB2B_PRIVATE_BIND_ADDRESS=127.0.0.1` in `.env.cloud` so internal
  ports stay private on the host.
- Keep `postgres`, `redis`, `kafka`, `config-server`, `eureka-server`, and
  business services private to Docker network.
- Configure firewall/security group in DigitalOcean to allow inbound
  only to gateway port (and SSH).

Optional:

- Run smoke automatically after startup:

```bash
ARKAB2B_RUN_SMOKE=true ./scripts/start-integrated-local.sh
```

- Remove infrastructure volumes on shutdown:

```bash
ARKAB2B_REMOVE_VOLUMES=true ./scripts/stop-integrated-local.sh
```

## Port matrix (local default)

- `config-server`: `8888`
- `eureka-server`: `8761`
- `api-gateway`: `8080`
- `identity-access-service`: `8081`
- `directory-service`: `8082`
- `catalog-service`: `8083`
- `inventory-service`: `8084`
- `order-service`: `8085`
- `notification-service`: `8086`
- `reporting-service`: `8087`
- `postgres`: `55432`
- `redis`: `56379`
- `kafka`: `59092`

## Startup order

1. Shared infra containers (`postgres`, `redis`, `kafka`, topic bootstrap)
2. `eureka-server`
3. `config-server`
4. `identity-access-service`
5. `directory-service`
6. `catalog-service`
7. `inventory-service`
8. `order-service`
9. `notification-service`
10. `reporting-service`
11. `api-gateway`

## Readiness policy by service

- `config-server`: `readinessState + ping` (control plane ready)
- `eureka-server`: `readinessState + ping` (registry ready)
- `api-gateway`: `readinessState + discoveryComposite` (routing plane ready)
- `identity-access-service`: `readinessState + db + redis + kafka` (all critical)
- `directory-service`: `readinessState + db + kafka` (`redis` degradable cache)
- `catalog-service`: `readinessState + db + kafka` (`redis` degradable cache)
- `inventory-service`: `readinessState + db + kafka` (`redis` degradable cache)
- `order-service`: `readinessState + db + kafka` (`redis` degradable cache)
- `notification-service`: `readinessState + db + kafka` (`redis` degradable cache)
- `reporting-service`: `readinessState + db + kafka` (`redis` degradable cache)

## What this baseline validates

- Coordinated startup and non-colliding ports
- Distributed config served from config-server
- Service registration into eureka
- Health/readiness endpoints reachable
- Core infra connectivity baseline

## Observability baseline (Phase 5)

- Prometheus endpoint exposed in services via actuator: `/actuator/prometheus`
- Trace sampling baseline: `management.tracing.sampling.probability=1.0`
- Correlation headers propagated internally:
  - `X-Trace-Id`
  - `X-Correlation-Id`
- Async consumers expose operational counters:
  - `arka.notification.kafka.order_events`
  - `arka.reporting.kafka.upstream_events`
- HTTP downstream operational metrics:
  - `arka.http.client.requests`

## What is intentionally deferred to Phase 6

- Full multi-service E2E certification
- Exhaustive business-flow validation
- Final cross-service contract certification pass
