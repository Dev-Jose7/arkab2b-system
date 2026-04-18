# Runbook: Config Server unavailable

## Symptom
Services fail at startup or do not refresh configuration; requests may return `5xx` after config drift.

## Possible causes
- `config-server` process down
- wrong `APP_CONFIG_SERVER_URI`
- Config repo path invalid

## First checks
- `curl -sS http://localhost:8888/actuator/health`
- `curl -sS http://localhost:8888/order-service/local`
- inspect `.run/config-server.log`

## Confirm problem
Health not `UP` or config endpoint does not return `200`.

## Mitigation / recovery
1. Restart `config-server`.
2. Validate `platform/config-repo` path and file naming.
3. Recycle affected services so they reload config.

## Escalation
Escalate if config payloads return inconsistent properties for multiple services after restart.
