# Order Management & Inventory System

Small, interview-focused Java 8 microservices project. Business features stay minimal; engineering concepts stay clear.

## Goal

Demonstrate Lead-level skills: Spring Boot, JPA, Kafka, Saga, JWT, Resilience4j, Docker, Jenkins, and AWS deployment concepts — without over-engineering.

## Architecture (simple)

```
Client
  |
  v
API Gateway (routing + JWT)
  |
  +----> Order Service --------> Order DB
  |          |
  |          +-- REST --> Inventory Service --> Inventory DB
  |          |
  |          +-- REST --> Payment Service ----> Payment DB
  |
  +----> Inventory Service (admin inventory APIs)
  |
  +----> Payment Service (direct only if needed)

Order Service -- events --> Kafka --> Notification Service (log only)
```

## Services

| Service | Port | Responsibility |
|---------|------|----------------|
| api-gateway | 8080 | Routing, JWT auth, correlation ID |
| order-service | 8081 | Orders, saga orchestration |
| inventory-service | 8082 | Stock check/reserve/release (`@Version`) |
| payment-service | 8083 | Simulated payment |
| notification-service | 8084 | Kafka consumer, log notifications |

## Tech stack (fixed)

- Java 8, Spring Boot 2.5.x, Maven
- Spring Data JPA, PostgreSQL (one DB per service)
- Apache Kafka, Resilience4j, Spring Security + JWT
- OpenAPI/Swagger, JUnit 5 + Mockito
- Docker Compose, Jenkinsfile, AWS docs (EC2/RDS/S3/CloudWatch/IAM)

## Order flow (happy path)

1. `POST /api/v1/orders` → status `CREATED`
2. Reserve inventory → `INVENTORY_RESERVED`
3. Process payment → `PAYMENT_PENDING` then `CONFIRMED`
4. Publish Kafka event → Notification Service logs it

## Failure path (Saga compensation)

Payment fails → release inventory → status `PAYMENT_FAILED` → publish event

## Implementation phases

| Phase | Scope |
|-------|--------|
| 1 | Project setup + Order Service |
| 2 | Inventory Service |
| 3 | Order ↔ Inventory REST integration |
| 4 | Payment Service |
| 5 | Saga / compensation |
| 6 | Kafka + Notification Service |
| 7 | API Gateway |
| 8 | JWT Security |
| 9 | Resilience4j |
| 10 | Unit + Integration tests |
| 11 | Docker Compose |
| 12 | Jenkins CI/CD |
| 13 | AWS deployment docs |
| 14 | Architecture docs polish |
| 15 | Interview Q&A |

**Status:** Phase 14 complete (Docs polish). Ready for Phase 15.

## Phase 1–13 — Summary

Services + Compose + Jenkins + AWS docs. See [docs/deployment.md](docs/deployment.md), [docs/aws.md](docs/aws.md).

## Phase 14 — Docs polish

- Diagrams in [docs/architecture.md](docs/architecture.md), [docs/saga.md](docs/saga.md), [docs/kafka.md](docs/kafka.md)
- Decision log: [docs/decisions.md](docs/decisions.md)
- Index: [docs/README.md](docs/README.md)

## Docs

- [Architecture](docs/architecture.md)
- [Decision log](docs/decisions.md)
- [API contracts](docs/api.md)
- [Database](docs/database.md)
- [Kafka](docs/kafka.md)
- [Saga](docs/saga.md)
- [Security](docs/security.md)
- [Resilience](docs/resilience.md)
- [Testing](docs/testing.md)
- [Deployment](docs/deployment.md)
- [AWS](docs/aws.md)
- [Interview questions](docs/interview-questions.md)

## Simplicity rules

- No Redis, no Kubernetes, no service discovery (unless needed later)
- No real payment/email providers
- REST for Order→Inventory and Order→Payment (sync saga)
- Kafka only for notifications (async)
- One module per service; shared code only if clearly needed later

## Local setup

```bash
docker compose up -d --build
# Gateway: http://localhost:8080
```

See [docs/deployment.md](docs/deployment.md).
