# Runbook: API Gateway routing or edge auth failure

## Symptom
External calls fail with `401/403/5xx` at gateway, while downstream services are healthy.

## Possible causes
- route misconfiguration
- JWT issuer/JWKS mismatch
- downstream service unavailable in discovery

## First checks
- `curl -sS http://localhost:8080/actuator/health/readiness`
- check gateway routes in `platform/config-repo/api-gateway.yml`
- inspect `.run/api-gateway.log`

## Confirm problem
Gateway readiness `DOWN` or repeated auth/route resolution errors in logs.

## Mitigation / recovery
1. Validate gateway JWT issuer and JWKS URI.
2. Validate route predicate/path mapping.
3. Confirm downstream service is registered in eureka.
4. Restart gateway after config corrections.

## Escalation
Escalate if gateway rejects valid tokens across multiple routes with correct downstream health.
