package com.salesmanager.core.business.services.customer;

import com.salesmanager.core.model.customer.UserLanguagePreference;

import java.util.Optional;

/**
 * Service interface for managing {@link UserLanguagePreference} entities.
 *
 * Provides CRUD operations so that callers (REST controllers, batch jobs, etc.)
 * are decoupled from the persistence layer.
 */
public interface UserLanguagePreferenceService {

    /**
     * Persist a new language/locale preference for a customer.
     * Throws {@link IllegalStateException} if a preference already exists for
     * the given customer — use {@link #update} in that case.
     *
     * @param preference the preference to create (customerId must be set)
     * @return the saved entity with its generated {@code id}
     */
    UserLanguagePreference create(UserLanguagePreference preference);

    /**
     * Retrieve the preference for a given customer.
     *
     * @param customerId the customer's primary key
     * @return an {@link Optional} containing the preference, or empty if none exists
     */
    Optional<UserLanguagePreference> findByCustomerId(Long customerId);

    /**
     * Update an existing preference.
     * Throws {@link javax.persistence.EntityNotFoundException} if no row exists
     * for the given customer.
     *
     * @param preference the preference with updated fields (customerId must be set)
     * @return the updated entity
     */
    UserLanguagePreference update(UserLanguagePreference preference);

    /**
     * Create or update the preference for a customer in a single call.
     * Useful when the caller does not know whether a row already exists.
     *
     * @param preference the preference to save or update
     * @return the saved/updated entity
     */
    UserLanguagePreference saveOrUpdate(UserLanguagePreference preference);

    /**
     * Delete the preference row for a given customer.
     * No-op if no row exists.
     *
     * @param customerId the customer's primary key
     */
    void deleteByCustomerId(Long customerId);
}
