package com.salesmanager.core.business.services.merchant;

import com.salesmanager.core.business.exception.StoreNotFoundException;
import com.salesmanager.core.business.repositories.merchant.MerchantStoreRepository;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.currency.Currency;
import com.salesmanager.shop.model.store.CheckoutCurrencyDTO;
import com.salesmanager.shop.model.store.CurrencyValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Pure unit tests for {@link StoreCurrencyServiceImpl}.
 *
 * <p>No Spring application context is loaded. All dependencies are mocked with Mockito.
 */
@ExtendWith(MockitoExtension.class)
class StoreCurrencyServiceImplTest {

    private static final String STORE_CODE = "DEFAULT";
    private static final String UNKNOWN_STORE_CODE = "UNKNOWN";

    @Mock
    private MerchantStoreRepository merchantStoreRepository;

    private StoreCurrencyServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new StoreCurrencyServiceImpl(merchantStoreRepository);
    }

    // =========================================================================
    // getEnabledCurrencies tests
    // =========================================================================

    @Test
    @DisplayName("getEnabledCurrencies — happy path: store has USD (default) and EUR enabled")
    void getEnabledCurrencies_happyPath() {
        // Arrange
        Currency usd = buildCurrency("USD");
        Currency eur = buildCurrency("EUR");

        MerchantStore store = buildStore(STORE_CODE, usd, usd, eur);
        when(merchantStoreRepository.findByCode(STORE_CODE)).thenReturn(Optional.of(store));

        // Act
        List<CheckoutCurrencyDTO> result = service.getEnabledCurrencies(STORE_CODE);

        // Assert
        assertThat(result).hasSize(2);

        CheckoutCurrencyDTO usdDto = result.stream()
                .filter(dto -> "USD".equals(dto.getCurrencyCode()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("USD not found in result"));

        CheckoutCurrencyDTO eurDto = result.stream()
                .filter(dto -> "EUR".equals(dto.getCurrencyCode()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("EUR not found in result"));

        assertThat(usdDto.isDefault())
                .as("USD should be marked as default because it matches the store's defaultCurrency")
                .isTrue();
        assertThat(eurDto.isDefault())
                .as("EUR should NOT be marked as default")
                .isFalse();

        assertThat(usdDto.getCurrencyCode()).isEqualTo("USD");
        assertThat(eurDto.getCurrencyCode()).isEqualTo("EUR");

        // Symbols must be non-null (resolved from JDK)
        assertThat(usdDto.getSymbol()).isNotNull();
        assertThat(eurDto.getSymbol()).isNotNull();
    }

    @Test
    @DisplayName("getEnabledCurrencies — unknown store throws StoreNotFoundException")
    void getEnabledCurrencies_unknownStore() {
        // Arrange
        when(merchantStoreRepository.findByCode(UNKNOWN_STORE_CODE)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> service.getEnabledCurrencies(UNKNOWN_STORE_CODE))
                .isInstanceOf(StoreNotFoundException.class)
                .hasMessageContaining(UNKNOWN_STORE_CODE);
    }

    @Test
    @DisplayName("getEnabledCurrencies — store exists but has no enabled currencies; returns only base currency")
    void getEnabledCurrencies_emptyList() {
        // Arrange
        Currency usd = buildCurrency("USD");
        MerchantStore store = buildStore(STORE_CODE, usd /* no additional currencies */);
        when(merchantStoreRepository.findByCode(STORE_CODE)).thenReturn(Optional.of(store));

        // Act
        List<CheckoutCurrencyDTO> result = service.getEnabledCurrencies(STORE_CODE);

        // Assert
        assertThat(result).hasSize(1);
        CheckoutCurrencyDTO dto = result.get(0);
        assertThat(dto.getCurrencyCode()).isEqualTo("USD");
        assertThat(dto.isDefault()).isTrue();
    }

    // =========================================================================
    // validateSessionCurrency tests
    // =========================================================================

    @Test
    @DisplayName("validateSessionCurrency — session currency is in enabled list; no fallback applied")
    void validateSessionCurrency_validCurrency() {
        // Arrange
        Currency usd = buildCurrency("USD");
        Currency eur = buildCurrency("EUR");
        MerchantStore store = buildStore(STORE_CODE, usd, usd, eur);
        when(merchantStoreRepository.findByCode(STORE_CODE)).thenReturn(Optional.of(store));

        // Act
        CurrencyValidationResult result = service.validateSessionCurrency(STORE_CODE, "EUR");

        // Assert
        assertThat(result.isFallbackApplied()).isFalse();
        assertThat(result.getResolvedCurrencyCode()).isEqualTo("EUR");
    }

    @Test
    @DisplayName("validateSessionCurrency — session currency not in enabled list; fallback to store base currency")
    void validateSessionCurrency_unsupportedCurrency() {
        // Arrange
        Currency usd = buildCurrency("USD");
        Currency eur = buildCurrency("EUR");
        MerchantStore store = buildStore(STORE_CODE, usd, usd, eur);
        when(merchantStoreRepository.findByCode(STORE_CODE)).thenReturn(Optional.of(store));

        // Act — GBP is not in the enabled list
        CurrencyValidationResult result = service.validateSessionCurrency(STORE_CODE, "GBP");

        // Assert
        assertThat(result.isFallbackApplied()).isTrue();
        assertThat(result.getResolvedCurrencyCode()).isEqualTo("USD");
    }

    @Test
    @DisplayName("validateSessionCurrency — null session currency; fallback to store base currency")
    void validateSessionCurrency_nullCurrency() {
        // Arrange
        Currency usd = buildCurrency("USD");
        MerchantStore store = buildStore(STORE_CODE, usd, usd);
        when(merchantStoreRepository.findByCode(STORE_CODE)).thenReturn(Optional.of(store));

        // Act
        CurrencyValidationResult result = service.validateSessionCurrency(STORE_CODE, null);

        // Assert
        assertThat(result.isFallbackApplied()).isTrue();
        assertThat(result.getResolvedCurrencyCode()).isEqualTo("USD");
    }

    @Test
    @DisplayName("validateSessionCurrency — blank string session currency; fallback applied")
    void validateSessionCurrency_blankCurrency() {
        // Arrange
        Currency usd = buildCurrency("USD");
        MerchantStore store = buildStore(STORE_CODE, usd, usd);
        when(merchantStoreRepository.findByCode(STORE_CODE)).thenReturn(Optional.of(store));

        // Act
        CurrencyValidationResult result = service.validateSessionCurrency(STORE_CODE, "   ");

        // Assert
        assertThat(result.isFallbackApplied()).isTrue();
        assertThat(result.getResolvedCurrencyCode()).isEqualTo("USD");
    }

    // =========================================================================
    // Private helpers
    // =========================================================================

    /**
     * Builds a {@link Currency} domain object with the given ISO 4217 code.
     */
    private Currency buildCurrency(String code) {
        Currency currency = new Currency();
        currency.setCode(code);
        return currency;
    }

    /**
     * Builds a {@link MerchantStore} with the given code, default currency, and
     * an optional set of additional enabled currencies.
     *
     * @param storeCode       the store code
     * @param defaultCurrency the store's base/default currency
     * @param enabledCurrencies zero or more currencies to add to the store's currency set
     */
    private MerchantStore buildStore(String storeCode, Currency defaultCurrency,
                                     Currency... enabledCurrencies) {
        MerchantStore store = new MerchantStore();
        store.setCode(storeCode);
        store.setCurrency(defaultCurrency);

        if (enabledCurrencies != null && enabledCurrencies.length > 0) {
            Set<Currency> currencySet = new HashSet<>();
            for (Currency c : enabledCurrencies) {
                currencySet.add(c);
            }
            store.setCurrencies(currencySet);
        }

        return store;
    }
}