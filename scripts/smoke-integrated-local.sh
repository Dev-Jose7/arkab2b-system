#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
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

check_health() {
  local name="$1"
  local port="$2"
  local url="http://localhost:${port}/actuator/health/readiness"
  local code
  code="$(curl -s -o /dev/null -w "%{http_code}" "${url}" || true)"
  if [[ "${code}" != "200" ]]; then
    echo "SMOKE-FAIL ${name}: readiness ${code} (${url})"
    return 1
  fi
  echo "SMOKE-OK   ${name}: readiness 200"
}

check_prometheus() {
  local name="$1"
  local port="$2"
  local url="http://localhost:${port}/actuator/prometheus"
  local code
  code="$(curl -s -o /dev/null -w "%{http_code}" "${url}" || true)"
  if [[ "${code}" != "200" ]]; then
    echo "SMOKE-FAIL ${name}: prometheus ${code} (${url})"
    return 1
  fi
  echo "SMOKE-OK   ${name}: prometheus 200"
}

check_registered() {
  local service_name="$1"
  local key
  key="$(echo "${service_name}" | tr '[:lower:]' '[:upper:]')"
  local registry
  registry="$(curl -fsS "http://localhost:${EUREKA_SERVER_PORT}/eureka/apps" 2>/dev/null || true)"
  if [[ -z "${registry}" ]] || ! echo "${registry}" | tr '[:lower:]' '[:upper:]' | grep -q "<NAME>${key}</NAME>"; then
    echo "SMOKE-FAIL ${service_name}: not present in Eureka registry"
    return 1
  fi
  echo "SMOKE-OK   ${service_name}: Eureka registry"
}

echo "==> Smoke check: control-plane readiness"
check_health "config-server" "${CONFIG_SERVER_PORT}"
check_health "eureka-server" "${EUREKA_SERVER_PORT}"
check_health "api-gateway" "${API_GATEWAY_PORT}"

echo "==> Smoke check: service readiness"
check_health "identity-access-service" "${IAM_PORT}"
check_health "directory-service" "${DIRECTORY_PORT}"
check_health "catalog-service" "${CATALOG_PORT}"
check_health "inventory-service" "${INVENTORY_PORT}"
check_health "order-service" "${ORDER_PORT}"
check_health "notification-service" "${NOTIFICATION_PORT}"
check_health "reporting-service" "${REPORTING_PORT}"

echo "==> Smoke check: prometheus exposure"
check_prometheus "api-gateway" "${API_GATEWAY_PORT}"
check_prometheus "identity-access-service" "${IAM_PORT}"
check_prometheus "order-service" "${ORDER_PORT}"

echo "==> Smoke check: config-server serving distributed config"
config_code="$(curl -s -o /dev/null -w "%{http_code}" "http://localhost:${CONFIG_SERVER_PORT}/order-service/local" || true)"
if [[ "${config_code}" != "200" ]]; then
  echo "SMOKE-FAIL config-server: /order-service/local -> ${config_code}"
  exit 1
fi
echo "SMOKE-OK   config-server: /order-service/local"

echo "==> Smoke check: eureka registrations"
check_registered "identity-access-service"
check_registered "directory-service"
check_registered "catalog-service"
check_registered "inventory-service"
check_registered "order-service"
check_registered "notification-service"
check_registered "reporting-service"
check_registered "api-gateway"

echo "==> Smoke checks passed"
