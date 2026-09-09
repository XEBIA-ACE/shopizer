package com.salesmanager.shop.store.facade.workflow;

import java.util.List;

import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.shop.model.workflow.PersistableWorkflow;
import com.salesmanager.shop.model.workflow.ReadableComplianceRule;
import com.salesmanager.shop.model.workflow.ReadableWorkflow;
import com.salesmanager.shop.model.workflow.ReadableWorkflowVersion;

public interface WorkflowConfigurationFacade {

	ReadableWorkflow create(PersistableWorkflow workflow, MerchantStore store);

	ReadableWorkflow update(String workflowId, PersistableWorkflow workflow, MerchantStore store);

	ReadableWorkflow get(String workflowId, MerchantStore store);

	List<ReadableWorkflowVersion> history(String workflowId, MerchantStore store);

	List<ReadableComplianceRule> complianceRules();
}
