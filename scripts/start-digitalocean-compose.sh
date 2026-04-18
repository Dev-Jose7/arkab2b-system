#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
STACK_ENV_FILE_INPUT="${ARKAB2B_STACK_ENV_FILE:-platform/stack/.env.cloud}"
BASE_COMPOSE_FILE="${ROOT_DIR}/docker-compose.yml"
CLOUD_COMPOSE_FILE="${ROOT_DIR}/docker-compose.cloud.yml"
ALLOW_DEFAULT_ENV="${ARKAB2B_ALLOW_DEFAULT_ENV:-false}"
SKIP_BUILD="${ARKAB2B_SKIP_BUILD:-false}"

EUREKA_SERVER_PORT="${ARKAB2B_EUREKA_SERVER_PORT:-8761}"
CONFIG_SERVER_PORT="${ARKAB2B_CONFIG_SERVER_PORT:-8888}"
IAM_PORT="${ARKAB2B_IAM_PORT:-8081}"
DIRECTORY_PORT="${ARKAB2B_DIRECTORY_PORT:-8082}"
CATALOG_PORT="${ARKAB2B_CATALOG_PORT:-8083}"
INVENTORY_PORT="${ARKAB2B_INVENTORY_PORT:-8084}"
ORDER_PORT="${ARKAB2B_ORDER_PORT:-8085}"
NOTIFICATION_PORT="${ARKAB2B_NOTIFICATION_PORT:-8086}"
REPORTING_PORT="${ARKAB2B_REPORTING_PORT:-8087}"
API_GATEWAY_PORT="${ARKAB2B_API_GATEWAY_PORT:-8080}"

info() {
  echo "==> $*"
}

warn() {
  echo "!! $*" >&2
}

fail() {
  echo "ERROR: $*" >&2
  exit 1
}

to_abs_path() {
  local path_value="$1"
  if [[ "${path_value}" == /* ]]; then
    printf '%s\n' "${path_value}"
  else
    printf '%s\n' "${ROOT_DIR}/${path_value#./}"
  fi
}

ensure_command() {
  local binary="$1"
  if ! command -v "${binary}" >/dev/null 2>&1; then
    fail "Missing required command: ${binary}"
  fi
}

load_stack_env() {
  local stack_env_file
  stack_env_file="$(to_abs_path "${STACK_ENV_FILE_INPUT}")"

  if [[ -f "${stack_env_file}" ]]; then
    info "Loading environment from ${stack_env_file}"
    set -a
    # shellcheck disable=SC1090
    source "${stack_env_file}"
    set +a
    return 0
  fi

  if [[ "${ALLOW_DEFAULT_ENV}" == "true" ]]; then
    warn "Env file not found (${stack_env_file}). Continuing with defaults/environment."
    warn "This is not recommended for first remote bootstrap."
    return 0
  fi

  fail "Missing ${stack_env_file}. Copy platform/stack/.env.cloud.example to that path or set ARKAB2B_STACK_ENV_FILE. If you really want defaults, set ARKAB2B_ALLOW_DEFAULT_ENV=true."
}

validate_java_version() {
  local java_spec
  java_spec="$(
    java -XshowSettings:properties -version 2>&1 \
      | awk -F'= ' '/java.specification.version =/{print $2; exit}'
  )"

  if [[ -z "${java_spec}" ]]; then
    warn "Unable to determine Java specification version. Continuing."
    return 0
  fi

  local java_major="${java_spec%%.*}"
  if [[ "${java_major}" =~ ^[0-9]+$ ]] && (( java_major < 21 )); then
    fail "Java 21+ is required. Detected java.specification.version=${java_spec}."
  fi

  info "Java version OK (java.specification.version=${java_spec})"
}

validate_docker_runtime() {
  docker compose version >/dev/null 2>&1 || fail "docker compose is required and not available."
  docker info >/dev/null 2>&1 || fail "Docker daemon is not reachable. Ensure docker is running and current user can access it."
}

validate_repo_layout() {
  local -a required_files=(
    "${BASE_COMPOSE_FILE}"
    "${CLOUD_COMPOSE_FILE}"
    "${ROOT_DIR}/infra/postgres/init-arkab2b.sql"
    "${ROOT_DIR}/infra/kafka/bootstrap-topics.sh"
  )

  local -a service_dirs=(
    "eureka-server"
    "config-server"
    "identity-access-service"
    "directory-service"
    "catalog-service"
    "inventory-service"
    "order-service"
    "notification-service"
    "reporting-service"
    "api-gateway"
  )

  for file in "${required_files[@]}"; do
    [[ -f "${file}" ]] || fail "Required file not found: ${file}"
  done

  [[ -d "${ROOT_DIR}/platform/stack" ]] || fail "Missing directory: ${ROOT_DIR}/platform/stack"
  [[ -d "${ROOT_DIR}/platform/secrets" ]] || fail "Missing directory: ${ROOT_DIR}/platform/secrets"

  for service_dir in "${service_dirs[@]}"; do
    [[ -d "${ROOT_DIR}/${service_dir}" ]] || fail "Missing service directory: ${ROOT_DIR}/${service_dir}"
    [[ -f "${ROOT_DIR}/${service_dir}/gradlew" ]] || fail "Missing Gradle wrapper in: ${ROOT_DIR}/${service_dir}"
  done
}

check_optional_tools() {
  if ! command -v git >/dev/null 2>&1; then
    warn "git is not installed. This is acceptable only if repo is already present and fixed."
  fi
}

check_host_resources() {
  local mem_mb=""
  local disk_mb=""
  local cpus=""

  if [[ -r /proc/meminfo ]]; then
    mem_mb="$(awk '/MemAvailable:/ {print int($2/1024)}' /proc/meminfo)"
  fi

  if command -v df >/dev/null 2>&1; then
    disk_mb="$(df -Pm "${ROOT_DIR}" | awk 'NR==2 {print $4}')"
  fi

  if command -v nproc >/dev/null 2>&1; then
    cpus="$(nproc)"
  elif command -v sysctl >/dev/null 2>&1; then
    cpus="$(sysctl -n hw.ncpu 2>/dev/null || true)"
  fi

  info "Host resources detected: cpu=${cpus:-unknown}, memAvailableMb=${mem_mb:-unknown}, diskAvailableMb=${disk_mb:-unknown}"

  if [[ "${cpus}" =~ ^[0-9]+$ ]] && (( cpus > 0 && cpus < 4 )); then
    warn "Low CPU count (${cpus}). First bootstrap and full bootJar build may be slow."
  fi
  if [[ "${mem_mb}" =~ ^[0-9]+$ ]] && (( mem_mb > 0 && mem_mb < 6144 )); then
    warn "Low available memory (${mem_mb} MB). Java builds may fail or restart under pressure."
  fi
  if [[ "${disk_mb}" =~ ^[0-9]+$ ]] && (( disk_mb > 0 && disk_mb < 15000 )); then
    warn "Low available disk (${disk_mb} MB). Build/cache/images may exhaust disk."
  fi
}

compose() {
  docker compose -f "${BASE_COMPOSE_FILE}" -f "${CLOUD_COMPOSE_FILE}" "$@"
}

validate_compose_merge() {
  compose config >/dev/null
  info "Compose merge validation OK (${BASE_COMPOSE_FILE} + ${CLOUD_COMPOSE_FILE})"
}

wait_http_readiness() {
  local name="$1"
  local port="$2"
  local max_attempts="${3:-180}"
  local attempt=1
  local last_code="000"
  local url="http://localhost:${port}/actuator/health/readiness"
  local container_name="arkab2b-${name}"

  until [[ "${attempt}" -gt "${max_attempts}" ]]; do
    last_code="$(curl -s -o /dev/null -w "%{http_code}" "${url}" || true)"
    if [[ "${last_code}" == "200" ]]; then
      info "${name} readiness OK (${url})"
      return 0
    fi
    attempt=$((attempt + 1))
    sleep 2
  done

  warn "${name} readiness timeout (${url}), last_http_code=${last_code}"
  warn "Diagnostics:"
  warn "- docker compose -f docker-compose.yml -f docker-compose.cloud.yml ps ${name}"
  warn "- docker logs ${container_name} --tail 200"
  compose ps "${name}" || true
  docker logs "${container_name}" --tail 120 || true
  return 1
}

wait_container_healthy() {
  local container_name="$1"
  local max_attempts="${2:-120}"
  local attempt=1
  local status=""
  while [[ "${attempt}" -le "${max_attempts}" ]]; do
    status="$(docker inspect -f '{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' "${container_name}" 2>/dev/null || true)"
    if [[ "${status}" == "healthy" || "${status}" == "running" ]]; then
      info "${container_name} healthy (${status})"
      return 0
    fi
    attempt=$((attempt + 1))
    sleep 2
  done
  warn "${container_name} did not become healthy (last_status=${status:-unknown})"
  docker logs "${container_name}" --tail 120 || true
  return 1
}

wait_topic_bootstrap() {
  local container_name="arkab2b-kafka-topic-init"
  local max_attempts=120
  local attempt=1

  while [[ "${attempt}" -le "${max_attempts}" ]]; do
    local status
    status="$(docker inspect -f '{{.State.Status}}' "${container_name}" 2>/dev/null || true)"
    if [[ "${status}" == "exited" ]]; then
      local exit_code
      exit_code="$(docker inspect -f '{{.State.ExitCode}}' "${container_name}" 2>/dev/null || true)"
      if [[ "${exit_code}" == "0" ]]; then
        info "Kafka topic bootstrap completed"
        return 0
      fi
      warn "Kafka topic bootstrap failed (exit=${exit_code})"
      docker logs "${container_name}" --tail 120 || true
      return 1
    fi
    attempt=$((attempt + 1))
    sleep 2
  done

  warn "Kafka topic bootstrap timeout"
  docker logs "${container_name}" --tail 120 || true
  return 1
}

resolve_boot_jar() {
  local service_dir="$1"
  local libs_dir="${ROOT_DIR}/${service_dir}/build/libs"

  [[ -d "${libs_dir}" ]] || return 1
  find "${libs_dir}" -maxdepth 1 -type f -name '*.jar' ! -name '*-plain.jar' | sort | head -n 1
}

ensure_boot_jar_exists() {
  local service_dir="$1"
  local jar_path
  jar_path="$(resolve_boot_jar "${service_dir}")" || true
  [[ -n "${jar_path}" && -f "${jar_path}" ]] || fail "Missing bootJar for ${service_dir}. Run without ARKAB2B_SKIP_BUILD or build manually."
}

build_service_boot_jar() {
  local service_dir="$1"
  info "Building ${service_dir} bootJar"
  (
    cd "${ROOT_DIR}/${service_dir}"
    ./gradlew --no-daemon --console=plain bootJar
  )
}

ensure_keys() {
  local secrets_host_dir="${APP_SECRETS_HOST_DIR:-./platform/secrets/cloud}"
  secrets_host_dir="$(to_abs_path "${secrets_host_dir}")"
  export APP_SECRETS_HOST_DIR="${secrets_host_dir}"

  local private_key_file="${secrets_host_dir}/identity-access-private.pem"
  local public_key_file="${secrets_host_dir}/identity-access-public.pem"

  mkdir -p "${secrets_host_dir}"

  if [[ ! -f "${private_key_file}" || ! -f "${public_key_file}" ]]; then
    info "Generating identity-access JWT key pair in ${secrets_host_dir}"
    openssl genrsa -out "${private_key_file}" 2048 >/dev/null 2>&1
    openssl rsa -in "${private_key_file}" -pubout -out "${public_key_file}" >/dev/null 2>&1
    chmod 600 "${private_key_file}"
    chmod 644 "${public_key_file}"
  else
    info "Using existing identity-access JWT key pair in ${secrets_host_dir}"
  fi
}

print_endpoints() {
  local local_url="http://localhost:${API_GATEWAY_PORT}"
  local external_host="${ARKAB2B_PUBLIC_HOST:-}"

  if [[ -z "${external_host}" ]]; then
    external_host="$(hostname -I 2>/dev/null | awk '{print $1}' || true)"
  fi
  if [[ -z "${external_host}" ]]; then
    external_host="$(curl -fsS --max-time 2 https://api.ipify.org 2>/dev/null || true)"
  fi
  if [[ -z "${external_host}" ]]; then
    external_host="<DROPLET_PUBLIC_IP>"
  fi

  local external_url="http://${external_host}:${API_GATEWAY_PORT}"

  echo
  info "Stack started."
  info "Gateway (inside host): ${local_url}"
  info "Gateway (outside host): ${external_url}"
  info "Expose only gateway in firewall/security group (plus SSH)."
}

main() {
  load_stack_env

  ensure_command docker
  ensure_command openssl
  ensure_command java
  ensure_command curl

  check_optional_tools
  validate_java_version
  validate_docker_runtime
  validate_repo_layout
  validate_compose_merge
  check_host_resources
  ensure_keys

  local -a service_dirs=(
    "eureka-server"
    "config-server"
    "identity-access-service"
    "directory-service"
    "catalog-service"
    "inventory-service"
    "order-service"
    "notification-service"
    "reporting-service"
    "api-gateway"
  )

  if [[ "${SKIP_BUILD}" == "true" ]]; then
    warn "ARKAB2B_SKIP_BUILD=true: skipping bootJar build."
    for service_dir in "${service_dirs[@]}"; do
      ensure_boot_jar_exists "${service_dir}"
    done
  else
    for service_dir in "${service_dirs[@]}"; do
      build_service_boot_jar "${service_dir}"
    done
  fi

  info "Using compose files:"
  info "- ${BASE_COMPOSE_FILE}"
  info "- ${CLOUD_COMPOSE_FILE}"

  info "Starting infrastructure layer"
  compose up -d postgres redis kafka kafka-topic-init
  wait_container_healthy "arkab2b-postgres"
  wait_container_healthy "arkab2b-redis"
  wait_container_healthy "arkab2b-kafka"
  wait_topic_bootstrap

  info "Starting control plane"
  compose up -d eureka-server
  wait_http_readiness "eureka-server" "${EUREKA_SERVER_PORT}" 240

  compose up -d config-server
  wait_http_readiness "config-server" "${CONFIG_SERVER_PORT}" 240

  info "Starting core services"
  compose up -d identity-access-service
  wait_http_readiness "identity-access-service" "${IAM_PORT}" 300

  compose up -d directory-service
  wait_http_readiness "directory-service" "${DIRECTORY_PORT}" 240

  compose up -d catalog-service
  wait_http_readiness "catalog-service" "${CATALOG_PORT}" 240

  compose up -d inventory-service
  wait_http_readiness "inventory-service" "${INVENTORY_PORT}" 240

  compose up -d order-service
  wait_http_readiness "order-service" "${ORDER_PORT}" 240

  compose up -d notification-service
  wait_http_readiness "notification-service" "${NOTIFICATION_PORT}" 300

  compose up -d reporting-service
  wait_http_readiness "reporting-service" "${REPORTING_PORT}" 300

  info "Starting edge"
  compose up -d api-gateway
  wait_http_readiness "api-gateway" "${API_GATEWAY_PORT}" 240

  info "Containers status"
  compose ps
  print_endpoints
}

main "$@"
