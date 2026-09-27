# API Gateway

Port **8080**. Routing + correlation ID + JWT (Phase 8).

## Run

```bash
mvn -pl api-gateway -am spring-boot:run
```

## Auth

```bash
curl -X POST http://localhost:8080/api/v1/auth/login -H "Content-Type: application/json" -d "{\"username\":\"customer\",\"password\":\"customer123\"}"
```

```bash
curl http://localhost:8080/api/v1/inventory/101 -H "Authorization: Bearer <token>"
```

Demo users: `customer`/`customer123` (CUSTOMER), `admin`/`admin123` (ADMIN).  
Secret: `JWT_SECRET`.

## Routes

| Path | Target |
|------|--------|
| `/api/v1/orders/**` | Order `:8081` |
| `/api/v1/inventory/**` | Inventory `:8082` |
| `/api/v1/payments/**` | Payment `:8083` |
| `/api/v1/auth/**` | Gateway (login) |
