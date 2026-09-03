```java
package com.shopizer.services.locale;

import java.util.Locale;
import java.util.ResourceBundle;
import org.springframework.stereotype.Service;

@Service
public class LanguageService {

    public String getMessage(String key, Locale locale) {
        ResourceBundle bundle = ResourceBundle.getBundle("messages", locale);
        return bundle.getString(key);
    }
}
```