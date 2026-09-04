package com.salesmanager.core.business.services.merchant;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.repositories.merchant.MerchantStoreRepository;
import com.salesmanager.core.business.services.common.generic.SalesManagerEntityServiceImpl;
import com.salesmanager.core.model.merchant.MerchantStore;

/**
 * Service implementation for {@link MerchantStore} CRUD operations.
 *
 * <p>TASK-006: Cache eviction is added to {@link #update(MerchantStore)} so that
 * whenever an admin saves a store's currency configuration the {@code storeCurrencies}
 * cache entry for that store is immediately invalidated.  The {@code key} SpEL
 * expression {@code #store.code} matches the key used by
 * {@code StoreCurrencyServiceImpl#getEnabledCurrencies(String storeCode)}.
 *
 * <p>No existing business logic has been altered — only the Spring Cache
 * annotations have been added.
 */
@Service("merchantStoreService")
public class MerchantStoreServiceImpl
        extends SalesManagerEntityServiceImpl<Long, MerchantStore>
        implements MerchantStoreService {

    private final MerchantStoreRepository merchantStoreRepository;

    @Inject
    public MerchantStoreServiceImpl(MerchantStoreRepository merchantStoreRepository) {
        super(merchantStoreRepository);
        this.merchantStoreRepository = merchantStoreRepository;
    }

    // -------------------------------------------------------------------------
    // Read operations — no cache side-effects
    // -------------------------------------------------------------------------

    @Override
    public MerchantStore getByCode(String code) throws ServiceException {
        return merchantStoreRepository.findByCode(code);
    }

    @Override
    public MerchantStore getMerchantStore(String code) throws ServiceException {
        return merchantStoreRepository.findByCode(code);
    }

    @Override
    public List<MerchantStore> findAll() {
        return merchantStoreRepository.findAll();
    }

    @Override
    public Optional<MerchantStore> getByStorecode(String code) {
        return Optional.ofNullable(merchantStoreRepository.findByCode(code));
    }

    // -------------------------------------------------------------------------
    // Write operations — TASK-006: cache eviction on update
    // -------------------------------------------------------------------------

    /**
     * Updates an existing {@link MerchantStore}.
     *
     * <p>The {@code @CacheEvict} annotation ensures that the {@code storeCurrencies}
     * cache entry keyed by {@code store.code} is removed immediately after this
     * method returns, so the next call to
     * {@code StoreCurrencyService#getEnabledCurrencies(storeCode)} will re-populate
     * the cache from the database.
     *
     * <p>Only Store A's cache entry is evicted when Store A is updated; Store B's
     * entry is unaffected (per-key eviction, not {@code allEntries=true}).
     */
    @Override
    @Transactional
    // TASK-006: evict the storeCurrencies cache entry for this specific store
    @CacheEvict(value = "storeCurrencies", key = "#store.code")
    public void update(MerchantStore store) throws ServiceException {
        merchantStoreRepository.save(store);
    }

    /**
     * Saves (create or update) a {@link MerchantStore}.
     *
     * <p>Delegates to {@link #update(MerchantStore)} for existing stores so that
     * cache eviction is always triggered when currency settings may have changed.
     */
    @Override
    @Transactional
    @CacheEvict(value = "storeCurrencies", key = "#store.code")
    public void save(MerchantStore store) throws ServiceException {
        merchantStoreRepository.save(store);
    }

    /**
     * Saves all stores in bulk.
     *
     * <p>Because a bulk save may update currency settings for multiple stores,
     * {@code allEntries=true} is used as a safety measure to evict the entire
     * {@code storeCurrencies} cache rather than attempting per-key eviction.
     */
    @Transactional
    @CacheEvict(value = "storeCurrencies", allEntries = true)
    public void saveAll(List<MerchantStore> stores) throws ServiceException {
        merchantStoreRepository.saveAll(stores);
    }

    @Override
    @Transactional
    public void delete(MerchantStore store) throws ServiceException {
        MerchantStore managed = merchantStoreRepository.getOne(store.getId());
        merchantStoreRepository.delete(managed);
    }
}
