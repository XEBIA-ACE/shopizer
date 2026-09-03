package com.salesmanager.test.shop.unit.onboarding;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.salesmanager.core.business.services.merchant.MerchantStoreService;
import com.salesmanager.core.business.services.reference.language.LanguageService;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.shop.model.onboarding.OnboardingCustomerProfile;
import com.salesmanager.shop.store.facade.onboarding.InMemoryOnboardingWorkflowRepository;
import com.salesmanager.shop.store.facade.onboarding.OnboardingAuditLogger;
import com.salesmanager.shop.store.facade.onboarding.OnboardingChannel;
import com.salesmanager.shop.store.facade.onboarding.OnboardingConfirmationDispatcherImpl;
import com.salesmanager.shop.store.facade.onboarding.OnboardingWorkflow;
import com.salesmanager.shop.store.facade.onboarding.OnboardingWorkflowRepository;
import com.salesmanager.shop.store.facade.onboarding.OnboardingWorkflowStatus;
import com.salesmanager.shop.utils.EmailTemplatesUtils;

public class OnboardingConfirmationDispatcherTest {

	private OnboardingConfirmationDispatcherImpl dispatcher;
	private OnboardingWorkflowRepository repository;
	private EmailTemplatesUtils emailTemplatesUtils;
	private MerchantStoreService merchantStoreService;
	private MerchantStore store;
	private OnboardingWorkflow workflow;

	@BeforeEach
	public void setUp() throws Exception {
		repository = new InMemoryOnboardingWorkflowRepository();
		emailTemplatesUtils = mock(EmailTemplatesUtils.class);
		merchantStoreService = mock(MerchantStoreService.class);
		LanguageService languageService = mock(LanguageService.class);
		when(languageService.toLocale(any(), any())).thenReturn(Locale.ENGLISH);

		store = new MerchantStore();
		store.setCode("DEFAULT");
		when(merchantStoreService.getByCode("DEFAULT")).thenReturn(store);

		OnboardingCustomerProfile profile = new OnboardingCustomerProfile();
		profile.setName("Jane Doe");
		profile.setEmail("jane@example.com");
		profile.setAccountType("CHECKING");
		workflow = new OnboardingWorkflow("wf-1", "DEFAULT", profile, OnboardingChannel.ONLINE);
		workflow.setStatus(OnboardingWorkflowStatus.CONFIRMATION_PENDING);
		repository.save(workflow);

		dispatcher = new OnboardingConfirmationDispatcherImpl();
		ReflectionTestUtils.setField(dispatcher, "repository", repository);
		ReflectionTestUtils.setField(dispatcher, "auditLogger", new OnboardingAuditLogger());
		ReflectionTestUtils.setField(dispatcher, "emailTemplatesUtils", emailTemplatesUtils);
		ReflectionTestUtils.setField(dispatcher, "merchantStoreService", merchantStoreService);
		ReflectionTestUtils.setField(dispatcher, "languageService", languageService);
	}

	@Test
	public void dispatchSendsEmailAndConfirms() throws Exception {
		dispatcher.dispatch("wf-1", store, Locale.ENGLISH, "");

		verify(emailTemplatesUtils).sendOnboardingConfirmationEmail(eq(workflow.getCustomerProfile()), eq(store),
				eq(Locale.ENGLISH), eq(""));
		assertEquals(OnboardingWorkflowStatus.CONFIRMED, workflow.getStatus());
		assertNotNull(workflow.getConfirmedAt());
	}

	@Test
	public void failedEmailKeepsConfirmationPendingAndRetries() throws Exception {
		doThrow(new RuntimeException("smtp down")).when(emailTemplatesUtils).sendOnboardingConfirmationEmail(any(),
				any(), any(), any());

		dispatcher.dispatch("wf-1", store, Locale.ENGLISH, "");
		assertEquals(OnboardingWorkflowStatus.CONFIRMATION_PENDING, workflow.getStatus());
		assertNull(workflow.getConfirmedAt());

		org.mockito.Mockito.reset(emailTemplatesUtils);
		dispatcher.retryPendingConfirmations();

		verify(emailTemplatesUtils, times(1)).sendOnboardingConfirmationEmail(any(), eq(store), eq(Locale.ENGLISH),
				eq(""));
		assertEquals(OnboardingWorkflowStatus.CONFIRMED, workflow.getStatus());
	}

	@Test
	public void alreadyConfirmedWorkflowIsNotResent() throws Exception {
		workflow.setStatus(OnboardingWorkflowStatus.CONFIRMED);

		dispatcher.dispatch("wf-1", store, Locale.ENGLISH, "");
		dispatcher.retryPendingConfirmations();

		verify(emailTemplatesUtils, times(0)).sendOnboardingConfirmationEmail(any(), any(), any(), any());
	}
}
