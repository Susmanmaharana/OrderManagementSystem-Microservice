# Database Design

Database-per-service. No shared tables. No cross-service foreign keys.

## Order DB (`order_db`)

### `orders`

| Column | Type | Notes |
|--------|------|--------|
| id | BIGSERIAL PK | |
| customer_id | BIGINT NOT NULL | |
| status | VARCHAR(32) NOT NULL | see state machine |
| total_amount | DECIMAL(12,2) | |
| idempotency_key | VARCHAR(64) UNIQUE | nullable if not provided |
| created_at | TIMESTAMP | |
| updated_at | TIMESTAMP | |

Index: `(customer_id)`, unique `(idempotency_key)` where not null.

### `order_items`

| Column | Type |
|--------|------|
| id | BIGSERIAL PK |
| order_id | BIGINT FK → orders.id |
| product_id | BIGINT |
| quantity | INT |
| price | DECIMAL(12,2) |

## Inventory DB (`inventory_db`)

### `inventory`

| Column | Type | Notes |
|--------|------|--------|
| product_id | BIGINT PK | |
| available_quantity | INT NOT NULL | |
| version | BIGINT NOT NULL | `@Version` optimistic lock |
| updated_at | TIMESTAMP | |

### `inventory_reservations` (simple audit / idempotency)

| Column | Type | Notes |
|--------|------|--------|
| id | BIGSERIAL PK | |
| order_id | BIGINT | |
| product_id | BIGINT | |
| quantity | INT | |
| status | VARCHAR(16) | RESERVED / RELEASED |
| created_at | TIMESTAMP | |

Unique `(order_id, product_id)` so reserve is idempotent per order line.

## Payment DB (`payment_db`)

### `payments`

| Column | Type | Notes |
|--------|------|--------|
| id | BIGSERIAL PK | |
| payment_ref | VARCHAR(32) UNIQUE | e.g. PAY-10001 |
| order_id | BIGINT | |
| amount | DECIMAL(12,2) | |
| status | VARCHAR(16) | SUCCESS / FAILED |
| idempotency_key | VARCHAR(64) UNIQUE | |
| created_at | TIMESTAMP | |

## Notification DB (`notification_db`)

### `processed_events`

| Column | Type | Notes |
|--------|------|--------|
| event_id | VARCHAR PK | Kafka event id for idempotency |
| event_type | VARCHAR | e.g. ORDER_CONFIRMED |
| order_id | BIGINT | for UI filter |
| customer_id | BIGINT | |
| message | VARCHAR | log-style notification text |
| processed_at | TIMESTAMP | |

API: `GET /api/v1/notifications`, `GET /api/v1/notifications/order/{orderId}`

## State machine (orders)

```
CREATED
  -> INVENTORY_RESERVED
  -> PAYMENT_PENDING
  -> CONFIRMED

CREATED / INVENTORY_RESERVED / PAYMENT_PENDING
  -> PAYMENT_FAILED   (after compensate release)
  -> CANCELLED
```
