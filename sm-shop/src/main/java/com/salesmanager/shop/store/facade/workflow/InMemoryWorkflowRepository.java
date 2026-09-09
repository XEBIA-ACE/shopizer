package com.salesmanager.shop.store.facade.workflow;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class InMemoryWorkflowRepository implements WorkflowRepository {

	private final Map<String, WorkflowDefinition> workflows = new ConcurrentHashMap<>();

	@Override
	public WorkflowDefinition save(WorkflowDefinition workflow) {
		workflows.put(key(workflow.getId(), workflow.getStoreCode()), workflow);
		return workflow;
	}

	@Override
	public Optional<WorkflowDefinition> findById(String workflowId, String storeCode) {
		return Optional.ofNullable(workflows.get(key(workflowId, storeCode)));
	}

	@Override
	public boolean existsById(String workflowId, String storeCode) {
		return workflows.containsKey(key(workflowId, storeCode));
	}

	private String key(String workflowId, String storeCode) {
		return storeCode + ":" + workflowId;
	}
}
