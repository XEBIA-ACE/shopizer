```java
package com.shopizer.localization;

import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LocalizationServiceTest {

    private final LocalizationService localizationService = new LocalizationService();

    @Test
    public void testGetLocalizedMessage() {
        assertEquals("Hello", localizationService.getLocalizedMessage("greeting", Locale.ENGLISH));
        assertEquals("Hola", localizationService.getLocalizedMessage("greeting", new Locale("es")));
        assertEquals("Goodbye", localizationService.getLocalizedMessage("farewell", new Locale("fr"))); // Fallback to default
    }

    @Test
    public void testFormatNumber() {
        double number = 1234567.89;
        assertEquals("1,234,567.89", localizationService.formatNumber(number, Locale.US));
        assertEquals("1.234.567,89", localizationService.formatNumber(number, Locale.GERMANY));
    }

    @Test
    public void testFormatDate() {
        Date date = new Date(1234567890L);
        assertEquals("Feb 14, 1970", localizationService.formatDate(date, Locale.US));
        assertEquals("14.02.1970", localizationService.formatDate(date, Locale.GERMANY));
    }
}
```