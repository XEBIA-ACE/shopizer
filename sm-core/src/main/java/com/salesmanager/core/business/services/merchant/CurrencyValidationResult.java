package com.salesmanager.core.business.services.merchant;

import com.salesmanager.shop.model.store.CheckoutCurrencyDTO;

/**
 * Value object returned by
 * {@link StoreCurrencyService#validateSessionCurrency(String, String)}.
 *
 * <p>When {@code valid} is {@code false}, {@code fallbackCurrency} holds the store's
 * default currency that should be used instead (AC-04 / FR-03).
 */
public class CurrencyValidationResult {

    private final boolean valid;
    private final CheckoutCurrencyDTO fallbackCurrency;

    private CurrencyValidationResult(boolean valid, CheckoutCurrencyDTO fallbackCurrency) {
        this.valid = valid;
        this.fallbackCurrency = fallbackCurrency;
    }

    /** Creates a result indicating the session currency is valid for this store. */
    public static CurrencyValidationResult valid() {
        return new CurrencyValidationResult(true, null);
    }

    /**
     * Creates a result indicating the session currency is NOT valid for this store.
     *
     * @param fallback the store's default currency to use as fallback
     */
    public static CurrencyValidationResult invalid(CheckoutCurrencyDTO fallback) {
        return new CurrencyValidationResult(false, fallback);
    }

    /** @return true when the session currency is in the store's enabled list */
    public boolean isValid() {
        return valid;
    }

    /**
     * @return the store's default currency to fall back to; {@code null} when
     *         {@link #isValid()} is {@code true}
     */
    public CheckoutCurrencyDTO getFallbackCurrency() {
        return fallbackCurrency;
    }
}
