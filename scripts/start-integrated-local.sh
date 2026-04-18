#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
RUN_DIR="${ROOT_DIR}/.run"
mkdir -p "${RUN_DIR}"

POSTGRES_PORT="${ARKAB2B_POSTGRES_PORT:-55432}"
REDIS_PORT="${ARKAB2B_REDIS_PORT:-56379}"
KAFKA_PORT="${ARKAB2B_KAFKA_PORT:-59092}"

DEFAULT_SHARED_TOKEN="eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6ImRldi1yc2Eta2V5LTEifQ.eyJzdWIiOiJzdmMtYXJrYS1zeXN0ZW0iLCJpc3MiOiJpZGVudGl0eS1hY2Nlc3Mtc2VydmljZSIsImF1ZCI6ImFya2EtYjJiIiwiaWF0IjoxNzc2NDYxOTMzLCJuYmYiOjE3NzY0NjE4NzMsImV4cCI6MjA5MTgyMTkzMywidGVuYW50X2lkIjoidGVuYW50LWRlbW8iLCJ0ZW5hbnRJZCI6InRlbmFudC1kZW1vIiwib3JnYW5pemF0aW9uX2lkIjoidGVuYW50LWRlbW8iLCJvcmdhbml6YXRpb25JZCI6InRlbmFudC1kZW1vIiwiY291bnRyeV9jb2RlIjoiQ08iLCJjb3VudHJ5Q29kZSI6IkNPIiwicm9sZXMiOlsiVFJVU1RFRF9TRVJWSUNFIiwiQVJLQV9BRE1JTiIsIk9SREVSX0FETUlOIiwiSU5WRU5UT1JZX0FETUlOIiwiRElSRUNUT1JZX0FETUlOIiwiQ0FUQUxPR19BRE1JTiIsIk5PVElGSUNBVElPTl9BRE1JTiIsIlJFUE9SVElOR19BRE1JTiJdLCJwZXJtaXNzaW9ucyI6WyJpYW0ucGVybWlzc2lvbi5yZWFkIiwiaWFtLnVzZXIucmVhZCIsImlhbS51c2VyLmNyZWF0ZSIsImlhbS51c2VyLnVwZGF0ZSIsImlhbS51c2VyLmFzc2lnbi1yb2xlIiwiaWFtLnVzZXIuYmxvY2siLCJpYW0uc2Vzc2lvbi5yZWFkIiwiaWFtLnNlc3Npb24ucmV2b2tlIiwiZGlyZWN0b3J5Lm9yZ2FuaXphdGlvbi5yZWFkIiwiZGlyZWN0b3J5Lm9yZ2FuaXphdGlvbi51cGRhdGUiLCJkaXJlY3RvcnkucHJvZmlsZS5yZWFkIiwiZGlyZWN0b3J5LnByb2ZpbGUuY3JlYXRlIiwiZGlyZWN0b3J5LnByb2ZpbGUudXBkYXRlIiwiaW52ZW50b3J5LnJlYWQiLCJpbnZlbnRvcnkud3JpdGUiLCJvcmRlci5yZWFkIiwib3JkZXIud3JpdGUiLCJvcmRlci5hZG1pbiJdLCJzY29wZSI6InNlcnZpY2UgdHJ1c3RlZCJ9.kM0_7VTsCtmA7EsMgcfIIFBEoOOnLQMs4MGM8bVLJD6_ZXAdlEjHQ5hrgfkdZmc0dHLqSF2u7Vjkju_bTmpFB-7Rrw4oLrl2CCVz6E5ZxlUwdMGMdC6Mlf-CWoYs15pmgzMCtjPi4GfE1DBBZRDC4FgejR7aXOoR62sKIPM6qZC-Ey3uZ0AY9G6gf-JdaeQKik1qFgGab_P52BWmLv6NDfzZBqbXvTJ9GMTKGxZWH4U8TqQoQolQHnNSd1CKqB37Jx7zFCgRutafdsbA-HdHRftrBGxcQiyNedB9kClRtT--MKE_xZL9HzexKg3ke_718I59lFPekf2v-MMpWIlAOQ"
APP_SECURITY_SERVICE_SHARED_TOKEN="${APP_SECURITY_SERVICE_SHARED_TOKEN:-${DEFAULT_SHARED_TOKEN}}"

echo "==> Starting shared infrastructure"
docker compose -f "${ROOT_DIR}/docker-compose.yml" up -d postgres redis kafka kafka-topic-init

wait_kafka_topics() {
  local attempts=0
  local max_attempts=60
  local container_name="arkab2b-kafka-topic-init"

  until [[ "${attempts}" -ge "${max_attempts}" ]]; do
    local status
    status="$(docker inspect -f '{{.State.Status}}' "${container_name}" 2>/dev/null || true)"
    if [[ "${status}" == "exited" ]]; then
      local exit_code
      exit_code="$(docker inspect -f '{{.State.ExitCode}}' "${container_name}" 2>/dev/null || true)"
      if [[ "${exit_code}" == "0" ]]; then
        echo "==> Kafka topics initialized"
        return 0
      fi
      echo "!! Kafka topic init failed (exit=${exit_code})"
      docker logs "${container_name}" || true
      return 1
    fi
    attempts=$((attempts + 1))
    sleep 1
  done

  echo "!! Kafka topic init did not finish in time"
  docker logs "${container_name}" || true
  return 1
}

start_service() {
  local service_dir="$1"
  local service_name="$2"
  local port="$3"
  local database_name="$4"
  local database_user="$5"
  local database_password="$6"
  local pid_file="${RUN_DIR}/${service_name}.pid"
  local log_file="${RUN_DIR}/${service_name}.log"
  local fallback_actor_enabled="false"
  local fallback_tenant_id=""
  local fallback_country_code=""

  if [[ "${service_name}" == "notification-service" || "${service_name}" == "reporting-service" ]]; then
    fallback_actor_enabled="true"
    fallback_tenant_id="tenant-demo"
    fallback_country_code="CO"
  fi

  if [[ -f "${pid_file}" ]]; then
    local current_pid
    current_pid="$(cat "${pid_file}")"
    if [[ -n "${current_pid}" ]] && kill -0 "${current_pid}" 2>/dev/null; then
      echo "==> ${service_name} already running (pid=${current_pid})"
      return
    fi
  fi

  echo "==> Starting ${service_name} on port ${port}"
  nohup bash -c "
    cd '${ROOT_DIR}/${service_dir}'
    export APP_SECURITY_SERVICE_SHARED_TOKEN='${APP_SECURITY_SERVICE_SHARED_TOKEN}'
    export SPRING_PROFILES_ACTIVE='local'
    export APP_DATABASE_SCHEMA_INITIALIZE_ON_STARTUP='true'
    export APP_LOCAL_R2DBC_URL='r2dbc:postgresql://localhost:${POSTGRES_PORT}/${database_name}'
    export APP_LOCAL_R2DBC_USERNAME='${database_user}'
    export APP_LOCAL_R2DBC_PASSWORD='${database_password}'
    export APP_LOCAL_REDIS_HOST='localhost'
    export APP_LOCAL_REDIS_PORT='${REDIS_PORT}'
    export APP_LOCAL_REDIS_PASSWORD=''
    export APP_LOCAL_KAFKA_BOOTSTRAP_SERVERS='localhost:${KAFKA_PORT}'
    export APP_SECURITY_ACTOR_FALLBACK_ENABLED='${fallback_actor_enabled}'
    export APP_SECURITY_ACTOR_FALLBACK_TENANT_ID='${fallback_tenant_id}'
    export APP_SECURITY_ACTOR_FALLBACK_COUNTRY_CODE='${fallback_country_code}'
    export SERVER_PORT='${port}'
    export GRADLE_USER_HOME='${ROOT_DIR}/${service_dir}/.gradle-user'
    exec ./gradlew bootRun --no-daemon -q
  " >"${log_file}" 2>&1 < /dev/null &
  echo "$!" >"${pid_file}"
}

wait_ready() {
  local service_name="$1"
  local port="$2"
  local health_url="http://localhost:${port}/actuator/health/readiness"
  local attempts=0
  local max_attempts=120

  until [[ "${attempts}" -ge "${max_attempts}" ]]; do
    local code
    code="$(curl -s -o /dev/null -w "%{http_code}" "${health_url}" || true)"
    if [[ "${code}" == "200" ]]; then
      echo "==> ${service_name} readiness OK (${health_url})"
      return 0
    fi
    attempts=$((attempts + 1))
    sleep 2
  done

  echo "!! ${service_name} did not become ready (${health_url})"
  tail -n 80 "${RUN_DIR}/${service_name}.log" || true
  return 1
}

wait_kafka_topics

start_service "identity-access-service" "identity-access-service" "8081" "identity_access" "identity_access" "identity_access"
start_service "directory-service" "directory-service" "8082" "directory" "directory" "directory"
start_service "catalog-service" "catalog-service" "8083" "arkab2b_catalog" "catalog" "catalog"
start_service "inventory-service" "inventory-service" "8084" "inventory" "inventory" "inventory"
start_service "order-service" "order-service" "8085" "order" "order" "order"
start_service "notification-service" "notification-service" "8086" "arkab2b_notification" "notification" "notification"
start_service "reporting-service" "reporting-service" "8087" "arkab2b_reporting" "reporting" "reporting"

wait_ready "identity-access-service" "8081"
wait_ready "directory-service" "8082"
wait_ready "catalog-service" "8083"
wait_ready "inventory-service" "8084"
wait_ready "order-service" "8085"
wait_ready "notification-service" "8086"
wait_ready "reporting-service" "8087"

echo "==> Integrated local stack is ready"
