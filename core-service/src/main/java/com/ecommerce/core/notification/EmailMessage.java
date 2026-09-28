package com.ecommerce.core.notification;

public record EmailMessage(String to, String subject, String body) {
}
