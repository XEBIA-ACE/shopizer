package com.salesmanager.shop.model.onboarding;

import java.io.Serializable;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class PersistableOnboardingWorkflow implements Serializable {

	private static final long serialVersionUID = 1L;

	@NotNull
	@Valid
	private OnboardingCustomerProfile customerProfile;
	private String channel;

	public OnboardingCustomerProfile getCustomerProfile() {
		return customerProfile;
	}

	public void setCustomerProfile(OnboardingCustomerProfile customerProfile) {
		this.customerProfile = customerProfile;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}
}
