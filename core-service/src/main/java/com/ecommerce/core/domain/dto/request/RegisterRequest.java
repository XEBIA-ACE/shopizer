package com.ecommerce.core.domain.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Email is required")
        @Size(max = 320, message = "Email must be at most 320 characters")
        @Email(regexp = ValidationPatterns.EMAIL, message = "Email must be a valid email address")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters")
        @Pattern(regexp = ValidationPatterns.PASSWORD,
                message = "Password must contain at least one uppercase letter and one digit")
        String password) {

    public RegisterRequest {
        email = email == null ? null : email.trim();
    }
}
