```java
package com.salesmanager.shop.utils;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;

@SpringBootTest
@WebAppConfiguration
public abstract class IntegrationTestUtil {

    @Autowired
    protected WebApplicationContext wac;

    protected MockMvc mockMvc;

    @PostConstruct
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    protected MockMvc authenticatedRequest(String url) throws Exception {
        return mockMvc.perform(buildAuthenticatedHttpRequest(url));
    }

    // Add helper methods for authenticated requests, json parsing, etc.
}
```