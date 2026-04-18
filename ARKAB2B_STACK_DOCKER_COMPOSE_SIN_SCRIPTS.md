# Opción recomendada: todo el stack con docker compose (sin scripts)

## 1. Ir a la raíz del repo

```bash
cd /Users/jose/Development/Java/arkab2b
```

## 2. (Opcional) cargar variables de puertos/config

Si quieres usar overrides del archivo de stack:

```bash
cp platform/stack/.env.example platform/stack/.env
set -a
source platform/stack/.env
set +a
```

Si no haces esto, se usan los puertos por defecto del `docker-compose.yml`.

## 3. Generar llaves JWT locales manualmente (sin script)

```bash
mkdir -p platform/secrets/local
openssl genrsa -out platform/secrets/local/identity-access-private.pem 2048
openssl rsa -in platform/secrets/local/identity-access-private.pem -pubout -out platform/secrets/local/identity-access-public.pem
chmod 600 platform/secrets/local/identity-access-private.pem
chmod 644 platform/secrets/local/identity-access-public.pem
```

## 4. Construir los jars de todos los servicios

`docker-compose` monta `build/libs`, así que primero hay que compilar:

```bash
(cd eureka-server && ./gradlew --no-daemon bootJar)
(cd config-server && ./gradlew --no-daemon bootJar)
(cd identity-access-service && ./gradlew --no-daemon bootJar)
(cd directory-service && ./gradlew --no-daemon bootJar)
(cd catalog-service && ./gradlew --no-daemon bootJar)
(cd inventory-service && ./gradlew --no-daemon bootJar)
(cd order-service && ./gradlew --no-daemon bootJar)
(cd notification-service && ./gradlew --no-daemon bootJar)
(cd reporting-service && ./gradlew --no-daemon bootJar)
(cd api-gateway && ./gradlew --no-daemon bootJar)
```

## 5. Levantar infraestructura base

```bash
docker compose up -d postgres redis kafka kafka-topic-init
```

Ver estado:

```bash
docker compose ps
docker logs arkab2b-kafka-topic-init --tail 100
```

`kafka-topic-init` debe terminar con exit code `0`.

## 6. Levantar servicios en orden

```bash
docker compose up -d eureka-server
docker compose up -d config-server
docker compose up -d identity-access-service
docker compose up -d directory-service
docker compose up -d catalog-service
docker compose up -d inventory-service
docker compose up -d order-service
docker compose up -d notification-service
docker compose up -d reporting-service
docker compose up -d api-gateway
```

## 7. Validar que todo quedó arriba

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

Registro Eureka:

```bash
curl -s http://localhost:8761/eureka/apps
```

Config Server sirviendo config:

```bash
curl -s http://localhost:8888/order-service/local
```

## 8. Ver logs si algo falla

```bash
docker logs arkab2b-order-service --tail 200
docker logs arkab2b-api-gateway --tail 200
docker logs arkab2b-identity-access-service --tail 200
```

## 9. Detener todo

```bash
docker compose down --remove-orphans
```

Si quieres borrar volúmenes también:

```bash
docker compose down --remove-orphans --volumes
```
