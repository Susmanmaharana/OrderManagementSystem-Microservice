# OMS React Frontend — notes from prompt

Keep the UI simple for interview demos. One app, service layer, no auth.

## Workspace findings

| Item | Status |
|------|--------|
| Existing React app | None |
| Backend | `order-management-system/` (Java 8, Boot 2.5) |
| System Node | v12.22.9 (too old for React 18 tooling) |
| Plan | Use portable Node 20 under `.tools/node` or upgrade system Node |

## Backend API gaps vs prompt

| Prompt expects | Actual backend |
|----------------|----------------|
| Order CRUD/cancel | Yes (`:8081`) |
| Inventory get/reserve/release | Yes (`:8082`) |
| Payment create + get by id | Yes (`:8083`) |
| `GET payments/order/{id}` | **Missing** |
| `GET /api/v1/notifications` | Yes (`:8084`) — added for Phase 5 UI |
| No auth | Services on 8081–8084 have no JWT; gateway `:8080` does |

Frontend calls **services directly** (8081–8084), not the gateway — matches “no authentication”.

CORS is enabled on Spring services for `http://localhost:3000`.

## Simplified structure (leaner than the prompt)

```
oms-frontend/
  src/
    components/   # shared UI: StatusBadge, Loading, ErrorMessage, Table
    layout/       # AppLayout + nav
    pages/        # Dashboard, Orders, Inventory, Payments, Notifications
    services/     # apiClient + *Service.js
    config/       # apiConfig.js
    App.js
    index.js
```

No separate `hooks/` / `utils/` / `constants/` trees until needed.

## Phases (same as prompt, one module at a time)

1. Scaffold + layout + routing + apiConfig + axios ✅
2. Orders ✅
3. Inventory ✅
4. Payments ✅
5. Notifications ✅
6. Dashboard aggregates ✅
7. Loading / errors / empty / confirm ✅
8. Light tests ✅
9. Docker + Nginx ✅
10. Verify Docker deploy ✅ — `docker compose up -d`; `/`, `/orders`, `/orders/new`, `/orders/1`, `/inventory` all HTTP 200
