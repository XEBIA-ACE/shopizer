package com.salesmanager.test.system.compliance;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.services.system.MerchantLogService;
import com.salesmanager.core.business.services.system.compliance.ComplianceAuditReportServiceImpl;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.system.MerchantLog;
import com.salesmanager.core.model.system.compliance.ComplianceAuditReport;

public class ComplianceAuditReportServiceImplTest {

	private MerchantLogService merchantLogService;
	private ComplianceAuditReportServiceImpl service;
	private MerchantStore store;

	@Before
	public void setUp() {
		merchantLogService = mock(MerchantLogService.class);
		service = new ComplianceAuditReportServiceImpl(merchantLogService);
		store = new MerchantStore();
		store.setId(1);
		store.setCode(MerchantStore.DEFAULT_STORE);
	}

	@Test
	public void verifiedDeletionProducesVerifiedReportAndPersistsLog() throws ServiceException {
		ComplianceAuditReport report = service.reportDataDeletion(store, "Customer", 42L, "john", false, 3);

		assertTrue(report.isVerified());
		assertEquals(ComplianceAuditReport.REPORT_TYPE_DATA_DELETION, report.getReportType());
		assertEquals("Customer", report.getEntityType());
		assertEquals(Long.valueOf(42L), report.getEntityId());
		assertEquals("john", report.getEntityReference());
		assertEquals(MerchantStore.DEFAULT_STORE, report.getStoreCode());
		assertEquals(3, report.getRelatedRecordsDeleted());
		assertNotNull(report.getTimestamp());

		ArgumentCaptor<MerchantLog> captor = ArgumentCaptor.forClass(MerchantLog.class);
		verify(merchantLogService).save(captor.capture());
		MerchantLog log = captor.getValue();
		assertEquals(ComplianceAuditReport.MODULE, log.getModule());
		assertEquals(store, log.getStore());
		assertTrue(log.getLog().contains("\"status\":\"VERIFIED\""));
		assertTrue(log.getLog().contains("\"entityId\":42"));
		assertTrue(log.getLog().contains("\"reportType\":\"DATA_DELETION\""));
	}

	@Test
	public void unverifiedDeletionProducesFailedReport() throws ServiceException {
		ComplianceAuditReport report = service.reportDataDeletion(store, "Customer", 7L, "jane", true, 0);

		assertFalse(report.isVerified());
		assertEquals(ComplianceAuditReport.STATUS_FAILED, report.getStatus());
		verify(merchantLogService).save(any(MerchantLog.class));
	}

	@Test
	public void formatProducesIsoTimestamp() throws ServiceException {
		ComplianceAuditReport report = service.reportDataDeletion(store, "Customer", 1L, "x", false, 0);
		String json = service.format(report);
		assertTrue(json.contains("\"timestamp\":\""));
	}
}
