package com.salesmanager.shop.store.api.v1.references;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.salesmanager.shop.constants.Constants;
import com.salesmanager.shop.model.references.PersistableCurrencySupport;
import com.salesmanager.shop.model.references.ReadableCurrency;
import com.salesmanager.shop.store.controller.currency.facade.CurrencyFacade;
import com.salesmanager.shop.store.controller.user.facade.UserFacade;
import com.salesmanager.shop.utils.AuthorizationUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;

/**
 * Administration of storefront currencies. Only SUPERADMIN / ADMIN users may
 * change which currencies are supported; the public list in ReferencesApi
 * reflects these changes immediately.
 */
@RestController
@RequestMapping("/api/v1")
@Api(tags = { "Currency management resource (Currency Management Api)" })
@SwaggerDefinition(tags = {
    @Tag(name = "Currency management resource", description = "Manage supported storefront currencies") })
public class CurrencyApi {

  private static final Logger LOGGER = LoggerFactory.getLogger(CurrencyApi.class);

  private static final List<String> MANAGER_ROLES =
      Arrays.asList(Constants.GROUP_SUPERADMIN, Constants.GROUP_ADMIN);

  @Inject private CurrencyFacade currencyFacade;

  @Inject private UserFacade userFacade;

  @Inject private AuthorizationUtils authorizationUtils;

  @GetMapping(value = "/private/currency", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(httpMethod = "GET", value = "List all currencies with their supported flag",
      notes = "Requires SUPERADMIN or ADMIN role", response = ReadableCurrency.class, responseContainer = "List")
  public List<ReadableCurrency> listAll() {
    authorize();
    return currencyFacade.getAll();
  }

  @PutMapping(value = "/private/currency/{code}/supported",
      consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(httpMethod = "PUT", value = "Enable or disable a currency on the storefront",
      notes = "Requires SUPERADMIN or ADMIN role", response = ReadableCurrency.class)
  public ReadableCurrency setSupported(
      @PathVariable String code, @Valid @RequestBody PersistableCurrencySupport support) {
    String user = authorize();
    ReadableCurrency currency = currencyFacade.setSupported(code, support.getSupported());
    LOGGER.info("User [{}] set currency [{}] supported=[{}]", user, code, support.getSupported());
    return currency;
  }

  private String authorize() {
    String authenticatedUser = authorizationUtils.authenticatedUser();
    userFacade.authorizedGroup(authenticatedUser, MANAGER_ROLES);
    return authenticatedUser;
  }
}
