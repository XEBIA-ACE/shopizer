```java
package com.salesmanager.core.business.services.system.auth;

import com.salesmanager.core.model.user.User;

public interface AuthorizationService {

    boolean hasRole(User user, String role);
}
```