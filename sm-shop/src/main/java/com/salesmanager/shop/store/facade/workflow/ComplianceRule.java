package com.salesmanager.shop.store.facade.workflow;

public class ComplianceRule {

	private final String ruleId;
	private final String description;
	private final boolean mandatory;
	private final ComplianceRuleType type;
	/** Comma separated keywords for *_KEYWORD rules, an integer for *_STEPS rules. */
	private final String parameter;

	public ComplianceRule(String ruleId, String description, boolean mandatory, ComplianceRuleType type,
			String parameter) {
		this.ruleId = ruleId;
		this.description = description;
		this.mandatory = mandatory;
		this.type = type;
		this.parameter = parameter;
	}

	public String getRuleId() {
		return ruleId;
	}

	public String getDescription() {
		return description;
	}

	public boolean isMandatory() {
		return mandatory;
	}

	public ComplianceRuleType getType() {
		return type;
	}

	public String getParameter() {
		return parameter;
	}
}
