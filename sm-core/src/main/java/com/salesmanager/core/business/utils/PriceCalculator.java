```java
package com.salesmanager.core.business.utils;

import com.salesmanager.core.business.services.system.ExchangeRateService;

public class PriceCalculator {

    private final ExchangeRateService exchangeRateService;

    public PriceCalculator(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    public double convertPrice(double baseAmount, String targetCurrency) {
        double exchangeRate = exchangeRateService.getExchangeRate(targetCurrency);
        return baseAmount * exchangeRate;
    }
}
```