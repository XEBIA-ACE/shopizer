package com.salesmanager.shop.model.references;

import java.io.Serializable;

public class ReadableCurrency implements Serializable {

  private static final long serialVersionUID = 1L;

  private Long id;
  private String code;
  private String name;
  private String symbol;
  private boolean supported;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSymbol() {
    return symbol;
  }

  public void setSymbol(String symbol) {
    this.symbol = symbol;
  }

  public boolean isSupported() {
    return supported;
  }

  public void setSupported(boolean supported) {
    this.supported = supported;
  }
}
