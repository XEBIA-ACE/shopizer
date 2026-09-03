package com.salesmanager.shop.store.facade.onboarding;

import java.util.List;
import java.util.Optional;

public interface OnboardingWorkflowRepository {

	OnboardingWorkflow save(OnboardingWorkflow workflow);

	Optional<OnboardingWorkflow> findById(String workflowId);

	List<OnboardingWorkflow> findByStatus(OnboardingWorkflowStatus status);
}
