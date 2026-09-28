# tasks.md

## Status Legend
- [ ] Not started
- [~] In progress
- [x] Complete

## Story: US-001 — User Registration via Email

## Phase 1: Scaffolding
- [x] Initialise gateway (Node.js/Express)
- [x] Initialise core-service (Spring Boot)
- [ ] Initialise search-service (Python/FastAPI) — out of scope for US-001
- [x] Create docker-compose.yml
- [x] Create .env.example

## Phase 2: Domain Implementation
- [x] Flyway migrations: `user_accounts`, `verification_tokens`, `user_sessions`
- [x] User registration (pending account, BCrypt hash, 24h single-use token, confirmation email) — FR-001..FR-008
- [x] Email verification (activate account, consume token, create session, `Location: /dashboard`) — FR-009..FR-015
- [x] Resend confirmation (60s cooldown, max 5 resends) — FR-017
- [x] Gateway routing for `/api/v1/auth/register`, `/api/v1/auth/verify/email`, `/api/v1/auth/register/resend`
- [ ] User profile CRUD (core-service) — future story
- [ ] Product CRUD / search — future stories

## Phase 3: Testing
- [x] Gateway unit + integration tests (≥90% coverage)
- [x] Core-service unit + integration tests (≥90% coverage, JaCoCo gate)

## Phase 4: Validation
- [x] All tests pass
- [x] Coverage thresholds met
- [x] Lint passes (eslint + prettier for gateway)
- [~] Docker Compose stack: gateway image builds; full flow verified against PostgreSQL 16 (core-service image build blocked locally by Maven Central 429)
