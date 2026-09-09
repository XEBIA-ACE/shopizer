package com.salesmanager.shop.store.facade.workflow;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class WorkflowDefinition {

	private final String id;
	private final String storeCode;
	private String name;
	private int version;
	private List<WorkflowStep> steps = new ArrayList<>();
	private List<WorkflowVersionEntry> history = new ArrayList<>();
	private List<ComplianceIssue> complianceIssues = new ArrayList<>();
	private boolean compliant = true;
	private final Instant createdAt = Instant.now();
	private Instant updatedAt = Instant.now();

	public WorkflowDefinition(String id, String name, String storeCode) {
		this.id = id;
		this.name = name;
		this.storeCode = storeCode;
	}

	public String getId() {
		return id;
	}

	public String getStoreCode() {
		return storeCode;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public List<WorkflowStep> getSteps() {
		return steps;
	}

	public void setSteps(List<WorkflowStep> steps) {
		this.steps = steps;
	}

	public List<WorkflowVersionEntry> getHistory() {
		return history;
	}

	public List<ComplianceIssue> getComplianceIssues() {
		return complianceIssues;
	}

	public void setComplianceIssues(List<ComplianceIssue> complianceIssues) {
		this.complianceIssues = complianceIssues;
	}

	public boolean isCompliant() {
		return compliant;
	}

	public void setCompliant(boolean compliant) {
		this.compliant = compliant;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}
}
