#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
RUN_DIR="${ROOT_DIR}/.run"
REMOVE_VOLUMES="${ARKAB2B_REMOVE_VOLUMES:-false}"
STACK_ENV_FILE="${ARKAB2B_STACK_ENV_FILE:-${ROOT_DIR}/platform/stack/.env}"

if [[ -f "${STACK_ENV_FILE}" ]]; then
  set -a
  # shellcheck disable=SC1090
  source "${STACK_ENV_FILE}"
  set +a
fi

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
  local service_name="$2"
  [[ "${command}" == *"${ROOT_DIR}/${service_name}/build/libs/${service_name}-0.1.0-SNAPSHOT.jar"* \
    || "${command}" == *"${ROOT_DIR}/${service_name}"*"/gradlew"* ]]
}

is_docker_port_proxy() {
  local command="$1"
  [[ "${command}" == *"com.docker.backend"* || "${command}" == *"docker-proxy"* ]]
}

stop_service_processes() {
  local service_name="$1"
  local port="$2"
  local jar_pattern="${ROOT_DIR}/${service_name}/build/libs/${service_name}-0.1.0-SNAPSHOT.jar"
  local -a candidate_pids=()
  local seen=" "

  while IFS= read -r pid; do
    candidate_pids+=("${pid}")
  done < <(lsof -tiTCP:"${port}" -sTCP:LISTEN 2>/dev/null || true)

  while IFS= read -r pid; do
    candidate_pids+=("${pid}")
  done < <(pgrep -f "${jar_pattern}" 2>/dev/null || true)

  while IFS= read -r pid; do
    candidate_pids+=("${pid}")
  done < <(pgrep -f "${ROOT_DIR}/${service_name}.*gradlew.*bootRun" 2>/dev/null || true)

  for pid in "${candidate_pids[@]-}"; do
    [[ -n "${pid}" ]] || continue
    if [[ "${seen}" == *" ${pid} "* ]]; then
      continue
    fi
    seen="${seen}${pid} "

    local command
    command="$(ps -p "${pid}" -o command= 2>/dev/null || true)"
    if is_service_local_process "${command}" "${service_name}"; then
      echo "==> Stopping ${service_name} residual pid=${pid}"
      graceful_stop_pid "${pid}"
    elif is_docker_port_proxy "${command}"; then
      continue
    fi
  done
}

for pid_file in "${RUN_DIR}"/*.pid; do
  [[ -e "${pid_file}" ]] || continue
  pid="$(cat "${pid_file}")"
  echo "==> Stopping process pid=${pid}"
  graceful_stop_pid "${pid}"
  pkill -P "${pid}" 2>/dev/null || true
  rm -f "${pid_file}"
done

stop_service_processes "eureka-server" "${EUREKA_SERVER_PORT}"
stop_service_processes "config-server" "${CONFIG_SERVER_PORT}"
stop_service_processes "identity-access-service" "${IAM_PORT}"
stop_service_processes "directory-service" "${DIRECTORY_PORT}"
stop_service_processes "catalog-service" "${CATALOG_PORT}"
stop_service_processes "inventory-service" "${INVENTORY_PORT}"
stop_service_processes "order-service" "${ORDER_PORT}"
stop_service_processes "notification-service" "${NOTIFICATION_PORT}"
stop_service_processes "reporting-service" "${REPORTING_PORT}"
stop_service_processes "api-gateway" "${API_GATEWAY_PORT}"

echo "==> Stopping shared infrastructure"
if [[ "${REMOVE_VOLUMES}" == "true" ]]; then
  docker compose -f "${ROOT_DIR}/docker-compose.yml" down --remove-orphans --volumes
else
  docker compose -f "${ROOT_DIR}/docker-compose.yml" down --remove-orphans
fi
