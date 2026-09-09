package com.salesmanager.shop.store.facade.workflow;

public class ComplianceIssue {

	private final ComplianceRule rule;
	private final String stepId;
	private final String message;

	public ComplianceIssue(ComplianceRule rule, String stepId, String message) {
		this.rule = rule;
		this.stepId = stepId;
		this.message = message;
	}

	public ComplianceRule getRule() {
		return rule;
	}

	public String getStepId() {
		return stepId;
	}

	public String getMessage() {
		return message;
	}
}
