package com.salesmanager.test.shop.unit.workflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.shop.model.workflow.PersistableWorkflow;
import com.salesmanager.shop.model.workflow.PersistableWorkflowStep;
import com.salesmanager.shop.model.workflow.ReadableComplianceRule;
import com.salesmanager.shop.model.workflow.ReadableWorkflow;
import com.salesmanager.shop.model.workflow.ReadableWorkflowVersion;
import com.salesmanager.shop.store.api.exception.ResourceNotFoundException;
import com.salesmanager.shop.store.api.exception.ServiceRuntimeException;
import com.salesmanager.shop.store.facade.workflow.ComplianceService;
import com.salesmanager.shop.store.facade.workflow.InMemoryComplianceRuleRepository;
import com.salesmanager.shop.store.facade.workflow.InMemoryWorkflowChangePublisher;
import com.salesmanager.shop.store.facade.workflow.InMemoryWorkflowRepository;
import com.salesmanager.shop.store.facade.workflow.WorkflowConfigurationFacadeImpl;
import com.salesmanager.shop.store.facade.workflow.WorkflowRepository;

public class WorkflowConfigurationFacadeTest {

	private WorkflowConfigurationFacadeImpl facade;
	private WorkflowRepository repository;
	private InMemoryWorkflowChangePublisher publisher;
	private MerchantStore store;

	@BeforeEach
	public void setUp() {
		repository = new InMemoryWorkflowRepository();
		ComplianceService complianceService = new ComplianceService();
		ReflectionTestUtils.setField(complianceService, "ruleRepository", new InMemoryComplianceRuleRepository());
		publisher = new InMemoryWorkflowChangePublisher();
		ReflectionTestUtils.setField(publisher, "onboardingChannels", Arrays.asList("DIGITAL", "BRANCH"));
		store = new MerchantStore();
		store.setCode("DEFAULT");

		facade = new WorkflowConfigurationFacadeImpl();
		ReflectionTestUtils.setField(facade, "repository", repository);
		ReflectionTestUtils.setField(facade, "complianceService", complianceService);
		ReflectionTestUtils.setField(facade, "changePublisher", publisher);
	}

	private PersistableWorkflow workflow(String name, String... stepDescriptions) {
		PersistableWorkflow workflow = new PersistableWorkflow();
		workflow.setName(name);
		workflow.setSteps(Arrays.stream(stepDescriptions).map(description -> {
			PersistableWorkflowStep step = new PersistableWorkflowStep();
			step.setDescription(description);
			return step;
		}).collect(Collectors.toList()));
		return workflow;
	}

	private PersistableWorkflow compliantWorkflow() {
		return workflow("Retail onboarding", "Verify customer identity", "Collect customer consent");
	}

	@Test
	public void createSavesCompliantWorkflowAndPropagatesToAllChannels() {
		ReadableWorkflow created = facade.create(compliantWorkflow(), store);

		assertEquals(1, created.getVersion());
		assertTrue(created.isCompliant());
		assertTrue(created.getComplianceIssues().isEmpty());
		assertEquals(2, created.getSteps().size());
		assertTrue(created.getSteps().stream().allMatch(s -> s.isComplianceStatus()));
		assertEquals(Arrays.asList("DIGITAL", "BRANCH"), created.getPropagatedChannels());
		assertEquals(1, publisher.channelVersions(created.getId()).get("DIGITAL").intValue());
		assertEquals(1, publisher.channelVersions(created.getId()).get("BRANCH").intValue());
	}

	@Test
	public void createFlagsNonCompliantWorkflow() {
		ReadableWorkflow created = facade.create(workflow("Incomplete", "Collect customer consent"), store);

		assertFalse(created.isCompliant());
		assertFalse(created.getComplianceIssues().isEmpty());
		assertTrue(created.getComplianceIssues().stream().anyMatch(i -> "ONB-001".equals(i.getRuleId())));
		// FR-007: the change is stored but never unflagged
		ReadableWorkflow loaded = facade.get(created.getId(), store);
		assertFalse(loaded.isCompliant());
		assertFalse(loaded.getComplianceIssues().isEmpty());
	}

	@Test
	public void createFlagsStepViolatingForbiddenKeyword() {
		PersistableWorkflow workflow = compliantWorkflow();
		PersistableWorkflowStep step = new PersistableWorkflowStep();
		step.setDescription("Ask customer for password");
		workflow.getSteps().add(step);

		ReadableWorkflow created = facade.create(workflow, store);

		assertFalse(created.isCompliant());
		assertEquals(1, created.getSteps().stream().filter(s -> !s.isComplianceStatus()).count());
	}

	@Test
	public void createRejectsWorkflowWithoutSteps() {
		ServiceRuntimeException e = assertThrows(ServiceRuntimeException.class,
				() -> facade.create(workflow("Empty"), store));
		assertTrue(e.getErrorMessage().contains("at least one step"));
	}

	@Test
	public void createRejectsBlankName() {
		assertThrows(ServiceRuntimeException.class, () -> facade.create(workflow(" ", "Verify identity"), store));
	}

	@Test
	public void createRejectsDuplicateStepIds() {
		PersistableWorkflow workflow = compliantWorkflow();
		workflow.getSteps().forEach(s -> s.setId("same"));

		ServiceRuntimeException e = assertThrows(ServiceRuntimeException.class,
				() -> facade.create(workflow, store));
		assertTrue(e.getErrorMessage().contains("Duplicate step id"));
	}

	@Test
	public void createRejectsExistingId() {
		PersistableWorkflow workflow = compliantWorkflow();
		workflow.setId("wf-1");
		facade.create(workflow, store);

		assertThrows(ServiceRuntimeException.class, () -> facade.create(workflow, store));
	}

	@Test
	public void updateIncrementsVersionRecordsHistoryAndPropagates() {
		ReadableWorkflow created = facade.create(compliantWorkflow(), store);

		PersistableWorkflow update = compliantWorkflow();
		update.setVersion(created.getVersion());
		update.getSteps().add(step("Schedule welcome call"));
		ReadableWorkflow updated = facade.update(created.getId(), update, store);

		assertEquals(2, updated.getVersion());
		assertEquals(3, updated.getSteps().size());
		assertEquals(Arrays.asList("DIGITAL", "BRANCH"), updated.getPropagatedChannels());
		assertEquals(2, publisher.channelVersions(created.getId()).get("BRANCH").intValue());

		List<ReadableWorkflowVersion> history = facade.history(created.getId(), store);
		assertEquals(2, history.size());
		assertEquals(1, history.get(0).getVersion());
		assertEquals(2, history.get(1).getVersion());
		assertTrue(history.get(1).getChangeSummary().contains("updated"));
	}

	@Test
	public void updateRejectsStaleVersion() {
		ReadableWorkflow created = facade.create(compliantWorkflow(), store);
		PersistableWorkflow update = compliantWorkflow();
		update.setVersion(created.getVersion());
		facade.update(created.getId(), update, store);

		PersistableWorkflow stale = compliantWorkflow();
		stale.setVersion(1);
		ServiceRuntimeException e = assertThrows(ServiceRuntimeException.class,
				() -> facade.update(created.getId(), stale, store));
		assertTrue(e.getErrorMessage().contains("modified concurrently"));
	}

	@Test
	public void updateMissingWorkflowThrowsNotFound() {
		assertThrows(ResourceNotFoundException.class,
				() -> facade.update("missing", compliantWorkflow(), store));
	}

	@Test
	public void getMissingWorkflowThrowsNotFound() {
		assertThrows(ResourceNotFoundException.class, () -> facade.get("missing", store));
	}

	@Test
	public void complianceRulesAreListed() {
		List<ReadableComplianceRule> rules = facade.complianceRules();
		assertTrue(rules.size() >= 3);
		assertTrue(rules.stream().anyMatch(ReadableComplianceRule::isMandatory));
	}

	private PersistableWorkflowStep step(String description) {
		PersistableWorkflowStep step = new PersistableWorkflowStep();
		step.setDescription(description);
		return step;
	}
}
