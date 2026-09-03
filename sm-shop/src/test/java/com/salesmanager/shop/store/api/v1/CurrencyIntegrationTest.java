```java
package com.salesmanager.shop.store.api.v1;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.salesmanager.core.business.services.finance.ExchangeRateService;
import com.salesmanager.shop.utils.IntegrationTestUtil;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class CurrencyIntegrationTest extends IntegrationTestUtil {

    @Autowired
    private ExchangeRateService exchangeRateService;

    @Test
    public void testCurrencyConversionOnSelection() throws Exception {
        // Simulate selecting a different currency
        mockMvc.perform(authenticatedRequest("/shop/currency/select/USD"))
                .andExpect(status().isOk());

        // Ensure that the prices are updated accordingly
        mockMvc.perform(authenticatedRequest("/shop/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.priceCurrency").value("USD"));
    }

    @Test
    public void testPriceUpdateWithExchangeRate() throws Exception {
        // Setup initial exchange rate
        exchangeRateService.setLastKnownRate("EUR", "USD", new BigDecimal("1.1"));

        // Simulate price update with new rate
        mockMvc.perform(authenticatedRequest("/shop/products/1/update-price"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value("110.00")) // Assuming the base price is 100 EUR
                .andExpect(jsonPath("$.priceCurrency").value("USD"));
    }
}
```