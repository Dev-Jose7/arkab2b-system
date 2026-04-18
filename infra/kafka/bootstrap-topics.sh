#!/usr/bin/env bash
set -euo pipefail

BOOTSTRAP_SERVER="${1:-kafka:9092}"
PARTITIONS="${KAFKA_TOPIC_PARTITIONS:-3}"
REPLICATION="${KAFKA_TOPIC_REPLICATION_FACTOR:-1}"
EXISTING_TOPICS=""

refresh_existing_topics() {
  EXISTING_TOPICS="$(
    /opt/kafka/bin/kafka-topics.sh \
      --bootstrap-server "${BOOTSTRAP_SERVER}" \
      --list 2>/dev/null || true
  )"
}

create_topic() {
  local topic="$1"
  if printf '%s\n' "${EXISTING_TOPICS}" | grep -Fxq "${topic}"; then
    echo "topic-ready ${topic}"
    return 0
  fi
  /opt/kafka/bin/kafka-topics.sh \
    --bootstrap-server "${BOOTSTRAP_SERVER}" \
    --create \
    --if-not-exists \
    --topic "${topic}" \
    --partitions "${PARTITIONS}" \
    --replication-factor "${REPLICATION}" >/dev/null
  refresh_existing_topics
  echo "topic-ready ${topic}"
}

TOPICS=(
  "iam.session-opened.v1"
  "iam.user-registered.v1"
  "iam.account-activated.v1"
  "iam.auth-failed.v1"
  "iam.session-refreshed.v1"
  "iam.session-revoked.v1"
  "iam.user-role-assigned.v1"
  "iam.user-blocked.v1"
  "directory.regional-policy-configured.v1"
  "directory.regional-policy-applied.v1"
  "directory.entity-mutated.v1"
  "catalog.offer-published.v1"
  "catalog.offer-updated.v1"
  "catalog.mutation.v1"
  "inventory.stock-updated.v1"
  "inventory.commitable-availability-recalculated.v1"
  "inventory.mutation.v1"
  "order.cart.events.v1"
  "order.events.v1"
  "notification.relevant-change-notification-emitted.v1"
  "notification.notification-delivery-recorded.v1"
  "notification.mutation.v1"
  "reporting.weekly-sales-report-generated.v1"
  "reporting.weekly-replenishment-report-generated.v1"
  "reporting.analytic-fact-applied.v1"
  "reporting.mutation.v1"
)

refresh_existing_topics

for topic in "${TOPICS[@]}"; do
  create_topic "${topic}"
done

CONSUMER_TOPICS=(
  "order.events.v1"
  "order.cart.events.v1"
  "inventory.stock-updated.v1"
  "inventory.commitable-availability-recalculated.v1"
  "inventory.mutation.v1"
  "directory.regional-policy-configured.v1"
  "directory.regional-policy-applied.v1"
  "directory.entity-mutated.v1"
  "catalog.offer-published.v1"
  "catalog.offer-updated.v1"
  "catalog.mutation.v1"
  "notification.relevant-change-notification-emitted.v1"
  "notification.notification-delivery-recorded.v1"
  "notification.mutation.v1"
)

for topic in "${CONSUMER_TOPICS[@]}"; do
  create_topic "${topic}.dlq"
done
