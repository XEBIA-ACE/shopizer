package com.ecommerce.core.notification;

public interface NotificationClient {

    void sendEmail(EmailMessage message);
}
