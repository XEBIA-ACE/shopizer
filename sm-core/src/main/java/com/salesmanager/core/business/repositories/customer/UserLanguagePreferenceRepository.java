package com.salesmanager.core.business.repositories.customer;

import com.salesmanager.core.model.customer.UserLanguagePreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link UserLanguagePreference}.
 *
 * All CRUD operations are inherited from {@link JpaRepository}.
 * Custom finder methods are added for the most common access patterns.
 */
@Repository
public interface UserLanguagePreferenceRepository
        extends JpaRepository<UserLanguagePreference, Long> {

    /**
     * Retrieve the preference row for a given customer.
     *
     * @param customerId the customer's primary key
     * @return an {@link Optional} containing the preference, or empty if none exists
     */
    Optional<UserLanguagePreference> findByCustomerId(Long customerId);

    /**
     * Check whether a preference row already exists for a customer.
     *
     * @param customerId the customer's primary key
     * @return {@code true} if a row exists
     */
    boolean existsByCustomerId(Long customerId);

    /**
     * Remove the preference row for a given customer (used during account deletion).
     *
     * @param customerId the customer's primary key
     */
    void deleteByCustomerId(Long customerId);
}
