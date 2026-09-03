```java
package com.salesmanager.shop.store.model;

public class CurrencyChangeResponse {

    private boolean success;
    private String currency;
    private double exchangeRate;
    private String locale;

    public CurrencyChangeResponse(boolean success, String currency, double exchangeRate, String locale) {
        this.success = success;
        this.currency = currency;
        this.exchangeRate = exchangeRate;
        this.locale = locale;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public double getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(double exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }
}
```