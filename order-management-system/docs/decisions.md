# Decision Log

Short Architecture Decision Records (ADRs) for interview talking points. Newest last.

---

## ADR-001 — Orchestration saga over choreography

**Status:** Accepted  
**Context:** Order needs reserve + pay across separate DBs.  
**Decision:** Order Service orchestrates sync REST steps; compensates on failure.  
**Why:** Explicit sequence, easy to debug and explain. Choreography via events for core money/stock would hide failure paths.  
**Consequence:** Order Service is a central orchestrator (acceptable for this size).

---

## ADR-002 — REST for saga, Kafka for notifications

**Status:** Accepted  
**Context:** Need both immediate success/fail and async side effects.  
**Decision:** Reserve/pay over HTTP; notify via Kafka topics.  
**Why:** Saga needs synchronous answers for compensation; notifications can be at-least-once.  
**Consequence:** Notification lag or Kafka down does not roll back a confirmed order (logged gap OK for v1).

---

## ADR-003 — Database per service (logical DBs)

**Status:** Accepted  
**Context:** Avoid shared schema coupling.  
**Decision:** `order_db`, `inventory_db`, `payment_db`, `notification_db` — no cross-service FKs.  
**Why:** Clear ownership; matches microservice interviews.  
**Consequence:** No single ACID; use saga + eventual consistency.

---

## ADR-004 — Optimistic locking on inventory

**Status:** Accepted  
**Context:** Concurrent reserves on last units.  
**Decision:** JPA `@Version` + retry on conflict in inventory commands.  
**Why:** Simpler than pessimistic locks; demonstrates concurrency control.  
**Consequence:** High contention → more retries/failures (acceptable for demo).

---

## ADR-005 — JWT only at API Gateway

**Status:** Accepted  
**Context:** Protect public HTTP APIs without duplicating security everywhere.  
**Decision:** Login + JWT validation on Gateway; Order/Inventory/Payment trust internal network.  
**Why:** One place for auth; S2S calls stay simple.  
**Consequence:** Compromised internal network can call services directly — harden later with mTLS/JWT if needed.

---

## ADR-006 — Resilience4j on Payment client only

**Status:** Accepted  
**Context:** Payment is the flaky/simulated dependency.  
**Decision:** Timeout + Retry + CircuitBreaker on `PaymentClient` (programmatic registries).  
**Why:** Focus resilience where demos need it; inventory stays simpler.  
**Consequence:** Inventory outages fail the create path without CB (can add later).

---

## ADR-007 — No Eureka / Redis / Kubernetes

**Status:** Accepted  
**Context:** Keep cognitive load low.  
**Decision:** Compose DNS / localhost URLs; no cache layer; no K8s.  
**Why:** Patterns matter more than platform sprawl for interviews.  
**Consequence:** Manual URLs in config; scale-out needs a later discovery story.

---

## ADR-008 — Local H2, Docker/AWS Postgres

**Status:** Accepted  
**Context:** Fast IDE runs vs realistic persistence.  
**Decision:** Default H2 in-memory for local JVM; Postgres via Compose/RDS.  
**Why:** Zero infra for unit/IT; Postgres for integration realism.  
**Consequence:** Dialect differences rare due to `MODE=PostgreSQL` on H2.

---

## ADR-009 — Deploy shape: EC2 + Compose + RDS

**Status:** Accepted (docs)  
**Context:** AWS without overbuilding.  
**Decision:** Single EC2 runs Compose; RDS holds four DBs; S3/CloudWatch/IAM as support.  
**Why:** Mirrors local stack; easy narrative.  
**Consequence:** Not highly available like ECS/EKS — call it a demo topology.

---

## Explicit non-goals

| Out of scope | Reason |
|--------------|--------|
| 2PC / XA | Wrong fit for microservices demo |
| Real payment / email providers | Noise |
| Shared libraries mega-module | Premature |
| API Gateway (AWS) / Lambda | Extra managed surface |
| Event sourcing / CQRS | Overkill for CRUD + saga |
