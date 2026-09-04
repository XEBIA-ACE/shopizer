package com.salesmanager.core.business.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

/**
 * Utility class for ISO 4217-compliant monetary rounding.
 * Uses {@link Currency#getDefaultFractionDigits()} to determine the correct
 * number of decimal places for each currency, and applies {@link RoundingMode#HALF_UP}.
 */
public final class CurrencyRoundingUtils {

    private CurrencyRoundingUtils() {
        // utility class — no instantiation
    }

    /**
     * Rounds the given amount to the number of decimal places defined by ISO 4217
     * for the specified currency code, using {@link RoundingMode#HALF_UP}.
     *
     * <p>Examples:
     * <ul>
     *   <li>USD (2 decimals): {@code round(1234.5678, "USD")} → {@code 1234.57}</li>
     *   <li>JPY (0 decimals): {@code round(1234.5678, "JPY")} → {@code 1235}</li>
     *   <li>KWD (3 decimals): {@code round(1.23456, "KWD")} → {@code 1.235}</li>
     * </ul>
     *
     * @param amount       the monetary amount to round; must not be {@code null}
     * @param currencyCode the ISO 4217 currency code (e.g., "USD", "JPY", "KWD"); must not be {@code null} or blank
     * @return the rounded amount
     * @throws IllegalArgumentException if {@code amount} is {@code null}, or if {@code currencyCode}
     *                                  is {@code null}, blank, or not a recognised ISO 4217 code
     */
    public static BigDecimal round(BigDecimal amount, String currencyCode) {
        if (amount == null) {
            throw new IllegalArgumentException("amount must not be null");
        }
        if (currencyCode == null || currencyCode.trim().isEmpty()) {
            throw new IllegalArgumentException("currencyCode must not be null or blank");
        }

        Currency currency;
        try {
            currency = Currency.getInstance(currencyCode.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Unknown or unsupported ISO 4217 currency code: " + currencyCode, e);
        }

        int fractionDigits = currency.getDefaultFractionDigits();
        // Some currencies (e.g. XAU — gold) return -1; treat as 0 decimals
        int scale = Math.max(fractionDigits, 0);
        return amount.setScale(scale, RoundingMode.HALF_UP);
    }
}