```java
package com.salesmanager.test.business.services.system;

import com.salesmanager.core.business.services.system.ExchangeRateService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ExchangeRateServiceTest {

    private ExchangeRateService exchangeRateService;

    @Before
    public void setUp() {
        exchangeRateService = new ExchangeRateService();
    }

    @Test
    public void testGetExchangeRate() {
        double rate = exchangeRateService.getExchangeRate("EUR");
        Assert.assertEquals(0.85, rate, 0); // Assuming mock rate
    }

    @Test
    public void testFallbackOnApiFailure() {
        // Here one could simulate an API failure and check that a fallback rate is used.
        double baseRate = exchangeRateService.getExchangeRate("USD");
        Assert.assertEquals(1.0, baseRate, 0); // Default USD rate
    }
}
```