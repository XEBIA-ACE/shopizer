package com.salesmanager.shop.store.controller.customer.deletion;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.salesmanager.core.model.customer.Customer;
import com.salesmanager.core.model.merchant.MerchantStore;

/**
 * Records the key events of the US-004 deletion confirmation flow (FR-US-004-006 / US-004-ac-6).
 *
 * Shopizer has no dedicated audit-trail store, so events are written through SLF4J (the
 * logging mechanism used across the codebase) to a dedicated logger category,
 * {@value #AUDIT_LOGGER_NAME}, which operators can route to a restricted, append-only appender.
 * Only the internal customer id and store code are recorded (no e-mail or other PII).
 *
 * TODO US-004 FR-US-004-006: the secure store technology, schema and retention period are not
 * specified; route {@value #AUDIT_LOGGER_NAME} to the approved secure audit sink once decided.
 */
@Component
public class AccountDeletionAuditService {

	public static final String AUDIT_LOGGER_NAME = "com.salesmanager.audit.AccountDeletion";

	private static final Logger AUDIT = LoggerFactory.getLogger(AUDIT_LOGGER_NAME);
	private static final Logger LOGGER = LoggerFactory.getLogger(AccountDeletionAuditService.class);

	public void record(AccountDeletionEvent event, Customer customer, MerchantStore store) {
		record(event, customer == null ? null : customer.getId(), store == null ? null : store.getCode());
	}

	/**
	 * Audit failures never interrupt the deletion flow nor the existing Delete Account behavior.
	 */
	public void record(AccountDeletionEvent event, Long customerId, String storeCode) {
		try {
			AUDIT.info("story=US-004 event={} customerId={} store={} timestamp={}", event, customerId, storeCode,
					Instant.now());
		} catch (Exception e) {
			LOGGER.error("Unable to record account deletion audit event [" + event + "]", e);
		}
	}

}
