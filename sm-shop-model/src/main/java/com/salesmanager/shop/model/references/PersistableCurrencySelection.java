package com.salesmanager.shop.model.references;

import java.io.Serializable;

import javax.validation.constraints.NotEmpty;

public class PersistableCurrencySelection implements Serializable {

	private static final long serialVersionUID = 1L;

	@NotEmpty
	private String code;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

}
