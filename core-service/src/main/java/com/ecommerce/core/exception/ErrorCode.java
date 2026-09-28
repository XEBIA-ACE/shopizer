package com.ecommerce.core.exception;

public final class ErrorCode {

    public static final String VALIDATION_FAILED = "validation_failed";
    public static final String EMAIL_ALREADY_IN_USE = "email_already_in_use";
    public static final String INVALID_TOKEN = "invalid_token";
    public static final String TOKEN_EXPIRED = "token_expired";
    public static final String TOKEN_CONSUMED = "token_consumed";
    public static final String ACCOUNT_NOT_PENDING = "account_not_pending";
    public static final String RESEND_COOLDOWN = "resend_cooldown";
    public static final String RESEND_LIMIT_EXCEEDED = "resend_limit_exceeded";
    public static final String NOT_FOUND = "not_found";
    public static final String NOTIFICATION_UNAVAILABLE = "notification_unavailable";
    public static final String MALFORMED_REQUEST = "malformed_request";
    public static final String INTERNAL_ERROR = "internal_error";

    private ErrorCode() {
    }
}
