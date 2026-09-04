package com.salesmanager.shop.store.controller.order;

import com.salesmanager.core.business.services.catalog.product.PricingService;
import com.salesmanager.core.business.services.customer.CustomerService;
import com.salesmanager.core.business.services.merchant.MerchantStoreService;
import com.salesmanager.core.business.services.merchant.StoreCurrencyService;
import com.salesmanager.core.business.services.order.OrderService;
import com.salesmanager.core.business.services.reference.language.LanguageService;
import com.salesmanager.core.business.services.shoppingcart.ShoppingCartService;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.shop.model.store.CurrencyValidationResult;
import com.salesmanager.shop.store.controller.AbstractController;
import com.salesmanager.shop.store.controller.customer.facade.CustomerFacade;
import com.salesmanager.shop.store.controller.order.facade.OrderFacade;
import com.salesmanager.shop.utils.GeoLocationUtils;
import com.salesmanager.shop.utils.LabelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Controller responsible for rendering the checkout start page and related
 * checkout flow pages.
 *
 * <p>TASK-009: On checkout entry the session currency is validated against the
 * store's enabled currency list via {@link StoreCurrencyService}. If the session
 * carries an unsupported currency the session attribute is updated to the store
 * base currency and the view model receives {@code currencyFallbackApplied=true}
 * and {@code fallbackCurrencyCode} so the Thymeleaf template can render the
 * informational banner (AC-04).</p>
 */
@Controller
@RequestMapping("/shop/order")
public class ShoppingOrderController extends AbstractController {

    private static final Logger LOG = LoggerFactory.getLogger(ShoppingOrderController.class);

    /** Session attribute key used throughout Shopizer for the active currency. */
    private static final String SESSION_ATTRIBUTE_CURRENCY = "currency";

    @Inject
    private MerchantStoreService merchantStoreService;

    @Inject
    private LanguageService languageService;

    @Inject
    private CustomerFacade customerFacade;

    @Inject
    private OrderFacade orderFacade;

    @Inject
    private ShoppingCartService shoppingCartService;

    @Inject
    private CustomerService customerService;

    @Inject
    private PricingService pricingService;

    @Inject
    private OrderService orderService;

    @Inject
    private LabelUtils messages;

    @Inject
    private GeoLocationUtils geoLocationUtils;

    /**
     * TASK-009: Injected to validate the session currency against the store's
     * enabled currency list on checkout entry.
     */
    @Inject
    private StoreCurrencyService storeCurrencyService;

    // -------------------------------------------------------------------------
    // Checkout start handler
    // -------------------------------------------------------------------------

    /**
     * Renders the checkout start page.
     *
     * <p>Validates the session currency against the store's enabled list. If the
     * session currency is unsupported the session is updated to the store base
     * currency and the fallback flag is surfaced to the view model so the template
     * can render the informational banner (AC-04).</p>
     *
     * @param model    Spring MVC model
     * @param request  HTTP request
     * @param response HTTP response
     * @param session  HTTP session
     * @return view name for the checkout start page
     */
    @RequestMapping(value = "/checkout", method = RequestMethod.GET)
    public String displayCheckout(
            Model model,
            HttpServletRequest request,
            HttpServletResponse response,
            HttpSession session) {

        try {
            // Resolve MerchantStore from server-side context (never from a query parameter —
            // FR-05 / NFR-03: storeCode must come from a trusted server-side source).
            MerchantStore store = getSessionAttribute(MerchantStore.class, request);
            Language language = getSessionAttribute(Language.class, request);

            if (store == null) {
                LOG.warn("No MerchantStore found in session context; redirecting to home.");
                return "redirect:/shop";
            }

            String storeCode = store.getCode();

            // ----------------------------------------------------------------
            // TASK-009: Session currency validation (AC-04)
            // ----------------------------------------------------------------
            String sessionCurrency = (String) session.getAttribute(SESSION_ATTRIBUTE_CURRENCY);

            CurrencyValidationResult result =
                    storeCurrencyService.validateSessionCurrency(storeCode, sessionCurrency);

            if (result.isFallbackApplied()) {
                // Update the session so subsequent requests within the same session do not
                // trigger repeated fallback (NFR-05).
                session.setAttribute(SESSION_ATTRIBUTE_CURRENCY, result.getResolvedCurrencyCode());

                // Surface fallback information to the Thymeleaf template (AC-04).
                model.addAttribute("currencyFallbackApplied", true);
                model.addAttribute("fallbackCurrencyCode", result.getResolvedCurrencyCode());

                LOG.info("Session currency fallback applied for store [{}]: resolved to [{}]",
                        storeCode, result.getResolvedCurrencyCode());
            }

            // ----------------------------------------------------------------
            // Expose storeCode to the view so the frontend JS can call the
            // currencies API: GET /api/v1/store/{storeCode}/currencies
            // ----------------------------------------------------------------
            model.addAttribute("storeCode", storeCode);

            // ----------------------------------------------------------------
            // Existing checkout model population (unchanged)
            // ----------------------------------------------------------------
            model.addAttribute("store", store);
            if (language != null) {
                model.addAttribute("language", language.getCode());
            }

            return "order.checkout";

        } catch (Exception e) {
            LOG.error("Error during checkout start for request [{}]", request.getRequestURI(), e);
            return "redirect:/shop";
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Retrieves a typed attribute from the request (covers both request attributes
     * and session attributes set by upstream filters/interceptors in Shopizer).
     */
    @SuppressWarnings("unchecked")
    private <T> T getSessionAttribute(Class<T> type, HttpServletRequest request) {
        // Shopizer stores MerchantStore and Language as request attributes set by
        // the StoreFilter / LanguageFilter interceptors.
        Object attribute = request.getAttribute(type.getSimpleName());
        if (attribute != null && type.isInstance(attribute)) {
            return type.cast(attribute);
        }
        // Fallback: check the HTTP session directly
        Object sessionAttr = request.getSession(false) != null
                ? request.getSession(false).getAttribute(type.getSimpleName())
                : null;
        if (sessionAttr != null && type.isInstance(sessionAttr)) {
            return type.cast(sessionAttr);
        }
        return null;
    }
}