package com.ecommerce.core.domain.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Locale;

@Converter(autoApply = true)
public class AccountStatusConverter implements AttributeConverter<AccountStatus, String> {

    @Override
    public String convertToDatabaseColumn(AccountStatus status) {
        return status == null ? null : status.name().toLowerCase(Locale.ROOT);
    }

    @Override
    public AccountStatus convertToEntityAttribute(String value) {
        return value == null ? null : AccountStatus.valueOf(value.toUpperCase(Locale.ROOT));
    }
}
