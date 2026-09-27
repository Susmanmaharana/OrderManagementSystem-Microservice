# Inventory Service (Phase 2)

Port **8082**. In-memory H2 by default.

## Run

```bash
mvn -pl inventory-service -am spring-boot:run
```

## APIs

| Method | Path | Notes |
|--------|------|--------|
| GET | `/api/v1/inventory/{productId}` | Stock + version |
| PUT | `/api/v1/inventory/{productId}` | Upsert quantity |
| POST | `/api/v1/inventory/reserve` | Idempotent per order+product |
| POST | `/api/v1/inventory/release` | Compensating action |

Swagger: http://localhost:8082/swagger-ui.html

## Seed data

| productId | quantity |
|-----------|----------|
| 101 | 50 |
| 102 | 30 |
| 103 | 10 |

## Interview points

- `@Version` optimistic locking
- Retry on `OptimisticLockingFailureException`
- Reservation table for idempotent reserve/release
