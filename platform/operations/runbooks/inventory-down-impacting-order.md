# Runbook: Inventory down impacting Order

## Symptom
Order confirmation fails due to reservation/availability validation failures.

## Possible causes
- `inventory-service` unavailable
- reservation validation endpoint failing
- Kafka/DB stress on inventory side

## First checks
- `curl -sS http://localhost:8084/actuator/health/readiness`
- inspect `.run/order-service.log` and `.run/inventory-service.log`
- verify reservation validation endpoint behavior

## Confirm problem
`order -> inventory` calls fail or return repeated conflict/unavailable errors.

## Mitigation / recovery
1. Recover inventory service and dependencies.
2. Validate S2S auth between order/inventory.
3. Resume order confirmation only when reservation checks are stable.

## Escalation
Escalate if inventory is `UP` but reservation consistency keeps failing.
