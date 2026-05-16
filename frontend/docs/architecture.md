# Frontend Architecture

## Layering

- `src/pages`: route-level screens and orchestration.
- `src/components`: reusable UI primitives (`AppModal`, `StatusBanner`, `ConfirmDialog`, `ToastStack`).
- `src/services`: API and session boundaries.
- `src/hooks`: reusable behavior (`useToasts`).
- `src/utils`: pure helper functions (`validation`, `env`).

## Data and Control Flow

1. Pages trigger service calls through `api`.
2. Services normalize auth headers and response handling.
3. Unauthorized API responses emit `app:session-expired`.
4. `App` listens and redirects user to login.
5. UI feedback is surfaced via status banners and toast notifications.

## Enterprise Patterns Added

- Role-aware dashboard copy and actions.
- Centralized session clearing to avoid destructive `localStorage.clear()`.
- Reusable modal and confirmation components instead of inline ad hoc dialog code.
- Explicit validation helpers for forms.
- Audit-like UI activity feed for operational visibility.
