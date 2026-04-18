# Runbook: Reporting stale or not consuming

## Symptom
Reporting views/facts lag behind upstream mutations.

## Possible causes
- reporting consumer disabled
- checkpoint not advancing
- upstream topics misconfigured

## First checks
- `curl -sS http://localhost:8087/actuator/health/readiness`
- inspect `.run/reporting-service.log`
- verify `app.kafka.consumers.upstream-events.enabled=true`

## Confirm problem
No recent analytic facts/checkpoint updates despite ongoing upstream events.

## Mitigation / recovery
1. Re-enable consumer if disabled.
2. Correct topic list and group-id configuration.
3. Restart reporting consumer process.
4. Trigger controlled rebuild/replay procedure if needed.

## Escalation
Escalate if replay/rebuild repeatedly fails or checkpoint updates are inconsistent.
