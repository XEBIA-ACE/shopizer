package com.salesmanager.shop.model.onboarding;

import java.io.Serializable;

import javax.validation.constraints.NotEmpty;

public class PersistableOnboardingChannel implements Serializable {

	private static final long serialVersionUID = 1L;

	@NotEmpty
	private String channel;

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}
}
