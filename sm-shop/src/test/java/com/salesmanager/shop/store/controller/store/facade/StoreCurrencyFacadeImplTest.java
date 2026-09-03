package com.salesmanager.shop.store.controller.store.facade;

import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.services.merchant.MerchantStoreService;
import com.salesmanager.core.business.services.reference.currency.CurrencyService;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.currency.Currency;
import com.salesmanager.shop.event.currency.CurrencyChangeEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link StoreCurrencyFacadeImpl}.
 *
 * Covers all acceptance criteria:
 * - AC1: Can add a currency to the storefront via the backend service.
 * - AC2: Can remove a currency from the storefront via the backend service.
 * - AC3: Real-time updates occur on currency changes (event published).
 * - AC4: Data integrity is maintained during add/remove operations.
 */
@RunWith(MockitoJUnitRunner.class)
public class StoreCurrencyFacadeImplTest {

    private static final String STORE_CODE = "DEFAULT";
    private static final String USD = "USD";
    private static final String EUR = "EUR";
    private static final String GBP = "GBP";

    @Mock
    private MerchantStoreService merchantStoreService;

    @Mock
    private CurrencyService currencyService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private StoreCurrencyFacadeImpl facade;

    private MerchantStore store;
    private Currency usdCurrency;
    private Currency eurCurrency;
    private Currency gbpCurrency;

    @Before
    public void setUp() throws Exception {
        usdCurrency = buildCurrency(USD);
        eurCurrency = buildCurrency(EUR);
        gbpCurrency = buildCurrency(GBP);

        store = new MerchantStore();
        store.setCode(STORE_CODE);
        store.setCurrency(usdCurrency); // USD is the primary currency
        store.setSupportedCurrencies(new HashSet<>());

        when(merchantStoreService.getByCode(STORE_CODE)).thenReturn(store);
        when(currencyService.getByCode(USD)).thenReturn(usdCurrency);
        when(currencyService.getByCode(EUR)).thenReturn(eurCurrency);
        when(currencyService.getByCode(GBP)).thenReturn(gbpCurrency);
    }

    // -----------------------------------------------------------------------
    // AC1 – Add currency
    // -----------------------------------------------------------------------

    @Test
    public void addCurrency_shouldAddCurrencyToStore() throws Exception {
        facade.addCurrency(STORE_CODE, EUR);

        assertTrue("EUR should be in supported currencies",
                store.getSupportedCurrencies().contains(eurCurrency));
        verify(merchantStoreService).update(store);
    }

    @Test
    public void addCurrency_shouldBeIdempotentWhenAlreadyPresent() throws Exception {
        store.getSupportedCurrencies().add(eurCurrency);

        facade.addCurrency(STORE_CODE, EUR);

        // update should NOT be called again for an already-present currency
        verify(merchantStoreService, never()).update(store);
    }

    @Test(expected = ServiceException.class)
    public void addCurrency_shouldThrowWhenStoreNotFound() throws Exception {
        when(merchantStoreService.getByCode("UNKNOWN")).thenReturn(null);
        facade.addCurrency("UNKNOWN", EUR);
    }

    @Test(expected = ServiceException.class)
    public void addCurrency_shouldThrowWhenCurrencyCodeUnknown() throws Exception {
        when(currencyService.getByCode("XYZ")).thenReturn(null);
        facade.addCurrency(STORE_CODE, "XYZ");
    }

    // -----------------------------------------------------------------------
    // AC2 – Remove currency
    // -----------------------------------------------------------------------

    @Test
    public void removeCurrency_shouldRemoveCurrencyFromStore() throws Exception {
        store.getSupportedCurrencies().add(eurCurrency);

        facade.removeCurrency(STORE_CODE, EUR);

        assertFalse("EUR should no longer be in supported currencies",
                store.getSupportedCurrencies().contains(eurCurrency));
        verify(merchantStoreService).update(store);
    }

    @Test(expected = ServiceException.class)
    public void removeCurrency_shouldThrowWhenCurrencyNotInSupportedList() throws Exception {
        // GBP was never added
        facade.removeCurrency(STORE_CODE, GBP);
    }

    @Test(expected = ServiceException.class)
    public void removeCurrency_shouldThrowWhenRemovingPrimaryCurrency() throws Exception {
        // USD is the primary currency – removal must be rejected
        store.getSupportedCurrencies().add(usdCurrency);
        facade.removeCurrency(STORE_CODE, USD);
    }

    @Test(expected = ServiceException.class)
    public void removeCurrency_shouldThrowWhenStoreNotFound() throws Exception {
        when(merchantStoreService.getByCode("UNKNOWN")).thenReturn(null);
        facade.removeCurrency("UNKNOWN", EUR);
    }

    // -----------------------------------------------------------------------
    // AC3 – Real-time updates (event publishing)
    // -----------------------------------------------------------------------

    @Test
    public void addCurrency_shouldPublishAddedEvent() throws Exception {
        facade.addCurrency(STORE_CODE, EUR);

        ArgumentCaptor<CurrencyChangeEvent> captor =
                ArgumentCaptor.forClass(CurrencyChangeEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        CurrencyChangeEvent event = captor.getValue();
        assertEquals(STORE_CODE, event.getStoreCode());
        assertEquals(EUR, event.getCurrencyCode());
        assertEquals(CurrencyChangeEvent.ChangeType.ADDED, event.getChangeType());
    }

    @Test
    public void removeCurrency_shouldPublishRemovedEvent() throws Exception {
        store.getSupportedCurrencies().add(eurCurrency);

        facade.removeCurrency(STORE_CODE, EUR);

        ArgumentCaptor<CurrencyChangeEvent> captor =
                ArgumentCaptor.forClass(CurrencyChangeEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        CurrencyChangeEvent event = captor.getValue();
        assertEquals(STORE_CODE, event.getStoreCode());
        assertEquals(EUR, event.getCurrencyCode());
        assertEquals(CurrencyChangeEvent.ChangeType.REMOVED, event.getChangeType());
    }

    @Test
    public void addCurrency_shouldNotPublishEventWhenAlreadyPresent() throws Exception {
        store.getSupportedCurrencies().add(eurCurrency);

        facade.addCurrency(STORE_CODE, EUR);

        verify(eventPublisher, never()).publishEvent(any());
    }

    // -----------------------------------------------------------------------
    // AC4 – Data integrity
    // -----------------------------------------------------------------------

    @Test
    public void addCurrency_shouldNotDuplicateCurrencyInSet() throws Exception {
        facade.addCurrency(STORE_CODE, EUR);
        // Simulate a second call (idempotent path)
        store.getSupportedCurrencies().add(eurCurrency); // already there
        facade.addCurrency(STORE_CODE, EUR);

        long count = store.getSupportedCurrencies().stream()
                .filter(c -> EUR.equals(c.getCode()))
                .count();
        assertEquals("EUR must appear exactly once", 1L, count);
    }

    @Test
    public void getSupportedCurrencies_shouldReturnSortedCodes() throws Exception {
        store.getSupportedCurrencies().add(gbpCurrency);
        store.getSupportedCurrencies().add(eurCurrency);

        List<String> codes = facade.getSupportedCurrencies(STORE_CODE);

        assertEquals(2, codes.size());
        assertEquals(EUR, codes.get(0)); // E before G
        assertEquals(GBP, codes.get(1));
    }

    @Test
    public void getSupportedCurrencies_shouldReturnEmptyListWhenNoneConfigured() throws Exception {
        List<String> codes = facade.getSupportedCurrencies(STORE_CODE);
        assertNotNull(codes);
        assertTrue(codes.isEmpty());
    }

    @Test
    public void removeCurrency_shouldNotAffectOtherCurrencies() throws Exception {
        store.getSupportedCurrencies().add(eurCurrency);
        store.getSupportedCurrencies().add(gbpCurrency);

        facade.removeCurrency(STORE_CODE, EUR);

        assertFalse(store.getSupportedCurrencies().contains(eurCurrency));
        assertTrue("GBP should still be present", store.getSupportedCurrencies().contains(gbpCurrency));
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private static Currency buildCurrency(String code) {
        Currency c = new Currency();
        c.setCode(code);
        c.setName(code + " Currency");
        c.setSupported(true);
        return c;
    }
}
