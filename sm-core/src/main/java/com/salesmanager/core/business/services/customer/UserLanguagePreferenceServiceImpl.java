package com.salesmanager.core.business.services.customer;

import com.salesmanager.core.business.repositories.customer.UserLanguagePreferenceRepository;
import com.salesmanager.core.model.customer.UserLanguagePreference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

/**
 * Default implementation of {@link UserLanguagePreferenceService}.
 *
 * All mutating operations run inside a transaction.
 * Read operations are marked read-only for performance.
 */
@Service
public class UserLanguagePreferenceServiceImpl implements UserLanguagePreferenceService {

    private final UserLanguagePreferenceRepository repository;

    public UserLanguagePreferenceServiceImpl(UserLanguagePreferenceRepository repository) {
        this.repository = repository;
    }

    // -------------------------------------------------------------------------
    // Create
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public UserLanguagePreference create(UserLanguagePreference preference) {
        if (preference.getCustomerId() == null) {
            throw new IllegalArgumentException("customerId must not be null");
        }
        if (repository.existsByCustomerId(preference.getCustomerId())) {
            throw new IllegalStateException(
                "A language preference already exists for customerId=" + preference.getCustomerId()
                + ". Use update() or saveOrUpdate() instead."
            );
        }
        return repository.save(preference);
    }

    // -------------------------------------------------------------------------
    // Read
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Optional<UserLanguagePreference> findByCustomerId(Long customerId) {
        return repository.findByCustomerId(customerId);
    }

    // -------------------------------------------------------------------------
    // Update
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public UserLanguagePreference update(UserLanguagePreference preference) {
        if (preference.getCustomerId() == null) {
            throw new IllegalArgumentException("customerId must not be null");
        }
        UserLanguagePreference existing = repository.findByCustomerId(preference.getCustomerId())
            .orElseThrow(() -> new EntityNotFoundException(
                "No language preference found for customerId=" + preference.getCustomerId()
            ));

        existing.setLanguageTag(preference.getLanguageTag());
        existing.setLocale(preference.getLocale());
        return repository.save(existing);
    }

    // -------------------------------------------------------------------------
    // Save-or-update (upsert)
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public UserLanguagePreference saveOrUpdate(UserLanguagePreference preference) {
        if (preference.getCustomerId() == null) {
            throw new IllegalArgumentException("customerId must not be null");
        }
        Optional<UserLanguagePreference> existing =
            repository.findByCustomerId(preference.getCustomerId());

        if (existing.isPresent()) {
            UserLanguagePreference toUpdate = existing.get();
            toUpdate.setLanguageTag(preference.getLanguageTag());
            toUpdate.setLocale(preference.getLocale());
            return repository.save(toUpdate);
        }
        return repository.save(preference);
    }

    // -------------------------------------------------------------------------
    // Delete
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public void deleteByCustomerId(Long customerId) {
        if (repository.existsByCustomerId(customerId)) {
            repository.deleteByCustomerId(customerId);
        }
        // No-op if not found — idempotent delete
    }
}
