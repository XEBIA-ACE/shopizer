```java
package com.salesmanager.core.business.services.common.exchange.impl;

import com.salesmanager.core.business.services.common.exchange.ExchangeRateProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import org.json.JSONObject;

@Service
public class ApiExchangeRateProviderImpl implements ExchangeRateProvider {

    @Value("${exchange.api.url}")
    private String apiUrl;

    @Value("${exchange.api.key}")
    private String apiKey;

    @Override
    public double getExchangeRate(String currencyCode) throws Exception {
        String urlString = apiUrl + "?access_key=" + apiKey + "&symbols=" + currencyCode;
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.connect();

        int responseCode = conn.getResponseCode();

        if (responseCode != 200) {
            throw new IOException("Failed to fetch exchange rate.");
        }

        Scanner sc = new Scanner(url.openStream());
        StringBuilder jsonResponse = new StringBuilder();
        while (sc.hasNext()) {
            jsonResponse.append(sc.nextLine());
        }
        sc.close();

        JSONObject jsonObject = new JSONObject(jsonResponse.toString());
        return jsonObject.getJSONObject("rates").getDouble(currencyCode);
    }
}
```