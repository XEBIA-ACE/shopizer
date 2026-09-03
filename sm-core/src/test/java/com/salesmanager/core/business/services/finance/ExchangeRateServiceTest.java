```java
package com.salesmanager.core.business.services.finance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import com.salesmanager.core.business.services.finance.impl.ExchangeRateServiceImpl;
import com.salesmanager.core.model.finance.ExclusiveRateRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ExchangeRateServiceTest {

    @InjectMocks
    private ExchangeRateServiceImpl exchangeRateService;
    
    @Mock
    private ExchangeRateApiClient exchangeRateApiClient;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetExchangeRate_success() {
        ExclusiveRateRequest request = new ExclusiveRateRequest("USD", "EUR");
        when(exchangeRateApiClient.getRate("USD", "EUR")).thenReturn(new BigDecimal("0.85"));

        BigDecimal rate = exchangeRateService.getExchangeRate(request);
        assertEquals(new BigDecimal("0.85"), rate);
    }

    @Test
    public void testGetExchangeRate_apiFailure() {
        ExclusiveRateRequest request = new ExclusiveRateRequest("USD", "EUR");
        when(exchangeRateApiClient.getRate("USD", "EUR")).thenReturn(null);

        assertThrows(RuntimeException.class, () -> {
            exchangeRateService.getExchangeRate(request);
        });
    }

    @Test
    public void testApplyFallbackRate_onApiFailure() {
        // Simulating an API failure, the service should use the last known rate
        ExclusiveRateRequest request = new ExclusiveRateRequest("USD", "EUR");
        when(exchangeRateApiClient.getRate("USD", "EUR")).thenThrow(new RuntimeException("API failure"));
        
        // Assuming last known rate was 0.84 (simulated cache or database call)
        exchangeRateService.setLastKnownRate(new BigDecimal("0.84"));

        BigDecimal rate = exchangeRateService.getExchangeRate(request);
        assertEquals(new BigDecimal("0.84"), rate);
    }
}
```