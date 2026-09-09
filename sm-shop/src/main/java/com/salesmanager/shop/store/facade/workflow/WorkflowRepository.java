package com.salesmanager.shop.store.facade.workflow;

import java.util.Optional;

public interface WorkflowRepository {

	WorkflowDefinition save(WorkflowDefinition workflow);

	Optional<WorkflowDefinition> findById(String workflowId, String storeCode);

	boolean existsById(String workflowId, String storeCode);
}
