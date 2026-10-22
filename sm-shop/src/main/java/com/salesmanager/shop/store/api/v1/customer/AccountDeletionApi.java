package com.salesmanager.shop.store.api.v1.customer;

import java.security.Principal;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.salesmanager.core.model.customer.Customer;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.shop.model.customer.deletion.ReadableAccountDeletionGuidance;
import com.salesmanager.shop.store.api.exception.ResourceNotFoundException;
import com.salesmanager.shop.store.controller.customer.deletion.AccountDeletionAuditService;
import com.salesmanager.shop.store.controller.customer.deletion.AccountDeletionEvent;
import com.salesmanager.shop.store.controller.customer.facade.CustomerFacade;
import com.salesmanager.shop.utils.LabelUtils;
import com.salesmanager.shop.utils.LocaleUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import springfox.documentation.annotations.ApiIgnore;

/**
 * US-004 guided account deletion for the logged in customer.
 *
 * Shopizer 3.x is headless: the Profile page / Account Settings / Account Actions UI lives in the
 * separate storefront application, so this repository exposes the content and flow hooks that
 * storefront renders. All endpoints sit under /auth/** and therefore keep the existing customer
 * JWT security chain. The final confirmation step reuses the existing
 * DELETE /api/v1/auth/customer/ behavior unchanged (FR-US-004-005).
 *
 * Flow: GET guidance (video entry, FR-US-004-001/002) -> POST initiate (opens confirmation
 * dialog, FR-US-004-003) -> POST cancel (FR-US-004-004) or DELETE /auth/customer/ (FR-US-004-005).
 */
@RestController
@RequestMapping(value = "/api/v1")
@Api(tags = { "Customer management resource (Customer Management Api)" })
public class AccountDeletionApi {

	static final String CONSEQUENCE_KEYS = "label.customer.deletion.dialog.consequence.";
	static final int CONSEQUENCE_COUNT = 4;

	@Inject
	private CustomerFacade customerFacade;

	@Inject
	private LabelUtils messages;

	@Inject
	private AccountDeletionAuditService auditService;

	/**
	 * Stakeholder-provided instructional video. TODO US-004 FR-US-004-002: hosting location and
	 * format are not specified; configure the final URL through this property.
	 */
	@Value("${config.account.deletion.videoUrl:}")
	private String videoUrl;

	@GetMapping("/auth/customer/deletion")
	@ApiOperation(httpMethod = "GET", value = "Account deletion guidance (instructional video and confirmation dialog content) for the logged in customer", notes = "Requires authentication", produces = "application/json", response = ReadableAccountDeletionGuidance.class)
	@ApiImplicitParams({ @ApiImplicitParam(name = "store", dataType = "string", defaultValue = "DEFAULT"),
			@ApiImplicitParam(name = "lang", dataType = "string", defaultValue = "en") })
	public ReadableAccountDeletionGuidance guidance(@ApiIgnore MerchantStore merchantStore,
			@ApiIgnore Language language, HttpServletRequest request) {
		authenticatedCustomer(merchantStore, request);
		return buildGuidance(language);
	}

	@PostMapping("/auth/customer/deletion/initiate")
	@ApiOperation(httpMethod = "POST", value = "Initiates the account deletion request of the logged in customer and returns the confirmation dialog content. Does not delete the account.", notes = "Requires authentication", produces = "application/json", response = ReadableAccountDeletionGuidance.class)
	@ApiImplicitParams({ @ApiImplicitParam(name = "store", dataType = "string", defaultValue = "DEFAULT"),
			@ApiImplicitParam(name = "lang", dataType = "string", defaultValue = "en") })
	public ReadableAccountDeletionGuidance initiate(@ApiIgnore MerchantStore merchantStore,
			@ApiIgnore Language language, HttpServletRequest request) {
		Customer customer = authenticatedCustomer(merchantStore, request);
		auditService.record(AccountDeletionEvent.INITIATED, customer, merchantStore);
		return buildGuidance(language);
	}

	@PostMapping("/auth/customer/deletion/cancel")
	@ApiOperation(httpMethod = "POST", value = "Cancels the account deletion request of the logged in customer. No account change is applied.", notes = "Requires authentication", response = Void.class)
	@ApiImplicitParams({ @ApiImplicitParam(name = "store", dataType = "string", defaultValue = "DEFAULT") })
	public void cancel(@ApiIgnore MerchantStore merchantStore, HttpServletRequest request) {
		Customer customer = authenticatedCustomer(merchantStore, request);
		auditService.record(AccountDeletionEvent.CANCELLED, customer, merchantStore);
	}

	private Customer authenticatedCustomer(MerchantStore merchantStore, HttpServletRequest request) {
		Principal principal = request.getUserPrincipal();
		String userName = principal.getName();
		Customer customer = customerFacade.getCustomerByUserName(userName, merchantStore);
		if (customer == null) {
			throw new ResourceNotFoundException("Customer [" + userName + "] not found");
		}
		return customer;
	}

	/**
	 * Copy comes from the message bundles so it can be revised without code changes.
	 * TODO US-004 FR-US-004-003: consequence wording is neutral placeholder copy pending
	 * stakeholder / legal review.
	 */
	private ReadableAccountDeletionGuidance buildGuidance(Language language) {
		Locale locale = LocaleUtils.getLocale(language);
		ReadableAccountDeletionGuidance guidance = new ReadableAccountDeletionGuidance();
		guidance.setSectionTitle(messages.getMessage("label.customer.deletion.section.title", locale));
		guidance.setVideoLabel(messages.getMessage("label.customer.deletion.video.label", locale));
		guidance.setVideoDescription(messages.getMessage("label.customer.deletion.video.description", locale));
		guidance.setVideoUrl(StringUtils.trimToNull(videoUrl));
		guidance.setVideoAvailable(guidance.getVideoUrl() != null);
		guidance.setDeleteAccountLabel(messages.getMessage("label.customer.deletion.delete", locale));
		guidance.setDialogTitle(messages.getMessage("label.customer.deletion.dialog.title", locale));
		guidance.setDialogPermanentNotice(messages.getMessage("label.customer.deletion.dialog.permanent", locale));
		guidance.setDialogConsequences(IntStream.rangeClosed(1, CONSEQUENCE_COUNT)
				.mapToObj(i -> messages.getMessage(CONSEQUENCE_KEYS + i, locale)).collect(Collectors.toList()));
		guidance.setDialogCancelLabel(messages.getMessage("label.customer.deletion.dialog.cancel", locale));
		guidance.setDialogConfirmLabel(messages.getMessage("label.customer.deletion.dialog.confirm", locale));
		return guidance;
	}

}
