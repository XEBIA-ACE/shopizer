```java
package com.shopizer.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class LocalizationTest {

    @Autowired
    private RestTemplate restTemplate;

    private String baseUrl = "http://localhost:8080/api";
    private HttpHeaders headers;

    @BeforeEach
    public void setUp() {
        headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Accept-Language", "en-US");
    }

    @Test
    public void testLanguagePersistenceAcrossSessions() {
        headers.set("Accept-Language", "fr-FR");
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);

        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + "/customers/123/preferences",
            HttpMethod.PUT, 
            entity, 
            String.class
        );
        
        assertEquals(200, response.getStatusCodeValue());

        ResponseEntity<String> responseCheck = restTemplate.exchange(
            baseUrl + "/customers/123/preferences",
            HttpMethod.GET, 
            new HttpEntity<String>(null, headers), 
            String.class
        );

        assertTrue(responseCheck.getBody().contains("fr-FR"));
    }

    @Test
    public void testFallbackToDefaultLanguage() {
        headers.set("Accept-Language", "es-ES");
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);

        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + "/products/1",
            HttpMethod.GET, 
            entity, 
            String.class
        );

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("default language content"));
    }

    @Test
    public void testLocaleBasedNumberAndDateFormatting() {
        headers.set("Accept-Language", "de-DE");
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);

        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + "/transactions/1/details",
            HttpMethod.GET, 
            entity, 
            String.class
        );

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("Number: 1.000,00"));
        assertTrue(response.getBody().contains("Date: dd.MM.yyyy"));
    }
}
```