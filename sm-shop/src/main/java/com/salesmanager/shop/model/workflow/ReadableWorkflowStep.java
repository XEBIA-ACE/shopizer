package com.salesmanager.shop.model.workflow;

import java.io.Serializable;

public class ReadableWorkflowStep implements Serializable {

	private static final long serialVersionUID = 1L;

	private String id;
	private String description;
	private boolean complianceStatus;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isComplianceStatus() {
		return complianceStatus;
	}

	public void setComplianceStatus(boolean complianceStatus) {
		this.complianceStatus = complianceStatus;
	}
}
