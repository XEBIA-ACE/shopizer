package com.salesmanager.shop.model.onboarding;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotEmpty;

public class PersistableOnboardingDocumentation implements Serializable {

	private static final long serialVersionUID = 1L;

	@NotEmpty
	private List<String> documentation = new ArrayList<>();
	private String channel;

	public List<String> getDocumentation() {
		return documentation;
	}

	public void setDocumentation(List<String> documentation) {
		this.documentation = documentation;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}
}
