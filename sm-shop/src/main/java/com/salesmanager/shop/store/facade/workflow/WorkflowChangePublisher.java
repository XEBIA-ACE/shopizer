package com.salesmanager.shop.store.facade.workflow;

import java.util.List;
import java.util.Map;

/**
 * Propagates workflow changes to all onboarding channels (FR-002).
 */
public interface WorkflowChangePublisher {

	/**
	 * Publishes the workflow's current version to every onboarding channel and
	 * returns the names of the channels that applied the change.
	 */
	List<String> propagate(WorkflowDefinition workflow);

	/** Channel name -> applied version, for a given workflow. */
	Map<String, Integer> channelVersions(String workflowId);
}
