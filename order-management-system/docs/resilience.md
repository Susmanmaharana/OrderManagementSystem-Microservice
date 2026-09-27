# Resilience (Phase 9)

Applied on **Order → Payment** (`PaymentClient`) via Resilience4j programmatic API.

| Mechanism | How |
|-----------|-----|
| Timeout | RestTemplate `oms.payment.read-timeout-ms` (default 3000) |
| Retry | `RetryRegistry` — max 3, 200ms wait — timeouts / 5xx only |
| Circuit breaker | `CircuitBreakerRegistry` — opens after failure threshold |
| Fallback | Circuit open / retries exhausted → `PaymentServiceException` → saga release |

## What is retried

- Connection / read timeouts (`ResourceAccessException`)
- Payment 5xx (`PaymentServiceException`)

## What is NOT retried

- Payment 4xx (`PaymentNonRetryableException`)
- Business `status=FAILED` in a successful HTTP response

## Demo

```bash
PAYMENT_DELAY_MS=5000 mvn -pl payment-service -am spring-boot:run
# then create orders — expect timeout → retries → PAYMENT_FAILED + stock release
```

Config: `ResilienceConfig` in order-service.
