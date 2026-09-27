# API integration

Base URLs: see `src/config/apiConfig.js` / `.env.example`.

## Order Service (`:8081`)

| Method | Path | UI |
|--------|------|-----|
| POST | `/api/v1/orders` | Create order |
| GET | `/api/v1/orders/{id}` | Order details / find |
| GET | `/api/v1/orders?customerId=` | Order list / dashboard |
| PUT | `/api/v1/orders/{id}/cancel` | Cancel |
| GET | `/actuator/health` | Dashboard status |

## Inventory Service (`:8082`)

| Method | Path | UI |
|--------|------|-----|
| GET | `/api/v1/inventory/{productId}` | Search / dashboard |
| PUT | `/api/v1/inventory/{productId}` | Set quantity |
| POST | `/api/v1/inventory/reserve` | Reserve |
| POST | `/api/v1/inventory/release` | Release |
| GET | `/actuator/health` | Dashboard status |

## Payment Service (`:8083`)

| Method | Path | UI |
|--------|------|-----|
| POST | `/api/v1/payments` | Create payment |
| GET | `/api/v1/payments/{paymentId}` | Find payment |
| GET | `/actuator/health` | Dashboard status |

No list-by-order endpoint.

## Notification Service (`:8084`)

| Method | Path | UI |
|--------|------|-----|
| GET | `/api/v1/notifications` | Load all / dashboard |
| GET | `/api/v1/notifications/order/{orderId}` | Filter by order |
| GET | `/health` | Dashboard status |

## Errors

Axios interceptor maps network/timeout/4xx/5xx to friendly messages (see `apiClient.js`).
