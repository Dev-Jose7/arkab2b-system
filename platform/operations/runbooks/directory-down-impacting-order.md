# Runbook: Directory down impacting Order

## Symptom
Order checkout/validation flows fail when resolving organization/policy/address context.

## Possible causes
- `directory-service` unavailable
- JWT propagation or authorization failures between order and directory
- regional context endpoint failures

## First checks
- `curl -sS http://localhost:8082/actuator/health/readiness`
- inspect `.run/order-service.log` for directory adapter errors
- inspect `.run/directory-service.log`

## Confirm problem
Order integration calls to directory fail with timeout/401/5xx.

## Mitigation / recovery
1. Recover directory readiness.
2. Validate bearer JWT propagation and receiver-side authorization for `order -> directory`.
3. Retry failed order operations only after context resolution recovers.

## Escalation
Escalate if directory is healthy but order still fails contract-level parsing/semantics.
