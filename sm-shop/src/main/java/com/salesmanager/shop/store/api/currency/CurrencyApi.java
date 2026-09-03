```java
package com.salesmanager.shop.store.api.currency;

import com.salesmanager.core.business.services.common.CurrencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/currency")
public class CurrencyApi {

    @Autowired
    private CurrencyService currencyService;

    @GetMapping("/rate")
    public ResponseEntity<Object> getExchangeRate(@RequestParam String currency) {
        try {
            double rate = currencyService.getExchangeRate(currency);
            return ResponseEntity.ok().body(Map.of("exchangeRate", rate));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to retrieve exchange rate"));
        }
    }
}
```