package com.salesmanager.shop.model.store;

import java.util.List;

/**
 * Response envelope for the {@code GET /api/v1/store/{storeCode}/currencies} endpoint.
 *
 * <p>JSON shape:
 * <pre>
 * {
 *   "storeCode": "DEFAULT",
 *   "currencies": [
 *     { "currencyCode": "USD", "symbol": "$", "isDefault": true },
 *     { "currencyCode": "EUR", "symbol": "€", "isDefault": false }
 *   ]
 * }
 * </pre>
 */
public class StoreCurrencyResponse {

    /** The store identifier this response is scoped to (mirrors the path variable). */
    private String storeCode;

    /** Ordered list of currencies enabled for this store. Never null; may be empty. */
    private List<CheckoutCurrencyDTO> currencies;

    public StoreCurrencyResponse() {
    }

    public String getStoreCode() {
        return storeCode;
    }

    public void setStoreCode(String storeCode) {
        this.storeCode = storeCode;
    }

    public List<CheckoutCurrencyDTO> getCurrencies() {
        return currencies;
    }

    public void setCurrencies(List<CheckoutCurrencyDTO> currencies) {
        this.currencies = currencies;
    }
}
