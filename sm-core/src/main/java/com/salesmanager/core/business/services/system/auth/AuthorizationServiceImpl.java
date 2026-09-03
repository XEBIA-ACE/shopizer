```java
package com.salesmanager.core.business.services.system.auth;

import com.salesmanager.core.model.user.User;
import com.salesmanager.core.model.user.Group;

public class AuthorizationServiceImpl implements AuthorizationService {

    @Override
    public boolean hasRole(User user, String role) {
        for (Group group : user.getGroups()) {
            if (group.getGroupName().equals(role)) {
                return true;
            }
        }
        return false;
    }
}
```