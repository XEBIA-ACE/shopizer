package com.salesmanager.shop.store.controller.currency.facade;

import com.salesmanager.core.model.reference.currency.Currency;
import com.salesmanager.shop.model.references.ReadableCurrency;
import java.util.List;

public interface CurrencyFacade {

  /**
   * Currencies currently offered on the storefront (supported = true)
   */
  List<Currency> getList();

  /**
   * Every currency known to the system, regardless of the supported flag (admin view)
   */
  List<ReadableCurrency> getAll();

  ReadableCurrency setSupported(String code, boolean supported);
}
