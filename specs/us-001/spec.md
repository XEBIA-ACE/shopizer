# Spec: Currency Selector at Checkout Start

## Story
**Currency Selector at Checkout Start** — expose a per-store currency dropdown on the checkout start page that reflects only the currencies enabled for that specific storefront, defaults to the store base currency when the session carries an unsupported currency, and updates without code deployment when an admin changes the store's currency list.

---

## 1. Background & Context

### Repository
- **URL:** `https://github.com/XEBIA-ACE/shopizer`
- **Ref / Tag:** `3.2.5`
- **Commit SHA:** `052d2ed3c026525329405cef433c0aca7dd2cee3`
- **Platform:** Shopizer — Java / Spring Boot multi-tenant e-commerce platform

### Graph-Backend Status
The code-insights Neo4j graph backend returned `Neo.ClientError.Security.Unauthorized` for all graph-dependent queries (jobs `7006e9d6` and `59e4a700`). All symbol, call-graph, and module-dependency results are therefore empty. Every structural claim in this spec is derived from confirmed tool outputs only (CI-GR-01, CI-GR-03).

---

## 2. Acceptance Criteria (Restated)

| ID | Criterion |
|----|-----------|
| AC-01 | A currency selector dropdown is visible on the checkout start page when the page loads. |
| AC-02 | The dropdown lists **only** the currencies enabled for the specific store (e.g., USD, EUR, GBP for a store configured with those three). |
| AC-03 | When an admin updates the store's currency list at runtime, the next page load reflects the change — no code deployment required. |
| AC-04 | If the shopper's browser/session carries an unsupported currency, the selector defaults to the store's base currency and an informational message is displayed. |
| AC-05 | In a multi-storefront deployment, Store A's checkout shows only Store A's currencies; Store B's currencies are never shown. |

---

## 3. Functional Requirements

### FR-01 — Store-Scoped Currency List API
A backend endpoint (or service method) **MUST** return the list of enabled currencies for a given `storeCode`. The response payload MUST include:
- `currencyCode` (ISO 4217, e.g., `"USD"`)
- `symbol` (e.g., `"$"`)
- `isDefault` (boolean — marks the store base currency)

The endpoint MUST be scoped by `storeCode` so that multi-tenant isolation is enforced at the data layer.

### FR-02 — Checkout Page Integration
The checkout start page template/component MUST:
1. Call FR-01 on page load (server-side render or client-side fetch before first paint).
2. Render a `<select>` (or equivalent accessible dropdown) populated exclusively from the FR-01 response.
3. Pre-select the currency matching the active session currency; fall back to `isDefault=true` if the session currency is absent from the list.

### FR-03 — Unsupported Currency Fallback
When the session currency is not in the enabled list:
1. The selector MUST default to the store base currency (`isDefault=true`).
2. An informational (non-blocking) message MUST be displayed: _"Your selected currency is not available for this store. Displaying prices in [base currency]."_
3. The session currency value MUST be updated to the base currency to prevent repeated fallback on subsequent requests.

### FR-04 — Runtime Currency List Refresh
The currency list MUST be served from a cache with a configurable TTL (default: 5 minutes). Cache invalidation MUST be triggered when an admin saves store currency settings. No synchronous external rate-fetch calls are permitted during checkout page load.

### FR-05 — Multi-Storefront Isolation
The `storeCode` parameter MUST be derived from the request context (host header, path prefix, or session attribute) — never from a client-supplied query parameter — to prevent cross-store data leakage.

### FR-06 — ISO 4217 Rounding
All price display values rendered alongside the currency selector MUST apply ISO 4217 minor-unit rounding (e.g., JPY = 0 decimal places, USD = 2, KWD = 3). A shared utility method MUST be used for all rounding operations.

---

## 4. Non-Functional Requirements

See `constitution_md` for full NFR list.

---

## 5. Data Model

### 5.1 Existing Entities (Shopizer 3.2.5 — confirmed by repo identity; graph not traversable)

Based on the confirmed repository identity (Shopizer tag 3.2.5), the following entities are known to exist in the codebase. **No symbol IDs are available** because the graph backend is unavailable (CI-GR-03).

| Entity | Role |
|--------|------|
| `MerchantStore` | Represents a storefront; holds `defaultCurrency` and the set of supported currencies |
| `Currency` | ISO 4217 currency entity; fields include `code`, `supported` |
| `ShoppingCart` / `Order` | Carries the active currency for a checkout session |

### 5.2 New / Modified Data

| Change | Description |
|--------|-------------|
| `MerchantStore.currencies` | Ensure the `Set<Currency>` association is eagerly loadable for the checkout API |
| `CheckoutCurrencyDTO` | New DTO: `{ currencyCode: String, symbol: String, isDefault: boolean }` |
| `StoreCurrencyCache` | New cache entry keyed by `storeCode`; TTL-configurable via `application.properties` |

---

## 6. API Design

### 6.1 New Endpoint — GET /api/v1/store/{storeCode}/currencies

```
GET /api/v1/store/{storeCode}/currencies
Authorization: none (public, read-only)
Response 200:
{
  "currencies": [
    { "currencyCode": "USD", "symbol": "$", "isDefault": true },
    { "currencyCode": "EUR", "symbol": "€", "isDefault": false }
  ],
  "storeCode": "DEFAULT"
}
Response 404: store not found
Response 400: invalid storeCode
```

**Cache behaviour:** Response is cached per `storeCode` with TTL = `store.currency.cache.ttl.seconds` (default 300). Cache is invalidated on `MerchantStoreUpdatedEvent`.

### 6.2 Modified Endpoint — GET /api/v1/cart/checkout (or equivalent checkout start)

Add `currencyCode` query parameter (optional). If provided and not in the enabled list, return HTTP 200 with `fallbackApplied: true` and `defaultCurrency` in the response body.

---

## 7. Component Design

```
┌─────────────────────────────────────────────────────────┐
│  Checkout Start Page (Frontend / Thymeleaf template)    │
│  ┌──────────────────────────────────────────────────┐   │
│  │  CurrencySelector component                      │   │
│  │  - Calls GET /api/v1/store/{storeCode}/currencies│   │
│  │  - Renders <select> from response                │   │
│  │  - Applies fallback logic (AC-04)                │   │
│  └──────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
         │ HTTP GET
         ▼
┌─────────────────────────────────────────────────────────┐
│  StoreCurrencyController                                │
│  GET /api/v1/store/{storeCode}/currencies               │
└─────────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────┐
│  StoreCurrencyService                                   │
│  - getEnabledCurrencies(storeCode)                      │
│  - validateSessionCurrency(storeCode, currencyCode)     │
│  - Cache: StoreCurrencyCache (Caffeine / Spring Cache)  │
└─────────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────┐
│  MerchantStoreRepository / CurrencyRepository           │
│  - findByCode(storeCode) → MerchantStore                │
│  - findEnabledByStore(storeCode) → List<Currency>       │
└─────────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────┐
│  Database: merchant_store, currency, store_currency     │
└─────────────────────────────────────────────────────────┘
```

---

## 8. Security Considerations

- `storeCode` MUST be resolved from server-side context (host/session), not from a raw client query parameter, to prevent cross-store data leakage (FR-05).
- The currency list endpoint is public (no auth required) but rate-limited.
- No PII is exposed in the currency list response.

---

## 9. Out of Scope

- Real-time exchange rate fetching (explicitly excluded by Definition of Done).
- Currency conversion logic (separate story).
- Admin UI for enabling/disabling currencies (separate story).
