package com.ecommerce.core.domain.dto.request;

public final class ValidationPatterns {

    public static final String EMAIL = "^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?"
            + "(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?)+$";

    public static final String PASSWORD = "^(?=.*[A-Z])(?=.*\\d).+$";

    private ValidationPatterns() {
    }
}
