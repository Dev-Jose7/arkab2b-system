#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
RUN_DIR="${ROOT_DIR}/.run"
STACK_ENV_FILE="${ARKAB2B_STACK_ENV_FILE:-${ROOT_DIR}/platform/stack/.env}"
mkdir -p "${RUN_DIR}"

if [[ -f "${STACK_ENV_FILE}" ]]; then
  set -a
  # shellcheck disable=SC1090
  source "${STACK_ENV_FILE}"
  set +a
fi

ensure_command() {
  local binary="$1"
  if ! command -v "${binary}" >/dev/null 2>&1; then
    echo "Missing required command: ${binary}"
    exit 1
  fi
}

ensure_command docker
ensure_command curl
ensure_command java

POSTGRES_PORT="${ARKAB2B_POSTGRES_PORT:-55432}"
REDIS_PORT="${ARKAB2B_REDIS_PORT:-56379}"
KAFKA_PORT="${ARKAB2B_KAFKA_PORT:-59092}"
CONFIG_SERVER_PORT="${ARKAB2B_CONFIG_SERVER_PORT:-8888}"
EUREKA_SERVER_PORT="${ARKAB2B_EUREKA_SERVER_PORT:-8761}"
API_GATEWAY_PORT="${ARKAB2B_API_GATEWAY_PORT:-8080}"
IAM_PORT="${ARKAB2B_IAM_PORT:-8081}"
DIRECTORY_PORT="${ARKAB2B_DIRECTORY_PORT:-8082}"
CATALOG_PORT="${ARKAB2B_CATALOG_PORT:-8083}"
INVENTORY_PORT="${ARKAB2B_INVENTORY_PORT:-8084}"
ORDER_PORT="${ARKAB2B_ORDER_PORT:-8085}"
NOTIFICATION_PORT="${ARKAB2B_NOTIFICATION_PORT:-8086}"
REPORTING_PORT="${ARKAB2B_REPORTING_PORT:-8087}"

APP_CONFIG_SERVER_URI="${APP_CONFIG_SERVER_URI:-http://localhost:${CONFIG_SERVER_PORT}}"
APP_EUREKA_DEFAULT_ZONE="${APP_EUREKA_DEFAULT_ZONE:-http://localhost:${EUREKA_SERVER_PORT}/eureka}"
READY_MAX_ATTEMPTS="${ARKAB2B_READY_MAX_ATTEMPTS:-240}"
REGISTER_MAX_ATTEMPTS="${ARKAB2B_REGISTER_MAX_ATTEMPTS:-120}"
KAFKA_TOPIC_INIT_MAX_ATTEMPTS="${ARKAB2B_KAFKA_TOPIC_INIT_MAX_ATTEMPTS:-240}"
RUN_SMOKE="${ARKAB2B_RUN_SMOKE:-false}"
LOCAL_SECRETS_DIR="${APP_LOCAL_SECRETS_DIR:-${ROOT_DIR}/platform/secrets/local}"
if [[ "${LOCAL_SECRETS_DIR}" != /* ]]; then
  LOCAL_SECRETS_DIR="${ROOT_DIR}/${LOCAL_SECRETS_DIR#./}"
fi

wait_container_healthy() {
  local container_name="$1"
  local attempts=0
  local max_attempts="${2:-120}"
  until [[ "${attempts}" -ge "${max_attempts}" ]]; do
    local status
    status="$(docker inspect -f '{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' "${container_name}" 2>/dev/null || true)"
    if [[ "${status}" == "healthy" || "${status}" == "running" ]]; then
      echo "==> ${container_name} healthy (${status})"
      return 0
    fi
    attempts=$((attempts + 1))
    sleep 2
  done

  echo "!! ${container_name} did not reach healthy state"
  docker logs "${container_name}" || true
  return 1
}

wait_kafka_topics() {
  local attempts=0
  local max_attempts="${KAFKA_TOPIC_INIT_MAX_ATTEMPTS}"
  local container_name="arkab2b-kafka-topic-init"
  local kafka_container="arkab2b-kafka"

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
    if [[ "${status}" == "running" ]]; then
      local topic_list
      topic_list="$(docker exec "${kafka_container}" /opt/kafka/bin/kafka-topics.sh --bootstrap-server kafka:9092 --list 2>/dev/null || true)"
      if printf '%s\n' "${topic_list}" | grep -Fxq "order.events.v1" \
        && printf '%s\n' "${topic_list}" | grep -Fxq "order.events.v1.dlq" \
        && printf '%s\n' "${topic_list}" | grep -Fxq "reporting.mutation.v1"; then
        echo "==> Kafka topics initialized"
        return 0
      fi
    fi
    attempts=$((attempts + 1))
    sleep 1
  done

  echo "!! Kafka topic init did not finish in time"
  docker logs "${container_name}" || true
  return 1
}

graceful_stop_pid() {
  local pid="$1"
  if [[ -z "${pid}" ]]; then
    return 0
  fi
  if ! kill -0 "${pid}" 2>/dev/null; then
    return 0
  fi

  kill "${pid}" 2>/dev/null || true
  for _ in {1..10}; do
    if ! kill -0 "${pid}" 2>/dev/null; then
      return 0
    fi
    sleep 1
  done

  kill -9 "${pid}" 2>/dev/null || true
}

is_service_local_process() {
  local command="$1"
  local service_dir="$2"
  local service_name="$3"

  [[ "${command}" == *"${service_dir}/build/libs/${service_name}-0.1.0-SNAPSHOT.jar"* \
    || "${command}" == *"${service_dir}"*"/gradlew"* ]]
}

is_docker_port_proxy() {
  local command="$1"
  [[ "${command}" == *"com.docker.backend"* || "${command}" == *"docker-proxy"* ]]
}

stop_stale_service_processes() {
  local service_dir="$1"
  local service_name="$2"
  local port="$3"
  local pid_file="${RUN_DIR}/${service_name}.pid"
  local jar_pattern="${ROOT_DIR}/${service_dir}/build/libs/${service_name}-0.1.0-SNAPSHOT.jar"
  local -a candidate_pids=()
  local seen=" "
  if [[ -f "${pid_file}" ]]; then
    candidate_pids+=("$(cat "${pid_file}")")
  fi

  while IFS= read -r pid; do
    candidate_pids+=("${pid}")
  done < <(lsof -tiTCP:"${port}" -sTCP:LISTEN 2>/dev/null || true)

  while IFS= read -r pid; do
    candidate_pids+=("${pid}")
  done < <(pgrep -f "${jar_pattern}" 2>/dev/null || true)

  for pid in "${candidate_pids[@]-}"; do
    [[ -n "${pid}" ]] || continue
    if [[ "${seen}" == *" ${pid} "* ]]; then
      continue
    fi
    seen="${seen}${pid} "

    if ! kill -0 "${pid}" 2>/dev/null; then
      continue
    fi

    local command
    command="$(ps -p "${pid}" -o command= 2>/dev/null || true)"
    if is_service_local_process "${command}" "${service_dir}" "${service_name}"; then
      echo "==> Stopping stale ${service_name} process pid=${pid}"
      graceful_stop_pid "${pid}"
    elif is_docker_port_proxy "${command}"; then
      if [[ "$(docker inspect -f '{{.State.Running}}' "arkab2b-${service_name}" 2>/dev/null || true)" == "true" ]]; then
        continue
      fi
      echo "!! Port ${port} is reserved by Docker but container arkab2b-${service_name} is not running"
      return 1
    elif [[ "${command}" == *"LISTEN"* ]]; then
      :
    elif [[ "${pid}" == "$(cat "${pid_file}" 2>/dev/null || true)" ]]; then
      echo "==> Stopping stale ${service_name} process pid=${pid}"
      graceful_stop_pid "${pid}"
    else
      echo "!! Port ${port} is occupied by an unrelated process: ${command}"
      return 1
    fi
  done

  rm -f "${pid_file}"
}

resolve_boot_jar() {
  local service_dir="$1"
  local libs_dir="${ROOT_DIR}/${service_dir}/build/libs"
  local jar_path=""

  if [[ ! -d "${libs_dir}" ]]; then
    return 1
  fi

  while IFS= read -r candidate; do
    if [[ "${candidate}" == *"-plain.jar" ]]; then
      continue
    fi
    jar_path="${candidate}"
    break
  done < <(find "${libs_dir}" -maxdepth 1 -type f -name '*.jar' | sort)

  if [[ -z "${jar_path}" ]]; then
    return 1
  fi

  printf '%s\n' "${jar_path}"
}

build_boot_jar() {
  local service_dir="$1"
  local service_name="$2"
  local log_file="$3"

  echo "==> Building ${service_name} boot jar" >&2
  (
    cd "${ROOT_DIR}/${service_dir}"
    ./gradlew --no-daemon --console=plain bootJar
  ) >>"${log_file}" 2>&1

  local jar_path
  jar_path="$(resolve_boot_jar "${service_dir}")" || {
    echo "!! Unable to locate boot jar for ${service_name}" >>"${log_file}"
    echo "!! Unable to locate boot jar for ${service_name}" >&2
    return 1
  }

  printf '%s\n' "${jar_path}"
}

start_platform_service() {
  local service_dir="$1"
  local service_name="$2"
  local port="$3"
  local log_file="${RUN_DIR}/${service_name}.log"

  echo "==> Starting ${service_name} on port ${port}"
  : >"${log_file}"
  stop_stale_service_processes "${service_dir}" "${service_name}" "${port}"
  build_boot_jar "${service_dir}" "${service_name}" "${log_file}" >/dev/null
  rm -f "${RUN_DIR}/${service_name}.pid"

  docker compose -f "${ROOT_DIR}/docker-compose.yml" up -d "${service_name}" >>"${log_file}" 2>&1

  local container_name="arkab2b-${service_name}"
  if ! docker inspect -f '{{.State.Running}}' "${container_name}" >/dev/null 2>&1; then
    echo "!! ${service_name} container failed to start" | tee -a "${log_file}"
    docker logs "${container_name}" 2>&1 | tail -n 120 || true
    return 1
  fi
}

start_business_service() {
  local service_dir="$1"
  local service_name="$2"
  local port="$3"
  local database_name="$4"
  local database_user="$5"
  local database_password="$6"
  local log_file="${RUN_DIR}/${service_name}.log"
  if [[ "${service_name}" == "identity-access-service" ]]; then
    local private_key_path="${APP_SECURITY_JWT_PRIVATE_KEY_PATH:-file:${LOCAL_SECRETS_DIR}/identity-access-private.pem}"
    local public_key_path="${APP_SECURITY_JWT_PUBLIC_KEY_PATH:-file:${LOCAL_SECRETS_DIR}/identity-access-public.pem}"

    if [[ "${private_key_path}" == file:* ]]; then
      local private_key_file="${private_key_path#file:}"
      if [[ ! -f "${private_key_file}" ]]; then
        echo "!! Missing JWT private key for identity-access-service: ${private_key_file}"
        echo "   Run ./scripts/generate-local-jwt-keys.sh or set APP_SECURITY_JWT_PRIVATE_KEY_PATH"
        exit 1
      fi
    fi

    if [[ "${public_key_path}" == file:* ]]; then
      local public_key_file="${public_key_path#file:}"
      if [[ ! -f "${public_key_file}" ]]; then
        echo "!! Missing JWT public key for identity-access-service: ${public_key_file}"
        echo "   Run ./scripts/generate-local-jwt-keys.sh or set APP_SECURITY_JWT_PUBLIC_KEY_PATH"
        exit 1
      fi
    fi
  fi

  echo "==> Starting ${service_name} on port ${port}"
  : >"${log_file}"
  stop_stale_service_processes "${service_dir}" "${service_name}" "${port}"
  build_boot_jar "${service_dir}" "${service_name}" "${log_file}" >/dev/null
  rm -f "${RUN_DIR}/${service_name}.pid"

  docker compose -f "${ROOT_DIR}/docker-compose.yml" up -d "${service_name}" >>"${log_file}" 2>&1

  local container_name="arkab2b-${service_name}"
  if ! docker inspect -f '{{.State.Running}}' "${container_name}" >/dev/null 2>&1; then
    echo "!! ${service_name} container failed to start" | tee -a "${log_file}"
    docker logs "${container_name}" 2>&1 | tail -n 120 || true
    return 1
  fi
}

wait_http_200() {
  local name="$1"
  local url="$2"
  local attempts=0
  local max_attempts="${3:-${READY_MAX_ATTEMPTS}}"
  until [[ "${attempts}" -ge "${max_attempts}" ]]; do
    local code
    code="$(curl -s -o /dev/null -w "%{http_code}" "${url}" || true)"
    if [[ "${code}" == "200" ]]; then
      echo "==> ${name} ready (${url})"
      return 0
    fi
    attempts=$((attempts + 1))
    sleep 2
  done
  echo "!! ${name} not ready (${url})"
  return 1
}

wait_ready() {
  local service_name="$1"
  local port="$2"
  local health_url="http://localhost:${port}/actuator/health/readiness"
  if ! wait_http_200 "${service_name}" "${health_url}" "${READY_MAX_ATTEMPTS}"; then
    tail -n 120 "${RUN_DIR}/${service_name}.log" || true
    return 1
  fi
}

wait_registered_in_eureka() {
  local service_name="$1"
  local service_key
  service_key="$(echo "${service_name}" | tr '[:lower:]' '[:upper:]')"
  local attempts=0
  local eureka_url="http://localhost:${EUREKA_SERVER_PORT}/eureka/apps"
  until [[ "${attempts}" -ge "${REGISTER_MAX_ATTEMPTS}" ]]; do
    local payload
    payload="$(curl -fsS "${eureka_url}" 2>/dev/null || true)"
    if [[ -n "${payload}" ]] && echo "${payload}" | tr '[:lower:]' '[:upper:]' | grep -q "<NAME>${service_key}</NAME>"; then
      echo "==> ${service_name} registered in Eureka"
      return 0
    fi
    attempts=$((attempts + 1))
    sleep 2
  done
  echo "!! ${service_name} not registered in Eureka within timeout"
  return 1
}

echo "==> Starting shared infrastructure"
docker compose -f "${ROOT_DIR}/docker-compose.yml" up -d postgres redis kafka kafka-topic-init

wait_container_healthy "arkab2b-postgres"
wait_container_healthy "arkab2b-redis"
wait_container_healthy "arkab2b-kafka"
wait_kafka_topics

# Startup order:
# 1) Infra
# 2) Control plane: eureka -> config
# 3) Technical/core services
# 4) Derived services
# 5) Edge

start_platform_service "eureka-server" "eureka-server" "${EUREKA_SERVER_PORT}"
wait_ready "eureka-server" "${EUREKA_SERVER_PORT}"

start_platform_service "config-server" "config-server" "${CONFIG_SERVER_PORT}"
wait_ready "config-server" "${CONFIG_SERVER_PORT}"
wait_registered_in_eureka "config-server" || true

start_business_service "identity-access-service" "identity-access-service" "${IAM_PORT}" "identity_access" "identity_access" "identity_access"
wait_ready "identity-access-service" "${IAM_PORT}"
wait_registered_in_eureka "identity-access-service"

start_business_service "directory-service" "directory-service" "${DIRECTORY_PORT}" "directory" "directory" "directory"
wait_ready "directory-service" "${DIRECTORY_PORT}"
wait_registered_in_eureka "directory-service"

start_business_service "catalog-service" "catalog-service" "${CATALOG_PORT}" "arkab2b_catalog" "catalog" "catalog"
wait_ready "catalog-service" "${CATALOG_PORT}"
wait_registered_in_eureka "catalog-service"

start_business_service "inventory-service" "inventory-service" "${INVENTORY_PORT}" "inventory" "inventory" "inventory"
wait_ready "inventory-service" "${INVENTORY_PORT}"
wait_registered_in_eureka "inventory-service"

start_business_service "order-service" "order-service" "${ORDER_PORT}" "order" "order" "order"
wait_ready "order-service" "${ORDER_PORT}"
wait_registered_in_eureka "order-service"

start_business_service "notification-service" "notification-service" "${NOTIFICATION_PORT}" "arkab2b_notification" "notification" "notification"
wait_ready "notification-service" "${NOTIFICATION_PORT}"
wait_registered_in_eureka "notification-service"

start_business_service "reporting-service" "reporting-service" "${REPORTING_PORT}" "arkab2b_reporting" "reporting" "reporting"
wait_ready "reporting-service" "${REPORTING_PORT}"
wait_registered_in_eureka "reporting-service"

start_platform_service "api-gateway" "api-gateway" "${API_GATEWAY_PORT}"
wait_ready "api-gateway" "${API_GATEWAY_PORT}"
wait_registered_in_eureka "api-gateway" || true

echo "==> Integrated local stack is ready"
echo "    Config Server : http://localhost:${CONFIG_SERVER_PORT}"
echo "    Eureka Server : http://localhost:${EUREKA_SERVER_PORT}"
echo "    API Gateway   : http://localhost:${API_GATEWAY_PORT}"
echo "    Services      : iam(${IAM_PORT}) directory(${DIRECTORY_PORT}) catalog(${CATALOG_PORT}) inventory(${INVENTORY_PORT}) order(${ORDER_PORT}) notification(${NOTIFICATION_PORT}) reporting(${REPORTING_PORT})"

if [[ "${RUN_SMOKE}" == "true" ]]; then
  echo "==> Running smoke checks"
  "${ROOT_DIR}/scripts/smoke-integrated-local.sh"
fi
