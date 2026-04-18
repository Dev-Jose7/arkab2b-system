# Runbook: Kafka topic missing or broker partial outage

## Symptom
Publish/consume paths fail with topic errors or consumer stalls.

## Possible causes
- broker unavailable
- topic bootstrap did not run
- wrong topic names in config

## First checks
- `docker ps | grep arkab2b-kafka`
- `docker logs arkab2b-kafka-topic-init`
- validate configured topics in `platform/config-repo/*service.yml`

## Confirm problem
Producer/consumer logs show topic-not-found or broker connection failures.

## Mitigation / recovery
1. Restart Kafka container.
2. Re-run topic bootstrap (`docker compose up kafka-topic-init`).
3. Align configured topic names with bootstrap list.
4. Restart affected consumers/producers.

## Escalation
Escalate if topics exist but sustained publish/consume failures continue.
