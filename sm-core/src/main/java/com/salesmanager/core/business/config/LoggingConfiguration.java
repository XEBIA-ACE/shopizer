```java
package com.salesmanager.core.business.config;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.salesmanager.core.business.constants.LoggingConstants;

public class LoggingConfiguration {

    public static void configureLogging() {
        Logger exchangeRateLogger = Logger.getLogger(LoggingConstants.EXCHANGE_RATE_SERVICE_LOG);
        exchangeRateLogger.setLevel(Level.ALL);
        
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
        exchangeRateLogger.addHandler(handler);
    }
}
```