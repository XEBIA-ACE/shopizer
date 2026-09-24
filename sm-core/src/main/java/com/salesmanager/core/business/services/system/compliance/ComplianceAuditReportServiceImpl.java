package com.salesmanager.core.business.services.system.compliance;

import java.util.Date;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.services.system.MerchantLogService;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.system.MerchantLog;
import com.salesmanager.core.model.system.compliance.ComplianceAuditReport;

@Service("complianceAuditReportService")
public class ComplianceAuditReportServiceImpl implements ComplianceAuditReportService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ComplianceAuditReportServiceImpl.class);

	private final MerchantLogService merchantLogService;
	private final ObjectMapper mapper = new ObjectMapper()
			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

	@Inject
	public ComplianceAuditReportServiceImpl(MerchantLogService merchantLogService) {
		this.merchantLogService = merchantLogService;
	}

	@Override
	public ComplianceAuditReport reportDataDeletion(MerchantStore store, String entityType, Long entityId,
			String entityReference, boolean stillExists, int relatedRecordsDeleted) throws ServiceException {

		String status = stillExists ? ComplianceAuditReport.STATUS_FAILED : ComplianceAuditReport.STATUS_VERIFIED;
		ComplianceAuditReport report = new ComplianceAuditReport(ComplianceAuditReport.REPORT_TYPE_DATA_DELETION,
				entityType, entityId, entityReference, store.getCode(), new Date(), status, relatedRecordsDeleted);

		String content = format(report);
		merchantLogService.save(new MerchantLog(store, ComplianceAuditReport.MODULE, content));
		LOGGER.info("Compliance audit report generated: {}", content);
		return report;
	}

	@Override
	public String format(ComplianceAuditReport report) {
		try {
			return mapper.writeValueAsString(report);
		} catch (JsonProcessingException e) {
			throw new IllegalStateException("Unable to serialize compliance audit report", e);
		}
	}
}
