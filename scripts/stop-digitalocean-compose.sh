#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
STACK_ENV_FILE_INPUT="${ARKAB2B_STACK_ENV_FILE:-platform/stack/.env.cloud}"
BASE_COMPOSE_FILE="${ROOT_DIR}/docker-compose.yml"
CLOUD_COMPOSE_FILE="${ROOT_DIR}/docker-compose.cloud.yml"
REMOVE_VOLUMES="${ARKAB2B_REMOVE_VOLUMES:-false}"

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

load_stack_env_if_present() {
  local stack_env_file
  stack_env_file="$(to_abs_path "${STACK_ENV_FILE_INPUT}")"

  if [[ -f "${stack_env_file}" ]]; then
    info "Loading environment from ${stack_env_file}"
    set -a
    # shellcheck disable=SC1090
    source "${stack_env_file}"
    set +a
  else
    warn "Env file not found (${stack_env_file}). Continuing with current environment/defaults."
  fi
}

validate_runtime() {
  ensure_command docker
  docker compose version >/dev/null 2>&1 || fail "docker compose is required and not available."
  docker info >/dev/null 2>&1 || fail "Docker daemon is not reachable."
  [[ -f "${BASE_COMPOSE_FILE}" ]] || fail "Missing compose file: ${BASE_COMPOSE_FILE}"
  [[ -f "${CLOUD_COMPOSE_FILE}" ]] || fail "Missing compose file: ${CLOUD_COMPOSE_FILE}"
}

compose() {
  docker compose -f "${BASE_COMPOSE_FILE}" -f "${CLOUD_COMPOSE_FILE}" "$@"
}

main() {
  load_stack_env_if_present
  validate_runtime

  info "Using compose files:"
  info "- ${BASE_COMPOSE_FILE}"
  info "- ${CLOUD_COMPOSE_FILE}"

  if [[ "${REMOVE_VOLUMES}" == "true" ]]; then
    warn "ARKAB2B_REMOVE_VOLUMES=true: persistent volumes will be removed."
    compose down --remove-orphans --volumes
  else
    info "Stopping stack without removing volumes."
    compose down --remove-orphans
  fi

  info "Stack stopped."
}

main "$@"
