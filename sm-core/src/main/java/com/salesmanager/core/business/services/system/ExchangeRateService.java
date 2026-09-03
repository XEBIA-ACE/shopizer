```java
package com.salesmanager.core.business.services.system;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExchangeRateService {

    private static final Logger LOGGER = Logger.getLogger(ExchangeRateService.class.getName());
    private static final long CACHE_DURATION = TimeUnit.HOURS.toMillis(1);
    private final Map<String, Double> exchangeRates;
    private long lastUpdated;
    
    public ExchangeRateService() {
        this.exchangeRates = new HashMap<>();
        this.lastUpdated = 0;
    }

    public double getExchangeRate(String currency) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdated > CACHE_DURATION) {
            updateExchangeRates();
        }
        return exchangeRates.getOrDefault(currency, 1.0);
    }

    private void updateExchangeRates() {
        try {
            // Code to fetch from external API
            // Simulating API response for illustration
            Map<String, Double> fetchedRates = fetchExchangeRatesFromAPI();

            // Update the local cache
            if (fetchedRates != null && !fetchedRates.isEmpty()) {
                exchangeRates.clear();
                exchangeRates.putAll(fetchedRates);
                lastUpdated = System.currentTimeMillis();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to update exchange rates. Using last known rates.", e);
        }
    }

    private Map<String, Double> fetchExchangeRatesFromAPI() {
        // This should implement the actual API call and return the currency rates.
        // For demonstration, let's return mock data.
        Map<String, Double> mockRates = new HashMap<>();
        mockRates.put("USD", 1.0);
        mockRates.put("EUR", 0.85);
        mockRates.put("GBP", 0.75);
        return mockRates;
    }
}
```