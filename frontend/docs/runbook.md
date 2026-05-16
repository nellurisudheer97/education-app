# Frontend Runbook

## Common Tasks

- Start app: `npm start`
- Run lint: `npm run lint`
- Run tests once: `npm run test:ci`
- Build: `npm run build`

## Troubleshooting

## API not reachable

- Check `REACT_APP_API_BASE_URL`.
- Verify backend is running and CORS allows frontend origin.

## Login redirects repeatedly

- Session may be expired; sign in again.
- Confirm backend returns valid token payload.

## File upload failures

- Validate backend upload routes are enabled.
- Retry with smaller files and supported browser formats.

## Manual Smoke Test

1. Register and login.
2. Verify dashboard loads and search/sort work.
3. Create a course (instructor/admin).
4. Open course, add lesson, create quiz.
5. Start quiz as learner and submit.
6. Review/delete quiz and lesson with confirmation dialogs.
