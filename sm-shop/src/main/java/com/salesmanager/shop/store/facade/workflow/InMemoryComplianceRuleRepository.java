package com.salesmanager.shop.store.facade.workflow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

/**
 * Pre-defined compliance rules for onboarding workflows. Rules are maintained
 * independently of workflow definitions (assumption A-002).
 */
@Component
public class InMemoryComplianceRuleRepository implements ComplianceRuleRepository {

	private final List<ComplianceRule> rules = new ArrayList<>();

	public InMemoryComplianceRuleRepository() {
		rules.add(new ComplianceRule("ONB-001",
				"Onboarding workflows must include an identity verification step", true,
				ComplianceRuleType.REQUIRED_KEYWORD, "identity,kyc"));
		rules.add(new ComplianceRule("ONB-002",
				"Onboarding workflows must include a customer consent step", true,
				ComplianceRuleType.REQUIRED_KEYWORD, "consent"));
		rules.add(new ComplianceRule("ONB-003",
				"Onboarding workflows should contain at least two steps", false,
				ComplianceRuleType.MIN_STEPS, "2"));
		rules.add(new ComplianceRule("ONB-004",
				"Onboarding workflow steps must not collect credentials", true,
				ComplianceRuleType.FORBIDDEN_KEYWORD, "password,pin"));
	}

	@Override
	public List<ComplianceRule> findAll() {
		return Collections.unmodifiableList(rules);
	}
}
