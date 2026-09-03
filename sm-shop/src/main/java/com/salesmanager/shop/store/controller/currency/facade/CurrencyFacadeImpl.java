package com.salesmanager.shop.store.controller.currency.facade;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.salesmanager.core.business.exception.ServiceException;
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
import com.salesmanager.shop.store.api.exception.ServiceRuntimeException;
import com.salesmanager.shop.store.api.exception.UnsupportedCurrencyException;

@Service
public class CurrencyFacadeImpl implements CurrencyFacade {

  /**
   * Optional payment module detail listing the currency codes accepted by the payment provider,
   * comma separated. A module without that detail accepts any currency.
   */
  private static final String SUPPORTED_CURRENCIES_DETAIL = "supportedCurrencies";

  @Inject
  private CurrencyService currencyService;

  @Inject
  private CustomerService customerService;

  @Inject
  private PaymentService paymentService;

  @Override
  public List<Currency> getList() {
    List<Currency> currencyList = currencyService.list();
    if (currencyList.isEmpty()){
      throw new ResourceNotFoundException("No languages found");
    }
    Collections.sort(currencyList, new Comparator<Currency>(){

    	  public int compare(Currency o1, Currency o2)
    	  {
    	     return o1.getCode().compareTo(o2.getCode());
    	  }
    	});
    return currencyList;
  }

  @Override
  public ReadableCurrencySelection getCurrencySelection(MerchantStore store, String userName,
      Locale locale, String selectedFromSession) {

    List<Currency> available = availableCurrencies();
    Set<String> paymentCurrencies = paymentSupportedCurrencies(store);

    Optional<Currency> customerCurrency = preferredCurrency(userName, store);
    if (customerCurrency.isPresent()) {
      return selection(customerCurrency.get(), CurrencySelectionSource.CUSTOMER, available,
          paymentCurrencies, locale);
    }

    Optional<Currency> sessionCurrency = find(available, selectedFromSession);
    if (sessionCurrency.isPresent()) {
      return selection(sessionCurrency.get(), CurrencySelectionSource.SESSION, available,
          paymentCurrencies, locale);
    }

    Optional<Currency> localeCurrency = find(available, localeCurrencyCode(locale));
    if (localeCurrency.isPresent()) {
      return selection(localeCurrency.get(), CurrencySelectionSource.LOCALE, available,
          paymentCurrencies, locale);
    }

    return selection(store.getCurrency(), CurrencySelectionSource.STORE, available,
        paymentCurrencies, locale);
  }

  @Override
  public ReadableCurrencySelection saveCurrencySelection(String code, MerchantStore store,
      String userName, Locale locale) {

    List<Currency> available = availableCurrencies();
    Currency currency = find(available, code)
        .orElseThrow(() -> new ResourceNotFoundException("Currency [" + code + "] is not available"));

    Set<String> paymentCurrencies = paymentSupportedCurrencies(store);
    if (!isPaymentSupported(currency, paymentCurrencies)) {
      List<String> alternatives = available.stream()
          .filter(c -> isPaymentSupported(c, paymentCurrencies)).map(Currency::getCode)
          .collect(Collectors.toList());
      throw new UnsupportedCurrencyException(currency.getCode(), alternatives);
    }

    persistPreferredCurrency(userName, store, currency);

    return selection(currency, userName != null ? CurrencySelectionSource.CUSTOMER
        : CurrencySelectionSource.SESSION, available, paymentCurrencies, locale);
  }

  private List<Currency> availableCurrencies() {
    return getList().stream().filter(c -> !Boolean.FALSE.equals(c.getSupported()))
        .collect(Collectors.toList());
  }

  private Optional<Currency> find(List<Currency> currencies, String code) {
    if (StringUtils.isBlank(code)) {
      return Optional.empty();
    }
    return currencies.stream().filter(c -> code.equals(c.getCode())).findFirst();
  }

  private String localeCurrencyCode(Locale locale) {
    if (locale == null || StringUtils.isBlank(locale.getCountry())) {
      return null;
    }
    try {
      return java.util.Currency.getInstance(locale).getCurrencyCode();
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  private Optional<Currency> preferredCurrency(String userName, MerchantStore store) {
    if (userName == null) {
      return Optional.empty();
    }
    Customer customer = customerService.getByNick(userName, store.getId());
    if (customer == null) {
      throw new ResourceNotFoundException("Customer [" + userName + "] not found");
    }
    return Optional.ofNullable(customer.getPreferredCurrency());
  }

  private void persistPreferredCurrency(String userName, MerchantStore store, Currency currency) {
    if (userName == null) {
      return;
    }
    Customer customer = customerService.getByNick(userName, store.getId());
    if (customer == null) {
      throw new ResourceNotFoundException("Customer [" + userName + "] not found");
    }
    customer.setPreferredCurrency(currency);
    try {
      customerService.saveOrUpdate(customer);
    } catch (ServiceException e) {
      throw new ServiceRuntimeException(
          "Cannot save currency [" + currency.getCode() + "] for customer [" + userName + "]", e);
    }
  }

  /**
   * Currency codes accepted by every payment module configured for that store. An empty set means
   * no restriction was declared by the payment modules.
   */
  private Set<String> paymentSupportedCurrencies(MerchantStore store) {
    Set<String> supported = null;
    try {
      Map<String, IntegrationConfiguration> configurations =
          paymentService.getPaymentModulesConfigured(store);
      if (configurations == null) {
        return Collections.emptySet();
      }
      for (IntegrationConfiguration configuration : configurations.values()) {
        if (configuration == null || !configuration.isActive()) {
          continue;
        }
        IntegrationModule module =
            paymentService.getPaymentMethodByCode(store, configuration.getModuleCode());
        Set<String> moduleCurrencies = declaredCurrencies(module);
        if (moduleCurrencies.isEmpty()) {
          continue;
        }
        if (supported == null) {
          supported = moduleCurrencies;
        } else {
          supported.retainAll(moduleCurrencies);
        }
      }
    } catch (ServiceException e) {
      throw new ServiceRuntimeException("Cannot retrieve payment modules for store ["
          + store.getCode() + "]", e);
    }
    return supported == null ? Collections.emptySet() : supported;
  }

  private Set<String> declaredCurrencies(IntegrationModule module) {
    if (module == null || module.getDetails() == null) {
      return Collections.emptySet();
    }
    String details = module.getDetails().get(SUPPORTED_CURRENCIES_DETAIL);
    if (StringUtils.isBlank(details)) {
      return Collections.emptySet();
    }
    return java.util.Arrays.stream(details.split(",")).map(String::trim)
        .filter(StringUtils::isNotBlank).map(String::toUpperCase)
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  private boolean isPaymentSupported(Currency currency, Set<String> paymentCurrencies) {
    return paymentCurrencies.isEmpty() || paymentCurrencies.contains(currency.getCode());
  }

  private ReadableCurrencySelection selection(Currency selected, CurrencySelectionSource source,
      List<Currency> available, Set<String> paymentCurrencies, Locale locale) {
    ReadableCurrencySelection selection = new ReadableCurrencySelection();
    selection.setSource(source);
    selection.setSelected(selected != null ? selected.getCode() : null);
    List<ReadableCurrency> currencies = new ArrayList<ReadableCurrency>();
    for (Currency currency : available) {
      currencies.add(readableCurrency(currency, paymentCurrencies, locale));
    }
    selection.setCurrencies(currencies);
    return selection;
  }

  private ReadableCurrency readableCurrency(Currency currency, Set<String> paymentCurrencies,
      Locale locale) {
    ReadableCurrency readable = new ReadableCurrency();
    readable.setCode(currency.getCode());
    readable.setName(currency.getName());
    readable.setSymbol(symbol(currency, locale));
    readable.setSupportedByPaymentProvider(isPaymentSupported(currency, paymentCurrencies));
    return readable;
  }

  private String symbol(Currency currency, Locale locale) {
    java.util.Currency javaCurrency = currency.getCurrency();
    if (javaCurrency == null) {
      return null;
    }
    return locale != null ? javaCurrency.getSymbol(locale) : javaCurrency.getSymbol();
  }
}
