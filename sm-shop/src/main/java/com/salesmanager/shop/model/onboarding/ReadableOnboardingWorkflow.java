package com.salesmanager.shop.model.onboarding;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ReadableOnboardingWorkflow implements Serializable {

	private static final long serialVersionUID = 1L;

	private String workflowId;
	private String status;
	private String channel;
	private OnboardingCustomerProfile customerProfile;
	private List<String> missingDocumentation = new ArrayList<>();
	private List<ReadableOnboardingAuditEntry> auditTrail = new ArrayList<>();

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

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public OnboardingCustomerProfile getCustomerProfile() {
		return customerProfile;
	}

	public void setCustomerProfile(OnboardingCustomerProfile customerProfile) {
		this.customerProfile = customerProfile;
	}

	public List<String> getMissingDocumentation() {
		return missingDocumentation;
	}

	public void setMissingDocumentation(List<String> missingDocumentation) {
		this.missingDocumentation = missingDocumentation;
	}

	public List<ReadableOnboardingAuditEntry> getAuditTrail() {
		return auditTrail;
	}

	public void setAuditTrail(List<ReadableOnboardingAuditEntry> auditTrail) {
		this.auditTrail = auditTrail;
	}
}
