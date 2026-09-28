# Test.1

E-Commerce Core Service.

| Component | Stack | Path |
|---|---|---|
| API gateway (external `/api/v1/**`) | Node.js 20, Express, Zod | `gateway/` |
| Core service (internal `/internal/**`) | Java 21, Spring Boot 3, Flyway | `core-service/` |

## User registration via email (US-001)

| Gateway endpoint | Success | Errors |
|---|---|---|
| `POST /api/v1/auth/register` `{email, password}` | `202 {message}` — pending account created, confirmation link emailed | `409 email_already_in_use`, `422 {field_errors}` |
| `POST /api/v1/auth/verify/email?token=…` | `200 {session_token, expires_at}` + `Location: /dashboard` | `410 token_expired`, `409 token_consumed`, `400 invalid_token` |
| `POST /api/v1/auth/register/resend` `{email}` | `202 {message}` | `404` (no pending account), `429 resend_cooldown` / `resend_limit_exceeded` |

Rules: password ≥ 8 chars with an uppercase letter and a digit; verification tokens are single-use and expire after 24h;
resends have a 60s cooldown and a limit of 5. All values are configurable, see `.env.example` at the repo root.

## Running locally

```bash
# core-service (dev profile, in-memory H2, emails logged instead of sent)
cd core-service && mvn spring-boot:run

# gateway
cd gateway && npm ci && npm run dev

# or the full stack with PostgreSQL
docker compose up --build
```

## Tests

```bash
cd core-service && mvn verify          # JUnit + JaCoCo (≥90% line coverage)
cd gateway && npm run lint && npm test # ESLint/Prettier + Jest (≥90% coverage)
```
