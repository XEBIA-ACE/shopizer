package com.salesmanager.core.model.reference.currency;

import javax.persistence.*;
import java.io.Serializable;

/**
 * ISO 4217 currency entity.
 *
 * <p>Stores the currency code (e.g., "USD") and a flag indicating whether
 * the currency is supported/enabled globally. Per-store enablement is managed
 * via the {@code MerchantStore.currencies} association.
 */
@Entity
@Table(name = "CURRENCY")
public class Currency implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CURRENCY_ID")
    private Long id;

    /**
     * ISO 4217 currency code, e.g. "USD", "EUR", "JPY".
     */
    @Column(name = "CURRENCY_CODE", unique = true, nullable = false, length = 3)
    private String code;

    /**
     * Whether this currency is globally supported in the platform.
     */
    @Column(name = "SUPPORTED")
    private boolean supported = true;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public Currency() {
    }

    public Currency(String code) {
        this.code = code;
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean isSupported() {
        return supported;
    }

    public void setSupported(boolean supported) {
        this.supported = supported;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Currency)) return false;
        Currency currency = (Currency) o;
        return code != null && code.equals(currency.code);
    }

    @Override
    public int hashCode() {
        return code != null ? code.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Currency{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", supported=" + supported +
                '}';
    }
}