#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SERVICES=(
  identity-access-service
  directory-service
  catalog-service
  inventory-service
  order-service
  notification-service
  reporting-service
)

SKIP_TESTS="${ARKAB2B_SKIP_TESTS:-false}"
PACKAGE_GATEWAY="${ARKAB2B_PACKAGE_GATEWAY:-true}"

if [[ "$SKIP_TESTS" != "true" ]]; then
  for service in "${SERVICES[@]}"; do
    echo "==> Generating JaCoCo report for ${service}"
    (
      cd "$ROOT_DIR/$service"
      ./gradlew --no-daemon test jacocoTestReport
    )
  done
else
  echo "==> Skipping test execution because ARKAB2B_SKIP_TESTS=true"
fi

echo "==> Syncing JaCoCo reports into api-gateway resources"
if [[ "$PACKAGE_GATEWAY" == "true" ]]; then
  (
    cd "$ROOT_DIR/api-gateway"
    ./gradlew --no-daemon processResources
  )
else
  (
    cd "$ROOT_DIR/api-gateway"
    ./gradlew --no-daemon syncJacocoReports
  )
fi

echo "==> JaCoCo Hub ready at /tools/ARKAB2B_JACOCO_HUB.html after rebuilding/restarting api-gateway if needed"
