```java
package com.salesmanager.test.business.utils;

import com.salesmanager.core.business.services.system.ExchangeRateService;
import com.salesmanager.core.business.utils.PriceCalculator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class PriceCalculatorTest {

    private ExchangeRateService exchangeRateService;
    private PriceCalculator priceCalculator;

    @Before
    public void setUp() {
        exchangeRateService = Mockito.mock(ExchangeRateService.class);
        priceCalculator = new PriceCalculator(exchangeRateService);
    }

    @Test
    public void testConvertPrice() {
        Mockito.when(exchangeRateService.getExchangeRate("EUR")).thenReturn(0.85);
        double convertedPrice = priceCalculator.convertPrice(100, "EUR");
        Assert.assertEquals(85.0, convertedPrice, 0.01);
    }
}
```