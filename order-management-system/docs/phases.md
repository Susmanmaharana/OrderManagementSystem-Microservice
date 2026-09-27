# Implementation Phases

Follow in order. Keep each phase small and shippable.

## PHASE 1 — Project setup + Order Service
- Spring Boot app, entities, CRUD-ish order APIs
- H2 or local Postgres for order_db
- Validation + exception handler
- No inventory/payment calls yet (status stays CREATED / CANCELLED)

## PHASE 2 — Inventory Service
- Inventory entity with @Version
- GET / reserve / release APIs
- Seed sample products

## PHASE 3 — Order ↔ Inventory
- RestTemplate/WebClient from Order to Inventory
- On create: reserve → INVENTORY_RESERVED

## PHASE 4 — Payment Service
- Simulated payment + failure toggle
- Idempotency key on payments

## PHASE 5 — Saga compensation
- On payment fail: release inventory → PAYMENT_FAILED
- Cancel flow releases reserved stock if needed

## PHASE 6 — Kafka + Notification
- Publish order/payment events
- Notification consumer + idempotent handling

## PHASE 7 — API Gateway
- Routes + correlation ID

## PHASE 8 — JWT
- CUSTOMER / ADMIN roles

## PHASE 9 — Resilience4j
- Timeout / retry / circuit breaker on Payment client

## PHASE 10 — Tests
- Unit + key integration scenarios

## PHASE 11 — Docker Compose
- Full local stack

## PHASE 12 — Jenkinsfile
- Simple CI pipeline

## PHASE 13 — AWS docs
- EC2 / RDS / S3 / CloudWatch / IAM

## PHASE 14 — Docs polish
- Diagrams and decision logs

## PHASE 15 — Interview prep
- Expand interview-questions.md with project references

---
Current: **Phase 14 complete** (Docs polish). Ready for Phase 15 (Interview prep).
