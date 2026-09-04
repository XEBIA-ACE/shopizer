package com.salesmanager.shop.store.controller.store;

import com.salesmanager.core.business.services.merchant.StoreCurrencyService;
import com.salesmanager.core.business.exception.StoreNotFoundException;
import com.salesmanager.shop.model.store.CheckoutCurrencyDTO;
import com.salesmanager.shop.model.store.StoreCurrencyResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Public REST endpoint that exposes the per-store enabled currency list.
 *
 * <p>Security: storeCode is resolved exclusively from the path variable (FR-05 / NFR-03).
 * No query-parameter override is provided to prevent cross-store data leakage (NFR-08).
 *
 * <p>Authentication: endpoint is intentionally public — no @PreAuthorize annotation.
 * Rate-limiting is delegated to the existing application-level filter if present.
 */
@RestController
@RequestMapping("/api/v1/store")
public class StoreCurrencyController {

    private final StoreCurrencyService storeCurrencyService;

    /**
     * Constructor injection — preferred over field injection for testability.
     *
     * @param storeCurrencyService service that resolves enabled currencies per store
     */
    public StoreCurrencyController(StoreCurrencyService storeCurrencyService) {
        this.storeCurrencyService = storeCurrencyService;
    }

    /**
     * Returns the list of currencies enabled for the given store.
     *
     * <p>AC-01 / AC-02: returns HTTP 200 with {@code {"storeCode":"...","currencies":[...]}}
     * for a valid store.
     * <p>AC-05 / NFR-08: response is scoped strictly to the requested store; no cross-store
     * data can appear because the service layer filters by storeCode.
     *
     * @param storeCode store identifier taken from the URL path only (never a query param)
     * @return 200 OK with currency list, 400 Bad Request for blank storeCode,
     *         404 Not Found when the store does not exist
     */
    @GetMapping("/{storeCode}/currencies")
    public ResponseEntity<StoreCurrencyResponse> getEnabledCurrencies(
            @PathVariable String storeCode) {

        // AC: Returns HTTP 400 when storeCode path variable is blank or null.
        // Spring MVC will not bind a null path variable, but the blank check guards
        // against an empty-string segment if routing ever allows it.
        if (storeCode == null || storeCode.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            // Delegate to the service; result is already scoped to this storeCode only.
            List<CheckoutCurrencyDTO> currencies =
                    storeCurrencyService.getEnabledCurrencies(storeCode);

            StoreCurrencyResponse response = new StoreCurrencyResponse();
            response.setStoreCode(storeCode);
            response.setCurrencies(currencies);

            return ResponseEntity.ok(response);

        } catch (StoreNotFoundException e) {
            // AC: Returns HTTP 404 when storeCode does not correspond to a known store.
            // Using 404 (not 403) to avoid store-existence enumeration (NFR-08).
            return ResponseEntity.notFound().build();
        }
    }
}
