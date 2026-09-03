```java
package com.shopizer.services;

import com.shopizer.entities.CustomerPreferences;
import com.shopizer.repositories.CustomerPreferencesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class LocalizationService {

    @Autowired
    private CustomerPreferencesRepository preferencesRepository;

    public String getPreferredLanguage(Long customerId) {
        Optional<CustomerPreferences> preferences = preferencesRepository.findByCustomerId(customerId);
        return preferences.map(CustomerPreferences::getPreferredLanguage).orElse("en-US");
    }

    public String getPreferredLocale(Long customerId) {
        Optional<CustomerPreferences> preferences = preferencesRepository.findByCustomerId(customerId);
        return preferences.map(CustomerPreferences::getPreferredLocale).orElse("en-US");
    }
}
```