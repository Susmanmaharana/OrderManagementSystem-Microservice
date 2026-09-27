# API Contracts (`/api/v1`)

All APIs return JSON. Errors use a standard body (see below).

## Auth (Phase 8)

- Header: `Authorization: Bearer <jwt>`
- Roles: `CUSTOMER`, `ADMIN`
- Idempotency (orders): header `Idempotency-Key` (optional but recommended)

## Order Service (`:8081`)

### Create order — `POST /api/v1/orders`
Role: CUSTOMER

```json
{ "customerId": 1001, "items": [{ "productId": 101, "quantity": 2 }] }
```

Response `201`:

```json
{
  "orderId": 1,
  "customerId": 1001,
  "status": "CREATED",
  "totalAmount": 0,
  "items": [{ "productId": 101, "quantity": 2, "price": 0 }],
  "createdAt": "2026-09-20T10:00:00"
}
```

### Get order — `GET /api/v1/orders/{orderId}`
Role: CUSTOMER (own) / ADMIN

### List by customer — `GET /api/v1/orders?customerId={id}`
Role: CUSTOMER / ADMIN

### Cancel — `PUT /api/v1/orders/{orderId}/cancel`
Role: CUSTOMER

## Inventory Service (`:8082`)

### Get stock — `GET /api/v1/inventory/{productId}`
Role: CUSTOMER / ADMIN

### Reserve — `POST /api/v1/inventory/reserve`
Internal/service call (Order Service); also ADMIN for demos

```json
{ "orderId": 1, "productId": 101, "quantity": 2 }
```

### Release — `POST /api/v1/inventory/release`
Same shape as reserve.

### Admin upsert (later) — `PUT /api/v1/inventory/{productId}`
Role: ADMIN — set available quantity.

## Payment Service (`:8083`)

### Pay — `POST /api/v1/payments`

```json
{ "orderId": 1001, "amount": 2500.00, "idempotencyKey": "abc-123" }
```

Response:

```json
{ "paymentId": "PAY-10001", "orderId": 1001, "status": "SUCCESS" }
```

`status`: `SUCCESS` | `FAILED`

## Standard error

```json
{
  "timestamp": "2026-09-20T10:00:00",
  "status": 400,
  "error": "BAD_REQUEST",
  "message": "Insufficient inventory",
  "path": "/api/v1/orders",
  "correlationId": "abc-123"
}
```

## Correlation

Every request may send `X-Correlation-Id`. Gateway generates one if missing and propagates it.
