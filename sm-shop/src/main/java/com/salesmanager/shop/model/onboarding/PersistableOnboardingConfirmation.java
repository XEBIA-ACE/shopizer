package com.salesmanager.shop.model.onboarding;

import java.io.Serializable;

import javax.validation.constraints.NotEmpty;

public class PersistableOnboardingConfirmation implements Serializable {

	private static final long serialVersionUID = 1L;

	@NotEmpty
	private String workflowId;

	public String getWorkflowId() {
		return workflowId;
	}

	public void setWorkflowId(String workflowId) {
		this.workflowId = workflowId;
	}
}
