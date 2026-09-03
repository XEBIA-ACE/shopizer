package com.salesmanager.shop.store.api.v1.currency;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.shop.constants.Constants;
import com.salesmanager.shop.model.references.PersistableCurrencySelection;
import com.salesmanager.shop.model.references.ReadableCurrencySelection;
import com.salesmanager.shop.store.controller.currency.facade.CurrencyFacade;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import springfox.documentation.annotations.ApiIgnore;

/**
 * Currency used by a shopper during checkout. Guests are served from their session while
 * authenticated shoppers have their selection persisted on their profile.
 */
@RestController
@RequestMapping(value = "/api/v1")
@Api(tags = { "Shopper currency api" })
public class ShopperCurrencyApi {

	@Autowired
	private CurrencyFacade currencyFacade;

	@GetMapping("/checkout/currency")
	@ApiOperation(httpMethod = "GET", value = "Get currency used by a guest shopper and the currencies available for that store", produces = "application/json", response = ReadableCurrencySelection.class)
	@ApiImplicitParams({ @ApiImplicitParam(name = "store", dataType = "string", defaultValue = "DEFAULT") })
	public ReadableCurrencySelection getCurrency(@ApiIgnore MerchantStore merchantStore,
			HttpServletRequest request) {
		return currencyFacade.getCurrencySelection(merchantStore, null, request.getLocale(),
				selectedFromSession(request));
	}

	@PatchMapping("/checkout/currency")
	@ApiOperation(httpMethod = "PATCH", value = "Set currency used by a guest shopper, kept for the duration of the session", produces = "application/json", response = ReadableCurrencySelection.class)
	@ApiImplicitParams({ @ApiImplicitParam(name = "store", dataType = "string", defaultValue = "DEFAULT") })
	public ReadableCurrencySelection saveCurrency(@ApiIgnore MerchantStore merchantStore,
			@Valid @RequestBody PersistableCurrencySelection selection, HttpServletRequest request) {
		ReadableCurrencySelection saved = currencyFacade.saveCurrencySelection(selection.getCode(),
				merchantStore, null, request.getLocale());
		request.getSession().setAttribute(Constants.CURRENCY, saved.getSelected());
		return saved;
	}

	@GetMapping("/auth/customer/currency")
	@ApiOperation(httpMethod = "GET", value = "Get currency used by a logged in shopper and the currencies available for that store", notes = "Requires authentication", produces = "application/json", response = ReadableCurrencySelection.class)
	@ApiImplicitParams({ @ApiImplicitParam(name = "store", dataType = "string", defaultValue = "DEFAULT") })
	public ReadableCurrencySelection getAuthCustomerCurrency(@ApiIgnore MerchantStore merchantStore,
			HttpServletRequest request) {
		return currencyFacade.getCurrencySelection(merchantStore, userName(request), request.getLocale(),
				selectedFromSession(request));
	}

	@PatchMapping("/auth/customer/currency")
	@ApiOperation(httpMethod = "PATCH", value = "Set currency used by a logged in shopper, persisted on the shopper profile", notes = "Requires authentication", produces = "application/json", response = ReadableCurrencySelection.class)
	@ApiImplicitParams({ @ApiImplicitParam(name = "store", dataType = "string", defaultValue = "DEFAULT") })
	public ReadableCurrencySelection saveAuthCustomerCurrency(@ApiIgnore MerchantStore merchantStore,
			@Valid @RequestBody PersistableCurrencySelection selection, HttpServletRequest request) {
		ReadableCurrencySelection saved = currencyFacade.saveCurrencySelection(selection.getCode(),
				merchantStore, userName(request), request.getLocale());
		request.getSession().setAttribute(Constants.CURRENCY, saved.getSelected());
		return saved;
	}

	private String userName(HttpServletRequest request) {
		Principal principal = request.getUserPrincipal();
		return principal != null ? principal.getName() : null;
	}

	private String selectedFromSession(HttpServletRequest request) {
		return (String) request.getSession().getAttribute(Constants.CURRENCY);
	}
}
