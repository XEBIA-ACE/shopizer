package com.salesmanager.shop.model.workflow;

import java.io.Serializable;

public class ReadableWorkflowVersion implements Serializable {

	private static final long serialVersionUID = 1L;

	private int version;
	private String timestamp;
	private String changeSummary;
	private boolean compliant;
	private int issueCount;

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getChangeSummary() {
		return changeSummary;
	}

	public void setChangeSummary(String changeSummary) {
		this.changeSummary = changeSummary;
	}

	public boolean isCompliant() {
		return compliant;
	}

	public void setCompliant(boolean compliant) {
		this.compliant = compliant;
	}

	public int getIssueCount() {
		return issueCount;
	}

	public void setIssueCount(int issueCount) {
		this.issueCount = issueCount;
	}
}
