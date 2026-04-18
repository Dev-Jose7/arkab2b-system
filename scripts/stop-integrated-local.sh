#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
RUN_DIR="${ROOT_DIR}/.run"

for pid_file in "${RUN_DIR}"/*.pid; do
  [[ -e "${pid_file}" ]] || continue
  pid="$(cat "${pid_file}")"
  if [[ -n "${pid}" ]] && kill -0 "${pid}" 2>/dev/null; then
    echo "==> Stopping process group pid=${pid}"
    kill -- -"${pid}" || kill "${pid}" || true
  fi
  rm -f "${pid_file}"
done

pkill -f "/Users/jose/Development/Java/arkab2b/.*/gradle-wrapper.jar bootRun --no-daemon -q" || true

echo "==> Stopping shared infrastructure"
docker compose -f "${ROOT_DIR}/docker-compose.yml" down --remove-orphans
