package com.ecommerce.core.notification;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoggingNotificationClient implements NotificationClient {

    @Override
    public void sendEmail(EmailMessage message) {
        log.info("Notification service not configured; email to={} subject=\"{}\" body=\"{}\"",
                message.to(), message.subject(), message.body());
    }
}
