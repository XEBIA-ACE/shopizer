package com.salesmanager.shop.store.facade.workflow;

public class WorkflowStep {

	private final String id;
	private String description;
	private boolean compliant = true;

	public WorkflowStep(String id, String description) {
		this.id = id;
		this.description = description;
	}

	public String getId() {
		return id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isCompliant() {
		return compliant;
	}

	public void setCompliant(boolean compliant) {
		this.compliant = compliant;
	}
}
