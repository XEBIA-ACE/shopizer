package com.salesmanager.shop.store.facade.onboarding;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.salesmanager.shop.model.onboarding.OnboardingCustomerProfile;

public class OnboardingWorkflow {

	private final String workflowId;
	private final String storeCode;
	private final OnboardingCustomerProfile customerProfile;
	private final Set<String> receivedDocumentation = new LinkedHashSet<>();
	private final List<OnboardingAuditEntry> auditTrail = new ArrayList<>();
	private OnboardingChannel channel;
	private OnboardingWorkflowStatus status;
	private Instant completedAt;
	private Instant confirmedAt;

	public OnboardingWorkflow(String workflowId, String storeCode, OnboardingCustomerProfile customerProfile,
			OnboardingChannel channel) {
		this.workflowId = workflowId;
		this.storeCode = storeCode;
		this.customerProfile = customerProfile;
		this.channel = channel;
		this.status = OnboardingWorkflowStatus.INITIATED;
	}

	public String getWorkflowId() {
		return workflowId;
	}

	public String getStoreCode() {
		return storeCode;
	}

	public OnboardingCustomerProfile getCustomerProfile() {
		return customerProfile;
	}

	public Set<String> getReceivedDocumentation() {
		return receivedDocumentation;
	}

	public List<OnboardingAuditEntry> getAuditTrail() {
		return auditTrail;
	}

	public OnboardingChannel getChannel() {
		return channel;
	}

	public void setChannel(OnboardingChannel channel) {
		this.channel = channel;
	}

	public OnboardingWorkflowStatus getStatus() {
		return status;
	}

	public void setStatus(OnboardingWorkflowStatus status) {
		this.status = status;
	}

	public Instant getCompletedAt() {
		return completedAt;
	}

	public void setCompletedAt(Instant completedAt) {
		this.completedAt = completedAt;
	}

	public Instant getConfirmedAt() {
		return confirmedAt;
	}

	public void setConfirmedAt(Instant confirmedAt) {
		this.confirmedAt = confirmedAt;
	}
}
