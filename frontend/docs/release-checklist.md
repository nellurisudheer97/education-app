# Frontend Release Checklist

- [ ] `npm ci` succeeds in a clean environment.
- [ ] `npm run lint` passes.
- [ ] `npm run test:ci` passes.
- [ ] `npm run build` passes.
- [ ] Auth flows validated (login, register, session expiry redirect).
- [ ] Dashboard validated for student and instructor/admin roles.
- [ ] Course detail flows validated (lesson add, quiz create/submit/review, delete confirm).
- [ ] Error, retry, and toast messages verified.

## Next Enterprise Extensions Backlog

1. Server-backed audit trail and activity history API.
2. RBAC policy matrix and permission guard utilities.
3. Server pagination and filter APIs for large catalogs.
4. Accessibility and keyboard navigation audit with automated checks.
5. Performance budget checks in CI.
