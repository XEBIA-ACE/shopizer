package com.salesmanager.shop.store.facade.onboarding;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

@Service
public class OnboardingWorkflowFacadeImpl implements OnboardingWorkflowFacade {

	@Inject
	private OnboardingWorkflowRepository repository;

	@Inject
	private OnboardingAuditLogger auditLogger;

	@Inject
	private OnboardingConfirmationDispatcher confirmationDispatcher;

	@Inject
	private LanguageService languageService;

	@Value("#{'${onboarding.documentation.required:IDENTITY,PROOF_OF_ADDRESS}'.split(',')}")
	private List<String> requiredDocumentation = new ArrayList<>();

	@Override
	public ReadableOnboardingWorkflow initiate(PersistableOnboardingWorkflow onboarding, MerchantStore store) {
		OnboardingCustomerProfile profile = onboarding.getCustomerProfile();
		OnboardingChannel channel = OnboardingChannel.from(onboarding.getChannel());
		OnboardingWorkflow workflow = new OnboardingWorkflow(UUID.randomUUID().toString(), store.getCode(), profile,
				channel);
		auditLogger.log(workflow, "INITIATED", "onboarding started for " + profile.getEmail() + " accountType="
				+ profile.getAccountType());
		receiveDocumentation(workflow, profile.getDocumentation());
		repository.save(workflow);
		return toReadable(workflow);
	}

	@Override
	public ReadableOnboardingWorkflow get(String workflowId, MerchantStore store) {
		return toReadable(load(workflowId, store));
	}

	@Override
	public ReadableOnboardingWorkflow submitDocumentation(String workflowId,
			PersistableOnboardingDocumentation documentation, MerchantStore store) {
		OnboardingWorkflow workflow = load(workflowId, store);
		synchronized (workflow) {
			if (documentation.getChannel() != null) {
				changeChannel(workflow, OnboardingChannel.from(documentation.getChannel()));
			}
			receiveDocumentation(workflow, documentation.getDocumentation());
			repository.save(workflow);
		}
		return toReadable(workflow);
	}

	@Override
	public ReadableOnboardingWorkflow switchChannel(String workflowId, PersistableOnboardingChannel channel,
			MerchantStore store) {
		OnboardingWorkflow workflow = load(workflowId, store);
		synchronized (workflow) {
			changeChannel(workflow, OnboardingChannel.from(channel.getChannel()));
			repository.save(workflow);
		}
		return toReadable(workflow);
	}

	@Override
	public ReadableOnboardingConfirmation confirm(PersistableOnboardingConfirmation confirmation, MerchantStore store,
			Language language, String contextPath) {
		OnboardingWorkflow workflow = load(confirmation.getWorkflowId(), store);
		synchronized (workflow) {
			if (workflow.getStatus() == OnboardingWorkflowStatus.CONFIRMED
					|| workflow.getStatus() == OnboardingWorkflowStatus.CONFIRMATION_PENDING) {
				return toConfirmation(workflow);
			}
			List<String> missing = missingDocumentation(workflow);
			if (!missing.isEmpty()) {
				auditLogger.log(workflow, "COMPLETION_REJECTED", "missing documentation " + missing);
				throw new ServiceRuntimeException(
						"Onboarding cannot be completed, missing documentation: " + String.join(",", missing));
			}
			workflow.setStatus(OnboardingWorkflowStatus.COMPLETED);
			workflow.setCompletedAt(Instant.now());
			auditLogger.log(workflow, "COMPLETED", "application completed");
			workflow.setStatus(OnboardingWorkflowStatus.CONFIRMATION_PENDING);
			auditLogger.log(workflow, "CONFIRMATION_REQUESTED", "confirmation queued for "
					+ workflow.getCustomerProfile().getEmail());
			repository.save(workflow);
		}
		Locale locale = languageService.toLocale(language, store);
		confirmationDispatcher.dispatch(workflow.getWorkflowId(), store, locale, contextPath);
		return toConfirmation(workflow);
	}

	private OnboardingWorkflow load(String workflowId, MerchantStore store) {
		return repository.findById(workflowId).filter(w -> w.getStoreCode().equals(store.getCode()))
				.orElseThrow(() -> new ResourceNotFoundException("Onboarding workflow [" + workflowId + "] not found"));
	}

	private void changeChannel(OnboardingWorkflow workflow, OnboardingChannel target) {
		if (workflow.getChannel() == target) {
			return;
		}
		OnboardingChannel previous = workflow.getChannel();
		workflow.setChannel(target);
		auditLogger.log(workflow, "CHANNEL_SWITCHED", "continued from " + previous + " on " + target
				+ " with status " + workflow.getStatus());
	}

	private void receiveDocumentation(OnboardingWorkflow workflow, List<String> docIds) {
		if (docIds == null) {
			docIds = new ArrayList<>();
		}
		Set<String> seen = new HashSet<>();
		List<String> duplicates = new ArrayList<>();
		for (String docId : docIds) {
			if (!seen.add(docId) || workflow.getReceivedDocumentation().contains(docId)) {
				duplicates.add(docId);
			}
		}
		if (!duplicates.isEmpty()) {
			auditLogger.log(workflow, "DOCUMENTATION_REJECTED", "duplicate documentation " + duplicates);
			throw new ServiceRuntimeException(
					"Duplicate documentation submitted, please clarify: " + String.join(",", duplicates));
		}
		workflow.getReceivedDocumentation().addAll(docIds);
		if (!docIds.isEmpty()) {
			auditLogger.log(workflow, "DOCUMENTATION_RECEIVED", "received " + docIds);
		}
		List<String> missing = missingDocumentation(workflow);
		OnboardingWorkflowStatus status = missing.isEmpty() ? OnboardingWorkflowStatus.DOCUMENTATION_COMPLETE
				: OnboardingWorkflowStatus.DOCUMENTATION_PENDING;
		if (workflow.getStatus() != status) {
			workflow.setStatus(status);
			auditLogger.log(workflow, "DOCUMENTATION_VALIDATED",
					missing.isEmpty() ? "all required documentation present" : "missing " + missing);
		}
	}

	private List<String> missingDocumentation(OnboardingWorkflow workflow) {
		return requiredDocumentation.stream().map(String::trim).filter(d -> !d.isEmpty())
				.filter(d -> !workflow.getReceivedDocumentation().contains(d)).collect(Collectors.toList());
	}

	private ReadableOnboardingWorkflow toReadable(OnboardingWorkflow workflow) {
		ReadableOnboardingWorkflow readable = new ReadableOnboardingWorkflow();
		readable.setWorkflowId(workflow.getWorkflowId());
		readable.setStatus(workflow.getStatus().name());
		readable.setChannel(workflow.getChannel().name());
		OnboardingCustomerProfile profile = new OnboardingCustomerProfile();
		profile.setName(workflow.getCustomerProfile().getName());
		profile.setEmail(workflow.getCustomerProfile().getEmail());
		profile.setPhone(workflow.getCustomerProfile().getPhone());
		profile.setAccountType(workflow.getCustomerProfile().getAccountType());
		profile.setDocumentation(new ArrayList<>(workflow.getReceivedDocumentation()));
		readable.setCustomerProfile(profile);
		readable.setMissingDocumentation(missingDocumentation(workflow));
		readable.setAuditTrail(workflow.getAuditTrail().stream().map(this::toReadable).collect(Collectors.toList()));
		return readable;
	}

	private ReadableOnboardingAuditEntry toReadable(OnboardingAuditEntry entry) {
		ReadableOnboardingAuditEntry readable = new ReadableOnboardingAuditEntry();
		readable.setTimestamp(entry.getTimestamp().toString());
		readable.setChannel(entry.getChannel().name());
		readable.setAction(entry.getAction());
		readable.setDetail(entry.getDetail());
		return readable;
	}

	private ReadableOnboardingConfirmation toConfirmation(OnboardingWorkflow workflow) {
		ReadableOnboardingConfirmation confirmation = new ReadableOnboardingConfirmation();
		confirmation.setWorkflowId(workflow.getWorkflowId());
		confirmation.setStatus(workflow.getStatus().name());
		return confirmation;
	}
}
