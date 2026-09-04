# Engineering Tasks

## Epic: Currency Selector at Checkout Start

---

### TASK-001 — Repository: Add per-store currency query
**Type:** Backend  
**Estimate:** 2 points  
**File(s):**
- `sm-core/src/main/java/com/salesmanager/core/business/repositories/merchant/MerchantStoreRepository.java`
- `sm-core/src/main/java/com/salesmanager/core/business/repositories/reference/currency/CurrencyRepository.java`

**Description:**
Add a JPQL/Spring Data query method `findEnabledCurrenciesByStoreCode(String storeCode)` that returns only the `Currency` entities associated with the given `MerchantStore`. Verify the `MerchantStore.currencies` association is correctly mapped (eager or explicit join fetch).

**Acceptance:** Unit test confirms Store A query does not return Store B currencies.

---

### TASK-002 — DTO: Create CheckoutCurrencyDTO
**Type:** Backend  
**Estimate:** 1 point  
**File(s):**
- `sm-shop/src/main/java/com/salesmanager/shop/model/store/CheckoutCurrencyDTO.java`

**Description:**
Create immutable DTO with fields: `currencyCode` (String, ISO 4217), `symbol` (String), `isDefault` (boolean). Include Jackson annotations for JSON serialisation.

**Acceptance:** DTO serialises to `{"currencyCode":"USD","symbol":"$","isDefault":true}`.

---

### TASK-003 — Service: StoreCurrencyService interface + implementation
**Type:** Backend  
**Estimate:** 3 points  
**File(s):**
- `sm-core/src/main/java/com/salesmanager/core/business/services/merchant/StoreCurrencyService.java`
- `sm-core/src/main/java/com/salesmanager/core/business/services/merchant/StoreCurrencyServiceImpl.java`

**Description:**
Implement:
1. `getEnabledCurrencies(String storeCode) → List<CheckoutCurrencyDTO>` — annotated `@Cacheable(value="storeCurrencies", key="#storeCode")`
2. `validateSessionCurrency(String storeCode, String currencyCode) → CurrencyValidationResult` — returns `{valid, fallbackCurrencyCode, fallbackApplied}`

Throw `StoreNotFoundException` if `storeCode` does not resolve to a `MerchantStore`.

**Acceptance:** Service returns only enabled currencies; cache is populated on first call; fallback returns base currency when session currency is unsupported.

---

### TASK-004 — Cache Configuration
**Type:** Backend / Config  
**Estimate:** 1 point  
**File(s):**
- `sm-shop/src/main/resources/application.properties`
- `sm-shop/src/main/java/com/salesmanager/shop/config/CacheConfig.java`

**Description:**
Register a `storeCurrencies` Caffeine cache with TTL driven by `store.currency.cache.ttl.seconds` (default `300`). Ensure Spring Cache abstraction is enabled (`@EnableCaching`).

**Acceptance:** Cache TTL is configurable without code change; cache is present in Spring context.

---

### TASK-005 — Controller: StoreCurrencyController
**Type:** Backend  
**Estimate:** 2 points  
**File(s):**
- `sm-shop/src/main/java/com/salesmanager/shop/store/controller/store/StoreCurrencyController.java`

**Description:**
Implement `GET /api/v1/store/{storeCode}/currencies`. Resolve `storeCode` from path variable (server-validated, not from query param). Return `StoreCurrencyResponse` wrapping `List<CheckoutCurrencyDTO>`. Return HTTP 404 for unknown store, HTTP 400 for blank/null storeCode.

**Acceptance:** Endpoint returns correct currencies for each store; cross-store isolation verified by integration test.

---

### TASK-006 — Cache Eviction on Store Update
**Type:** Backend  
**Estimate:** 1 point  
**File(s):**
- `sm-core/src/main/java/com/salesmanager/core/business/services/merchant/MerchantStoreServiceImpl.java`

**Description:**
Add `@CacheEvict(value="storeCurrencies", key="#store.code")` to the `update(MerchantStore store)` method (and `save` if applicable). This satisfies AC-03 (runtime update without deployment).

**Acceptance:** Integration test confirms cache is evicted after store update; next request returns updated currency list.

---

### TASK-007 — ISO 4217 Rounding Utility
**Type:** Backend / Util  
**Estimate:** 1 point  
**File(s):**
- `sm-core/src/main/java/com/salesmanager/core/business/utils/CurrencyRoundingUtils.java`

**Description:**
Implement `round(BigDecimal amount, String currencyCode)` using `java.util.Currency.getInstance(currencyCode).getDefaultFractionDigits()` and `RoundingMode.HALF_UP`. Cover JPY (0 decimals), USD (2), KWD (3).

**Acceptance:** Unit tests pass for JPY, USD, KWD, EUR, BHD.

---

### TASK-008 — Frontend: Currency Selector Component
**Type:** Frontend  
**Estimate:** 3 points  
**File(s):**
- `sm-shop/src/main/webapp/WEB-INF/templates/checkout/checkout.html` (or equivalent)
- `sm-shop/src/main/webapp/static/js/checkout-currency-selector.js`

**Description:**
1. Add `<select id="currency-selector">` to checkout start page.
2. On DOM ready, fetch `/api/v1/store/{storeCode}/currencies` (storeCode injected server-side into page context).
3. Populate options from response; pre-select session currency or `isDefault` fallback.
4. If `fallbackApplied=true` in response (or detected client-side), display informational banner: _"Your selected currency is not available for this store. Displaying prices in [base currency]."_
5. On currency change, update session via `POST /api/v1/cart/currency` (new or existing endpoint).

**Acceptance:** AC-01, AC-02, AC-04 verified by manual test and Selenium test.

---

### TASK-009 — Session Currency Validation at Checkout Entry
**Type:** Backend  
**Estimate:** 2 points  
**File(s):**
- Checkout controller (e.g., `sm-shop/src/main/java/com/salesmanager/shop/store/controller/order/ShoppingOrderController.java`)

**Description:**
On checkout start request, call `StoreCurrencyService.validateSessionCurrency(storeCode, sessionCurrency)`. If `fallbackApplied=true`, update session currency to base currency and pass `fallbackApplied` flag to the view model.

**Acceptance:** AC-04 — session currency updated; informational message rendered.

---

### TASK-010 — Integration Tests (REQ-004 session, REQ-012 cache)
**Type:** Test  
**Estimate:** 3 points  
**File(s):**
- `sm-shop/src/test/java/com/salesmanager/shop/store/controller/store/StoreCurrencyControllerIT.java`
- `sm-core/src/test/java/com/salesmanager/core/business/services/merchant/StoreCurrencyServiceImplTest.java`

**Description:**
Write Spring Boot integration tests covering:
- **REQ-004 (session):** Session with unsupported currency → fallback to base currency → `fallbackApplied=true` in response.
- **REQ-012 (cache):** First call populates cache; second call hits cache (repository called exactly once); store update evicts cache; third call re-populates.
- **Multi-store isolation:** Store A request returns only Store A currencies; Store B request returns only Store B currencies.
- **ISO 4217 rounding:** `CurrencyRoundingUtilsTest` covers JPY, USD, KWD.

**Acceptance:** All integration tests pass in CI pipeline.

---

### TASK-011 — Unit Tests: StoreCurrencyServiceImpl
**Type:** Test  
**Estimate:** 2 points  
**File(s):**
- `sm-core/src/test/java/com/salesmanager/core/business/services/merchant/StoreCurrencyServiceImplTest.java`

**Description:**
Mock `MerchantStoreRepository` and `CurrencyRepository`. Test:
- Happy path: returns enabled currencies for store
- Unknown store: throws `StoreNotFoundException`
- Unsupported session currency: returns fallback DTO with `fallbackApplied=true`
- Empty currency list: returns list with only base currency

**Acceptance:** 100% branch coverage on `StoreCurrencyServiceImpl`.

---

### TASK-012 — Documentation & ADR
**Type:** Documentation  
**Estimate:** 1 point  
**File(s):**
- `docs/adr/ADR-001-currency-selector-checkout.md`

**Description:**
Create an Architecture Decision Record documenting:
- Decision to use Spring Cache (Caffeine) for per-store currency list caching
- Decision to resolve `storeCode` server-side (security rationale)
- Decision to defer exchange-rate fetching to a separate story

**Acceptance:** ADR created and linked to `StoreCurrencyService` symbol (when graph backend is restored).

---

## Task Summary

| Task | Type | Points | Sprint |
|------|------|--------|--------|
| TASK-001 | Backend | 2 | 1 |
| TASK-002 | Backend | 1 | 1 |
| TASK-003 | Backend | 3 | 1 |
| TASK-004 | Config | 1 | 1 |
| TASK-005 | Backend | 2 | 1 |
| TASK-006 | Backend | 1 | 1 |
| TASK-007 | Util | 1 | 1 |
| TASK-008 | Frontend | 3 | 1 |
| TASK-009 | Backend | 2 | 1 |
| TASK-010 | Test | 3 | 2 |
| TASK-011 | Test | 2 | 2 |
| TASK-012 | Docs | 1 | 2 |
| **Total** | | **22** | |
