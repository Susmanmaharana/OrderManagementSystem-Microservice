# Notification Service (Phase 6)

Port **8084**. Consumes Kafka events and logs notifications (no real email).

## Run

```bash
docker compose -f docker-compose.kafka.yml up -d
mvn -pl notification-service -am spring-boot:run
```

Health: http://localhost:8084/health

## Behavior

- Listens to `order-events` and `payment-events`
- Idempotent via `processed_events.event_id`
- Logs: `Notification sent to customer ...`
