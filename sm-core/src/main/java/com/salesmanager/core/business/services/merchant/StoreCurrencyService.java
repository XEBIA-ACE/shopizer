package com.salesmanager.core.business.services.merchant;

import com.salesmanager.shop.model.store.CheckoutCurrencyDTO;
import com.salesmanager.shop.model.store.CurrencyValidationResult;

import java.util.List;

public interface StoreCurrencyService {

    /**
     * Returns the list of enabled currencies for the given store.
     *
     * @param storeCode the store code
     * @return list of enabled currency DTOs
     * @throws com.salesmanager.core.business.exception.StoreNotFoundException if the store is not found
     */
    List<CheckoutCurrencyDTO> getEnabledCurrencies(String storeCode);

    /**
     * Validates whether the given currency code is supported by the store.
     * If not, falls back to the store's default currency.
     *
     * @param storeCode    the store code
     * @param currencyCode the session currency code (may be null or blank)
     * @return validation result containing resolved currency and fallback flag
     * @throws com.salesmanager.core.business.exception.StoreNotFoundException if the store is not found
     */
    CurrencyValidationResult validateSessionCurrency(String storeCode, String currencyCode);
}