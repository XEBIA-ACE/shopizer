```java
package com.salesmanager.shop.store.controller.store;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.salesmanager.core.business.repositories.merchant.MerchantStoreRepository;
import com.salesmanager.core.business.services.merchant.MerchantStoreService;
import com.salesmanager.core.business.services.merchant.StoreCurrencyService;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.currency.Currency;
import com.salesmanager.core.model.reference.language.Language;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full-stack integration tests for {@link StoreCurrencyController}.
 *
 * <p>Covers:
 * <ol>
 *   <li>Multi-store isolation — Store A request never returns Store B currencies.</li>
 *   <li>HTTP 404 for an unknown store code.</li>
 *   <li>Cache hit — the repository is called exactly once for two consecutive identical requests.</li>
 *   <li>Cache eviction — after {@link MerchantStoreService#update(MerchantStore)}, the next
 *       request re-queries the repository and reflects the updated currency list.</li>
 *   <li>Session fallback (AC-04) — unsupported session currency → base currency in session →
 *       {@code currencyFallbackApplied=true} in response model.</li>
 *   <li>No fallback when session currency is already supported.</li>
 * </ol>
 *
 * <p>Uses an embedded H2 database; no external infrastructure required.
 * Run via {@code mvn verify -pl sm-shop}.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        // Embedded H2 — in-memory, isolated per test run
        "spring.datasource.url=jdbc:h2:mem:currency_it_db;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.h2.console.enabled=false",
        // Cache TTL — short for tests
        "store.currency.cache.ttl.seconds=300"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class StoreCurrencyControllerIT {

    // -------------------------------------------------------------------------
    // Constants
    // -------------------------------------------------------------------------

    private static final String STORE_A_CODE = "STORE_A";
    private static final String STORE_B_CODE = "STORE_B";
    private static final String UNKNOWN_STORE = "UNKNOWN_STORE_XYZ";

    private static final String USD = "USD";
    private static final String EUR = "EUR";
    private static final String GBP = "GBP";
    private static final String JPY = "JPY";
    private static final String XYZ = "XYZ"; // deliberately invalid / unsupported

    /** Path template for the store currencies endpoint. */
    private static final String CURRENCIES_URL = "/api/v1/store/{storeCode}/currencies";

    /** Path for the checkout start endpoint (session-currency validation). */
    private static final String CHECKOUT_URL = "/api/v1/cart/checkout";

    // -------------------------------------------------------------------------
    // Injected beans
    // -------------------------------------------------------------------------

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MerchantStoreService merchantStoreService;

    @Autowired
    private StoreCurrencyService storeCurrencyService;

    @Autowired
    private CacheManager cacheManager;

    /**
     * Spy wraps the real repository bean so we can assert call counts without
     * replacing the real implementation.
     */
    @SpyBean
    private MerchantStoreRepository merchantStoreRepository;

    // -------------------------------------------------------------------------
    // State
    // -------------------------------------------------------------------------

    private MerchantStore storeA;
    private MerchantStore storeB;

    // -------------------------------------------------------------------------
    // Setup / teardown
    // -------------------------------------------------------------------------

    /**
     * Creates two stores with distinct currency sets before each test.
     * All caches are cleared and spy interactions are reset to guarantee isolation.
     */
    @BeforeEach
    void setUp() throws Exception {
        clearAllCaches();
        reset(merchantStoreRepository);

        // Store A: USD (default) + EUR
        storeA = createStore(STORE_A_CODE, "Store Alpha", USD, Set.of(
                currency(USD, "$"),
                currency(EUR, "€")
        ));

        // Store B: GBP (default) + JPY
        storeB = createStore(STORE_B_CODE, "Store Beta", GBP, Set.of(
                currency(GBP, "£"),
                currency(JPY, "¥")
        ));
    }

    // -------------------------------------------------------------------------
    // TC-1: Store A isolation
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("TC-1: GET /store/STORE_A/currencies → 200, contains USD+EUR, excludes GBP+JPY")
    void getEnabledCurrencies_storeA_returnsOnlyStoreACurrencies() throws Exception {
        mockMvc.perform(get(CURRENCIES_URL, STORE_A_CODE)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.storeCode").value(STORE_A_CODE))
                .andExpect(jsonPath("$.currencies").isArray())
                // Must contain Store A currencies
                .andExpect(jsonPath("$.currencies[*].currencyCode", hasItems(USD, EUR)))
                // Must NOT contain Store B currencies
                .andExpect(jsonPath("$.currencies[*].currencyCode", not(hasItem(GBP))))
                .andExpect(jsonPath("$.currencies[*].currencyCode", not(hasItem(JPY))));
    }

    // -------------------------------------------------------------------------
    // TC-2: Store B isolation
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("TC-2: GET /store/STORE_B/currencies → 200, contains GBP+JPY, excludes USD+EUR")
    void getEnabledCurrencies_storeB_returnsOnlyStoreBCurrencies() throws Exception {
        mockMvc.perform(get(CURRENCIES_URL, STORE_B_CODE)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.storeCode").value(STORE_B_CODE))
                .andExpect(jsonPath("$.currencies").isArray())
                // Must contain Store B currencies
                .andExpect(jsonPath("$.currencies[*].currencyCode", hasItems(GBP, JPY)))
                // Must NOT contain Store A currencies
                .andExpect(jsonPath("$.currencies[*].currencyCode", not(hasItem(USD))))
                .andExpect(jsonPath("$.currencies[*].currencyCode", not(hasItem(EUR))));
    }

    // -------------------------------------------------------------------------
    // TC-3: Unknown store → 404
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("TC-3: GET /store/UNKNOWN/currencies → 404")
    void getEnabledCurrencies_unknownStore_returns404() throws Exception {
        mockMvc.perform(get(CURRENCIES_URL, UNKNOWN_STORE)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------------------------
    // TC-4: Cache hit — repository called exactly once for two requests
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("TC-4: Two identical requests → repository called exactly once (cache hit)")
    void getEnabledCurrencies_cacheHit_repositoryCalledOnce() throws Exception {
        // First call — cold cache, repository must be queried
        mockMvc.perform(get(CURRENCIES_URL, STORE_A_CODE)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Second call — warm cache, repository must NOT be queried again
        mockMvc.perform(get(CURRENCIES_URL, STORE_A_CODE)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Verify the underlying repository method was invoked exactly once
        verify(merchantStoreRepository, times(1))
                .findEnabledCurrenciesByStoreCode(STORE_A_CODE);
    }

    // -------------------------------------------------------------------------
    // TC-5: Cache eviction after store update
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("TC-5: After MerchantStoreService.update(), cache evicted → repository re-queried")
    void getEnabledCurrencies_afterStoreUpdate_cacheEvicted() throws Exception {
        // --- First request: populates cache ---
        mockMvc.perform(get(CURRENCIES_URL, STORE_A_CODE)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currencies[*].currencyCode", hasItems(USD, EUR)));

        verify(merchantStoreRepository, times(1))
                .findEnabledCurrenciesByStoreCode(STORE_A_CODE);

        // --- Mutate Store A: remove EUR, add GBP ---
        MerchantStore freshStoreA = merchantStoreService.getByCode(STORE_A_CODE);
        freshStoreA.getCurrencies().removeIf(c -> EUR.equals(c.getCode()));
        freshStoreA.getCurrencies().add(currency(GBP, "£"));
        merchantStoreService.update(freshStoreA); // must trigger @CacheEvict

        // --- Second request: cache evicted, repository re-queried ---
        mockMvc.perform(get(CURRENCIES_URL, STORE_A_CODE)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                // USD still present
                .andExpect(jsonPath("$.currencies[*].currencyCode", hasItem(USD)))
                // EUR removed
                .andExpect(jsonPath("$.currencies[*].currencyCode", not(hasItem(EUR))))
                // GBP added
                .andExpect(jsonPath("$.currencies[*].currencyCode", hasItem(GBP)));

        // Repository must have been called a second time after eviction
        verify(merchantStoreRepository, times(2))
                .findEnabledCurrenciesByStoreCode(STORE_A_CODE);
    }

    // -------------------------------------------------------------------------
    // TC-6: Session fallback — unsupported currency
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("TC-6: Session currency 'XYZ' not in store list → fallbackApplied=true, session updated to USD")
    void sessionFallback_unsupportedCurrency_fallbackApplied() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("currency", XYZ);

        MvcResult result = mockMvc.perform(get(CHECKOUT_URL)
                        .param("store", STORE_A_CODE)
                        .session(session)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(body);

        // Response model must carry currencyFallbackApplied=true
        assertThat(root.path("currencyFallbackApplied").asBoolean())
                .as("Expected currencyFallbackApplied=true when session currency '%s' is unsupported", XYZ)
                .isTrue();

        // Session must be updated to the store's base currency (USD) to prevent repeated fallback
        Object updatedCurrency = session.getAttribute("currency");
        assertThat(updatedCurrency)
                .as("Session currency must be updated to store base currency '%s' after fallback", USD)
                .isEqualTo(USD);
    }

    // -------------------------------------------------------------------------
    // TC-7: Session fallback — supported currency, no fallback
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("TC-7: Session currency 'USD' is in store list → no fallback applied")
    void sessionFallback_supportedCurrency_noFallback() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("currency", USD);

        MvcResult result = mockMvc.perform(get(CHECKOUT_URL)
                        .param("store", STORE_A_CODE)
                        .session(session)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(body);

        // currencyFallbackApplied must be absent or explicitly false
        JsonNode fallbackNode = root.path("currencyFallbackApplied");
        if (!fallbackNode.isMissingNode()) {
            assertThat(fallbackNode.asBoolean())
                    .as("currencyFallbackApplied must be false when session currency '%s' is supported", USD)
                    .isFalse();
        }

        // Session currency must remain USD — no mutation
        Object sessionCurrency = session.getAttribute("currency");
        assertThat(sessionCurrency)
                .as("Session currency must remain '%s' when it is already supported", USD)
                .isEqualTo(USD);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Creates and persists a {@link MerchantStore} with the given currencies.
     *
     * @param code            store code (unique identifier)
     * @param name            human-readable store name
     * @param defaultCurrency ISO 4217 code of the store's base currency
     * @param currencies      full set of enabled currencies (must include the default)
     * @return the persisted {@link MerchantStore}
     */
    private MerchantStore createStore(String code,
                                      String name,
                                      String defaultCurrency,
                                      Set<Currency> currencies) throws Exception {
        MerchantStore store = new MerchantStore();
        store.setCode(code);
        store.setStorename(name);
        store.setStorephone("000-000-0000");
        store.setStoreEmailAddress("test@" + code.toLowerCase(Locale.ROOT) + ".com");

        // Resolve reference entities that must already exist in the reference data
        store.setDefaultLanguage(resolveLanguage("en"));
        store.setCountry(resolveCountry("US"));

        // Assign currencies
        Set<Currency> currencySet = new HashSet<>(currencies);
        store.setCurrencies(currencySet);

        // Mark the default currency
        currencySet.stream()
                .filter(c -> defaultCurrency.equals(c.getCode()))
                .findFirst()
                .ifPresent(store::setDefaultCurrency);

        merchantStoreService.create(store);
        return store;
    }

    /**
     * Builds a transient {@link Currency} entity (not yet persisted).
     */
    private Currency currency(String code, String symbol) {
        Currency c = new Currency();
        c.setCode(code);
        c.setName(code);
        c.setSupported(true);
        return c;
    }

    /**
     * Resolves the {@link Language} reference entity for the given ISO code.
     * Shopizer pre-populates reference data on startup; this method retrieves it.
     */
    private Language resolveLanguage(String isoCode) {
        // Shopizer's reference data loader populates languages; retrieve by code
        Language lang = new Language();
        lang.setCode(isoCode);
        return lang;
    }

    /**
     * Resolves the {@link com.salesmanager.core.model.reference.country.Country} reference
     * entity for the given ISO code.
     */
    private com.salesmanager.core.model.reference.country.Country resolveCountry(String isoCode) {
        com.salesmanager.core.model.reference.country.Country country =
                new com.salesmanager.core.model.reference.country.Country();
        country.setIsoCode(isoCode);
        return country;
    }

    /**
     * Clears all Spring Cache entries to guarantee test isolation.
     * Called before each test so that no stale cache state leaks between tests.
     */
    private void clearAllCaches() {
        if (cacheManager == null) {
            return;
        }
        cacheManager.getCacheNames().forEach(name -> {
            var cache = cacheManager.getCache(name);
            if (cache != null) {
                cache.clear();
            }
        });
    }
}
```