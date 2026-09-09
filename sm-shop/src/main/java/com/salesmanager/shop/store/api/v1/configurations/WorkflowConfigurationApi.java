package com.salesmanager.shop.store.api.v1.configurations;

import java.util.List;

import javax.inject.Inject;
import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.shop.model.workflow.PersistableWorkflow;
import com.salesmanager.shop.model.workflow.ReadableComplianceRule;
import com.salesmanager.shop.model.workflow.ReadableWorkflow;
import com.salesmanager.shop.model.workflow.ReadableWorkflowVersion;
import com.salesmanager.shop.store.facade.workflow.WorkflowConfigurationFacade;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;
import springfox.documentation.annotations.ApiIgnore;

/**
 * No-code administration of customer onboarding workflows. Every change is
 * checked against compliance rules and propagated to all onboarding channels.
 */
@RestController
@RequestMapping("/api/v1")
@Api(tags = { "Workflow configuration management" })
@SwaggerDefinition(tags = { @Tag(name = "Workflow configuration management",
		description = "Administrative configuration of onboarding workflows") })
public class WorkflowConfigurationApi {

	@Inject
	private WorkflowConfigurationFacade workflowConfigurationFacade;

	@PostMapping("/private/configurations/workflow")
	@ResponseStatus(HttpStatus.CREATED)
	@ApiOperation(httpMethod = "POST", value = "Creates an onboarding workflow", notes = "Requires administration access", produces = "application/json", response = ReadableWorkflow.class)
	@ApiImplicitParams({ @ApiImplicitParam(name = "store", dataType = "string", defaultValue = "DEFAULT") })
	public ReadableWorkflow create(@Valid @RequestBody PersistableWorkflow workflow,
			@ApiIgnore MerchantStore merchantStore) {
		return workflowConfigurationFacade.create(workflow, merchantStore);
	}

	@PutMapping("/private/configurations/workflow/{workflowId}")
	@ApiOperation(httpMethod = "PUT", value = "Updates an onboarding workflow", notes = "Requires administration access", produces = "application/json", response = ReadableWorkflow.class)
	@ApiImplicitParams({ @ApiImplicitParam(name = "store", dataType = "string", defaultValue = "DEFAULT") })
	public ReadableWorkflow update(@PathVariable String workflowId,
			@Valid @RequestBody PersistableWorkflow workflow, @ApiIgnore MerchantStore merchantStore) {
		return workflowConfigurationFacade.update(workflowId, workflow, merchantStore);
	}

	@GetMapping("/private/configurations/workflow/{workflowId}")
	@ApiOperation(httpMethod = "GET", value = "Retrieves an onboarding workflow", notes = "Requires administration access", produces = "application/json", response = ReadableWorkflow.class)
	@ApiImplicitParams({ @ApiImplicitParam(name = "store", dataType = "string", defaultValue = "DEFAULT") })
	public ReadableWorkflow get(@PathVariable String workflowId, @ApiIgnore MerchantStore merchantStore) {
		return workflowConfigurationFacade.get(workflowId, merchantStore);
	}

	@GetMapping("/private/configurations/workflow/{workflowId}/history")
	@ApiOperation(httpMethod = "GET", value = "Lists the version history of an onboarding workflow", notes = "Requires administration access", produces = "application/json", response = List.class)
	@ApiImplicitParams({ @ApiImplicitParam(name = "store", dataType = "string", defaultValue = "DEFAULT") })
	public List<ReadableWorkflowVersion> history(@PathVariable String workflowId,
			@ApiIgnore MerchantStore merchantStore) {
		return workflowConfigurationFacade.history(workflowId, merchantStore);
	}

	@GetMapping("/private/configurations/workflow/compliance/rules")
	@ApiOperation(httpMethod = "GET", value = "Lists the compliance rules applied to workflows", notes = "Requires administration access", produces = "application/json", response = List.class)
	@ApiImplicitParams({ @ApiImplicitParam(name = "store", dataType = "string", defaultValue = "DEFAULT") })
	public List<ReadableComplianceRule> complianceRules(@ApiIgnore MerchantStore merchantStore) {
		return workflowConfigurationFacade.complianceRules();
	}
}
