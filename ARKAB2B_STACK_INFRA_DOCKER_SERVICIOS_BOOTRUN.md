# ArkaB2B: Infra en Docker + Microservicios con `bootRun` (sin scripts)

Esta guía levanta:

- Infraestructura compartida en Docker: `postgres`, `redis`, `kafka`, `kafka-topic-init`
- Servicios Java en tu host con `./gradlew bootRun` (una terminal por servicio)

## 1. Ir a la raíz del repo

```bash
cd /Users/jose/Development/Java/arkab2b
```

## 2. Generar llaves JWT locales (manual, sin script)

```bash
mkdir -p platform/secrets/local
openssl genrsa -out platform/secrets/local/identity-access-private.pem 2048
openssl rsa -in platform/secrets/local/identity-access-private.pem -pubout -out platform/secrets/local/identity-access-public.pem
chmod 600 platform/secrets/local/identity-access-private.pem
chmod 644 platform/secrets/local/identity-access-public.pem
```

## 3. Levantar solo infraestructura base en Docker

```bash
docker compose up -d postgres redis kafka kafka-topic-init
```

Verificar:

```bash
docker compose ps
docker logs arkab2b-kafka-topic-init --tail 100
```

`arkab2b-kafka-topic-init` debe terminar con `exit code 0`.

## 4. Abrir terminales para microservicios

Abre 10 terminales (o pestañas) y usa esta convención:

- T1: `eureka-server`
- T2: `config-server`
- T3: `identity-access-service`
- T4: `directory-service`
- T5: `catalog-service`
- T6: `inventory-service`
- T7: `order-service`
- T8: `notification-service`
- T9: `reporting-service`
- T10: `api-gateway`

## 5. Variables comunes para servicios de negocio y gateway

En T3..T10, antes de arrancar cada servicio, exporta este bloque:

```bash
export SPRING_PROFILES_ACTIVE=local
export APP_CONFIG_SERVER_URI=http://localhost:8888
export APP_EUREKA_DEFAULT_ZONE=http://localhost:8761/eureka
export APP_LOCAL_REDIS_HOST=localhost
export APP_LOCAL_REDIS_PORT=56379
export APP_LOCAL_KAFKA_BOOTSTRAP_SERVERS=localhost:59092
export APP_SECURITY_JWT_JWKS_URI=http://localhost:8081/.well-known/jwks.json
export APP_SECURITY_S2S_IDENTITY_BASE_URL=http://localhost:8081

# Overrides host-to-host para evitar URLs por nombre de contenedor
export APP_EXTERNAL_IDENTITY_BASE_URL=http://localhost:8081
export APP_EXTERNAL_DIRECTORY_BASE_URL=http://localhost:8082
export APP_EXTERNAL_CATALOG_BASE_URL=http://localhost:8083
export APP_EXTERNAL_INVENTORY_BASE_URL=http://localhost:8084
export APP_EXTERNAL_ORDER_BASE_URL=http://localhost:8085
export APP_EXTERNAL_NOTIFICATION_BASE_URL=http://localhost:8086
```

## 6. Arrancar servicios en orden (bootRun)

### T1 — Eureka

```bash
cd /Users/jose/Development/Java/arkab2b/eureka-server
SERVER_PORT=8761 ./gradlew bootRun
```

### T2 — Config Server

```bash
cd /Users/jose/Development/Java/arkab2b/config-server
SPRING_PROFILES_ACTIVE=local \
SERVER_PORT=8888 \
APP_EUREKA_DEFAULT_ZONE=http://localhost:8761/eureka \
APP_CONFIG_REPO_PATH=file:/Users/jose/Development/Java/arkab2b/platform/config-repo \
./gradlew bootRun
```

### T3 — Identity Access (IAM)

```bash
cd /Users/jose/Development/Java/arkab2b/identity-access-service
export SPRING_PROFILES_ACTIVE=local
export APP_CONFIG_SERVER_URI=http://localhost:8888
export APP_EUREKA_DEFAULT_ZONE=http://localhost:8761/eureka
export APP_LOCAL_REDIS_HOST=localhost
export APP_LOCAL_REDIS_PORT=56379
export APP_LOCAL_KAFKA_BOOTSTRAP_SERVERS=localhost:59092
export APP_LOCAL_R2DBC_URL=r2dbc:postgresql://localhost:55432/identity_access
export APP_SECURITY_JWT_PRIVATE_KEY_PATH=file:/Users/jose/Development/Java/arkab2b/platform/secrets/local/identity-access-private.pem
export APP_SECURITY_JWT_PUBLIC_KEY_PATH=file:/Users/jose/Development/Java/arkab2b/platform/secrets/local/identity-access-public.pem
SERVER_PORT=8081 ./gradlew bootRun
```

### T4 — Directory

```bash
cd /Users/jose/Development/Java/arkab2b/directory-service
export SPRING_PROFILES_ACTIVE=local
export APP_CONFIG_SERVER_URI=http://localhost:8888
export APP_EUREKA_DEFAULT_ZONE=http://localhost:8761/eureka
export APP_LOCAL_REDIS_HOST=localhost
export APP_LOCAL_REDIS_PORT=56379
export APP_LOCAL_KAFKA_BOOTSTRAP_SERVERS=localhost:59092
export APP_SECURITY_JWT_JWKS_URI=http://localhost:8081/.well-known/jwks.json
export APP_SECURITY_S2S_IDENTITY_BASE_URL=http://localhost:8081
export APP_EXTERNAL_IDENTITY_BASE_URL=http://localhost:8081
export APP_LOCAL_R2DBC_URL=r2dbc:postgresql://localhost:55432/directory
SERVER_PORT=8082 ./gradlew bootRun
```

### T5 — Catalog

```bash
cd /Users/jose/Development/Java/arkab2b/catalog-service
export SPRING_PROFILES_ACTIVE=local
export APP_CONFIG_SERVER_URI=http://localhost:8888
export APP_EUREKA_DEFAULT_ZONE=http://localhost:8761/eureka
export APP_LOCAL_REDIS_HOST=localhost
export APP_LOCAL_REDIS_PORT=56379
export APP_LOCAL_KAFKA_BOOTSTRAP_SERVERS=localhost:59092
export APP_SECURITY_JWT_JWKS_URI=http://localhost:8081/.well-known/jwks.json
export APP_SECURITY_S2S_IDENTITY_BASE_URL=http://localhost:8081
export APP_EXTERNAL_IDENTITY_BASE_URL=http://localhost:8081
export APP_EXTERNAL_DIRECTORY_BASE_URL=http://localhost:8082
export APP_LOCAL_R2DBC_URL=r2dbc:postgresql://localhost:55432/arkab2b_catalog
SERVER_PORT=8083 ./gradlew bootRun
```

### T6 — Inventory

```bash
cd /Users/jose/Development/Java/arkab2b/inventory-service
export SPRING_PROFILES_ACTIVE=local
export APP_CONFIG_SERVER_URI=http://localhost:8888
export APP_EUREKA_DEFAULT_ZONE=http://localhost:8761/eureka
export APP_LOCAL_REDIS_HOST=localhost
export APP_LOCAL_REDIS_PORT=56379
export APP_LOCAL_KAFKA_BOOTSTRAP_SERVERS=localhost:59092
export APP_SECURITY_JWT_JWKS_URI=http://localhost:8081/.well-known/jwks.json
export APP_SECURITY_S2S_IDENTITY_BASE_URL=http://localhost:8081
export APP_EXTERNAL_IDENTITY_BASE_URL=http://localhost:8081
export APP_EXTERNAL_DIRECTORY_BASE_URL=http://localhost:8082
export APP_EXTERNAL_CATALOG_BASE_URL=http://localhost:8083
export APP_EXTERNAL_ORDER_BASE_URL=http://localhost:8085
export APP_LOCAL_R2DBC_URL=r2dbc:postgresql://localhost:55432/inventory
SERVER_PORT=8084 ./gradlew bootRun
```

### T7 — Order

```bash
cd /Users/jose/Development/Java/arkab2b/order-service
export SPRING_PROFILES_ACTIVE=local
export APP_CONFIG_SERVER_URI=http://localhost:8888
export APP_EUREKA_DEFAULT_ZONE=http://localhost:8761/eureka
export APP_LOCAL_REDIS_HOST=localhost
export APP_LOCAL_REDIS_PORT=56379
export APP_LOCAL_KAFKA_BOOTSTRAP_SERVERS=localhost:59092
export APP_SECURITY_JWT_JWKS_URI=http://localhost:8081/.well-known/jwks.json
export APP_SECURITY_S2S_IDENTITY_BASE_URL=http://localhost:8081
export APP_EXTERNAL_IDENTITY_BASE_URL=http://localhost:8081
export APP_EXTERNAL_DIRECTORY_BASE_URL=http://localhost:8082
export APP_EXTERNAL_CATALOG_BASE_URL=http://localhost:8083
export APP_EXTERNAL_INVENTORY_BASE_URL=http://localhost:8084
export APP_LOCAL_R2DBC_URL=r2dbc:postgresql://localhost:55432/order
SERVER_PORT=8085 ./gradlew bootRun
```

### T8 — Notification

```bash
cd /Users/jose/Development/Java/arkab2b/notification-service
export SPRING_PROFILES_ACTIVE=local
export APP_CONFIG_SERVER_URI=http://localhost:8888
export APP_EUREKA_DEFAULT_ZONE=http://localhost:8761/eureka
export APP_LOCAL_REDIS_HOST=localhost
export APP_LOCAL_REDIS_PORT=56379
export APP_LOCAL_KAFKA_BOOTSTRAP_SERVERS=localhost:59092
export APP_SECURITY_JWT_JWKS_URI=http://localhost:8081/.well-known/jwks.json
export APP_SECURITY_S2S_IDENTITY_BASE_URL=http://localhost:8081
export APP_EXTERNAL_IDENTITY_BASE_URL=http://localhost:8081
export APP_EXTERNAL_DIRECTORY_BASE_URL=http://localhost:8082
export APP_EXTERNAL_ORDER_BASE_URL=http://localhost:8085
export APP_LOCAL_R2DBC_URL=r2dbc:postgresql://localhost:55432/arkab2b_notification
SERVER_PORT=8086 ./gradlew bootRun
```

### T9 — Reporting

```bash
cd /Users/jose/Development/Java/arkab2b/reporting-service
export SPRING_PROFILES_ACTIVE=local
export APP_CONFIG_SERVER_URI=http://localhost:8888
export APP_EUREKA_DEFAULT_ZONE=http://localhost:8761/eureka
export APP_LOCAL_REDIS_HOST=localhost
export APP_LOCAL_REDIS_PORT=56379
export APP_LOCAL_KAFKA_BOOTSTRAP_SERVERS=localhost:59092
export APP_SECURITY_JWT_JWKS_URI=http://localhost:8081/.well-known/jwks.json
export APP_SECURITY_S2S_IDENTITY_BASE_URL=http://localhost:8081
export APP_EXTERNAL_IDENTITY_BASE_URL=http://localhost:8081
export APP_EXTERNAL_DIRECTORY_BASE_URL=http://localhost:8082
export APP_EXTERNAL_ORDER_BASE_URL=http://localhost:8085
export APP_LOCAL_R2DBC_URL=r2dbc:postgresql://localhost:55432/arkab2b_reporting
SERVER_PORT=8087 ./gradlew bootRun
```

### T10 — API Gateway

```bash
cd /Users/jose/Development/Java/arkab2b/api-gateway
export SPRING_PROFILES_ACTIVE=local
export APP_CONFIG_SERVER_URI=http://localhost:8888
export APP_EUREKA_DEFAULT_ZONE=http://localhost:8761/eureka
export APP_SECURITY_JWT_JWKS_URI=http://localhost:8081/.well-known/jwks.json
SERVER_PORT=8080 ./gradlew bootRun
```

## 7. Validaciones rápidas

Readiness:

```bash
curl -sf http://localhost:8761/actuator/health/readiness
curl -sf http://localhost:8888/actuator/health/readiness
curl -sf http://localhost:8081/actuator/health/readiness
curl -sf http://localhost:8082/actuator/health/readiness
curl -sf http://localhost:8083/actuator/health/readiness
curl -sf http://localhost:8084/actuator/health/readiness
curl -sf http://localhost:8085/actuator/health/readiness
curl -sf http://localhost:8086/actuator/health/readiness
curl -sf http://localhost:8087/actuator/health/readiness
curl -sf http://localhost:8080/actuator/health/readiness
```

Eureka registry:

```bash
curl -s http://localhost:8761/eureka/apps
```

Config server:

```bash
curl -s http://localhost:8888/order-service/local
```

## 8. Troubleshooting rápido

Logs Docker infra:

```bash
docker logs arkab2b-postgres --tail 100
docker logs arkab2b-redis --tail 100
docker logs arkab2b-kafka --tail 100
```

Si un `bootRun` falla por conexión:

- revisa que `APP_LOCAL_R2DBC_URL` apunte a `localhost:55432`,
- revisa que Redis sea `localhost:56379`,
- revisa que Kafka sea `localhost:59092`,
- confirma que IAM está arriba antes de los demás (`http://localhost:8081/.well-known/jwks.json`).

## 9. Apagado correcto

1. Detén cada `bootRun` con `Ctrl+C` (T1..T10).
2. Baja infraestructura Docker:

```bash
docker compose down --remove-orphans
```

Si quieres limpiar volúmenes:

```bash
docker compose down --remove-orphans --volumes
```
