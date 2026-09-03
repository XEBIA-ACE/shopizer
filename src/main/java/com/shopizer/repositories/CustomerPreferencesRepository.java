```java
package com.shopizer.repositories;

import com.shopizer.entities.CustomerPreferences;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CustomerPreferencesRepository extends JpaRepository<CustomerPreferences, Long> {
    Optional<CustomerPreferences> findByCustomerId(Long customerId);
}
```