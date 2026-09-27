# AWS Deployment (docs only)

Interview-ready layout for OMS. No Terraform/CDK required for this phase — describe the shape, then deploy Compose on EC2.

## Target architecture

```
Internet
   |
   v
[ALB or EC2 security group :443/:8080]
   |
   v
EC2 (Docker Compose)
  - api-gateway :8080
  - order / inventory / payment / notification
  - Kafka + Zookeeper  (same host for demo; MSK later if needed)
   |
   +-- JDBC --> RDS PostgreSQL
         order_db | inventory_db | payment_db | notification_db
   |
   +-- (optional) S3 — Jenkins/build artifacts, backups
   |
   +-- CloudWatch — logs + basic metrics/alarms
```

Keep it to **EC2 + RDS + S3 + CloudWatch + IAM**. Skip EKS, ECS, Redis, Eureka, API Gateway (AWS), Lambda.

## 1. EC2

| Item | Choice |
|------|--------|
| OS | Amazon Linux 2023 (or Ubuntu LTS) |
| Size | `t3.large`+ (Compose + Kafka needs RAM) |
| Install | Docker Engine + Docker Compose plugin |
| Deploy | Clone/copy project → `docker compose up -d --build` |
| Public entry | Security group: inbound `443` (or `8080`) from clients; SSH/`22` from your IP only |

**Compose changes vs local**

- Point JDBC URLs at RDS endpoint (not `postgres` container).
- Prefer **omit** the Compose `postgres` service in AWS, or leave it unused.
- Kafka can stay on the EC2 for demos; production would use Amazon MSK (out of scope here).
- Set `JWT_SECRET` and DB passwords via env / SSM Parameter Store (not committed `.env`).

Example env (host or `.env` on EC2):

```bash
ORDER_DB_URL=jdbc:postgresql://oms-rds.xxxxx.ap-south-1.rds.amazonaws.com:5432/order_db
INVENTORY_DB_URL=jdbc:postgresql://oms-rds.xxxxx.ap-south-1.rds.amazonaws.com:5432/inventory_db
PAYMENT_DB_URL=jdbc:postgresql://oms-rds.xxxxx.ap-south-1.rds.amazonaws.com:5432/payment_db
NOTIFICATION_DB_URL=jdbc:postgresql://oms-rds.xxxxx.ap-south-1.rds.amazonaws.com:5432/notification_db
DB_USERNAME=oms
DB_PASSWORD=<from-secrets-manager-or-ssm>
DB_DRIVER=org.postgresql.Driver
KAFKA_BOOTSTRAP_SERVERS=kafka:29092
JWT_SECRET=<long-random>
```

Optional: put an ALB in front of the EC2 and terminate TLS there; gateway still listens on 8080 privately.

## 2. RDS (PostgreSQL)

| Item | Choice |
|------|--------|
| Engine | PostgreSQL 15 |
| Topology | Single AZ for demo; Multi-AZ for prod |
| Isolation | Same VPC as EC2; SG allows `5432` **only from EC2 SG** |
| Databases | Create four DBs (or one instance + four databases): `order_db`, `inventory_db`, `payment_db`, `notification_db` |
| Schema | Hibernate `ddl-auto=update` OK for demo; Flyway later for prod |

Database-per-service stays logical: **one RDS instance, four databases** is enough for interviews. Separate instances only if you need hard isolation.

## 3. S3 (optional)

Use for CI/CD and ops — not for runtime order data.

| Use | Example |
|-----|---------|
| Build artifacts | Jenkins uploads `*.jar` / compose bundle |
| Backups | RDS snapshots are primary; S3 for exported dumps/logs if needed |
| Static docs | Optional hosting of OpenAPI HTML |

Bucket: private; block public access; encrypt with SSE-S3 or KMS.

## 4. CloudWatch

| Signal | How |
|--------|-----|
| App logs | Docker `json-file` → CloudWatch agent, **or** awslogs driver on containers |
| Metrics | EC2 CPU/mem; RDS CPU/connections/storage |
| Health | Hit gateway `/actuator/health` via ALB health check or cron + alarm |
| Alarms | RDS free storage low; EC2 status check failed; 5xx spike on ALB |

Correlation ID is already in app logs — preserve it in the log format so CloudWatch Logs Insights can filter by `corr=`.

## 5. IAM

Least privilege. Prefer **roles** over long-lived access keys on EC2.

| Principal | Permissions (concept) |
|-----------|----------------------|
| EC2 instance role | `CloudWatchAgentServerPolicy` (or custom logs:PutLogEvents); optional `s3:GetObject`/`PutObject` on artifact bucket; `ssm:GetParameter` if secrets in SSM |
| Jenkins (if on EC2/elsewhere) | Push to S3; optional `ec2:Describe*` / SSM Run Command for deploy |
| Humans | Console/SSO roles: RDS read, CloudWatch read, EC2 SSH via SSM Session Manager preferred |

Do **not** put AWS keys in the Spring Boot apps for this design.

## Security checklist (AWS)

- [ ] RDS not publicly accessible
- [ ] Security groups: app → RDS only; clients → gateway/ALB only
- [ ] TLS at ALB (ACM certificate)
- [ ] Strong `JWT_SECRET`; rotate demo passwords
- [ ] Secrets in SSM / Secrets Manager, injected as env
- [ ] Disable H2 console in any AWS profile (H2 is local-only)

## Cost / simplicity notes

| Do | Don't (for this project) |
|----|---------------------------|
| One EC2 + one RDS + Compose | EKS / ECS Fargate / Service Mesh |
| Kafka on EC2 for demo | Require MSK day-one |
| Four DBs on one RDS | Shared tables across services |
| CloudWatch + IAM roles | Extra managed services for decoration |

## Deploy sketch (manual)

1. Create VPC (or default), SG, RDS Postgres, four databases.
2. Launch EC2, attach instance role, install Docker.
3. Copy project (or pull from Git); set env to RDS.
4. `docker compose up -d --build` (without local Postgres, or with profile that skips it).
5. Open ALB/SG to gateway; verify login + create order.
6. Install CloudWatch agent; confirm logs appear.

CI path: Jenkins (Phase 12) builds → upload artifacts to S3 → SSH/SSM to EC2 → `docker compose pull/up`.

## Interview one-liners

- **Why EC2 + Compose?** Matches local stack; lowest cognitive load for demos.
- **Why RDS not container Postgres?** Persistence, backups, Multi-AZ without managing volumes on EC2.
- **Why not K8s?** Overkill for five services and interview narrative.
- **How do services find each other?** Compose DNS (`order-service`, etc.); no Eureka.
- **Secrets?** SSM/Secrets Manager → env vars; never in Git.
