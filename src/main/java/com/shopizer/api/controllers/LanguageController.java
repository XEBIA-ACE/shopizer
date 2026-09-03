```java
package com.shopizer.api.controllers;

import com.shopizer.services.locale.LanguageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/language")
public class LanguageController {

    @Autowired
    private LanguageService languageService;

    @PostMapping("/set")
    public void setLanguage(@RequestBody Map<String, String> languageParams) {
        String language = languageParams.get("language");
        Locale locale = new Locale(language);
        // Assuming session management is in place to store user preferences
        // session.setAttribute("userLocale", locale);
    }

    @GetMapping("/message")
    public String getMessage(@RequestParam String key, @RequestParam String language) {
        Locale locale = new Locale(language);
        return languageService.getMessage(key, locale);
    }
}
```