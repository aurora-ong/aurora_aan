
docker-compose exec aan-kafka bash -c "
  kafka-console-producer \
  --bootstrap-server kafka:9092 \
  --topic aurora-aan-commands \
  --property 'parse.key=true' \
  --property 'key.separator=|'"
