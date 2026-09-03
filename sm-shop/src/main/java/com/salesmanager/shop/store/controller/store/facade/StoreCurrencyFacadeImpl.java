package com.salesmanager.shop.store.controller.store.facade;

import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.services.merchant.MerchantStoreService;
import com.salesmanager.core.business.services.reference.currency.CurrencyService;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.currency.Currency;
import com.salesmanager.shop.event.currency.CurrencyChangeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link StoreCurrencyFacade}.
 *
 * <p>All mutating operations are wrapped in a transaction so that the
 * database update and the in-memory state change are atomic.  After a
 * successful commit an {@link CurrencyChangeEvent} is published on the
 * Spring {@link ApplicationEventPublisher} so that any listener (e.g. a
 * WebSocket broadcaster or cache invalidator) can react in real-time.
 */
@Service("storeCurrencyFacade")
public class StoreCurrencyFacadeImpl implements StoreCurrencyFacade {

    private static final Logger LOGGER = LoggerFactory.getLogger(StoreCurrencyFacadeImpl.class);

    @Inject
    private MerchantStoreService merchantStoreService;

    @Inject
    private CurrencyService currencyService;

    @Inject
    private ApplicationEventPublisher eventPublisher;

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    @Override
    @Transactional
    public void addCurrency(String storeCode, String currencyCode) throws Exception {
        MerchantStore store = requireStore(storeCode);
        Currency currency = requireCurrency(currencyCode);

        Set<Currency> supported = store.getSupportedCurrencies();
        if (supported.contains(currency)) {
            LOGGER.debug("Currency {} is already supported by store {}", currencyCode, storeCode);
            return; // idempotent – not an error
        }

        supported.add(currency);
        merchantStoreService.update(store);

        LOGGER.info("Currency {} added to store {}", currencyCode, storeCode);
        publishEvent(storeCode, currencyCode, CurrencyChangeEvent.ChangeType.ADDED);
    }

    @Override
    @Transactional
    public void removeCurrency(String storeCode, String currencyCode) throws Exception {
        MerchantStore store = requireStore(storeCode);
        Currency currency = requireCurrency(currencyCode);

        // Guard: do not allow removal of the store's primary currency
        if (store.getCurrency() != null
                && store.getCurrency().getCode().equalsIgnoreCase(currencyCode)) {
            throw new ServiceException(
                    "Cannot remove the primary currency '" + currencyCode
                            + "' from store '" + storeCode + "'. "
                            + "Change the primary currency first.");
        }

        Set<Currency> supported = store.getSupportedCurrencies();
        boolean removed = supported.remove(currency);
        if (!removed) {
            throw new ServiceException(
                    "Currency '" + currencyCode + "' is not in the supported list of store '"
                            + storeCode + "'.");
        }

        merchantStoreService.update(store);

        LOGGER.info("Currency {} removed from store {}", currencyCode, storeCode);
        publishEvent(storeCode, currencyCode, CurrencyChangeEvent.ChangeType.REMOVED);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getSupportedCurrencies(String storeCode) throws Exception {
        MerchantStore store = requireStore(storeCode);
        Set<Currency> supported = store.getSupportedCurrencies();
        if (supported == null || supported.isEmpty()) {
            return new ArrayList<>();
        }
        return supported.stream()
                .map(Currency::getCode)
                .sorted()
                .collect(Collectors.toList());
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private MerchantStore requireStore(String storeCode) throws Exception {
        MerchantStore store = merchantStoreService.getByCode(storeCode);
        if (store == null) {
            throw new ServiceException("Merchant store not found: " + storeCode);
        }
        return store;
    }

    private Currency requireCurrency(String currencyCode) throws ServiceException {
        Currency currency = currencyService.getByCode(currencyCode);
        if (currency == null) {
            throw new ServiceException("Unknown currency code: " + currencyCode);
        }
        return currency;
    }

    private void publishEvent(String storeCode, String currencyCode,
                              CurrencyChangeEvent.ChangeType changeType) {
        try {
            eventPublisher.publishEvent(
                    new CurrencyChangeEvent(this, storeCode, currencyCode, changeType));
        } catch (Exception e) {
            // Event publishing must never roll back the main transaction
            LOGGER.warn("Failed to publish CurrencyChangeEvent for store={} currency={}: {}",
                    storeCode, currencyCode, e.getMessage());
        }
    }
}
