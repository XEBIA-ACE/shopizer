```java
package com.salesmanager.core.business.services.currency;

import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.model.reference.currency.Currency;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.user.User;
import java.util.List;

public interface CurrencyManagementService {

    List<Currency> listByStore(MerchantStore store, User user) throws ServiceException;

    void addCurrency(MerchantStore store, Currency currency, User user) throws ServiceException;

    void removeCurrency(MerchantStore store, Currency currency, User user) throws ServiceException;
}
```