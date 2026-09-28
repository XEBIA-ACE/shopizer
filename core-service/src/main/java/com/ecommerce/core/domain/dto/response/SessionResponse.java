package com.ecommerce.core.domain.dto.response;

import java.time.Instant;

public record SessionResponse(String sessionToken, Instant expiresAt) {
}
