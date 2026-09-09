package com.salesmanager.shop.model.workflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ReadableWorkflow implements Serializable {

	private static final long serialVersionUID = 1L;

	private String id;
	private String name;
	private int version;
	private List<ReadableWorkflowStep> steps = new ArrayList<>();
	private boolean compliant;
	private List<ReadableComplianceIssue> complianceIssues = new ArrayList<>();
	private List<String> propagatedChannels = new ArrayList<>();
	private String lastModified;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	public List<ReadableWorkflowStep> getSteps() {
		return steps;
	}

	public void setSteps(List<ReadableWorkflowStep> steps) {
		this.steps = steps;
	}

	public boolean isCompliant() {
		return compliant;
	}

	public void setCompliant(boolean compliant) {
		this.compliant = compliant;
	}

	public List<ReadableComplianceIssue> getComplianceIssues() {
		return complianceIssues;
	}

	public void setComplianceIssues(List<ReadableComplianceIssue> complianceIssues) {
		this.complianceIssues = complianceIssues;
	}

	public List<String> getPropagatedChannels() {
		return propagatedChannels;
	}

	public void setPropagatedChannels(List<String> propagatedChannels) {
		this.propagatedChannels = propagatedChannels;
	}

	public String getLastModified() {
		return lastModified;
	}

	public void setLastModified(String lastModified) {
		this.lastModified = lastModified;
	}
}
