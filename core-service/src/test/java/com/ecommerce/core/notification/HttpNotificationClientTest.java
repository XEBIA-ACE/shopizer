package com.ecommerce.core.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.ecommerce.core.config.AppProperties;
import com.ecommerce.core.config.NotificationConfig;
import com.ecommerce.core.exception.NotificationException;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class HttpNotificationClientTest {

    private static final EmailMessage MESSAGE = new EmailMessage("u@example.com", "Subject", "Body");

    @Test
    void postsEmailToNotificationService() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://notify");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("http://notify/v1/notifications/email"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json(
                        "{\"from\":\"no-reply@example.com\",\"to\":\"u@example.com\",\"subject\":\"Subject\",\"body\":\"Body\"}"))
                .andRespond(withSuccess());

        new HttpNotificationClient(builder.build(), "no-reply@example.com").sendEmail(MESSAGE);

        server.verify();
    }

    @Test
    void wrapsFailuresInNotificationException() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://notify");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("http://notify/v1/notifications/email")).andRespond(withServerError());

        HttpNotificationClient client = new HttpNotificationClient(builder.build(), "no-reply@example.com");

        assertThatThrownBy(() -> client.sendEmail(MESSAGE)).isInstanceOf(NotificationException.class);
    }

    @Test
    void configSelectsClientBasedOnBaseUrl() {
        NotificationConfig config = new NotificationConfig();

        assertThat(config.notificationClient(properties(""), RestClient.builder()))
                .isInstanceOf(LoggingNotificationClient.class);
        assertThat(config.notificationClient(properties("http://notify"), RestClient.builder()))
                .isInstanceOf(HttpNotificationClient.class);
    }

    @Test
    void loggingClientDoesNotThrow() {
        new LoggingNotificationClient().sendEmail(MESSAGE);
    }

    private static AppProperties properties(String baseUrl) {
        return new AppProperties(null, null, null, null,
                new AppProperties.Notification(baseUrl, "no-reply@example.com", Duration.ofSeconds(1)), null);
    }
}
