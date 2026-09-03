```java
package com.salesmanager.core.business.services.currency;

public interface CurrencyService {
    double getExchangeRate(String currency);
    Locale getLocaleForCurrency(String currency);
}
```