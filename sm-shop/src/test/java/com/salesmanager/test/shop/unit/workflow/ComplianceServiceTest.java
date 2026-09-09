package com.salesmanager.test.shop.unit.workflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.salesmanager.shop.store.api.exception.ServiceRuntimeException;
import com.salesmanager.shop.store.facade.workflow.ComplianceIssue;
import com.salesmanager.shop.store.facade.workflow.ComplianceRule;
import com.salesmanager.shop.store.facade.workflow.ComplianceRuleType;
import com.salesmanager.shop.store.facade.workflow.ComplianceService;
import com.salesmanager.shop.store.facade.workflow.InMemoryComplianceRuleRepository;
import com.salesmanager.shop.store.facade.workflow.WorkflowDefinition;
import com.salesmanager.shop.store.facade.workflow.WorkflowStep;

public class ComplianceServiceTest {

	private ComplianceService complianceService;

	@BeforeEach
	public void setUp() {
		complianceService = new ComplianceService();
		ReflectionTestUtils.setField(complianceService, "ruleRepository", new InMemoryComplianceRuleRepository());
	}

	private WorkflowDefinition workflow(String... stepDescriptions) {
		WorkflowDefinition workflow = new WorkflowDefinition("wf-1", "name", "DEFAULT");
		int i = 1;
		for (String description : stepDescriptions) {
			workflow.getSteps().add(new WorkflowStep("step-" + i++, description));
		}
		return workflow;
	}

	@Test
	public void passesWhenMandatoryKeywordsPresent() {
		List<ComplianceIssue> issues = complianceService
				.check(workflow("Verify customer identity", "Collect customer consent"));

		assertTrue(issues.stream().noneMatch(i -> i.getRule().isMandatory()));
	}

	@Test
	public void flagsMissingIdentityVerificationStep() {
		List<ComplianceIssue> issues = complianceService
				.check(workflow("Collect customer consent", "Schedule welcome call"));

		assertTrue(issues.stream().anyMatch(i -> "ONB-001".equals(i.getRule().getRuleId()) && i.getRule().isMandatory()));
	}

	@Test
	public void flagsStepContainingForbiddenKeyword() {
		WorkflowDefinition workflow = workflow("Verify identity", "Collect consent", "Ask customer for password");

		List<ComplianceIssue> issues = complianceService.check(workflow);

		assertTrue(issues.stream().anyMatch(i -> "ONB-004".equals(i.getRule().getRuleId())
				&& "step-3".equals(i.getStepId()) && i.getRule().isMandatory()));
	}

	@Test
	public void keywordMatchingDoesNotMatchSubstrings() {
		// "shipping" contains "pin" but must not trip the forbidden-keyword rule
		WorkflowDefinition workflow = workflow("Verify identity", "Collect consent", "Arrange shipping of card");

		List<ComplianceIssue> issues = complianceService.check(workflow);

		assertTrue(issues.stream().noneMatch(i -> "ONB-004".equals(i.getRule().getRuleId())));
	}

	@Test
	public void advisoryRuleDoesNotBlockCompliance() {
		WorkflowDefinition workflow = workflow("Verify identity and collect consent");

		List<ComplianceIssue> issues = complianceService.check(workflow);

		assertEquals(1, issues.size());
		assertEquals("ONB-003", issues.get(0).getRule().getRuleId());
		assertTrue(!issues.get(0).getRule().isMandatory());
	}

	@Test
	public void invalidRuleParameterIsRejected() {
		ComplianceRule badRule = new ComplianceRule("BAD-1", "bad rule", false, ComplianceRuleType.MAX_STEPS,
				"not-a-number");
		ComplianceService service = new ComplianceService();
		ReflectionTestUtils.setField(service, "ruleRepository",
				(com.salesmanager.shop.store.facade.workflow.ComplianceRuleRepository) () -> Collections
						.singletonList(badRule));

		ServiceRuntimeException e = assertThrows(ServiceRuntimeException.class,
				() -> service.check(workflow("Verify identity")));

		assertTrue(e.getErrorMessage().contains("Invalid compliance rule"));
	}

	@Test
	public void listsPreDefinedRules() {
		assertTrue(complianceService.rules().size() >= 3);
	}
}
