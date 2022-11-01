echo "Waiting for Kafka to come online..."

cub kafka-ready -b kafka:9092 1 20

# topic aurora-aan-commands
kafka-topics \
  --bootstrap-server kafka:9092 \
  --topic aurora-aan-commands \
  --replication-factor 1 \
  --partitions 1 \
  --create

# topic aurora-aan-events
kafka-topics \
  --bootstrap-server kafka:9092 \
  --topic aurora-aan-events \
  --replication-factor 1 \
  --partitions 1 \
  --create

sleep infinity
