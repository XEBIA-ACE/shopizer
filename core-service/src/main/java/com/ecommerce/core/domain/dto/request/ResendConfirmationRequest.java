package com.ecommerce.core.domain.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResendConfirmationRequest(
        @NotBlank(message = "Email is required")
        @Size(max = 320, message = "Email must be at most 320 characters")
        @Email(regexp = ValidationPatterns.EMAIL, message = "Email must be a valid email address")
        String email) {

    public ResendConfirmationRequest {
        email = email == null ? null : email.trim();
    }
}
