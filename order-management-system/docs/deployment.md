# Deployment

## Local (Phase 11)

```bash
# From order-management-system/
cp .env.example .env   # optional; compose has defaults
docker compose up -d --build
```

Stack: one Postgres (4 DBs), Zookeeper, Kafka, Inventory, Payment, Order, Notification, API Gateway.

| Component | Port |
|-----------|------|
| api-gateway | 8080 (`GATEWAY_HOST_PORT` to remap) |
| order-service | 8081 |
| inventory-service | 8082 |
| payment-service | 8083 |
| notification-service | 8084 |
| Postgres | 5432 |
| Kafka (host) | 9092 |

Config via env (see `.env.example`): `POSTGRES_*`, `JWT_SECRET`, `PAYMENT_FAILURE_RATE`, `PAYMENT_DELAY_MS`.

Kafka-only (IDE runs): `docker compose -f docker-compose.kafka.yml up -d`

Smoke check:

```bash
# If host 8080 is busy (e.g. another listener), set GATEWAY_HOST_PORT=18080
curl -s http://localhost:${GATEWAY_HOST_PORT:-8080}/actuator/health
curl -s -X POST http://localhost:${GATEWAY_HOST_PORT:-8080}/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"customer","password":"customer123"}'
```

## Jenkins (Phase 12)

Pipeline file: [`Jenkinsfile`](../Jenkinsfile) (Declarative).

Stages: **Checkout → Compile → Test → Package → Docker Build (main/optional) → Deploy (optional)**.

Jenkins tools expected: JDK tool named `JDK8`, Maven tool named `Maven3`. Rename in `tools {}` to match your Jenkins config.

```
parameters:
  BUILD_IMAGES  — force docker compose build
  DEPLOY        — placeholder deploy step (off by default)
```

## AWS (Phase 13 — docs only)

Full write-up: [aws.md](aws.md)

```
EC2 (Docker Compose) + RDS PostgreSQL (4 DBs)
S3 (artifacts) + CloudWatch (logs/metrics) + IAM roles
```

No EKS/ECS/Lambda for this project. Secrets via SSM/Secrets Manager → env vars.
