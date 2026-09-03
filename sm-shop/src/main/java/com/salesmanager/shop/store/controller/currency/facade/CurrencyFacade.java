package com.salesmanager.shop.store.controller.currency.facade;

import java.util.List;
import java.util.Locale;

import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.currency.Currency;
import com.salesmanager.shop.model.references.ReadableCurrencySelection;

public interface CurrencyFacade {

  List<Currency> getList();

  /**
   * Currency used by a shopper along with the currencies made available by the administrator. When
   * no currency was ever selected the shopper locale currency is used when it is available for that
   * store, otherwise the store default currency is used.
   *
   * @param store store the shopper is shopping in
   * @param userName authenticated shopper user name, null for a guest
   * @param locale shopper browser locale
   * @return currency selection
   */
  ReadableCurrencySelection getCurrencySelection(MerchantStore store, String userName, Locale locale,
      String selectedFromSession);

  /**
   * Validates the requested currency against the currencies made available by the administrator and
   * against the currencies supported by the payment modules configured for that store. The selection
   * is persisted on the shopper profile when the shopper is authenticated.
   *
   * @param code requested currency code
   * @param store store the shopper is shopping in
   * @param userName authenticated shopper user name, null for a guest
   * @param locale shopper browser locale
   * @return currency selection
   */
  ReadableCurrencySelection saveCurrencySelection(String code, MerchantStore store, String userName,
      Locale locale);
}
