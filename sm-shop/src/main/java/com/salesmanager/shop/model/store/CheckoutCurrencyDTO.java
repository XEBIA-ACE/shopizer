package com.salesmanager.shop.model.store;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CheckoutCurrencyDTO {

    @JsonProperty("currencyCode")
    private String currencyCode;

    @JsonProperty("symbol")
    private String symbol;

    @JsonProperty("isDefault")
    private boolean isDefault;

    public CheckoutCurrencyDTO() {
    }

    public CheckoutCurrencyDTO(String currencyCode, String symbol, boolean isDefault) {
        this.currencyCode = currencyCode;
        this.symbol = symbol;
        this.isDefault = isDefault;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    @Override
    public String toString() {
        return "CheckoutCurrencyDTO{" +
                "currencyCode='" + currencyCode + '\'' +
                ", symbol='" + symbol + '\'' +
                ", isDefault=" + isDefault +
                '}';
    }
}