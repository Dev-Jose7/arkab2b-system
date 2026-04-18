# Runbook: Outbox relay stuck

## Symptom
Domain mutations persist but downstream events are not observed in Kafka.

## Possible causes
- relay scheduler disabled
- publisher failures/retry exhaustion
- outbox rows stuck in `PENDING`/`FAILED`

## First checks
- verify `app.outbox.relay.enabled=true`
- inspect service logs for outbox relay errors
- query outbox table for growing pending backlog

## Confirm problem
Outbox backlog grows while business writes continue.

## Mitigation / recovery
1. Re-enable relay if disabled.
2. Fix broker connectivity/topic errors.
3. Trigger service restart to recover scheduler.
4. Reprocess `FAILED` rows per operational procedure.

## Escalation
Escalate when outbox backlog is growing across multiple services simultaneously.
