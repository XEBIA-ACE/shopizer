```java
package com.shopizer.services.localization;

import java.util.Locale;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.shopizer.services.store.CustomerPreferenceStorage;

@Service
public class LocaleService {

    @Autowired
    private CustomerPreferenceStorage customerPreferenceStorage;

    /**
     * Retrieves the user's preferred locale or returns a default.
     * 
     * @param userId Unique identifier for the user.
     * @return Locale object representing user's preferred locale.
     */
    public Locale getUserLocale(Long userId) {
        Optional<Locale> locale = customerPreferenceStorage.getPreferredLocale(userId);
        return locale.orElse(Locale.ENGLISH); // Default to English if not set
    }
    
    // Additional methods for setting and updating user preferences can be added here
}
```