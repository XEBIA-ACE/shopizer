package com.ecommerce.core.config;

import com.ecommerce.core.notification.HttpNotificationClient;
import com.ecommerce.core.notification.LoggingNotificationClient;
import com.ecommerce.core.notification.NotificationClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Configuration
public class NotificationConfig {

    @Bean
    public NotificationClient notificationClient(AppProperties properties, RestClient.Builder builder) {
        AppProperties.Notification notification = properties.notification();
        if (!StringUtils.hasText(notification.baseUrl())) {
            return new LoggingNotificationClient();
        }
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(notification.timeout());
        requestFactory.setReadTimeout(notification.timeout());
        RestClient restClient = builder
                .baseUrl(notification.baseUrl())
                .requestFactory(requestFactory)
                .build();
        return new HttpNotificationClient(restClient, notification.fromAddress());
    }
}
