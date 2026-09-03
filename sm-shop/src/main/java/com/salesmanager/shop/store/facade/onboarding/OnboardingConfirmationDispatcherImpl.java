package com.salesmanager.shop.store.facade.onboarding;

import java.time.Instant;
import java.util.Locale;
import java.util.Optional;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.salesmanager.core.business.services.merchant.MerchantStoreService;
import com.salesmanager.core.business.services.reference.language.LanguageService;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.shop.utils.EmailTemplatesUtils;

@Service
public class OnboardingConfirmationDispatcherImpl implements OnboardingConfirmationDispatcher {

	private static final Logger LOGGER = LoggerFactory.getLogger(OnboardingConfirmationDispatcherImpl.class);

	@Inject
	private OnboardingWorkflowRepository repository;

	@Inject
	private OnboardingAuditLogger auditLogger;

	@Inject
	private EmailTemplatesUtils emailTemplatesUtils;

	@Inject
	private MerchantStoreService merchantStoreService;

	@Inject
	private LanguageService languageService;

	@Async
	@Override
	public void dispatch(String workflowId, MerchantStore store, Locale locale, String contextPath) {
		Optional<OnboardingWorkflow> workflow = repository.findById(workflowId);
		workflow.ifPresent(w -> send(w, store, locale, contextPath));
	}

	@Scheduled(fixedDelayString = "${onboarding.confirmation.retry.delay.ms:60000}")
	public void retryPendingConfirmations() {
		for (OnboardingWorkflow workflow : repository.findByStatus(OnboardingWorkflowStatus.CONFIRMATION_PENDING)) {
			try {
				MerchantStore store = merchantStoreService.getByCode(workflow.getStoreCode());
				if (store == null) {
					continue;
				}
				Locale locale = languageService.toLocale(store.getDefaultLanguage(), store);
				send(workflow, store, locale, "");
			} catch (Exception e) {
				LOGGER.error("Unable to retry onboarding confirmation for workflow {}", workflow.getWorkflowId(), e);
			}
		}
	}

	private void send(OnboardingWorkflow workflow, MerchantStore store, Locale locale, String contextPath) {
		synchronized (workflow) {
			if (workflow.getStatus() != OnboardingWorkflowStatus.CONFIRMATION_PENDING) {
				return;
			}
			try {
				emailTemplatesUtils.sendOnboardingConfirmationEmail(workflow.getCustomerProfile(), store, locale,
						contextPath);
				workflow.setStatus(OnboardingWorkflowStatus.CONFIRMED);
				workflow.setConfirmedAt(Instant.now());
				auditLogger.log(workflow, "CONFIRMATION_SENT",
						"confirmation email sent to " + workflow.getCustomerProfile().getEmail());
			} catch (Exception e) {
				LOGGER.error("Unable to send onboarding confirmation for workflow {}", workflow.getWorkflowId(), e);
				auditLogger.log(workflow, "CONFIRMATION_DEFERRED", "confirmation stored for later delivery: "
						+ e.getMessage());
			}
			repository.save(workflow);
		}
	}
}
