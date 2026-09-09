package com.salesmanager.shop.store.facade.workflow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Synchronous in-process propagation to the configured onboarding channels:
 * every save makes the new version immediately visible to each channel. An
 * asynchronous transport (e.g. a message queue) can replace this implementation
 * without touching the facade.
 */
@Component
public class InMemoryWorkflowChangePublisher implements WorkflowChangePublisher {

	/** channel -> (workflowId -> applied version) */
	private final Map<String, Map<String, Integer>> channelVersions = new ConcurrentHashMap<>();

	@Value("#{'${workflow.propagation.channels:DIGITAL,BRANCH}'.split(',')}")
	private List<String> onboardingChannels = new ArrayList<>();

	@Override
	public List<String> propagate(WorkflowDefinition workflow) {
		List<String> applied = new ArrayList<>();
		for (String channel : onboardingChannels) {
			String name = channel.trim();
			if (name.isEmpty()) {
				continue;
			}
			channelVersions.computeIfAbsent(name, c -> new ConcurrentHashMap<>()).put(workflow.getId(),
					workflow.getVersion());
			applied.add(name);
		}
		return applied;
	}

	@Override
	public Map<String, Integer> channelVersions(String workflowId) {
		Map<String, Integer> applied = new ConcurrentHashMap<>();
		for (Map.Entry<String, Map<String, Integer>> channel : channelVersions.entrySet()) {
			Integer version = channel.getValue().get(workflowId);
			if (version != null) {
				applied.put(channel.getKey(), version);
			}
		}
		return Collections.unmodifiableMap(applied);
	}
}
