# Testing (Phase 10)

## Levels

1. **Unit** — JUnit 5 + Mockito (services, clients, JWT, filters)
2. **Integration** — `@SpringBootTest` + MockMvc + H2 (`@ActiveProfiles("test")`)
3. **API** — [Postman collection](../postman/oms-api.postman_collection.json)

## Run all module tests

```bash
mvn -pl order-service,inventory-service,payment-service,notification-service,api-gateway test
```

## Integration coverage

| Module | Scenarios |
|--------|-----------|
| Order | Happy path → CONFIRMED; insufficient inventory; payment FAILED + release; payment down 503; idempotency; 404; validation 400; cancel conflict on CONFIRMED |
| Inventory | Seed/get; reserve/release idempotent; insufficient stock; 404 |
| Payment | SUCCESS; force FAILED; idempotent; validation |
| Notification | Duplicate `eventId` ignored (unit) |
| Gateway | Correlation ID; JWT create/parse; demo users (unit) |

## Manual / demo

- Payment timeout / CB: `PAYMENT_DELAY_MS=5000` + create orders
- JWT wrong role: customer token on `PUT /api/v1/inventory/101` → 403
- Kafka duplicate: re-publish same `eventId` → notification skips

## Note

Order IT mocks Inventory/Payment clients (no live network). Full E2E is manual or Docker Compose (Phase 11).
