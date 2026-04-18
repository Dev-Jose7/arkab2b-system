# Runbook: Notification provider degraded or callback failure

## Symptom
Notification dispatch backlog increases, delivery confirmations fail, or callback reconciliation degrades.

## Possible causes
- external provider unavailable/degraded
- provider auth token invalid
- callback endpoint mismatch/timeouts

## First checks
- inspect `.run/notification-service.log`
- verify provider base URL/auth token config
- check dispatch scheduler status and queue growth

## Confirm problem
Dispatch attempts repeatedly fail and no successful delivery records are produced.

## Mitigation / recovery
1. Stabilize provider connectivity/authentication.
2. Temporarily tune scheduler poll/batch if backlog spikes.
3. Retry pending dispatches after provider recovery.
4. Validate callback payload contract and endpoint availability.

## Escalation
Escalate if provider recovers but callback failures persist (possible contract drift).
