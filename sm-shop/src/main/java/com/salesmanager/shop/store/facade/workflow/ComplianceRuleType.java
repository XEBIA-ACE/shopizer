package com.salesmanager.shop.store.facade.workflow;

public enum ComplianceRuleType {

	/** At least one of the keywords in the rule parameter must appear in a step. */
	REQUIRED_KEYWORD,

	/** No step may contain a keyword from the rule parameter. */
	FORBIDDEN_KEYWORD,

	/** The workflow must contain at least the given number of steps. */
	MIN_STEPS,

	/** The workflow must contain at most the given number of steps. */
	MAX_STEPS
}
