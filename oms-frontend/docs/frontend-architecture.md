# Frontend architecture

```
React SPA (oms-frontend)
  ├── pages/          Dashboard, Orders, Inventory, Payments, Notifications
  ├── components/     Domain UI + shared (Loading, ErrorMessage, ConfirmDialog, …)
  ├── services/       Axios wrappers per microservice (no Axios in components)
  ├── config/         apiConfig.js — base URLs from REACT_APP_*
  └── layout/         Shell + nav
         │
         ▼
  apiClient (timeout, friendly errors)
         │
         ├── Order Service      :8081
         ├── Inventory Service  :8082
         ├── Payment Service    :8083
         └── Notification       :8084
```

## Why a service layer?

- One place for URLs and headers
- Components stay UI-focused
- Easier to mock in tests

## Auth

None in this UI. Calls services directly (not the JWT gateway).

## Docker

Production image serves the CRA build with Nginx on port 3000. React Router deep links use `try_files … /index.html`.
