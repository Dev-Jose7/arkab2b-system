# ArkaB2B DigitalOcean Deployment Guide (Docker Compose)

## 1) Scope

This guide covers a practical cloud baseline for:

- DigitalOcean Droplet + Docker Compose
- Coolify host using the same compose files

The target is not enterprise HA yet; it is a controlled cloud baseline for
integration/E2E validation.

## 2) Compose files used

- Base stack: `docker-compose.yml`
- Cloud override: `docker-compose.cloud.yml`
- Cloud env template: `platform/stack/.env.cloud.example`

## 3) Prepare server

```bash
sudo apt-get update
sudo apt-get install -y git docker.io docker-compose-plugin
sudo usermod -aG docker "$USER"
newgrp docker
```

Clone repo and go to root:

```bash
git clone <your-repo-url> arkab2b
cd arkab2b
```

## 4) Configure cloud env

```bash
cp platform/stack/.env.cloud.example platform/stack/.env.cloud
```

Review at least:

- `ARKAB2B_API_GATEWAY_PORT`
- `ARKAB2B_PUBLIC_BIND_ADDRESS`
- `ARKAB2B_PRIVATE_BIND_ADDRESS=127.0.0.1`
- `APP_SECRETS_HOST_DIR`

## 5) Start stack

```bash
./scripts/start-digitalocean-compose.sh
```

This script:

1. loads `.env.cloud` (if present),
2. generates IAM keypair if missing,
3. builds all service jars,
4. starts compose with cloud override.

## 6) Validate remotely

Gateway readiness:

```bash
curl -sf http://127.0.0.1:${ARKAB2B_API_GATEWAY_PORT:-8080}/actuator/health/readiness
```

Gateway from public IP (if firewall allows):

```bash
curl -sf http://<droplet-public-ip>:${ARKAB2B_API_GATEWAY_PORT:-8080}/actuator/health/readiness
```

Container status:

```bash
docker compose -f docker-compose.yml -f docker-compose.cloud.yml ps
```

## 7) Stop stack

```bash
./scripts/stop-digitalocean-compose.sh
```

Optional volume reset:

```bash
ARKAB2B_REMOVE_VOLUMES=true ./scripts/stop-digitalocean-compose.sh
```

## 8) Exposure policy (recommended)

Public:

- `api-gateway` only

Private (Docker network only):

- `postgres`
- `redis`
- `kafka`
- `config-server`
- `eureka-server`
- `identity-access-service`
- `directory-service`
- `catalog-service`
- `inventory-service`
- `order-service`
- `notification-service`
- `reporting-service`

## 9) Firewall baseline (DigitalOcean)

Allow inbound:

- SSH (`22/tcp`) from trusted IPs
- Gateway port (`ARKAB2B_API_GATEWAY_PORT`, default `8080`)

Block inbound to all other stack ports.

## 10) Coolify notes

- Use compose files:
  - `docker-compose.yml`
  - `docker-compose.cloud.yml`
- Provide `.env.cloud` variables in Coolify environment.
- Keep volume mounts/persistence enabled for:
  - Postgres data
  - Kafka data
  - Redis data
