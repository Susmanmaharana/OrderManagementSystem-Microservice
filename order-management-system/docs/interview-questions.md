# Interview Questions (seed)

Full Q&A filled during Phase 15. Starter set:

## Microservices

**Q:** Why database-per-service?  
**A:** Independent schema evolution, ownership, scaling; avoids coupling via shared tables. Trade-off: no single ACID across services → Saga/eventual consistency.

**Q:** Why REST for saga and Kafka for notifications?  
**A:** Sync steps need immediate success/fail for compensation; notifications are fire-and-forget.

## Concurrency

**Q:** Why `@Version` on inventory?  
**A:** Detect lost updates when two orders reserve the last units; loser retries or fails cleanly.

## Resilience

**Q:** When not to retry?  
**A:** 4xx business errors (insufficient stock, validation). Retry timeouts/5xx carefully with idempotency.

## Java 8

Show real usage of Stream/Optional/lambdas in mappers and filters — not forced demos.
