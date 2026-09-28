package com.ecommerce.core.service;

import com.ecommerce.core.config.AppProperties;
import com.ecommerce.core.notification.EmailMessage;
import com.ecommerce.core.notification.NotificationClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class ConfirmationEmailService {

    static final String SUBJECT = "Confirm your email address";

    private final NotificationClient notificationClient;
    private final AppProperties properties;

    public void send(String emailAddress, String rawToken) {
        String link = UriComponentsBuilder.fromUriString(properties.verification().linkBaseUrl())
                .queryParam("token", rawToken)
                .encode(StandardCharsets.UTF_8)
                .toUriString();
        Duration ttl = properties.verification().tokenTtl();
        String body = "Welcome! Please confirm your email address to activate your account:\n\n"
                + link + "\n\nThis link expires in " + ttl.toHours() + " hours and can only be used once.";
        notificationClient.sendEmail(new EmailMessage(emailAddress, SUBJECT, body));
    }
}
