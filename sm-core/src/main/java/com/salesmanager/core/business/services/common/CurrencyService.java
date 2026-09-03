```java
package com.salesmanager.core.business.services.common;

import com.salesmanager.core.business.repositories.common.CurrencyRepository;
import com.salesmanager.core.business.services.common.exchange.ExchangeRateProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CurrencyService {

    @Autowired
    private CurrencyRepository currencyRepository;
  
    @Autowired
    private ExchangeRateProvider exchangeRateProvider;
    
    public double getExchangeRate(String currencyCode) throws Exception {
        try {
            return exchangeRateProvider.getExchangeRate(currencyCode);
        } catch (Exception e) {
            throw new Exception("Failed to fetch exchange rate", e);
        }
    }
}
```