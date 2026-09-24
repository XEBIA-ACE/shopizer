package com.salesmanager.core.business.services.system.compliance;

import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.system.compliance.ComplianceAuditReport;

/**
 * Generates and persists compliance audit reports for verified data deletion requests.
 */
public interface ComplianceAuditReportService {

	/**
	 * Builds a data deletion report and persists it as a merchant log entry.
	 *
	 * @param store store owning the deleted data
	 * @param entityType type of deleted entity (e.g. "Customer")
	 * @param entityId id of the deleted entity
	 * @param entityReference human readable reference (e.g. nick or email)
	 * @param stillExists whether the entity could still be found after deletion
	 * @param relatedRecordsDeleted number of dependent records removed alongside the entity
	 * @return the generated report
	 * @throws ServiceException if the report cannot be persisted
	 */
	ComplianceAuditReport reportDataDeletion(MerchantStore store, String entityType, Long entityId,
			String entityReference, boolean stillExists, int relatedRecordsDeleted) throws ServiceException;

	/**
	 * Serializes a report to its persisted textual (JSON) form.
	 */
	String format(ComplianceAuditReport report);
}
