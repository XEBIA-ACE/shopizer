```java
package com.shopizer.localization;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class LocalizationService {

    private static final String BUNDLE_BASE_NAME = "messages";
    private static final Locale DEFAULT_LOCALE = Locale.ENGLISH;

    public String getLocalizedMessage(String key, Locale userLocale) {
        ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_BASE_NAME, userLocale != null ? userLocale : DEFAULT_LOCALE);
        return bundle.containsKey(key) ? bundle.getString(key) : ResourceBundle.getBundle(BUNDLE_BASE_NAME, DEFAULT_LOCALE).getString(key);
    }

    public String formatNumber(double number, Locale userLocale) {
        NumberFormat numberFormat = NumberFormat.getInstance(userLocale != null ? userLocale : DEFAULT_LOCALE);
        return numberFormat.format(number);
    }

    public String formatDate(java.util.Date date, Locale userLocale) {
        SimpleDateFormat dateFormatter = (SimpleDateFormat) SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM,
                userLocale != null ? userLocale : DEFAULT_LOCALE);
        return dateFormatter.format(date);
    }
}
```