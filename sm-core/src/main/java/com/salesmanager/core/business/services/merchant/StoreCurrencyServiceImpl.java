package com.salesmanager.core.business.services.merchant;

import com.salesmanager.core.business.exception.StoreNotFoundException;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.currency.Currency;
import com.salesmanager.core.business.repositories.merchant.MerchantStoreRepository;
import com.salesmanager.shop.model.store.CheckoutCurrencyDTO;
import com.salesmanager.shop.model.store.CurrencyValidationResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Default implementation of {@link StoreCurrencyService}.
 *
 * <p>Retrieves the set of enabled currencies for a given store from the
 * {@link MerchantStoreRepository} and maps them to {@link CheckoutCurrencyDTO}s.
 * The store's {@code defaultCurrency} is marked with {@code isDefault=true}.
 *
 * <p>If the store has no enabled currencies, the base currency is returned as the
 * sole entry in the list.
 */
@Service
public class StoreCurrencyServiceImpl implements StoreCurrencyService {

    private final MerchantStoreRepository merchantStoreRepository;

    public StoreCurrencyServiceImpl(MerchantStoreRepository merchantStoreRepository) {
        this.merchantStoreRepository = merchantStoreRepository;
    }

    /**
     * {@inheritDoc}
     *
     * @throws StoreNotFoundException if no {@link MerchantStore} exists for the given {@code storeCode}
     */
    @Override
    public List<CheckoutCurrencyDTO> getEnabledCurrencies(String storeCode) {
        MerchantStore store = resolveStore(storeCode);

        Currency defaultCurrency = store.getCurrency();
        String defaultCode = (defaultCurrency != null) ? defaultCurrency.getCode() : null;

        Set<Currency> currencies = store.getCurrencies();

        if (currencies == null || currencies.isEmpty()) {
            // Return only the base currency when no explicit list is configured
            return buildBaseCurrencyList(defaultCurrency);
        }

        List<CheckoutCurrencyDTO> result = new ArrayList<>();
        for (Currency currency : currencies) {
            if (currency == null) {
                continue;
            }
            boolean isDefault = currency.getCode() != null
                    && currency.getCode().equals(defaultCode);
            result.add(toDTO(currency, isDefault));
        }

        // Ensure the default currency is always present
        boolean defaultPresent = result.stream()
                .anyMatch(CheckoutCurrencyDTO::isDefault);
        if (!defaultPresent && defaultCurrency != null) {
            result.add(0, toDTO(defaultCurrency, true));
        }

        return Collections.unmodifiableList(result);
    }

    /**
     * {@inheritDoc}
     *
     * @throws StoreNotFoundException if no {@link MerchantStore} exists for the given {@code storeCode}
     */
    @Override
    public CurrencyValidationResult validateSessionCurrency(String storeCode, String currencyCode) {
        MerchantStore store = resolveStore(storeCode);

        Currency defaultCurrency = store.getCurrency();
        String defaultCode = (defaultCurrency != null) ? defaultCurrency.getCode() : null;

        // Null or blank session currency → always fall back
        if (currencyCode == null || currencyCode.trim().isEmpty()) {
            return new CurrencyValidationResult(true, defaultCode);
        }

        String trimmedCode = currencyCode.trim();

        // Check whether the session currency is in the store's enabled list
        Set<Currency> currencies = store.getCurrencies();
        if (currencies != null) {
            boolean supported = currencies.stream()
                    .filter(c -> c != null && c.getCode() != null)
                    .anyMatch(c -> c.getCode().equals(trimmedCode));
            if (supported) {
                return new CurrencyValidationResult(false, trimmedCode);
            }
        }

        // Also accept the default currency even if not in the explicit set
        if (trimmedCode.equals(defaultCode)) {
            return new CurrencyValidationResult(false, trimmedCode);
        }

        return new CurrencyValidationResult(true, defaultCode);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private MerchantStore resolveStore(String storeCode) {
        if (storeCode == null || storeCode.trim().isEmpty()) {
            throw new StoreNotFoundException("storeCode must not be null or blank");
        }
        Optional<MerchantStore> storeOpt = merchantStoreRepository.findByCode(storeCode);
        return storeOpt.orElseThrow(() ->
                new StoreNotFoundException("No store found for code: " + storeCode));
    }

    private List<CheckoutCurrencyDTO> buildBaseCurrencyList(Currency defaultCurrency) {
        if (defaultCurrency == null) {
            return Collections.emptyList();
        }
        return Collections.singletonList(toDTO(defaultCurrency, true));
    }

    private CheckoutCurrencyDTO toDTO(Currency currency, boolean isDefault) {
        String symbol = resolveSymbol(currency.getCode());
        return new CheckoutCurrencyDTO(currency.getCode(), symbol, isDefault);
    }

    /**
     * Resolves the display symbol for a currency code using {@link java.util.Currency}.
     * Falls back to the code itself if the JDK does not recognise the code.
     */
    private String resolveSymbol(String code) {
        if (code == null) {
            return "";
        }
        try {
            return java.util.Currency.getInstance(code).getSymbol();
        } catch (IllegalArgumentException e) {
            return code;
        }
    }
}