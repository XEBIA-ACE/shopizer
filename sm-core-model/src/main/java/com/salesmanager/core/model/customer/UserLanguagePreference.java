package com.salesmanager.core.model.customer;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * Stores a customer's preferred language tag (BCP-47, e.g. "en", "fr") and
 * locale string (IETF, e.g. "en-US", "fr-FR").
 *
 * The table is additive — no existing columns are modified.
 */
@Entity
@Table(
    name = "USER_LANGUAGE_PREFERENCE",
    uniqueConstraints = @UniqueConstraint(
        name = "UQ_LANG_PREF_CUSTOMER",
        columnNames = "CUSTOMER_ID"
    )
)
public class UserLanguagePreference implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    /**
     * Owning customer. One preference row per customer (1-to-1 from this side).
     * Declared as a simple FK column to avoid circular dependency on the full
     * Customer entity graph; callers resolve the Customer separately.
     */
    @Column(name = "CUSTOMER_ID", nullable = false, unique = true)
    @NotNull
    private Long customerId;

    /**
     * BCP-47 language tag, e.g. "en", "fr", "zh-Hans".
     * Defaults to "en" so existing rows without a preference still resolve.
     */
    @Column(name = "LANGUAGE_TAG", nullable = false, length = 20)
    @NotNull
    @Size(min = 2, max = 20)
    private String languageTag = "en";

    /**
     * IETF locale string, e.g. "en-US", "fr-FR".
     * Drives date/number formatting on the front-end.
     */
    @Column(name = "LOCALE", nullable = false, length = 20)
    @NotNull
    @Size(min = 2, max = 20)
    private String locale = "en-US";

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public UserLanguagePreference() {
    }

    public UserLanguagePreference(Long customerId, String languageTag, String locale) {
        this.customerId = customerId;
        this.languageTag = languageTag;
        this.locale = locale;
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getLanguageTag() {
        return languageTag;
    }

    public void setLanguageTag(String languageTag) {
        this.languageTag = languageTag;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    @Override
    public String toString() {
        return "UserLanguagePreference{" +
               "id=" + id +
               ", customerId=" + customerId +
               ", languageTag='" + languageTag + '\'' +
               ", locale='" + locale + '\'' +
               '}';
    }
}
