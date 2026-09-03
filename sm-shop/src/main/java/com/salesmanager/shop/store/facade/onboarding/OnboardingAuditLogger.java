package com.salesmanager.shop.store.facade.onboarding;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class OnboardingAuditLogger {

	private static final Logger LOGGER = LoggerFactory.getLogger("ONBOARDING_AUDIT");

	public void log(OnboardingWorkflow workflow, String action, String detail) {
		OnboardingAuditEntry entry = new OnboardingAuditEntry(Instant.now(), workflow.getChannel(), action, detail);
		workflow.getAuditTrail().add(entry);
		LOGGER.info("workflowId={} store={} channel={} status={} action={} detail={}", workflow.getWorkflowId(),
				workflow.getStoreCode(), entry.getChannel(), workflow.getStatus(), action, detail);
	}
}
