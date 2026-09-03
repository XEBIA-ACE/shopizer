package com.salesmanager.shop.model.references;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ReadableCurrencySelection implements Serializable {

	private static final long serialVersionUID = 1L;

	private String selected;
	private CurrencySelectionSource source;
	private List<ReadableCurrency> currencies = new ArrayList<ReadableCurrency>();

	public String getSelected() {
		return selected;
	}

	public void setSelected(String selected) {
		this.selected = selected;
	}

	public CurrencySelectionSource getSource() {
		return source;
	}

	public void setSource(CurrencySelectionSource source) {
		this.source = source;
	}

	public List<ReadableCurrency> getCurrencies() {
		return currencies;
	}

	public void setCurrencies(List<ReadableCurrency> currencies) {
		this.currencies = currencies;
	}

}
