package com.salesmanager.shop.model.references;

import java.io.Serializable;

public class ReadableCurrency implements Serializable {

	private static final long serialVersionUID = 1L;

	private String code;
	private String name;
	private String symbol;
	private boolean supportedByPaymentProvider = true;

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

	public boolean isSupportedByPaymentProvider() {
		return supportedByPaymentProvider;
	}

	public void setSupportedByPaymentProvider(boolean supportedByPaymentProvider) {
		this.supportedByPaymentProvider = supportedByPaymentProvider;
	}

}
