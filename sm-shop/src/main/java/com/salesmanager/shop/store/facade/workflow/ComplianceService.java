package com.salesmanager.shop.store.facade.workflow;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.salesmanager.shop.store.api.exception.ServiceRuntimeException;

/**
 * Evaluates workflows against the registered {@link ComplianceRule}s (FR-003).
 */
@Service
public class ComplianceService {

	@Inject
	private ComplianceRuleRepository ruleRepository;

	public List<ComplianceRule> rules() {
		return ruleRepository.findAll();
	}

	/**
	 * Returns the compliance issues raised by the workflow. An issue produced by a
	 * mandatory rule flags the workflow as non-compliant.
	 */
	public List<ComplianceIssue> check(WorkflowDefinition workflow) {
		List<ComplianceIssue> issues = new ArrayList<>();
		for (ComplianceRule rule : ruleRepository.findAll()) {
			issues.addAll(evaluate(rule, workflow));
		}
		return issues;
	}

	private List<ComplianceIssue> evaluate(ComplianceRule rule, WorkflowDefinition workflow) {
		List<ComplianceIssue> issues = new ArrayList<>();
		switch (rule.getType()) {
		case REQUIRED_KEYWORD:
			List<String> required = keywords(rule);
			if (required.stream().noneMatch(keyword -> anyStepContains(workflow, keyword))) {
				issues.add(new ComplianceIssue(rule, null,
						rule.getDescription() + " (expected one of: " + String.join(", ", required) + ")"));
			}
			break;
		case FORBIDDEN_KEYWORD:
			for (WorkflowStep step : workflow.getSteps()) {
				for (String keyword : keywords(rule)) {
					if (tokens(step.getDescription()).contains(keyword)) {
						issues.add(new ComplianceIssue(rule, step.getId(), "Step '" + step.getDescription()
								+ "' violates rule " + rule.getRuleId() + ": " + rule.getDescription()));
					}
				}
			}
			break;
		case MIN_STEPS:
			if (workflow.getSteps().size() < intParameter(rule)) {
				issues.add(new ComplianceIssue(rule, null, rule.getDescription() + " (found "
						+ workflow.getSteps().size() + ")"));
			}
			break;
		case MAX_STEPS:
			if (workflow.getSteps().size() > intParameter(rule)) {
				issues.add(new ComplianceIssue(rule, null, rule.getDescription() + " (found "
						+ workflow.getSteps().size() + ")"));
			}
			break;
		default:
			throw new ServiceRuntimeException("400",
					"Invalid compliance rule " + rule.getRuleId() + ": unsupported type " + rule.getType());
		}
		return issues;
	}

	private boolean anyStepContains(WorkflowDefinition workflow, String keyword) {
		for (WorkflowStep step : workflow.getSteps()) {
			if (tokens(step.getDescription()).contains(keyword)) {
				return true;
			}
		}
		return false;
	}

	private List<String> keywords(ComplianceRule rule) {
		List<String> keywords = new ArrayList<>();
		if (StringUtils.isBlank(rule.getParameter())) {
			throw new ServiceRuntimeException("400",
					"Invalid compliance rule " + rule.getRuleId() + ": missing parameter");
		}
		for (String keyword : rule.getParameter().split(",")) {
			String normalized = keyword.trim().toLowerCase(Locale.ROOT);
			if (!normalized.isEmpty()) {
				keywords.add(normalized);
			}
		}
		if (keywords.isEmpty()) {
			throw new ServiceRuntimeException("400",
					"Invalid compliance rule " + rule.getRuleId() + ": missing parameter");
		}
		return keywords;
	}

	private int intParameter(ComplianceRule rule) {
		try {
			return Integer.parseInt(rule.getParameter().trim());
		} catch (RuntimeException e) {
			throw new ServiceRuntimeException("400",
					"Invalid compliance rule " + rule.getRuleId() + ": parameter must be an integer");
		}
	}

	/** Token match so that e.g. "latest" never matches a "test" rule keyword. */
	private Set<String> tokens(String description) {
		Set<String> tokens = new HashSet<>();
		if (description != null) {
			for (String token : description.toLowerCase(Locale.ROOT).split("[^a-z0-9]+")) {
				if (!token.isEmpty()) {
					tokens.add(token);
				}
			}
		}
		return tokens;
	}
}
