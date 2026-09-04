# Non-Functional Requirements & Constraints

## Source
All NFRs are derived from the story's Definition of Done, the confirmed repository identity (Shopizer 3.2.5, Java/Spring Boot), and the code-insights tool outputs. No NFR is inferred from undocumented conventions.

---

## NFR-01 — No Synchronous External Rate Calls During Checkout
**Source:** Definition of Done — "No synchronous external rate calls during checkout session."  
**Constraint:** The `StoreCurrencyService.getEnabledCurrencies()` method MUST NOT make any HTTP calls to external exchange-rate APIs (e.g., Open Exchange Rates, ECB) during its execution. Rate data, if needed elsewhere, must be pre-fetched asynchronously and stored.  
**Verification:** Code review gate; integration test confirms no outbound HTTP during checkout page load.

## NFR-02 — ISO 4217 Rounding Compliance
**Source:** Definition of Done — "ISO 4217 rounding validated for all supported currencies."  
**Constraint:** All monetary amounts displayed in the checkout flow MUST use `java.util.Currency.getDefaultFractionDigits()` for scale. `RoundingMode.HALF_UP` is the required rounding mode. A single shared utility (`CurrencyRoundingUtils`) MUST be used — no inline rounding.  
**Verification:** Unit tests for JPY (0), USD (2), EUR (2), KWD (3), BHD (3).

## NFR-03 — Per-Store Scoping (Multi-Tenancy)
**Source:** Acceptance Criteria AC-05 and Definition of Done — "Per-store scoping verified across multiple storefronts."  
**Constraint:** The `storeCode` used to scope currency queries MUST be resolved from a server-side trusted source (host header, session attribute, or path variable validated against the store registry). It MUST NOT be accepted as a raw, unvalidated client query parameter.  
**Verification:** Integration test with two stores; security review.

## NFR-04 — Cache TTL Configurability
**Source:** AC-03 — "selector reflects the updated list without any code deployment."  
**Constraint:** The currency list cache TTL MUST be configurable via `application.properties` (`store.currency.cache.ttl.seconds`). Default value: 300 seconds. Cache eviction MUST be triggered programmatically on store update (not only by TTL expiry).  
**Verification:** Integration test (TASK-010, REQ-012).

## NFR-05 — Session Fallback Behaviour
**Source:** AC-04 — "selector defaults to the store base currency and an informational message is displayed."  
**Constraint:** When session currency is unsupported, the fallback MUST be the store's `defaultCurrency` (not a hardcoded value). The informational message MUST be non-blocking (not a modal or error page). The session MUST be updated to the fallback currency to prevent repeated fallback on subsequent requests within the same session.  
**Verification:** Integration test (TASK-010, REQ-004).

## NFR-06 — Response Latency
**Source:** Implied by "No synchronous external rate calls" and general checkout UX requirements.  
**Constraint:** `GET /api/v1/store/{storeCode}/currencies` MUST respond within 200ms at p95 under normal load (cache warm). Cold-cache response (first call after eviction) MUST complete within 500ms.  
**Verification:** Load test with k6 or JMeter; cache warm/cold scenarios.

## NFR-07 — Accessibility
**Source:** General web accessibility standards applicable to checkout flows.  
**Constraint:** The currency selector `<select>` element MUST have an associated `<label>` element with descriptive text. ARIA attributes (`aria-label`, `aria-describedby`) MUST be applied if a visible label is not used.  
**Verification:** axe-core automated accessibility scan on checkout page.

## NFR-08 — Security: No Cross-Store Data Leakage
**Source:** AC-05 and FR-05.  
**Constraint:** The currency list endpoint MUST return HTTP 403 (or 404 to avoid enumeration) if the resolved `storeCode` does not match the request context. No currency data from Store B must appear in a Store A response under any input.  
**Verification:** Integration test with two stores; penetration test scenario.

## NFR-09 — Graph Backend Dependency
**Source:** Code-insights tool results — graph backend returned `Neo.ClientError.Security.Unauthorized` for all graph queries.  
**Constraint:** This spec was produced with **zero graph-derived symbol IDs**. When the graph backend is restored, the implementation team MUST:
1. Re-run `find_symbol` for `StoreCurrencyService`, `MerchantStore`, `Currency`, `MerchantStoreServiceImpl`.
2. Verify blast-radius using `get_blast_radius` on `MerchantStoreServiceImpl.update()` before merging TASK-006.
3. Update the ADR (TASK-012) with confirmed symbol IDs.

## NFR-10 — Backward Compatibility
**Source:** Shopizer 3.2.5 existing API surface.  
**Constraint:** No existing API endpoints may be removed or have their response schema changed in a breaking way. The new `/api/v1/store/{storeCode}/currencies` endpoint is additive. Modifications to `MerchantStoreServiceImpl.update()` (TASK-006) must not alter the method signature or existing return value.  
**Verification:** Existing Shopizer integration tests must continue to pass.
