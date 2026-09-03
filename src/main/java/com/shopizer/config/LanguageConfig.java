```java
package com.shopizer.config;

import java.util.Locale;

/**
 * LanguageConfig stores the user language and locale preferences.
 */
public class LanguageConfig {

    private Locale userLocale;

    // Set user preference of Locale
    public void setUserLocale(Locale userLocale) {
        this.userLocale = userLocale;
    }

    // Get user preference of Locale
    public Locale getUserLocale() {
        return userLocale;
    }
}
```