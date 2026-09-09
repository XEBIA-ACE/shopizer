package com.salesmanager.shop.store.facade.workflow;

import java.time.Instant;

public class WorkflowVersionEntry {

	private final int version;
	private final Instant timestamp;
	private final String changeSummary;
	private final boolean compliant;
	private final int issueCount;

	public WorkflowVersionEntry(int version, Instant timestamp, String changeSummary, boolean compliant,
			int issueCount) {
		this.version = version;
		this.timestamp = timestamp;
		this.changeSummary = changeSummary;
		this.compliant = compliant;
		this.issueCount = issueCount;
	}

	public int getVersion() {
		return version;
	}

	public Instant getTimestamp() {
		return timestamp;
	}

	public String getChangeSummary() {
		return changeSummary;
	}

	public boolean isCompliant() {
		return compliant;
	}

	public int getIssueCount() {
		return issueCount;
	}
}
