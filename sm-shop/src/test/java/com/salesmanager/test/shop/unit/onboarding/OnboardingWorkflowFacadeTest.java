package com.salesmanager.test.shop.unit.onboarding;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.salesmanager.core.business.services.reference.language.LanguageService;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.shop.model.onboarding.OnboardingCustomerProfile;
import com.salesmanager.shop.model.onboarding.PersistableOnboardingChannel;
import com.salesmanager.shop.model.onboarding.PersistableOnboardingConfirmation;
import com.salesmanager.shop.model.onboarding.PersistableOnboardingDocumentation;
import com.salesmanager.shop.model.onboarding.PersistableOnboardingWorkflow;
import com.salesmanager.shop.model.onboarding.ReadableOnboardingAuditEntry;
import com.salesmanager.shop.model.onboarding.ReadableOnboardingConfirmation;
import com.salesmanager.shop.model.onboarding.ReadableOnboardingWorkflow;
import com.salesmanager.shop.store.api.exception.ResourceNotFoundException;
import com.salesmanager.shop.store.api.exception.ServiceRuntimeException;
import com.salesmanager.shop.store.facade.onboarding.InMemoryOnboardingWorkflowRepository;
import com.salesmanager.shop.store.facade.onboarding.OnboardingAuditLogger;
import com.salesmanager.shop.store.facade.onboarding.OnboardingConfirmationDispatcher;
import com.salesmanager.shop.store.facade.onboarding.OnboardingWorkflowFacadeImpl;
import com.salesmanager.shop.store.facade.onboarding.OnboardingWorkflowRepository;

public class OnboardingWorkflowFacadeTest {

	private OnboardingWorkflowFacadeImpl facade;
	private OnboardingWorkflowRepository repository;
	private OnboardingConfirmationDispatcher dispatcher;
	private MerchantStore store;
	private Language language;

	@BeforeEach
	public void setUp() {
		repository = new InMemoryOnboardingWorkflowRepository();
		dispatcher = mock(OnboardingConfirmationDispatcher.class);
		LanguageService languageService = mock(LanguageService.class);
		store = new MerchantStore();
		store.setCode("DEFAULT");
		language = new Language("en");
		when(languageService.toLocale(any(), any())).thenReturn(Locale.ENGLISH);

		facade = new OnboardingWorkflowFacadeImpl();
		ReflectionTestUtils.setField(facade, "repository", repository);
		ReflectionTestUtils.setField(facade, "auditLogger", new OnboardingAuditLogger());
		ReflectionTestUtils.setField(facade, "confirmationDispatcher", dispatcher);
		ReflectionTestUtils.setField(facade, "languageService", languageService);
		ReflectionTestUtils.setField(facade, "requiredDocumentation", Arrays.asList("IDENTITY", "PROOF_OF_ADDRESS"));
	}

	@Test
	public void initiateOnlineWorkflowWithCompleteDocumentation() {
		ReadableOnboardingWorkflow workflow = facade.initiate(request("ONLINE", "IDENTITY", "PROOF_OF_ADDRESS"), store);

		assertTrue(workflow.getWorkflowId() != null && !workflow.getWorkflowId().isEmpty());
		assertEquals("ONLINE", workflow.getChannel());
		assertEquals("DOCUMENTATION_COMPLETE", workflow.getStatus());
		assertTrue(workflow.getMissingDocumentation().isEmpty());
		assertTrue(actions(workflow).containsAll(
				Arrays.asList("INITIATED", "DOCUMENTATION_RECEIVED", "DOCUMENTATION_VALIDATED")));
	}

	@Test
	public void completedOnlineApplicationTriggersConfirmation() {
		ReadableOnboardingWorkflow workflow = facade.initiate(request("ONLINE", "IDENTITY", "PROOF_OF_ADDRESS"), store);

		ReadableOnboardingConfirmation confirmation = facade.confirm(confirmation(workflow.getWorkflowId()), store,
				language, "");

		assertEquals(workflow.getWorkflowId(), confirmation.getWorkflowId());
		assertEquals("CONFIRMATION_PENDING", confirmation.getStatus());
		verify(dispatcher).dispatch(eq(workflow.getWorkflowId()), eq(store), eq(Locale.ENGLISH), eq(""));
		assertTrue(actions(facade.get(workflow.getWorkflowId(), store)).containsAll(
				Arrays.asList("COMPLETED", "CONFIRMATION_REQUESTED")));
	}

	@Test
	public void branchApplicationContinuesOnMobileWithoutRestart() {
		ReadableOnboardingWorkflow branch = facade.initiate(request("BRANCH", "IDENTITY"), store);
		assertEquals("DOCUMENTATION_PENDING", branch.getStatus());
		assertEquals(Arrays.asList("PROOF_OF_ADDRESS"), branch.getMissingDocumentation());

		PersistableOnboardingChannel mobile = new PersistableOnboardingChannel();
		mobile.setChannel("mobile");
		ReadableOnboardingWorkflow switched = facade.switchChannel(branch.getWorkflowId(), mobile, store);

		assertEquals(branch.getWorkflowId(), switched.getWorkflowId());
		assertEquals("MOBILE", switched.getChannel());
		assertEquals("DOCUMENTATION_PENDING", switched.getStatus());
		assertEquals("Jane Doe", switched.getCustomerProfile().getName());
		assertEquals(Arrays.asList("IDENTITY"), switched.getCustomerProfile().getDocumentation());
		assertTrue(actions(switched).contains("CHANNEL_SWITCHED"));

		PersistableOnboardingDocumentation docs = new PersistableOnboardingDocumentation();
		docs.setDocumentation(Arrays.asList("PROOF_OF_ADDRESS"));
		ReadableOnboardingWorkflow completed = facade.submitDocumentation(branch.getWorkflowId(), docs, store);
		assertEquals("DOCUMENTATION_COMPLETE", completed.getStatus());

		ReadableOnboardingConfirmation confirmation = facade.confirm(confirmation(branch.getWorkflowId()), store,
				language, "");
		assertEquals("CONFIRMATION_PENDING", confirmation.getStatus());
	}

	@Test
	public void completionRejectedWhenDocumentationMissing() {
		ReadableOnboardingWorkflow workflow = facade.initiate(request("ONLINE", "IDENTITY"), store);

		assertThrows(ServiceRuntimeException.class,
				() -> facade.confirm(confirmation(workflow.getWorkflowId()), store, language, ""));
		verify(dispatcher, never()).dispatch(any(), any(), any(), any());
		ReadableOnboardingWorkflow current = facade.get(workflow.getWorkflowId(), store);
		assertEquals("DOCUMENTATION_PENDING", current.getStatus());
		assertTrue(actions(current).contains("COMPLETION_REJECTED"));
	}

	@Test
	public void duplicateDocumentationRejected() {
		ReadableOnboardingWorkflow workflow = facade.initiate(request("BRANCH", "IDENTITY"), store);

		PersistableOnboardingDocumentation docs = new PersistableOnboardingDocumentation();
		docs.setDocumentation(Arrays.asList("IDENTITY"));
		assertThrows(ServiceRuntimeException.class,
				() -> facade.submitDocumentation(workflow.getWorkflowId(), docs, store));
		assertTrue(actions(facade.get(workflow.getWorkflowId(), store)).contains("DOCUMENTATION_REJECTED"));
	}

	@Test
	public void confirmIsIdempotent() {
		ReadableOnboardingWorkflow workflow = facade.initiate(request("ONLINE", "IDENTITY", "PROOF_OF_ADDRESS"), store);
		facade.confirm(confirmation(workflow.getWorkflowId()), store, language, "");
		facade.confirm(confirmation(workflow.getWorkflowId()), store, language, "");

		verify(dispatcher).dispatch(eq(workflow.getWorkflowId()), any(), any(), any());
	}

	@Test
	public void unknownWorkflowOrOtherStoreNotFound() {
		ReadableOnboardingWorkflow workflow = facade.initiate(request("ONLINE"), store);
		MerchantStore other = new MerchantStore();
		other.setCode("OTHER");

		assertThrows(ResourceNotFoundException.class, () -> facade.get("missing", store));
		assertThrows(ResourceNotFoundException.class, () -> facade.get(workflow.getWorkflowId(), other));
	}

	@Test
	public void unknownChannelRejected() {
		assertThrows(ServiceRuntimeException.class, () -> facade.initiate(request("FAX"), store));
	}

	private static PersistableOnboardingWorkflow request(String channel, String... docs) {
		OnboardingCustomerProfile profile = new OnboardingCustomerProfile();
		profile.setName("Jane Doe");
		profile.setEmail("jane@example.com");
		profile.setPhone("+1 555 0100");
		profile.setAccountType("CHECKING");
		profile.setDocumentation(Arrays.asList(docs));
		PersistableOnboardingWorkflow request = new PersistableOnboardingWorkflow();
		request.setCustomerProfile(profile);
		request.setChannel(channel);
		return request;
	}

	private static PersistableOnboardingConfirmation confirmation(String workflowId) {
		PersistableOnboardingConfirmation confirmation = new PersistableOnboardingConfirmation();
		confirmation.setWorkflowId(workflowId);
		return confirmation;
	}

	private static List<String> actions(ReadableOnboardingWorkflow workflow) {
		return workflow.getAuditTrail().stream().map(ReadableOnboardingAuditEntry::getAction)
				.collect(Collectors.toList());
	}
}
