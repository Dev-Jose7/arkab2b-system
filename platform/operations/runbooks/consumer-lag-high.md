# Runbook: Consumer lag high

## Symptom
Derived services (notification/reporting) are delayed; lag accumulates.

## Possible causes
- high event throughput
- slow downstream dependencies
- repeated consumer retries

## First checks
- inspect consumer logs for repeated retries/errors
- check readiness and DB/Kafka health on consumer service
- inspect checkpoint progression in reporting (if applicable)

## Confirm problem
Offsets/checkpoints stop moving while new events continue arriving.

## Mitigation / recovery
1. Stabilize downstream dependencies (DB/Kafka/external HTTP).
2. Scale consumer instances (if environment supports).
3. Reduce retry pressure temporarily to avoid lockup.
4. Resume normal retry profile after stabilization.

## Escalation
Escalate if lag continues growing after dependency recovery.
