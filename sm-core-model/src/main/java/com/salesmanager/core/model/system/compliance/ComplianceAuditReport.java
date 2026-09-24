package com.salesmanager.core.model.system.compliance;

import java.io.Serializable;
import java.util.Date;

/**
 * Immutable record describing a verified data deletion, produced for compliance
 * (e.g. GDPR art. 17 / right to erasure) audit purposes.
 */
public class ComplianceAuditReport implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String MODULE = "COMPLIANCE_AUDIT";
	public static final String REPORT_TYPE_DATA_DELETION = "DATA_DELETION";
	public static final String STATUS_VERIFIED = "VERIFIED";
	public static final String STATUS_FAILED = "FAILED";

	private final String reportType;
	private final String entityType;
	private final Long entityId;
	private final String entityReference;
	private final String storeCode;
	private final Date timestamp;
	private final String status;
	private final int relatedRecordsDeleted;

	public ComplianceAuditReport(String reportType, String entityType, Long entityId, String entityReference,
			String storeCode, Date timestamp, String status, int relatedRecordsDeleted) {
		this.reportType = reportType;
		this.entityType = entityType;
		this.entityId = entityId;
		this.entityReference = entityReference;
		this.storeCode = storeCode;
		this.timestamp = timestamp;
		this.status = status;
		this.relatedRecordsDeleted = relatedRecordsDeleted;
	}

	public String getReportType() {
		return reportType;
	}

	public String getEntityType() {
		return entityType;
	}

	public Long getEntityId() {
		return entityId;
	}

	public String getEntityReference() {
		return entityReference;
	}

	public String getStoreCode() {
		return storeCode;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public String getStatus() {
		return status;
	}

	public int getRelatedRecordsDeleted() {
		return relatedRecordsDeleted;
	}

	public boolean isVerified() {
		return STATUS_VERIFIED.equals(status);
	}
}
