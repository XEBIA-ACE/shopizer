## MerchantStore Model – Required Change for US-002

The `MerchantStore` entity (`sm-core/src/main/java/com/salesmanager/core/model/merchant/MerchantStore.java`)
must be extended with a `supportedCurrencies` collection to support admin-configurable
multi-currency management.

### Fields to add

```java
import com.salesmanager.core.model.reference.currency.Currency;
import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

// Inside MerchantStore class body:

/**
 * The set of currencies that are enabled for display on this storefront.
 * Managed via StoreCurrencyFacade (add/remove with real-time event publishing).
 */
@ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
@JoinTable(
    name = "MERCHANT_STORE_CURRENCY",
    joinColumns        = @JoinColumn(name = "MERCHANT_STORE_ID"),
    inverseJoinColumns = @JoinColumn(name = "CURRENCY_ID")
)
private Set<Currency> supportedCurrencies = new HashSet<>();

public Set<Currency> getSupportedCurrencies() {
    return supportedCurrencies;
}

public void setSupportedCurrencies(Set<Currency> supportedCurrencies) {
    this.supportedCurrencies = supportedCurrencies;
}
```

### Database migration (Liquibase / Flyway)

```sql
CREATE TABLE MERCHANT_STORE_CURRENCY (
    MERCHANT_STORE_ID INT NOT NULL,
    CURRENCY_ID       INT NOT NULL,
    PRIMARY KEY (MERCHANT_STORE_ID, CURRENCY_ID),
    CONSTRAINT FK_MSC_STORE    FOREIGN KEY (MERCHANT_STORE_ID) REFERENCES MERCHANT_STORE(MERCHANT_STORE_ID),
    CONSTRAINT FK_MSC_CURRENCY FOREIGN KEY (CURRENCY_ID)       REFERENCES CURRENCY(CURRENCY_ID)
);
```

### Why this change is needed

`StoreCurrencyFacadeImpl` calls `store.getSupportedCurrencies()` and `store.setSupportedCurrencies()`
to maintain the per-store currency list.  Without this field the facade will not compile.
