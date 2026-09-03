```java
package com.salesmanager.core.business.services.common.exchange;

public interface ExchangeRateProvider {
    double getExchangeRate(String currencyCode) throws Exception;
}
```