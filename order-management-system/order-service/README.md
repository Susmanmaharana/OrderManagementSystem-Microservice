# Order Service

Port **8081**. Saga orchestration + Kafka producer + Resilience4j on Payment.

## Resilience (Phase 9)

- Programmatic Resilience4j Retry + CircuitBreaker on `PaymentClient`
- RestTemplate read timeout = payment call timeout
- Health: http://localhost:8081/actuator/health

## Run

```bash
mvn -pl order-service -am spring-boot:run
```

Without Kafka: `KAFKA_ENABLED=false`
