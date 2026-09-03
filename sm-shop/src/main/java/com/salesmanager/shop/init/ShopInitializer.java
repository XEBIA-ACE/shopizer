```java
package com.salesmanager.shop.init;

import javax.annotation.PostConstruct;

import com.salesmanager.core.business.config.LoggingConfiguration;

public class ShopInitializer {

    @PostConstruct
    public void init() {
        // Other initialization logic...
        LoggingConfiguration.configureLogging();
    }
}
```