```java
package com.salesmanager.shop.store.api;

import com.salesmanager.core.business.services.currency.CurrencyService;
import com.salesmanager.shop.store.model.CurrencyChangeResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/currency")
public class CurrencyController {

    @Autowired
    private CurrencyService currencyService;

    @PostMapping("/change")
    public CurrencyChangeResponse changeCurrency(@RequestBody CurrencyRequest request) {
        try {
            double exchangeRate = currencyService.getExchangeRate(request.getCurrency());
            Locale locale = currencyService.getLocaleForCurrency(request.getCurrency());
            
            // Logic to persist selected currency for session or user
            // session.setCurrency(request.getCurrency());
            
            return new CurrencyChangeResponse(true, request.getCurrency(), exchangeRate, locale.toString());
        } catch (Exception e) {
            // Handle exception and log error
            return new CurrencyChangeResponse(false, null, 0, null);
        }
    }
}

class CurrencyRequest {
    private String currency;

    // Getters and Setters
    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}

```