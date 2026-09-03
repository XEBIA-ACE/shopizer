package com.salesmanager.shop.model.onboarding;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

public class OnboardingCustomerProfile implements Serializable {

	private static final long serialVersionUID = 1L;

	@NotEmpty
	private String name;
	@NotEmpty
	@Email
	private String email;
	@NotEmpty
	private String phone;
	@NotEmpty
	private String accountType;
	private List<String> documentation = new ArrayList<>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getAccountType() {
		return accountType;
	}

	public void setAccountType(String accountType) {
		this.accountType = accountType;
	}

	public List<String> getDocumentation() {
		return documentation;
	}

	public void setDocumentation(List<String> documentation) {
		this.documentation = documentation;
	}
}
