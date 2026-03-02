#!/bin/sh
# Provisions FulfillFlow Kafka topics: domain topics plus retry and dead-letter
# topics for each domain. Idempotent (create-if-absent). Runs inside a
# cp-kafka container after the broker is healthy.
set -eu

BOOTSTRAP="${KAFKA_BOOTSTRAP:-kafka:9092}"
PARTITIONS="${KAFKA_TOPIC_PARTITIONS:-3}"
REPLICATION="${KAFKA_TOPIC_REPLICATION:-1}"
RETENTION_MS="${KAFKA_TOPIC_RETENTION_MS:-604800000}" # 7 days

create_topic() {
  name="$1"
  echo "[kafka-init] creating topic: $name"
  kafka-topics --bootstrap-server "$BOOTSTRAP" \
    --create --if-not-exists \
    --topic "$name" \
    --partitions "$PARTITIONS" \
    --replication-factor "$REPLICATION" \
    --config retention.ms="$RETENTION_MS"
}

DOMAINS="orders inventory deliveries notifications"

for domain in $DOMAINS; do
  create_topic "${domain}.events.v1"
  create_topic "${domain}.events.v1.retry"
  create_topic "${domain}.events.v1.dlt"
done

echo "[kafka-init] topics provisioned:"
kafka-topics --bootstrap-server "$BOOTSTRAP" --list
