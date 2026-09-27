# OMS Frontend

Simple React UI for the Order Management microservices demo.

## Stack

- React 18 + React Router + Axios
- JavaScript only (no TypeScript)
- **No authentication** — calls Order/Inventory/Payment/Notification on ports 8081–8084

## Run (dev)

```bash
# Need Node 18+ (system Node 12 is too old)
npm install
npm start
```

Open http://localhost:3000

## Env

Copy `.env.example` → `.env.development` (already present for local).

| Variable | Default |
|----------|---------|
| `REACT_APP_ORDER_SERVICE_URL` | `http://localhost:8081` |
| `REACT_APP_INVENTORY_SERVICE_URL` | `http://localhost:8082` |
| `REACT_APP_PAYMENT_SERVICE_URL` | `http://localhost:8083` |
| `REACT_APP_NOTIFICATION_SERVICE_URL` | `http://localhost:8084` |

## Structure

```
src/
  config/apiConfig.js
  services/          # Axios wrappers per microservice
  layout/            # Shell + nav
  pages/             # One page per area
  components/        # Shared UI (added as needed)
```

## Phases

1. Scaffold + layout + routing + API config
2. Orders
3. Inventory
4. Payments
5. Notifications
6. Dashboard aggregates
7. Loading / errors / empty / confirm
8. Tests
9. Docker + Nginx
10. Verify Docker deploy ← **complete**

### Docker (Phase 9)

```bash
cd oms-frontend
docker compose up --build -d
# UI: http://localhost:3000
```

Multi-stage: Node 20 build → Nginx on port **3000** with SPA `try_files`.  
Docker UI calls **same-origin** `/backend/*`; Nginx proxies to host ports:

| Proxy path | Host port | Service |
|------------|-----------|---------|
| `/backend/order/` | 8081 | order-service |
| `/backend/inventory/` | 8082 | inventory-service |
| `/backend/payment/` | 8083 | payment-service |
| `/backend/notification/` | 8084 | notification-service |
| `/backend/gateway/` | 18080 | api-gateway (optional) |

`npm start` still uses `http://localhost:8081–8084` directly.

Verified: deep links return **200** (not Nginx 404) after refresh.

### Orders (Phase 2)

| Route | What |
|-------|------|
| `/orders` | Search by customer ID or order ID |
| `/orders/new` | Create order (+ optional force payment failure) |
| `/orders/:orderId` | Details, lifecycle, cancel |

Seed products for demos: `101`, `102`, `103`.

### Inventory (Phase 3)

| Action | API |
|--------|-----|
| Search | `GET /api/v1/inventory/{productId}` |
| Set qty | `PUT /api/v1/inventory/{productId}` |
| Reserve / Release | `POST .../reserve` · `POST .../release` |

### Payments (Phase 4)

| Action | API |
|--------|-----|
| Create | `POST /api/v1/payments` |
| Get by ID | `GET /api/v1/payments/{paymentId}` |

No `GET .../order/{orderId}` on the backend — UI notes that gap.

### Notifications (Phase 5)

| Action | API |
|--------|-----|
| List all | `GET /api/v1/notifications` |
| By order | `GET /api/v1/notifications/order/{orderId}` |

### Dashboard (Phase 6)

Client-side aggregates: orders by customer, inventory 101–103, notification count, health probes. Payment success/fail approximated from order statuses.

### UX polish (Phase 7)

- Friendly Axios errors (service name, timeout, 404/400/500)
- Shared `ConfirmDialog`, `SuccessMessage`, spinner `Loading`
- Confirm before cancel / reserve / release / stock upsert
- Refresh on Dashboard, Inventory, Payments, Notifications, Order details
- Disable forms while submitting

### Tests (Phase 8)

```bash
npm test
```

Covers dashboard render, order validation/create loading, order/notification/payment/inventory display, empty/loading/error UI, inventory search, Axios error mapping.

See `NOTES.md` for backend API gaps vs the integration prompt.

## Backend CORS

Browsers call `localhost:808x` from `localhost:3000`. Enable CORS on each Spring service (or use the gateway with a proxy later).
