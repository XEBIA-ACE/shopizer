package com.salesmanager.core.business.repositories.reference.currency;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.salesmanager.core.model.reference.currency.Currency;

/**
 * Spring Data JPA repository for {@link Currency}.
 *
 * <p>TASK-001: Verified and extended to support per-store currency lookups required by the
 * Currency Selector at Checkout Start feature (US-001).
 *
 * <ul>
 *   <li>{@link #findByCode(String)} — primary lookup by ISO 4217 currency code.</li>
 *   <li>{@link #findSupportedByMerchantStoreCode(String)} — fallback named query that mirrors
 *       the join defined on {@code MerchantStoreRepository} for use-cases where the caller
 *       already holds a {@code CurrencyRepository} reference.</li>
 * </ul>
 */
public interface CurrencyRepository extends JpaRepository<Currency, Long> {

    /**
     * Finds a {@link Currency} by its ISO 4217 currency code (e.g. {@code "USD"}, {@code "EUR"}).
     *
     * <p>This method satisfies the acceptance criterion: "CurrencyRepository.findByCode(String
     * code) exists and returns the correct Currency entity."
     *
     * @param code the ISO 4217 three-letter currency code; must not be {@code null}
     * @return an {@link Optional} containing the matching currency, or empty if not found
     */
    Optional<Currency> findByCode(String code);

    /**
     * Fallback query that returns all {@link Currency} entities that are:
     * <ol>
     *   <li>associated with the {@link com.salesmanager.core.model.merchant.MerchantStore}
     *       identified by {@code storeCode} via the {@code store_currency} join table, and</li>
     *   <li>marked as {@code supported = true}.</li>
     * </ol>
     *
     * <p>This mirrors {@code MerchantStoreRepository.findEnabledCurrenciesByStoreCode} and is
     * provided as a convenience for callers that hold a {@code CurrencyRepository} reference.
     * Both methods produce the same result set; prefer the one on {@code MerchantStoreRepository}
     * when the store entity is the primary aggregate root.
     *
     * @param storeCode the unique code of the target store
     * @return list of supported {@link Currency} entities for the given store; never {@code null}
     */
    @Query("SELECT c FROM MerchantStore ms JOIN ms.currencies c "
            + "WHERE ms.code = :storeCode AND c.supported = true")
    List<Currency> findSupportedByMerchantStoreCode(@Param("storeCode") String storeCode);
}
