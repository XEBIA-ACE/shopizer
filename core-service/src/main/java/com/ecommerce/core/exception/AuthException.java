package com.ecommerce.core.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AuthException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;

    public AuthException(HttpStatus status, String errorCode, String message) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    public static AuthException emailAlreadyInUse() {
        return new AuthException(HttpStatus.CONFLICT, ErrorCode.EMAIL_ALREADY_IN_USE,
                "An account with this email address already exists. Please sign in or use a different email.");
    }

    public static AuthException invalidToken() {
        return new AuthException(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_TOKEN,
                "This confirmation link is invalid.");
    }

    public static AuthException tokenExpired() {
        return new AuthException(HttpStatus.GONE, ErrorCode.TOKEN_EXPIRED,
                "This confirmation link has expired. Please request a new confirmation link.");
    }

    public static AuthException tokenConsumed() {
        return new AuthException(HttpStatus.CONFLICT, ErrorCode.TOKEN_CONSUMED,
                "This confirmation link is no longer valid.");
    }

    public static AuthException accountNotPending() {
        return new AuthException(HttpStatus.CONFLICT, ErrorCode.ACCOUNT_NOT_PENDING,
                "This account is not awaiting email confirmation.");
    }

    public static AuthException resendCooldown(long retryAfterSeconds) {
        return new AuthException(HttpStatus.TOO_MANY_REQUESTS, ErrorCode.RESEND_COOLDOWN,
                "Please wait " + retryAfterSeconds + " seconds before requesting another confirmation email.");
    }

    public static AuthException resendLimitExceeded() {
        return new AuthException(HttpStatus.TOO_MANY_REQUESTS, ErrorCode.RESEND_LIMIT_EXCEEDED,
                "The maximum number of confirmation email resends has been reached.");
    }
}
