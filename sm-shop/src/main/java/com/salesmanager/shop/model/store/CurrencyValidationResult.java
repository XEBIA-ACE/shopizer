package com.salesmanager.shop.model.store;

public class CurrencyValidationResult {

    private boolean fallbackApplied;
    private String resolvedCurrencyCode;

    public CurrencyValidationResult() {
    }

    public CurrencyValidationResult(boolean fallbackApplied, String resolvedCurrencyCode) {
        this.fallbackApplied = fallbackApplied;
        this.resolvedCurrencyCode = resolvedCurrencyCode;
    }

    public boolean isFallbackApplied() {
        return fallbackApplied;
    }

    public void setFallbackApplied(boolean fallbackApplied) {
        this.fallbackApplied = fallbackApplied;
    }

    public String getResolvedCurrencyCode() {
        return resolvedCurrencyCode;
    }

    public void setResolvedCurrencyCode(String resolvedCurrencyCode) {
        this.resolvedCurrencyCode = resolvedCurrencyCode;
    }

    @Override
    public String toString() {
        return "CurrencyValidationResult{" +
                "fallbackApplied=" + fallbackApplied +
                ", resolvedCurrencyCode='" + resolvedCurrencyCode + '\'' +
                '}';
    }
}