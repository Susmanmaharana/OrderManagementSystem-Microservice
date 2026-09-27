# Security

## JWT (Phase 8)

- Login: `POST /api/v1/auth/login` on the API Gateway
- Secret via `JWT_SECRET` env var (never commit real secrets)
- Roles: `CUSTOMER`, `ADMIN`
- Downstream services trust the gateway in v1 (JWT not re-validated in Order/Inventory/Payment)

## Demo users

| Username | Password | Role |
|----------|----------|------|
| `customer` | `customer123` | CUSTOMER |
| `admin` | `admin123` | ADMIN |

```bash
curl -X POST http://localhost:8080/api/v1/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"customer\",\"password\":\"customer123\"}"
```

Use: `Authorization: Bearer <accessToken>`

## Authorization matrix (gateway)

| API | CUSTOMER | ADMIN |
|-----|----------|-------|
| Create / view / cancel order | Yes | Yes |
| Inventory GET | Yes | Yes |
| Inventory PUT (upsert) | No | Yes |
| Inventory reserve/release | No (S2S direct) | Yes (demo via gateway) |
| Payments POST via gateway | No | Yes |
| `/actuator/gateway/**` | No | Yes |
| `/api/v1/auth/**`, `/actuator/health` | Public | Public |

Order → Inventory/Payment calls stay **direct** (no JWT).

## Practices

- Do not log passwords or JWT tokens
- HTTPS in AWS docs (local HTTP OK)
- Trade-off: trusting the gateway simplifies services; add service-level JWT later if the network is untrusted
