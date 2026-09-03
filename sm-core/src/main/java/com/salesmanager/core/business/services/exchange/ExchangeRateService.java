```java
package com.salesmanager.core.business.services.exchange;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;

public class ExchangeRateService {

    private static final String EXCHANGE_RATE_API_URL = "https://api.example.com/latest";
    private static final String API_KEY = "your_api_key";
    private static final int MAX_RETRIES = 3;
    private static final Logger LOGGER = Logger.getLogger(ExchangeRateService.class.getName());

    private BigDecimal lastKnownRate = BigDecimal.ONE; // default to 1 if no rate is available

    public BigDecimal getExchangeRate(String baseCurrency, String targetCurrency) {
        int attempts = 0;
        while (attempts < MAX_RETRIES) {
            try {
                URL url = new URL(EXCHANGE_RATE_API_URL + "?base=" + baseCurrency + "&symbols=" + targetCurrency + "&apikey=" + API_KEY);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/json");
                
                if (connection.getResponseCode() == 200) {
                    // parse JSON response and extract exchange rate
                    BigDecimal rate = parseExchangeRate(connection);
                    lastKnownRate = rate; // update last known good rate
                    return rate;
                } else {
                    LOGGER.warning("Failed to fetch exchange rate. HTTP Error Code: " + connection.getResponseCode());
                }
            } catch (IOException e) {
                LOGGER.severe("Error occurred while fetching exchange rate: " + e.getMessage());
            }
            attempts++;
        }
        LOGGER.warning("Returning last known exchange rate due to repeated API failures.");
        return lastKnownRate;
    }

    private BigDecimal parseExchangeRate(HttpURLConnection connection) {
        // Placeholder for parsing logic; assumes correct JSON handling and response parsing
        return BigDecimal.ONE;
    }
}
```