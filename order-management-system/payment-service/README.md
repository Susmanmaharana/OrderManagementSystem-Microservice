# Payment Service (Phase 4)

Port **8083**. Simulated payments only — no real gateway.

## Run

```bash
mvn -pl payment-service -am spring-boot:run
```

## APIs

| Method | Path | Notes |
|--------|------|--------|
| POST | `/api/v1/payments` | Body or `Idempotency-Key` header |
| GET | `/api/v1/payments/{paymentId}` | e.g. `PAY-00001` |

Demo headers:
- `X-Force-Payment-Failure: true` → status `FAILED`

Config:
- `PAYMENT_FAILURE_RATE` (0.0–1.0)
- `PAYMENT_DELAY_MS` (timeout demos)

Swagger: http://localhost:8083/swagger-ui.html
