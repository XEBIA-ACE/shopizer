# ADR-001: Currency Selector at Checkout Start

## Status
Accepted

## Context

Shopizer is a multi-tenant e-commerce platform where each merchant operates an independent storefront (`MerchantStore`). Each store may support a different subset of ISO 4217 currencies, with one designated as the store's base (default) currency.

The requirement is to expose a per-store currency selector on the checkout start page so that shoppers can see and select only the currencies enabled for the specific storefront they are visiting. This must work correctly across all storefronts in a single deployment (multi-tenancy), and currency list changes made by an admin must be reflected at runtime — without requiring a code deployment or application restart.

Key constraints driving these decisions:

- **Multi-tenancy:** A single Shopizer instance may serve many stores. Currency data must be strictly scoped per store to prevent cross-store data leakage.
- **Runtime configurability:** Admins update store settings (including enabled currencies) through the admin UI. The checkout page must reflect those changes within a bounded time window without a deployment.
- **Performance:** The checkout start page is a high-traffic, latency-sensitive path. Repeated database queries on every page load are unacceptable.
- **Minimal infrastructure footprint:** The base Shopizer 3.2.5 deployment does not include a distributed cache (e.g., Redis). Solutions must work within the existing infrastructure.
- **Security:** In a multi-tenant deployment, a malicious client must not be able to retrieve another store's currency configuration by manipulating request parameters.

---

## Decision 1: Spring Cache (Caffeine) for per-store currency list caching

### Decision
Use the Spring Cache abstraction (`@Cacheable`, `@CacheEvict`) backed by Caffeine as the in-process cache provider, with a configurable TTL (default 300 seconds, controlled by `store.currency.cache.ttl.seconds` in `application.properties`).

The cache is keyed by `storeCode`. Cache population occurs on the first call to `StoreCurrencyService.getEnabledCurrencies(storeCode)` after a cache miss. Cache eviction is triggered explicitly via `@CacheEvict` on `MerchantStoreServiceImpl.update()` so that admin changes propagate within the current TTL window rather than waiting for natural expiry.

```java
// StoreCurrencyServiceImpl — cache population
@Cacheable(value = "storeCurrencies", key = "#storeCode")
public List<CheckoutCurrencyDTO> getEnabledCurrencies(String storeCode) { ... }

// MerchantStoreServiceImpl — cache eviction on admin update
@CacheEvict(value = "storeCurrencies", key = "#store.code")
public MerchantStore update(MerchantStore store) { ... }
```

Cache TTL is registered in `CacheConfig.java`:

```java
Caffeine.newBuilder()
    .expireAfterWrite(ttlSeconds, TimeUnit.SECONDS)
    .build();
```

### Rationale
- Avoids repeated database queries on every checkout page load, satisfying the latency requirement (NFR-06: p95 < 200ms on warm cache).
- TTL-based expiry provides eventual consistency as a safety net even if the `@CacheEvict` path is not triggered (e.g., direct database updates bypassing the service layer).
- `@CacheEvict` on `MerchantStoreServiceImpl.update()` provides near-real-time consistency for admin changes made through the normal application path, satisfying AC-03.
- Caffeine is a pure in-process JVM cache with no additional infrastructure dependencies, consistent with the base Shopizer deployment model.
- The Spring Cache abstraction decouples the service code from the cache provider, allowing a future migration to Redis or another distributed cache without changing service logic.

### Alternatives Considered

| Alternative | Reason Rejected |
|-------------|-----------------|
| **No cache (query DB on every request)** | Unacceptable performance impact on a high-traffic checkout path. Every page load would issue at least one JOIN query across `merchant_store`, `currency`, and the join table. |
| **Redis / distributed cache** | Adds an infrastructure dependency (Redis server, connection pool, serialisation configuration) not present in the base Shopizer 3.2.5 deployment. Introduces operational complexity (Redis availability, network latency) disproportionate to the benefit for a single-node or small-cluster deployment. Can be adopted in a future story if horizontal scaling requires shared cache state. |
| **EhCache** | Viable alternative to Caffeine, but Caffeine is the recommended Spring Boot 3.x default and has a simpler API for TTL configuration. No meaningful advantage over Caffeine for this use case. |
| **HTTP response caching (Cache-Control headers)** | Does not help with database load on the server side. Also inappropriate because the response is user/session-contextual (session currency pre-selection). |

---

## Decision 2: storeCode resolved server-side only

### Decision
The `storeCode` used to scope all currency queries is **always** resolved from a server-side trusted source. Accepted sources, in priority order:

1. **Path variable** in the REST endpoint (`/api/v1/store/{storeCode}/currencies`) — validated against the store registry before use.
2. **Host header** — resolved to a `storeCode` via the existing Shopizer store-resolution mechanism.
3. **Session attribute** — set during store entry and trusted for the session lifetime.

A raw, unvalidated `storeCode` supplied as a client query parameter (e.g., `?storeCode=OTHER_STORE`) is **never** used to scope data queries.

Path variable values are validated against the store registry (a `MerchantStore` lookup by code) before any data is returned. An unknown `storeCode` returns HTTP 404. A blank or null `storeCode` returns HTTP 400.

### Rationale
- Prevents cross-store data leakage in multi-tenant deployments (FR-05, AC-05, NFR-08). Without this control, a malicious client could supply an arbitrary `storeCode` to retrieve another store's currency configuration, potentially revealing business-sensitive information (e.g., which currencies a competitor store supports).
- Path variables in REST endpoints are a standard, well-understood pattern. They are logged, auditable, and validated by the framework before reaching controller logic.
- Returning HTTP 404 for unknown store codes (rather than 403) avoids store code enumeration attacks — a client cannot distinguish "store does not exist" from "store exists but you cannot access it."
- Consistent with the existing Shopizer multi-tenant request-scoping pattern where `storeCode` is resolved from the request context, not from client-supplied body parameters.

### Alternatives Considered

| Alternative | Reason Rejected |
|-------------|-----------------|
| **Client-supplied query parameter (`?storeCode=...`)** | Direct security risk: any client can request any store's data. Rejected unconditionally (NFR-08, FR-05). |
| **JWT claim / OAuth scope** | Would require authentication on a public endpoint (the currency list is public, read-only data). Adds friction for anonymous checkout flows. The path-variable + server-validation approach achieves the same isolation without requiring authentication. |
| **Implicit resolution only (no path variable)** | Would make the API less explicit and harder to test. Path variable with server-side validation is the clearest contract. |

---

## Decision 3: Exchange-rate fetching deferred to a separate story

### Decision
This implementation does **not** fetch live or near-live exchange rates from any external service. All currency display uses stored/configured values only — specifically, the `Currency` entities associated with the `MerchantStore` as persisted in the Shopizer database. No HTTP calls to external exchange-rate APIs (e.g., Open Exchange Rates, European Central Bank, Fixer.io) are made during checkout page load or during `StoreCurrencyService` execution.

If exchange-rate data is required in the future, it must be fetched asynchronously (e.g., via a scheduled job) and stored in the database or a dedicated cache, so that the checkout path reads pre-fetched data rather than making synchronous external calls.

### Rationale
- **Latency:** Synchronous external HTTP calls during checkout page load introduce unpredictable latency. A slow or unavailable exchange-rate API would directly degrade the checkout experience, potentially causing timeouts (NFR-01, NFR-06).
- **Availability:** External rate APIs are third-party services with their own SLAs, rate limits, and failure modes. Making checkout availability dependent on an external service is an unacceptable reliability risk.
- **Scope:** The story requirement is to display the currencies enabled for a store, not to perform currency conversion with live rates. The two concerns are separable and should be delivered independently.
- **Complexity:** Handling API keys, rate limiting, circuit breaking, fallback behaviour, and currency conversion logic is a non-trivial engineering effort that would significantly expand the scope and risk of this story.
- **Definition of Done:** The story's Definition of Done explicitly states "No synchronous external rate calls during checkout session."

### Alternatives Considered

| Alternative | Reason Rejected |
|-------------|-----------------|
| **Synchronous external rate fetch on checkout page load** | Violates NFR-01 and the Definition of Done. Introduces latency and availability risk. Rejected unconditionally. |
| **Asynchronous rate fetch with in-memory cache** | Technically viable for a future story. Out of scope here — the current story does not require rate conversion, only currency list display. |
| **Hardcoded exchange rates** | Unmaintainable and inaccurate. Rejected. |

---

## Consequences

### Positive
- The checkout currency list endpoint responds within the NFR-06 latency budget (p95 < 200ms warm, < 500ms cold) because the hot path reads from an in-process Caffeine cache.
- Admin currency list changes propagate to the checkout page within seconds (via `@CacheEvict`) rather than requiring a deployment.
- Multi-tenant isolation is enforced at the service and controller layers, preventing cross-store data leakage.
- No new infrastructure dependencies are introduced; the solution runs on the existing Shopizer deployment.
- The Spring Cache abstraction allows a future migration to a distributed cache (e.g., Redis) with minimal code changes.

### Negative / Trade-offs
- **TTL propagation window:** If the `@CacheEvict` path is not triggered (e.g., a direct database update bypassing `MerchantStoreServiceImpl.update()`), currency list changes will not propagate until the TTL expires (up to 300 seconds by default). Operators performing direct database updates must be aware of this behaviour.
- **In-process cache is not shared across nodes:** In a horizontally scaled deployment (multiple JVM instances), each node maintains its own Caffeine cache. A cache eviction on one node does not evict the cache on other nodes. This means currency list changes may take up to TTL seconds to propagate across all nodes after an admin update. If this becomes a problem, migration to a distributed cache (Decision 1 alternative: Redis) should be revisited.
- **No live exchange rates:** Price display uses stored currency data only. If a store requires real-time currency conversion, a separate exchange-rate story must be implemented before that feature is available.
- **All price display must use `CurrencyRoundingUtils`:** To ensure ISO 4217 compliance (NFR-02), all monetary amounts rendered alongside the currency selector must use the shared `CurrencyRoundingUtils.round(BigDecimal, String)` utility. Inline rounding is prohibited. This is a code-review enforcement requirement.

### Future Work
- **TASK-012 follow-up (NFR-09):** When the code-insights Neo4j graph backend is restored, re-run `find_symbol` for `StoreCurrencyService`, `MerchantStore`, `Currency`, and `MerchantStoreServiceImpl`. Verify blast-radius using `get_blast_radius` on `MerchantStoreServiceImpl.update()` before merging TASK-006. Update this ADR with confirmed symbol IDs.
- **Exchange-rate story:** Implement asynchronous exchange-rate fetching (scheduled job → database/cache → checkout reads pre-fetched rates). Reference this ADR's Decision 3 as the deferral record.
- **Distributed cache migration:** If horizontal scaling requires shared cache state, migrate `storeCurrencies` cache from Caffeine to Redis using the Spring Cache abstraction. No service-layer code changes required.

---

## References

- `StoreCurrencyService` — `sm-core/src/main/java/com/salesmanager/core/business/services/merchant/StoreCurrencyService.java` — `@see ADR-001`
- `StoreCurrencyServiceImpl` — `sm-core/src/main/java/com/salesmanager/core/business/services/merchant/StoreCurrencyServiceImpl.java`
- `MerchantStoreServiceImpl` — `sm-core/src/main/java/com/salesmanager/core/business/services/merchant/MerchantStoreServiceImpl.java`
- `CacheConfig` — `sm-shop/src/main/java/com/salesmanager/shop/config/CacheConfig.java`
- `CurrencyRoundingUtils` — `sm-core/src/main/java/com/salesmanager/core/business/utils/CurrencyRoundingUtils.java`
- Spec: `spec.md` — Currency Selector at Checkout Start
- NFR list: `constitution.md`
- Task list: `tasks.md` — TASK-012