package com.salesmanager.shop.store.api.exception;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Requested currency is not accepted by the payment modules configured for that store, the shopper
 * has to pick one of the supported alternatives.
 */
public class UnsupportedCurrencyException extends RestApiException {

  private static final long serialVersionUID = 1L;

  private static final String ERROR_CODE = "400";

  private final List<String> supportedCurrencies;

  public UnsupportedCurrencyException(String code, List<String> supportedCurrencies) {
    super(ERROR_CODE, "Currency [" + code + "] is not supported by the payment provider, "
        + "supported currencies are " + supportedCurrencies);
    this.supportedCurrencies = supportedCurrencies == null ? new ArrayList<String>()
        : new ArrayList<String>(supportedCurrencies);
  }

  public List<String> getSupportedCurrencies() {
    return Collections.unmodifiableList(supportedCurrencies);
  }

}
