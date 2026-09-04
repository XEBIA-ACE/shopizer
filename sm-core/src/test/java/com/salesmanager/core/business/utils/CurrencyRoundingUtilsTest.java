package com.salesmanager.core.business.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Pure unit tests for {@link CurrencyRoundingUtils}.
 *
 * <p>Verifies ISO 4217 minor-unit rounding for a representative set of currencies
 * and validates error handling for invalid inputs.
 */
class CurrencyRoundingUtilsTest {

    // =========================================================================
    // Happy-path rounding tests
    // =========================================================================

    @Test
    @DisplayName("USD (2 decimal places): 1234.5678 → 1234.57")
    void round_usd_twoDecimalPlaces() {
        BigDecimal result = CurrencyRoundingUtils.round(new BigDecimal("1234.5678"), "USD");
        assertThat(result).isEqualByComparingTo(new BigDecimal("1234.57"));
        assertThat(result.scale()).isEqualTo(2);
    }

    @Test
    @DisplayName("JPY (0 decimal places): 1234.5678 → 1235")
    void round_jpy_zeroDecimalPlaces() {
        BigDecimal result = CurrencyRoundingUtils.round(new BigDecimal("1234.5678"), "JPY");
        assertThat(result).isEqualByComparingTo(new BigDecimal("1235"));
        assertThat(result.scale()).isEqualTo(0);
    }

    @Test
    @DisplayName("KWD (3 decimal places): 1.23456 → 1.235")
    void round_kwd_threeDecimalPlaces() {
        BigDecimal result = CurrencyRoundingUtils.round(new BigDecimal("1.23456"), "KWD");
        assertThat(result).isEqualByComparingTo(new BigDecimal("1.235"));
        assertThat(result.scale()).isEqualTo(3);
    }

    @Test
    @DisplayName("EUR (2 decimal places): 1.005 → 1.01 (HALF_UP rounding)")
    void round_eur_halfUpRounding() {
        BigDecimal result = CurrencyRoundingUtils.round(new BigDecimal("1.005"), "EUR");
        assertThat(result).isEqualByComparingTo(new BigDecimal("1.01"));
        assertThat(result.scale()).isEqualTo(2);
    }

    @Test
    @DisplayName("BHD (3 decimal places): 1.23456 → 1.235")
    void round_bhd_threeDecimalPlaces() {
        BigDecimal result = CurrencyRoundingUtils.round(new BigDecimal("1.23456"), "BHD");
        assertThat(result).isEqualByComparingTo(new BigDecimal("1.235"));
        assertThat(result.scale()).isEqualTo(3);
    }

    // =========================================================================
    // Parameterized happy-path tests (additional coverage)
    // =========================================================================

    @ParameterizedTest(name = "{0}: round({1}) → {2}")
    @CsvSource({
            "USD, 0.004,  0.00",
            "USD, 0.005,  0.01",
            "JPY, 0.4,    0",
            "JPY, 0.5,    1",
            "KWD, 1.0004, 1.000",
            "KWD, 1.0005, 1.001",
            "EUR, 99.995, 100.00"
    })
    @DisplayName("Parameterized HALF_UP rounding boundary cases")
    void round_boundaryValues(String currencyCode, String input, String expected) {
        BigDecimal result = CurrencyRoundingUtils.round(new BigDecimal(input), currencyCode);
        assertThat(result).isEqualByComparingTo(new BigDecimal(expected));
    }

    // =========================================================================
    // Error-handling tests
    // =========================================================================

    @Test
    @DisplayName("Null amount → IllegalArgumentException")
    void round_nullAmount_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> CurrencyRoundingUtils.round(null, "USD"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("amount");
    }

    @Test
    @DisplayName("Unknown currency code → IllegalArgumentException")
    void round_unknownCurrencyCode_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> CurrencyRoundingUtils.round(new BigDecimal("1.00"), "XYZ"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("XYZ");
    }

    @Test
    @DisplayName("Null currency code → IllegalArgumentException")
    void round_nullCurrencyCode_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> CurrencyRoundingUtils.round(new BigDecimal("1.00"), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("currencyCode");
    }

    @Test
    @DisplayName("Blank currency code → IllegalArgumentException")
    void round_blankCurrencyCode_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> CurrencyRoundingUtils.round(new BigDecimal("1.00"), "   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("currencyCode");
    }
}