package com.salesmanager.shop.store.facade.onboarding;

import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.shop.model.onboarding.PersistableOnboardingChannel;
import com.salesmanager.shop.model.onboarding.PersistableOnboardingConfirmation;
import com.salesmanager.shop.model.onboarding.PersistableOnboardingDocumentation;
import com.salesmanager.shop.model.onboarding.PersistableOnboardingWorkflow;
import com.salesmanager.shop.model.onboarding.ReadableOnboardingConfirmation;
import com.salesmanager.shop.model.onboarding.ReadableOnboardingWorkflow;

public interface OnboardingWorkflowFacade {

	ReadableOnboardingWorkflow initiate(PersistableOnboardingWorkflow onboarding, MerchantStore store);

	ReadableOnboardingWorkflow get(String workflowId, MerchantStore store);

	ReadableOnboardingWorkflow submitDocumentation(String workflowId, PersistableOnboardingDocumentation documentation,
			MerchantStore store);

	ReadableOnboardingWorkflow switchChannel(String workflowId, PersistableOnboardingChannel channel, MerchantStore store);

	ReadableOnboardingConfirmation confirm(PersistableOnboardingConfirmation confirmation, MerchantStore store,
			Language language, String contextPath);
}
