```java
package com.salesmanager.core.business.services.currency;

import com.salesmanager.core.business.services.common.GenericEntityServiceImpl;
import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.repositories.currency.CurrencyRepository;
import com.salesmanager.core.model.reference.currency.Currency;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.user.User;
import com.salesmanager.core.business.services.system.auth.AuthorizationService;
import com.salesmanager.core.constants.RolesConstants;
import java.util.List;

public class CurrencyManagementServiceImpl extends GenericEntityServiceImpl<Long, Currency> implements CurrencyManagementService {

    private CurrencyRepository currencyRepository;
    private AuthorizationService authorizationService;

    public CurrencyManagementServiceImpl(CurrencyRepository currencyRepository, AuthorizationService authorizationService) {
        super(currencyRepository);
        this.currencyRepository = currencyRepository;
        this.authorizationService = authorizationService;
    }

    @Override
    public List<Currency> listByStore(MerchantStore store, User user) throws ServiceException {
        if (!authorizationService.hasRole(user, RolesConstants.ADMIN)) {
            logUnauthorizedAccess(user);
            throw new ServiceException("Unauthorized access to currency management");
        }
        return currencyRepository.findByStore(store.getId());
    }

    @Override
    public void addCurrency(MerchantStore store, Currency currency, User user) throws ServiceException {
        if (!authorizationService.hasRole(user, RolesConstants.ADMIN)) {
            logUnauthorizedAccess(user);
            throw new ServiceException("Unauthorized access to add currency");
        }
        currencyRepository.save(currency);
    }

    @Override
    public void removeCurrency(MerchantStore store, Currency currency, User user) throws ServiceException {
        if (!authorizationService.hasRole(user, RolesConstants.ADMIN)) {
            logUnauthorizedAccess(user);
            throw new ServiceException("Unauthorized access to remove currency");
        }
        currencyRepository.delete(currency);
    }

    private void logUnauthorizedAccess(User user) {
        // Log unauthorized access attempt
        System.out.println("Unauthorized access attempt by user: " + user.getUsername());
    }
}
```