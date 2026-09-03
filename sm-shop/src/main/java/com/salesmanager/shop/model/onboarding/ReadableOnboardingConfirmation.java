package com.salesmanager.shop.model.onboarding;

import java.io.Serializable;

public class ReadableOnboardingConfirmation implements Serializable {

	private static final long serialVersionUID = 1L;

	private String workflowId;
	private String status;

	public String getWorkflowId() {
		return workflowId;
	}

	public void setWorkflowId(String workflowId) {
		this.workflowId = workflowId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}
