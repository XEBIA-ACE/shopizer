package com.salesmanager.shop.store.api.v1.onboarding;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.shop.model.onboarding.PersistableOnboardingChannel;
import com.salesmanager.shop.model.onboarding.PersistableOnboardingConfirmation;
import com.salesmanager.shop.model.onboarding.PersistableOnboardingDocumentation;
import com.salesmanager.shop.model.onboarding.PersistableOnboardingWorkflow;
import com.salesmanager.shop.model.onboarding.ReadableOnboardingConfirmation;
import com.salesmanager.shop.model.onboarding.ReadableOnboardingWorkflow;
import com.salesmanager.shop.store.facade.onboarding.OnboardingWorkflowFacade;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping("/api/v1")
@Api(tags = { "Customer onboarding workflow" })
@SwaggerDefinition(tags = {
		@Tag(name = "Customer onboarding workflow", description = "Configurable multi-channel customer onboarding") })
public class OnboardingApi {

	@Inject
	private OnboardingWorkflowFacade onboardingWorkflowFacade;

	@PostMapping("/onboarding")
	@ResponseStatus(HttpStatus.CREATED)
	@ApiOperation(httpMethod = "POST", value = "Initiates a customer onboarding workflow", produces = "application/json", response = ReadableOnboardingWorkflow.class)
	@ApiImplicitParams({ @ApiImplicitParam(name = "store", dataType = "String", defaultValue = "DEFAULT") })
	public ReadableOnboardingWorkflow initiate(@Valid @RequestBody PersistableOnboardingWorkflow onboarding,
			@ApiIgnore MerchantStore merchantStore) {
		return onboardingWorkflowFacade.initiate(onboarding, merchantStore);
	}

	@GetMapping("/onboarding/{workflowId}")
	@ApiOperation(httpMethod = "GET", value = "Get an onboarding workflow with its audit trail", produces = "application/json", response = ReadableOnboardingWorkflow.class)
	@ApiImplicitParams({ @ApiImplicitParam(name = "store", dataType = "String", defaultValue = "DEFAULT") })
	public ReadableOnboardingWorkflow get(@PathVariable String workflowId, @ApiIgnore MerchantStore merchantStore) {
		return onboardingWorkflowFacade.get(workflowId, merchantStore);
	}

	@PostMapping("/onboarding/{workflowId}/documentation")
	@ApiOperation(httpMethod = "POST", value = "Submits and validates documentation for an onboarding workflow", produces = "application/json", response = ReadableOnboardingWorkflow.class)
	@ApiImplicitParams({ @ApiImplicitParam(name = "store", dataType = "String", defaultValue = "DEFAULT") })
	public ReadableOnboardingWorkflow submitDocumentation(@PathVariable String workflowId,
			@Valid @RequestBody PersistableOnboardingDocumentation documentation,
			@ApiIgnore MerchantStore merchantStore) {
		return onboardingWorkflowFacade.submitDocumentation(workflowId, documentation, merchantStore);
	}

	@PostMapping("/onboarding/{workflowId}/channel")
	@ApiOperation(httpMethod = "POST", value = "Continues an onboarding workflow on another channel without restarting", produces = "application/json", response = ReadableOnboardingWorkflow.class)
	@ApiImplicitParams({ @ApiImplicitParam(name = "store", dataType = "String", defaultValue = "DEFAULT") })
	public ReadableOnboardingWorkflow switchChannel(@PathVariable String workflowId,
			@Valid @RequestBody PersistableOnboardingChannel channel, @ApiIgnore MerchantStore merchantStore) {
		return onboardingWorkflowFacade.switchChannel(workflowId, channel, merchantStore);
	}

	@PostMapping("/onboarding/confirmation")
	@ApiOperation(httpMethod = "POST", value = "Completes an onboarding workflow and sends the confirmation", produces = "application/json", response = ReadableOnboardingConfirmation.class)
	@ApiImplicitParams({ @ApiImplicitParam(name = "store", dataType = "String", defaultValue = "DEFAULT"),
			@ApiImplicitParam(name = "lang", dataType = "String", defaultValue = "en") })
	public ReadableOnboardingConfirmation confirm(@Valid @RequestBody PersistableOnboardingConfirmation confirmation,
			@ApiIgnore MerchantStore merchantStore, @ApiIgnore Language language, HttpServletRequest request) {
		return onboardingWorkflowFacade.confirm(confirmation, merchantStore, language, request.getContextPath());
	}
}
