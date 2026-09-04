package com.salesmanager.core.business.repositories.merchant;

import com.salesmanager.core.model.merchant.MerchantStore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MerchantStoreRepository extends JpaRepository<MerchantStore, Long> {

    /**
     * Finds a {@link MerchantStore} by its unique store code.
     *
     * @param code the store code
     * @return an {@link Optional} containing the store, or empty if not found
     */
    Optional<MerchantStore> findByCode(String code);

    /**
     * Finds a {@link MerchantStore} by its store code, eagerly fetching the
     * associated currencies collection to avoid lazy-loading issues outside a
     * transaction.
     *
     * @param code the store code
     * @return an {@link Optional} containing the store with currencies loaded, or empty
     */
    @Query("SELECT s FROM MerchantStore s LEFT JOIN FETCH s.currencies WHERE s.code = :code")
    Optional<MerchantStore> findByCodeWithCurrencies(@Param("code") String code);
}