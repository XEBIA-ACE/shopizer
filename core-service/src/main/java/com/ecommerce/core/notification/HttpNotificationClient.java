package com.ecommerce.core.notification;

import com.ecommerce.core.exception.NotificationException;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

public class HttpNotificationClient implements NotificationClient {

    static final String EMAIL_PATH = "/v1/notifications/email";

    private final RestClient restClient;
    private final String fromAddress;

    public HttpNotificationClient(RestClient restClient, String fromAddress) {
        this.restClient = restClient;
        this.fromAddress = fromAddress;
    }

    @Override
    public void sendEmail(EmailMessage message) {
        try {
            restClient.post()
                    .uri(EMAIL_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new Payload(fromAddress, message.to(), message.subject(), message.body()))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException ex) {
            throw new NotificationException("Notification service rejected or failed the email request", ex);
        }
    }

    record Payload(String from, String to, String subject, String body) {
    }
}
