package com.salesmanager.shop.store.controller.store.facade;

import java.util.List;

/**
 * Facade for admin-configurable currency management on a storefront.
 *
 * Supports adding and removing currencies from a merchant store with
 * real-time update capabilities and data integrity guarantees.
 */
public interface StoreCurrencyFacade {

    /**
     * Add a currency (identified by ISO 4217 code) to the given store's
     * set of supported currencies.
     *
     * @param storeCode    the unique code of the merchant store
     * @param currencyCode ISO 4217 currency code (e.g. "USD", "EUR")
     * @throws com.salesmanager.core.business.exception.ServiceException
     *         if the currency code is unknown or the store does not exist
     */
    void addCurrency(String storeCode, String currencyCode) throws Exception;

    /**
     * Remove a currency from the given store's set of supported currencies.
     *
     * <p>If the removed currency is the store's primary currency the operation
     * is rejected to preserve data integrity.
     *
     * @param storeCode    the unique code of the merchant store
     * @param currencyCode ISO 4217 currency code to remove
     * @throws com.salesmanager.core.business.exception.ServiceException
     *         if the currency is not currently supported or is the primary currency
     */
    void removeCurrency(String storeCode, String currencyCode) throws Exception;

    /**
     * Return the list of ISO 4217 currency codes currently supported by the store.
     *
     * @param storeCode the unique code of the merchant store
     * @return list of currency codes (never {@code null})
     */
    List<String> getSupportedCurrencies(String storeCode) throws Exception;
}
