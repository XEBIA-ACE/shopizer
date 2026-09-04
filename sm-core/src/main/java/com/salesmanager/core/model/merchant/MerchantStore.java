package com.salesmanager.core.model.merchant;

import com.salesmanager.core.model.reference.currency.Currency;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a merchant storefront in the Shopizer multi-tenant platform.
 *
 * <p>Each store has a unique {@code code}, a {@code defaultCurrency} (the base currency
 * used for fallback), and an optional set of {@code currencies} representing all
 * currencies enabled for that storefront.
 */
@Entity
@Table(name = "MERCHANT_STORE")
public class MerchantStore implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MERCHANT_ID")
    private Long id;

    @Column(name = "STORE_CODE", unique = true, nullable = false, length = 100)
    private String code;

    @Column(name = "STORE_NAME", length = 200)
    private String storeName;

    /**
     * The store's base / default currency.
     * Used as the fallback when a session currency is unsupported.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "CURRENCY_ID")
    private Currency currency;

    /**
     * All currencies enabled for this storefront.
     * When empty, only the {@link #currency} (base currency) is available.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "STORE_CURRENCY",
            joinColumns = @JoinColumn(name = "MERCHANT_ID"),
            inverseJoinColumns = @JoinColumn(name = "CURRENCY_ID")
    )
    private Set<Currency> currencies = new HashSet<>();

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public MerchantStore() {
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

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public Set<Currency> getCurrencies() {
        return currencies;
    }

    public void setCurrencies(Set<Currency> currencies) {
        this.currencies = currencies;
    }

    @Override
    public String toString() {
        return "MerchantStore{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", storeName='" + storeName + '\'' +
                '}';
    }
}