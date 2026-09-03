package com.salesmanager.shop.model.references;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

/**
 * Admin request payload toggling whether a currency is offered on the storefront.
 */
public class PersistableCurrencySupport implements Serializable {

  private static final long serialVersionUID = 1L;

  @NotNull
  private Boolean supported;

  public Boolean getSupported() {
    return supported;
  }

  public void setSupported(Boolean supported) {
    this.supported = supported;
  }
}
