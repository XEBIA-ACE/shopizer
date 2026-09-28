package com.ecommerce.core.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        Verification verification,
        Resend resend,
        Session session,
        Jwt jwt,
        Notification notification,
        Security security) {

    public record Verification(Duration tokenTtl, String linkBaseUrl) {
    }

    public record Resend(Duration cooldown, int maxAttempts) {
    }

    public record Session(Duration ttl, String redirectLocation) {
    }

    public record Jwt(String issuer, String privateKeyPath, String publicKeyPath) {
    }

    public record Notification(String baseUrl, String fromAddress, Duration timeout) {
    }

    public record Security(int bcryptStrength) {
    }
}
