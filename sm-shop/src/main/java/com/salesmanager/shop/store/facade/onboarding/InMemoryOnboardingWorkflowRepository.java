package com.salesmanager.shop.store.facade.onboarding;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

@Repository
public class InMemoryOnboardingWorkflowRepository implements OnboardingWorkflowRepository {

	private final Map<String, OnboardingWorkflow> workflows = new ConcurrentHashMap<>();

	@Override
	public OnboardingWorkflow save(OnboardingWorkflow workflow) {
		workflows.put(workflow.getWorkflowId(), workflow);
		return workflow;
	}

	@Override
	public Optional<OnboardingWorkflow> findById(String workflowId) {
		return Optional.ofNullable(workflows.get(workflowId));
	}

	@Override
	public List<OnboardingWorkflow> findByStatus(OnboardingWorkflowStatus status) {
		return workflows.values().stream().filter(w -> w.getStatus() == status).collect(Collectors.toList());
	}
}
