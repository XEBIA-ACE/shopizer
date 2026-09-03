package com.salesmanager.shop.store.api.v1.store;

import com.salesmanager.shop.store.controller.store.facade.StoreCurrencyFacade;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * REST API for admin-configurable currency management on a merchant store.
 *
 * <p>All endpoints require the caller to hold the {@code ADMIN} role
 * (enforced via Spring Security's {@code @PreAuthorize}).
 *
 * <pre>
 * GET    /api/v1/store/{storeCode}/currencies          – list supported currencies
 * POST   /api/v1/store/{storeCode}/currencies/{code}   – add a currency
 * DELETE /api/v1/store/{storeCode}/currencies/{code}   – remove a currency
 * </pre>
 */
@RestController
@RequestMapping("/api/v1")
@Api(tags = {"Store Currency Management"})
public class StoreCurrencyApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(StoreCurrencyApi.class);

    @Inject
    private StoreCurrencyFacade storeCurrencyFacade;

    // -----------------------------------------------------------------------
    // GET – list supported currencies
    // -----------------------------------------------------------------------

    @GetMapping(value = "/store/{storeCode}/currencies",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(
            value = "List currencies supported by a store",
            notes = "Returns the ISO 4217 codes of all currencies currently enabled for the store.")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<String>> getSupportedCurrencies(
            @ApiParam(value = "Unique store code", required = true)
            @PathVariable String storeCode,
            HttpServletRequest request) {

        try {
            List<String> currencies = storeCurrencyFacade.getSupportedCurrencies(storeCode);
            return ResponseEntity.ok(currencies);
        } catch (Exception e) {
            LOGGER.error("Error retrieving currencies for store {}: {}", storeCode, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // -----------------------------------------------------------------------
    // POST – add a currency
    // -----------------------------------------------------------------------

    @PostMapping(value = "/store/{storeCode}/currencies/{currencyCode}")
    @ApiOperation(
            value = "Add a currency to a store",
            notes = "Adds the specified ISO 4217 currency to the store's supported-currency list "
                    + "and triggers a real-time update to the storefront.")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> addCurrency(
            @ApiParam(value = "Unique store code", required = true)
            @PathVariable String storeCode,
            @ApiParam(value = "ISO 4217 currency code to add (e.g. USD, EUR)", required = true)
            @PathVariable String currencyCode,
            HttpServletRequest request) {

        try {
            storeCurrencyFacade.addCurrency(storeCode, currencyCode.toUpperCase());
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (IllegalArgumentException | com.salesmanager.core.business.exception.ServiceException e) {
            LOGGER.warn("Bad request adding currency {} to store {}: {}", currencyCode, storeCode, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            LOGGER.error("Error adding currency {} to store {}: {}", currencyCode, storeCode, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // -----------------------------------------------------------------------
    // DELETE – remove a currency
    // -----------------------------------------------------------------------

    @DeleteMapping(value = "/store/{storeCode}/currencies/{currencyCode}")
    @ApiOperation(
            value = "Remove a currency from a store",
            notes = "Removes the specified ISO 4217 currency from the store's supported-currency list "
                    + "and triggers a real-time update to the storefront. "
                    + "The store's primary currency cannot be removed.")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removeCurrency(
            @ApiParam(value = "Unique store code", required = true)
            @PathVariable String storeCode,
            @ApiParam(value = "ISO 4217 currency code to remove (e.g. USD, EUR)", required = true)
            @PathVariable String currencyCode,
            HttpServletRequest request) {

        try {
            storeCurrencyFacade.removeCurrency(storeCode, currencyCode.toUpperCase());
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException | com.salesmanager.core.business.exception.ServiceException e) {
            LOGGER.warn("Bad request removing currency {} from store {}: {}", currencyCode, storeCode, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            LOGGER.error("Error removing currency {} from store {}: {}", currencyCode, storeCode, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
