package com.salesmanager.shop.store.facade.workflow;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.shop.model.workflow.PersistableWorkflow;
import com.salesmanager.shop.model.workflow.PersistableWorkflowStep;
import com.salesmanager.shop.model.workflow.ReadableComplianceIssue;
import com.salesmanager.shop.model.workflow.ReadableComplianceRule;
import com.salesmanager.shop.model.workflow.ReadableWorkflow;
import com.salesmanager.shop.model.workflow.ReadableWorkflowStep;
import com.salesmanager.shop.model.workflow.ReadableWorkflowVersion;
import com.salesmanager.shop.store.api.exception.ResourceNotFoundException;
import com.salesmanager.shop.store.api.exception.ServiceRuntimeException;

/**
 * Administration of onboarding workflows: create/update with validation,
 * automatic compliance flagging, version history and immediate propagation to
 * all onboarding channels.
 */
@Service
public class WorkflowConfigurationFacadeImpl implements WorkflowConfigurationFacade {

	@Inject
	private WorkflowRepository repository;

	@Inject
	private ComplianceService complianceService;

	@Inject
	private WorkflowChangePublisher changePublisher;

	@Override
	public ReadableWorkflow create(PersistableWorkflow persistable, MerchantStore store) {
		validate(persistable);
		String id = StringUtils.isBlank(persistable.getId()) ? UUID.randomUUID().toString() : persistable.getId();
		if (repository.existsById(id, store.getCode())) {
			throw new ServiceRuntimeException("409", "Workflow '" + id + "' already exists");
		}
		WorkflowDefinition workflow = new WorkflowDefinition(id, persistable.getName().trim(), store.getCode());
		synchronized (workflow) {
			workflow.setSteps(toSteps(persistable));
			applyCompliance(workflow);
			workflow.setVersion(1);
			workflow.getHistory()
					.add(new WorkflowVersionEntry(1, Instant.now(), "Workflow created", workflow.isCompliant(),
							workflow.getComplianceIssues().size()));
			repository.save(workflow);
		}
		return toReadable(workflow, changePublisher.propagate(workflow));
	}

	@Override
	public ReadableWorkflow update(String workflowId, PersistableWorkflow persistable, MerchantStore store) {
		validate(persistable);
		WorkflowDefinition workflow = load(workflowId, store);
		synchronized (workflow) {
			if (persistable.getVersion() != null && persistable.getVersion().intValue() != workflow.getVersion()) {
				throw new ServiceRuntimeException("409",
						"Workflow '" + workflowId + "' was modified concurrently: expected version "
								+ workflow.getVersion() + " but got " + persistable.getVersion()
								+ ". Reload the workflow and retry.");
			}
			workflow.setName(persistable.getName().trim());
			workflow.setSteps(toSteps(persistable));
			applyCompliance(workflow);
			int version = workflow.getVersion() + 1;
			workflow.setVersion(version);
			workflow.setUpdatedAt(Instant.now());
			workflow.getHistory()
					.add(new WorkflowVersionEntry(version, Instant.now(),
							"Workflow updated (" + workflow.getSteps().size() + " steps)",
							workflow.isCompliant(), workflow.getComplianceIssues().size()));
			repository.save(workflow);
		}
		return toReadable(workflow, changePublisher.propagate(workflow));
	}

	@Override
	public ReadableWorkflow get(String workflowId, MerchantStore store) {
		WorkflowDefinition workflow = load(workflowId, store);
		List<String> channels = new ArrayList<>(changePublisher.channelVersions(workflow.getId()).keySet());
		return toReadable(workflow, channels);
	}

	@Override
	public List<ReadableWorkflowVersion> history(String workflowId, MerchantStore store) {
		return load(workflowId, store).getHistory().stream().map(this::toReadableVersion)
				.collect(Collectors.toList());
	}

	@Override
	public List<ReadableComplianceRule> complianceRules() {
		return complianceService.rules().stream().map(this::toReadableRule).collect(Collectors.toList());
	}

	/** FR-008 / EC-001: invalid changes are rejected with a clear message. */
	private void validate(PersistableWorkflow persistable) {
		if (persistable == null) {
			throw new ServiceRuntimeException("400", "Workflow payload is required");
		}
		if (StringUtils.isBlank(persistable.getName())) {
			throw new ServiceRuntimeException("400", "Workflow name is required");
		}
		if (persistable.getSteps() == null || persistable.getSteps().isEmpty()) {
			throw new ServiceRuntimeException("400", "Workflow must contain at least one step");
		}
		Set<String> ids = new HashSet<>();
		for (PersistableWorkflowStep step : persistable.getSteps()) {
			if (step == null || StringUtils.isBlank(step.getDescription())) {
				throw new ServiceRuntimeException("400", "Every workflow step requires a description");
			}
			if (StringUtils.isNotBlank(step.getId()) && !ids.add(step.getId())) {
				throw new ServiceRuntimeException("400", "Duplicate step id '" + step.getId() + "'");
			}
		}
	}

	private List<WorkflowStep> toSteps(PersistableWorkflow persistable) {
		List<WorkflowStep> steps = new ArrayList<>();
		int position = 1;
		for (PersistableWorkflowStep step : persistable.getSteps()) {
			String id = StringUtils.isBlank(step.getId()) ? "step-" + position : step.getId();
			steps.add(new WorkflowStep(id, step.getDescription().trim()));
			position++;
		}
		return steps;
	}

	/** FR-003/FR-004/FR-007: every saved version is checked and flagged. */
	private void applyCompliance(WorkflowDefinition workflow) {
		List<ComplianceIssue> issues = complianceService.check(workflow);
		Set<String> flaggedSteps = issues.stream().filter(i -> i.getStepId() != null).map(ComplianceIssue::getStepId)
				.collect(Collectors.toSet());
		for (WorkflowStep step : workflow.getSteps()) {
			step.setCompliant(!flaggedSteps.contains(step.getId()));
		}
		workflow.setComplianceIssues(issues);
		workflow.setCompliant(issues.stream().noneMatch(i -> i.getRule().isMandatory()));
	}

	private WorkflowDefinition load(String workflowId, MerchantStore store) {
		return repository.findById(workflowId, store.getCode())
				.orElseThrow(() -> new ResourceNotFoundException("Workflow '" + workflowId + "' not found"));
	}

	private ReadableWorkflow toReadable(WorkflowDefinition workflow, List<String> propagatedChannels) {
		ReadableWorkflow readable = new ReadableWorkflow();
		readable.setId(workflow.getId());
		readable.setName(workflow.getName());
		readable.setVersion(workflow.getVersion());
		readable.setCompliant(workflow.isCompliant());
		readable.setLastModified(workflow.getUpdatedAt().toString());
		readable.setSteps(workflow.getSteps().stream().map(this::toReadableStep).collect(Collectors.toList()));
		readable.setComplianceIssues(workflow.getComplianceIssues().stream().map(this::toReadableIssue)
				.collect(Collectors.toList()));
		readable.setPropagatedChannels(propagatedChannels);
		return readable;
	}

	private ReadableWorkflowStep toReadableStep(WorkflowStep step) {
		ReadableWorkflowStep readable = new ReadableWorkflowStep();
		readable.setId(step.getId());
		readable.setDescription(step.getDescription());
		readable.setComplianceStatus(step.isCompliant());
		return readable;
	}

	private ReadableComplianceIssue toReadableIssue(ComplianceIssue issue) {
		ReadableComplianceIssue readable = new ReadableComplianceIssue();
		readable.setRuleId(issue.getRule().getRuleId());
		readable.setRuleDescription(issue.getRule().getDescription());
		readable.setMandatory(issue.getRule().isMandatory());
		readable.setStepId(issue.getStepId());
		readable.setMessage(issue.getMessage());
		return readable;
	}

	private ReadableWorkflowVersion toReadableVersion(WorkflowVersionEntry entry) {
		ReadableWorkflowVersion readable = new ReadableWorkflowVersion();
		readable.setVersion(entry.getVersion());
		readable.setTimestamp(entry.getTimestamp().toString());
		readable.setChangeSummary(entry.getChangeSummary());
		readable.setCompliant(entry.isCompliant());
		readable.setIssueCount(entry.getIssueCount());
		return readable;
	}

	private ReadableComplianceRule toReadableRule(ComplianceRule rule) {
		ReadableComplianceRule readable = new ReadableComplianceRule();
		readable.setRuleId(rule.getRuleId());
		readable.setDescription(rule.getDescription());
		readable.setMandatory(rule.isMandatory());
		readable.setType(rule.getType().name());
		readable.setParameter(rule.getParameter());
		return readable;
	}
}
