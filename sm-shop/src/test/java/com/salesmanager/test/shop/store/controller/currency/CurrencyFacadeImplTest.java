package com.salesmanager.test.shop.store.controller.currency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.salesmanager.core.business.services.customer.CustomerService;
import com.salesmanager.core.business.services.payments.PaymentService;
import com.salesmanager.core.business.services.reference.currency.CurrencyService;
import com.salesmanager.core.model.customer.Customer;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.currency.Currency;
import com.salesmanager.core.model.system.IntegrationConfiguration;
import com.salesmanager.core.model.system.IntegrationModule;
import com.salesmanager.shop.model.references.CurrencySelectionSource;
import com.salesmanager.shop.model.references.ReadableCurrency;
import com.salesmanager.shop.model.references.ReadableCurrencySelection;
import com.salesmanager.shop.store.api.exception.ResourceNotFoundException;
import com.salesmanager.shop.store.api.exception.UnsupportedCurrencyException;
import com.salesmanager.shop.store.controller.currency.facade.CurrencyFacadeImpl;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CurrencyFacadeImplTest {

  private static final String CUSTOMER_NICK = "shopper@shopizer.com";

  @Mock
  private CurrencyService currencyService;

  @Mock
  private CustomerService customerService;

  @Mock
  private PaymentService paymentService;

  @InjectMocks
  private CurrencyFacadeImpl currencyFacade;

  private Currency usd;
  private Currency cad;
  private Currency eur;
  private MerchantStore store;

  @BeforeEach
  void setUp() throws Exception {
    usd = currency("USD", "US Dollar");
    cad = currency("CAD", "Canadian Dollar");
    eur = currency("EUR", "Euro");

    store = new MerchantStore();
    store.setId(1);
    store.setCode("DEFAULT");
    store.setCurrency(usd);

    when(currencyService.list()).thenReturn(Arrays.asList(usd, cad, eur));
    when(paymentService.getPaymentModulesConfigured(store)).thenReturn(Collections.emptyMap());
  }

  @Test
  void selectionFallsBackOnStoreCurrencyWhenLocaleCurrencyIsNotAvailable() {
    ReadableCurrencySelection selection =
        currencyFacade.getCurrencySelection(store, null, Locale.JAPAN, null);

    assertEquals("USD", selection.getSelected());
    assertEquals(CurrencySelectionSource.STORE, selection.getSource());
    assertEquals(Arrays.asList("CAD", "EUR", "USD"),
        selection.getCurrencies().stream().map(ReadableCurrency::getCode).collect(java.util.stream.Collectors.toList()));
  }

  @Test
  void selectionUsesLocaleCurrencyWhenAvailable() {
    ReadableCurrencySelection selection =
        currencyFacade.getCurrencySelection(store, null, Locale.CANADA, null);

    assertEquals("CAD", selection.getSelected());
    assertEquals(CurrencySelectionSource.LOCALE, selection.getSource());
  }

  @Test
  void sessionSelectionTakesPrecedenceOverLocale() {
    ReadableCurrencySelection selection =
        currencyFacade.getCurrencySelection(store, null, Locale.CANADA, "EUR");

    assertEquals("EUR", selection.getSelected());
    assertEquals(CurrencySelectionSource.SESSION, selection.getSource());
  }

  @Test
  void customerPreferenceTakesPrecedenceOverSessionAndLocale() {
    Customer customer = new Customer();
    customer.setPreferredCurrency(eur);
    when(customerService.getByNick(CUSTOMER_NICK, 1)).thenReturn(customer);

    ReadableCurrencySelection selection =
        currencyFacade.getCurrencySelection(store, CUSTOMER_NICK, Locale.CANADA, "CAD");

    assertEquals("EUR", selection.getSelected());
    assertEquals(CurrencySelectionSource.CUSTOMER, selection.getSource());
  }

  @Test
  void authenticatedCustomerWithoutPreferenceFallsBackOnLocale() {
    Customer customer = new Customer();
    when(customerService.getByNick(CUSTOMER_NICK, 1)).thenReturn(customer);

    ReadableCurrencySelection selection =
        currencyFacade.getCurrencySelection(store, CUSTOMER_NICK, Locale.CANADA, null);

    assertEquals("CAD", selection.getSelected());
    assertEquals(CurrencySelectionSource.LOCALE, selection.getSource());
  }

  @Test
  void savedSelectionIsPersistedOnCustomerProfile() throws Exception {
    Customer customer = new Customer();
    when(customerService.getByNick(CUSTOMER_NICK, 1)).thenReturn(customer);

    ReadableCurrencySelection selection =
        currencyFacade.saveCurrencySelection("EUR", store, CUSTOMER_NICK, Locale.CANADA);

    assertEquals("EUR", selection.getSelected());
    assertEquals(CurrencySelectionSource.CUSTOMER, selection.getSource());

    ArgumentCaptor<Customer> saved = ArgumentCaptor.forClass(Customer.class);
    verify(customerService).saveOrUpdate(saved.capture());
    assertEquals("EUR", saved.getValue().getPreferredCurrency().getCode());
  }

  @Test
  void guestSelectionIsNotPersisted() throws Exception {
    ReadableCurrencySelection selection =
        currencyFacade.saveCurrencySelection("CAD", store, null, Locale.CANADA);

    assertEquals("CAD", selection.getSelected());
    assertEquals(CurrencySelectionSource.SESSION, selection.getSource());
    verify(customerService, org.mockito.Mockito.never()).saveOrUpdate(any(Customer.class));
  }

  @Test
  void unknownCurrencyIsRejected() {
    assertThrows(ResourceNotFoundException.class,
        () -> currencyFacade.saveCurrencySelection("XOF", store, null, Locale.CANADA));
  }

  @Test
  void currencyNotSupportedByPaymentProviderIsRejectedWithAlternatives() throws Exception {
    configurePaymentModule("stripe", "USD, CAD");

    UnsupportedCurrencyException exception = assertThrows(UnsupportedCurrencyException.class,
        () -> currencyFacade.saveCurrencySelection("EUR", store, null, Locale.CANADA));

    assertEquals(Arrays.asList("CAD", "USD"), exception.getSupportedCurrencies());
  }

  @Test
  void currenciesNotSupportedByPaymentProviderAreFlagged() throws Exception {
    configurePaymentModule("stripe", "USD,CAD");

    ReadableCurrencySelection selection =
        currencyFacade.getCurrencySelection(store, null, Locale.CANADA, null);

    Map<String, Boolean> supported = new HashMap<String, Boolean>();
    for (ReadableCurrency currency : selection.getCurrencies()) {
      supported.put(currency.getCode(), currency.isSupportedByPaymentProvider());
    }
    assertTrue(supported.get("CAD"));
    assertTrue(supported.get("USD"));
    assertFalse(supported.get("EUR"));
  }

  @Test
  void symbolIsFormattedForShopperLocale() {
    ReadableCurrencySelection selection =
        currencyFacade.getCurrencySelection(store, null, Locale.US, null);

    ReadableCurrency canadianDollar = selection.getCurrencies().stream()
        .filter(c -> "CAD".equals(c.getCode())).findFirst().get();
    assertEquals(java.util.Currency.getInstance("CAD").getSymbol(Locale.US),
        canadianDollar.getSymbol());
  }

  @Test
  void noCurrencyConfiguredIsReported() {
    when(currencyService.list()).thenReturn(Collections.emptyList());

    assertThrows(ResourceNotFoundException.class,
        () -> currencyFacade.getCurrencySelection(store, null, Locale.US, null));
  }

  private void configurePaymentModule(String code, String supportedCurrencies) throws Exception {
    IntegrationConfiguration configuration = new IntegrationConfiguration();
    configuration.setModuleCode(code);
    configuration.setActive(true);
    Map<String, IntegrationConfiguration> configurations =
        new HashMap<String, IntegrationConfiguration>();
    configurations.put(code, configuration);

    IntegrationModule module = new IntegrationModule();
    module.setCode(code);
    Map<String, String> details = new HashMap<String, String>();
    details.put("supportedCurrencies", supportedCurrencies);
    module.setDetails(details);

    when(paymentService.getPaymentModulesConfigured(store)).thenReturn(configurations);
    when(paymentService.getPaymentMethodByCode(any(MerchantStore.class), anyString()))
        .thenReturn(module);
  }

  private Currency currency(String code, String name) {
    Currency currency = new Currency();
    currency.setCurrency(java.util.Currency.getInstance(code));
    currency.setName(name);
    return currency;
  }
}
