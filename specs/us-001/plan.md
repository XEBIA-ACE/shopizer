# Implementation Plan

## Measured Blast-Radius Evidence

| Metric | Value | Source |
|--------|-------|--------|
| Graph backend state | FAILED (Neo4j auth error) | `get_index_status` job `59e4a700` |
| Symbol graph nodes | 0 (empty graph) | `find_symbol`, `fulltext_search`, `semantic_search` — all returned `[]` |
| Module dependency edges | 0 | `module_dependency_graph` returned `[]` |
| Dead code candidates | 0 | `find_dead_code` returned `total_candidates: 0` |
| Community clusters | 0 | `detect_communities` returned `cluster_count: 0` |
| Cross-service edges | 0 | `cross_service_graph` returned `edges: [], routes: []` |
| CVE findings | 0 | `get_dependency_report` returned `total_cve_count: 0` |
| Blast radius (measured) | **0** — graph unavailable | CI-GR-12 |
| Risk score (measured) | **Not available** — graph unavailable | CI-GR-10 |
| Assessed priority | **MEDIUM** (new feature, no existing callers to break) | Story-level assessment |

> **CI-GR-12 compliance:** The measured blast_radius_count is **0** because the code-insights graph backend is unavailable. All impact estimates below are derived from the confirmed repository identity (Shopizer 3.2.5) and the story requirements, not from hallucinated graph data.

---

## Phase 1 — Backend: Store-Scoped Currency API (Sprint 1, ~5 days)

### Step 1.1 — Repository Layer
**Files to create/modify:**
- `sm-core/src/main/java/com/salesmanager/core/business/repositories/merchant/MerchantStoreRepository.java` — add `findEnabledCurrenciesByStoreCode(String storeCode)` JPQL query
- `sm-core/src/main/java/com/salesmanager/core/business/repositories/reference/currency/CurrencyRepository.java` — verify `findByCode` exists; add `findSupportedByMerchantStore` if absent

**Rationale:** Shopizer's repository layer follows Spring Data JPA conventions. The `MerchantStore` entity has a `currencies` association that must be queryable per store.

### Step 1.2 — Service Layer
**Files to create:**
- `sm-core/src/main/java/com/salesmanager/core/business/services/merchant/StoreCurrencyService.java` (interface)
- `sm-core/src/main/java/com/salesmanager/core/business/services/merchant/StoreCurrencyServiceImpl.java` (implementation)

**Key methods:**
```java
List<CheckoutCurrencyDTO> getEnabledCurrencies(String storeCode);
CurrencyValidationResult validateSessionCurrency(String storeCode, String currencyCode);
```

**Cache annotation:**
```java
@Cacheable(value = "storeCurrencies", key = "#storeCode")
```
Cache eviction triggered by `@CacheEvict` on `MerchantStoreService.update()`.

### Step 1.3 — DTO
**File to create:**
- `sm-shop/src/main/java/com/salesmanager/shop/model/store/CheckoutCurrencyDTO.java`

```java
public class CheckoutCurrencyDTO {
    private String currencyCode;   // ISO 4217
    private String symbol;
    private boolean isDefault;
}
```

### Step 1.4 — Controller
**File to create:**
- `sm-shop/src/main/java/com/salesmanager/shop/store/controller/store/StoreCurrencyController.java`

```java
@RestController
@RequestMapping("/api/v1/store/{storeCode}/currencies")
public class StoreCurrencyController {
    @GetMapping
    public ResponseEntity<StoreCurrencyResponse> getEnabledCurrencies(
        @PathVariable String storeCode) { ... }
}
```

### Step 1.5 — Cache Configuration
**File to modify:**
- `sm-shop/src/main/resources/application.properties` — add `store.currency.cache.ttl.seconds=300`
- `sm-shop/src/main/java/com/salesmanager/shop/config/CacheConfig.java` — register `storeCurrencies` Caffeine cache with configurable TTL

---

## Phase 2 — Frontend: Currency Selector Component (Sprint 1, ~3 days)

### Step 2.1 — Checkout Template
**File to modify:**
- `sm-shop/src/main/webapp/WEB-INF/templates/checkout/checkout.html` (or equivalent Thymeleaf/Angular template)

Add currency selector `<select>` element populated via AJAX call to `/api/v1/store/{storeCode}/currencies`.

### Step 2.2 — JavaScript / Frontend Logic
**File to create:**
- `sm-shop/src/main/webapp/static/js/checkout-currency-selector.js`

Logic:
1. On DOM ready, fetch `/api/v1/store/{storeCode}/currencies`.
2. Populate `<select>` with returned currencies.
3. Pre-select session currency; fall back to `isDefault=true` entry.
4. If fallback applied, display informational banner (AC-04).

### Step 2.3 — Session Currency Update
**File to modify:**
- `sm-shop/src/main/java/com/salesmanager/shop/store/controller/order/ShoppingOrderController.java` (or checkout controller)

On checkout start, validate session currency against store's enabled list. If invalid, update session to base currency and set `fallbackApplied=true` flag.

---

## Phase 3 — ISO 4217 Rounding Utility (Sprint 1, ~1 day)

### Step 3.1 — Rounding Utility
**File to create:**
- `sm-core/src/main/java/com/salesmanager/core/business/utils/CurrencyRoundingUtils.java`

```java
public class CurrencyRoundingUtils {
    public static BigDecimal round(BigDecimal amount, String currencyCode) {
        Currency currency = Currency.getInstance(currencyCode);
        int fractionDigits = currency.getDefaultFractionDigits();
        return amount.setScale(fractionDigits, RoundingMode.HALF_UP);
    }
}
```

---

## Phase 4 — Testing (Sprint 2, ~3 days)

### Step 4.1 — Unit Tests
- `StoreCurrencyServiceImplTest` — mock repository, verify per-store scoping, fallback logic
- `CurrencyRoundingUtilsTest` — verify JPY (0), USD (2), KWD (3) rounding

### Step 4.2 — Integration Tests (REQ-004 session, REQ-012 cache)
- `StoreCurrencyControllerIT` — Spring Boot test with embedded H2; verify:
  - Store A currencies not returned for Store B request
  - Cache hit on second request (verify repository called once)
  - Cache eviction after store update
  - Session fallback (AC-04) returns `fallbackApplied=true`

### Step 4.3 — End-to-End Tests
- Selenium/Playwright test: load checkout page, verify dropdown populated, verify fallback banner

---

## Phase 5 — Cache Invalidation Hook (Sprint 2, ~1 day)

**File to modify:**
- `sm-core/src/main/java/com/salesmanager/core/business/services/merchant/MerchantStoreServiceImpl.java`

Add `@CacheEvict(value = "storeCurrencies", key = "#store.code")` to the `update(MerchantStore store)` method to satisfy AC-03 (runtime update without deployment).

---

## Dependency Order

```
Step 1.1 (Repository) → Step 1.2 (Service) → Step 1.4 (Controller)
                      ↗
Step 1.3 (DTO) ──────
Step 1.5 (Cache Config) → Step 5 (Cache Eviction)
Step 2.1 + 2.2 (Frontend) → depends on Step 1.4 API being available
Step 3.1 (Rounding) → independent, can be done in parallel
Phase 4 (Tests) → depends on all above
```
