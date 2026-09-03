package com.salesmanager.core.business.services.customer;

import com.salesmanager.core.business.repositories.customer.UserLanguagePreferenceRepository;
import com.salesmanager.core.model.customer.UserLanguagePreference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link UserLanguagePreferenceServiceImpl}.
 *
 * All persistence calls are mocked — no database is required.
 * Covers every CRUD operation and the main error paths.
 */
@ExtendWith(MockitoExtension.class)
class UserLanguagePreferenceServiceImplTest {

    @Mock
    private UserLanguagePreferenceRepository repository;

    @InjectMocks
    private UserLanguagePreferenceServiceImpl service;

    private static final Long CUSTOMER_ID = 42L;

    private UserLanguagePreference buildPreference(String lang, String locale) {
        UserLanguagePreference pref = new UserLanguagePreference();
        pref.setCustomerId(CUSTOMER_ID);
        pref.setLanguageTag(lang);
        pref.setLocale(locale);
        return pref;
    }

    // =========================================================================
    // CREATE
    // =========================================================================

    @Test
    @DisplayName("create() persists and returns the saved preference")
    void create_persistsPreference() {
        UserLanguagePreference input = buildPreference("fr", "fr-FR");
        UserLanguagePreference saved  = buildPreference("fr", "fr-FR");
        saved.setId(1L);

        when(repository.existsByCustomerId(CUSTOMER_ID)).thenReturn(false);
        when(repository.save(input)).thenReturn(saved);

        UserLanguagePreference result = service.create(input);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getLanguageTag()).isEqualTo("fr");
        assertThat(result.getLocale()).isEqualTo("fr-FR");
        verify(repository).save(input);
    }

    @Test
    @DisplayName("create() throws IllegalStateException when preference already exists")
    void create_throwsWhenAlreadyExists() {
        UserLanguagePreference input = buildPreference("en", "en-US");
        when(repository.existsByCustomerId(CUSTOMER_ID)).thenReturn(true);

        assertThatThrownBy(() -> service.create(input))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("already exists");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("create() throws IllegalArgumentException when customerId is null")
    void create_throwsWhenCustomerIdNull() {
        UserLanguagePreference input = new UserLanguagePreference();
        // customerId intentionally left null

        assertThatThrownBy(() -> service.create(input))
            .isInstanceOf(IllegalArgumentException.class);
    }

    // =========================================================================
    // READ
    // =========================================================================

    @Test
    @DisplayName("findByCustomerId() returns the preference when it exists")
    void findByCustomerId_returnsPreference() {
        UserLanguagePreference stored = buildPreference("de", "de-DE");
        stored.setId(5L);

        when(repository.findByCustomerId(CUSTOMER_ID)).thenReturn(Optional.of(stored));

        Optional<UserLanguagePreference> result = service.findByCustomerId(CUSTOMER_ID);

        assertThat(result).isPresent();
        assertThat(result.get().getLanguageTag()).isEqualTo("de");
        assertThat(result.get().getLocale()).isEqualTo("de-DE");
    }

    @Test
    @DisplayName("findByCustomerId() returns empty Optional when no preference exists")
    void findByCustomerId_returnsEmptyWhenNotFound() {
        when(repository.findByCustomerId(CUSTOMER_ID)).thenReturn(Optional.empty());

        Optional<UserLanguagePreference> result = service.findByCustomerId(CUSTOMER_ID);

        assertThat(result).isEmpty();
    }

    // =========================================================================
    // UPDATE
    // =========================================================================

    @Test
    @DisplayName("update() modifies language and locale of existing preference")
    void update_modifiesExistingPreference() {
        UserLanguagePreference existing = buildPreference("en", "en-US");
        existing.setId(10L);

        UserLanguagePreference updateRequest = buildPreference("es", "es-ES");

        when(repository.findByCustomerId(CUSTOMER_ID)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        UserLanguagePreference result = service.update(updateRequest);

        assertThat(result.getLanguageTag()).isEqualTo("es");
        assertThat(result.getLocale()).isEqualTo("es-ES");
        verify(repository).save(existing);
    }

    @Test
    @DisplayName("update() throws EntityNotFoundException when no preference exists")
    void update_throwsWhenNotFound() {
        UserLanguagePreference updateRequest = buildPreference("es", "es-ES");
        when(repository.findByCustomerId(CUSTOMER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(updateRequest))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining(String.valueOf(CUSTOMER_ID));

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("update() throws IllegalArgumentException when customerId is null")
    void update_throwsWhenCustomerIdNull() {
        UserLanguagePreference input = new UserLanguagePreference();

        assertThatThrownBy(() -> service.update(input))
            .isInstanceOf(IllegalArgumentException.class);
    }

    // =========================================================================
    // SAVE-OR-UPDATE (upsert)
    // =========================================================================

    @Test
    @DisplayName("saveOrUpdate() creates a new preference when none exists")
    void saveOrUpdate_createsWhenAbsent() {
        UserLanguagePreference input = buildPreference("ja", "ja-JP");
        UserLanguagePreference saved  = buildPreference("ja", "ja-JP");
        saved.setId(20L);

        when(repository.findByCustomerId(CUSTOMER_ID)).thenReturn(Optional.empty());
        when(repository.save(input)).thenReturn(saved);

        UserLanguagePreference result = service.saveOrUpdate(input);

        assertThat(result.getId()).isEqualTo(20L);
        assertThat(result.getLanguageTag()).isEqualTo("ja");
    }

    @Test
    @DisplayName("saveOrUpdate() updates an existing preference when one exists")
    void saveOrUpdate_updatesWhenPresent() {
        UserLanguagePreference existing = buildPreference("en", "en-US");
        existing.setId(21L);

        UserLanguagePreference updateRequest = buildPreference("ko", "ko-KR");

        when(repository.findByCustomerId(CUSTOMER_ID)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        UserLanguagePreference result = service.saveOrUpdate(updateRequest);

        assertThat(result.getLanguageTag()).isEqualTo("ko");
        assertThat(result.getLocale()).isEqualTo("ko-KR");
    }

    @Test
    @DisplayName("saveOrUpdate() throws IllegalArgumentException when customerId is null")
    void saveOrUpdate_throwsWhenCustomerIdNull() {
        UserLanguagePreference input = new UserLanguagePreference();

        assertThatThrownBy(() -> service.saveOrUpdate(input))
            .isInstanceOf(IllegalArgumentException.class);
    }

    // =========================================================================
    // DELETE
    // =========================================================================

    @Test
    @DisplayName("deleteByCustomerId() removes the preference when it exists")
    void deleteByCustomerId_deletesWhenExists() {
        when(repository.existsByCustomerId(CUSTOMER_ID)).thenReturn(true);

        service.deleteByCustomerId(CUSTOMER_ID);

        verify(repository).deleteByCustomerId(CUSTOMER_ID);
    }

    @Test
    @DisplayName("deleteByCustomerId() is a no-op when no preference exists")
    void deleteByCustomerId_noOpWhenAbsent() {
        when(repository.existsByCustomerId(CUSTOMER_ID)).thenReturn(false);

        service.deleteByCustomerId(CUSTOMER_ID);

        verify(repository, never()).deleteByCustomerId(any());
    }
}
