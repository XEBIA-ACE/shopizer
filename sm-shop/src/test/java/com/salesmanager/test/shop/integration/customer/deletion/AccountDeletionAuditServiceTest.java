package com.salesmanager.test.shop.integration.customer.deletion;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import com.salesmanager.shop.store.controller.customer.deletion.AccountDeletionAuditService;
import com.salesmanager.shop.store.controller.customer.deletion.AccountDeletionEvent;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

/**
 * US-004-ac-6 / FR-US-004-006: key events of the deletion confirmation flow are written to the
 * dedicated audit logger category.
 */
public class AccountDeletionAuditServiceTest {

	private final AccountDeletionAuditService service = new AccountDeletionAuditService();
	private final Logger auditLogger = (Logger) LoggerFactory.getLogger(AccountDeletionAuditService.AUDIT_LOGGER_NAME);
	private final ListAppender<ILoggingEvent> appender = new ListAppender<>();

	@Before
	public void attach() {
		appender.start();
		auditLogger.addAppender(appender);
	}

	@After
	public void detach() {
		auditLogger.detachAppender(appender);
	}

	@Test
	public void us004ac6_recordsInitiationConfirmationCancellationAndCompletion() {
		service.record(AccountDeletionEvent.INITIATED, 42L, "DEFAULT");
		service.record(AccountDeletionEvent.CANCELLED, 42L, "DEFAULT");
		service.record(AccountDeletionEvent.INITIATED, 42L, "DEFAULT");
		service.record(AccountDeletionEvent.CONFIRMED, 42L, "DEFAULT");
		service.record(AccountDeletionEvent.COMPLETED, 42L, "DEFAULT");

		List<String> messages = appender.list.stream().map(ILoggingEvent::getFormattedMessage)
				.collect(Collectors.toList());
		assertEquals(5, messages.size());
		String[] expected = { "INITIATED", "CANCELLED", "INITIATED", "CONFIRMED", "COMPLETED" };
		for (int i = 0; i < expected.length; i++) {
			String message = messages.get(i);
			assertTrue(message, message.contains("story=US-004"));
			assertTrue(message, message.contains("event=" + expected[i]));
			assertTrue(message, message.contains("customerId=42"));
			assertTrue(message, message.contains("store=DEFAULT"));
			assertTrue(message, message.contains("timestamp="));
		}
	}

	@Test
	public void us004ac6_nullCustomerAndStoreDoNotBreakAuditing() {
		service.record(AccountDeletionEvent.FAILED, null, (com.salesmanager.core.model.merchant.MerchantStore) null);
		assertEquals(1, appender.list.size());
		assertTrue(appender.list.get(0).getFormattedMessage().contains("event=FAILED"));
	}

}
