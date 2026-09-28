package com.ecommerce.core.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ecommerce.core.exception.NotificationException;
import com.ecommerce.core.notification.EmailMessage;
import com.ecommerce.core.notification.NotificationClient;
import com.ecommerce.core.service.VerificationTokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.security.KeyPair;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RegistrationFlowIntegrationTest {

    private static final Pattern TOKEN_IN_LINK = Pattern.compile("[?&]token=([A-Za-z0-9_-]+)");
    private static final String PASSWORD = "Str0ngPass";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private KeyPair jwtSigningKeyPair;

    @MockitoBean
    private NotificationClient notificationClient;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.update("DELETE FROM user_sessions");
        jdbcTemplate.update("DELETE FROM verification_tokens");
        jdbcTemplate.update("DELETE FROM user_accounts");
    }

    @Test
    void registerSendsConfirmationLinkAndCreatesPendingAccount() throws Exception {
        register("Shopper@Example.com", PASSWORD)
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.message").value(
                        "Registration received. A confirmation link has been sent to your email address."));

        EmailMessage sent = lastEmail();
        assertThat(sent.to()).isEqualTo("shopper@example.com");
        assertThat(sent.body()).contains("http://localhost:3000/verify-email?token=");

        Map<String, Object> row = jdbcTemplate.queryForMap(
                "SELECT account_status, email_verified, password_hash FROM user_accounts WHERE email_address = ?",
                "shopper@example.com");
        assertThat(row.get("account_status")).isEqualTo("pending");
        assertThat(row.get("email_verified")).isEqualTo(false);
        assertThat((String) row.get("password_hash")).startsWith("$2a$").doesNotContain(PASSWORD);

        String storedToken = jdbcTemplate.queryForObject("SELECT token_value FROM verification_tokens", String.class);
        assertThat(storedToken).isEqualTo(VerificationTokenService.hash(tokenFrom(sent)));
    }

    @Test
    void registerRejectsAlreadyRegisteredEmail() throws Exception {
        register("dup@example.com", PASSWORD).andExpect(status().isAccepted());

        register("DUP@example.com ", PASSWORD)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("email_already_in_use"))
                .andExpect(jsonPath("$.message").value(containsString("already exists")));
        verify(notificationClient, times(1)).sendEmail(any());
    }

    @Test
    void registerReturnsFieldErrorsForInvalidInput() throws Exception {
        register("not-an-email", "weak")
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("validation_failed"))
                .andExpect(jsonPath("$.field_errors.email").value("Email must be a valid email address"))
                .andExpect(jsonPath("$.field_errors.password").exists());
    }

    @Test
    void registerRejectsMalformedBody() throws Exception {
        mockMvc.perform(post("/internal/auth/register").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("malformed_request"));
    }

    @Test
    void registerRollsBackWhenNotificationServiceFails() throws Exception {
        doThrow(new NotificationException("down", null)).when(notificationClient).sendEmail(any());

        register("rollback@example.com", PASSWORD)
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("notification_unavailable"));

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user_accounts", Integer.class);
        assertThat(count).isZero();
    }

    @Test
    void verifyActivatesAccountSignsInAndRedirectsToDashboard() throws Exception {
        register("verify@example.com", PASSWORD).andExpect(status().isAccepted());
        String token = tokenFrom(lastEmail());

        String body = mockMvc.perform(post("/internal/auth/verify/email").param("token", token))
                .andExpect(status().isOk())
                .andExpect(header().string("Location", "/dashboard"))
                .andExpect(jsonPath("$.session_token").isString())
                .andExpect(jsonPath("$.expires_at").isString())
                .andReturn().getResponse().getContentAsString();

        Map<String, Object> account = jdbcTemplate.queryForMap(
                "SELECT id, account_status, email_verified FROM user_accounts WHERE email_address = ?",
                "verify@example.com");
        assertThat(account.get("account_status")).isEqualTo("active");
        assertThat(account.get("email_verified")).isEqualTo(true);
        assertThat(jdbcTemplate.queryForObject("SELECT consumed FROM verification_tokens", Boolean.class)).isTrue();

        String sessionToken = body.replaceAll(".*\"session_token\":\"([^\"]+)\".*", "$1");
        Claims claims = Jwts.parser().verifyWith(jwtSigningKeyPair.getPublic()).build()
                .parseSignedClaims(sessionToken).getPayload();
        assertThat(claims.getSubject()).isEqualTo(account.get("id").toString());
        assertThat(claims.get("email", String.class)).isEqualTo("verify@example.com");
        String sessionId = jdbcTemplate.queryForObject("SELECT session_identifier FROM user_sessions", String.class);
        assertThat(claims.getId()).isEqualTo(sessionId);
    }

    @Test
    void verifyRejectsConsumedToken() throws Exception {
        register("reuse@example.com", PASSWORD).andExpect(status().isAccepted());
        String token = tokenFrom(lastEmail());
        mockMvc.perform(post("/internal/auth/verify/email").param("token", token)).andExpect(status().isOk());

        mockMvc.perform(post("/internal/auth/verify/email").param("token", token))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("token_consumed"))
                .andExpect(jsonPath("$.message").value("This confirmation link is no longer valid."));
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user_sessions", Integer.class)).isEqualTo(1);
    }

    @Test
    void verifyRejectsExpiredTokenAndPromptsForNewLink() throws Exception {
        register("expired@example.com", PASSWORD).andExpect(status().isAccepted());
        String token = tokenFrom(lastEmail());
        jdbcTemplate.update("UPDATE verification_tokens SET expiry_timestamp = DATEADD('HOUR', -25, CURRENT_TIMESTAMP)");

        mockMvc.perform(post("/internal/auth/verify/email").param("token", token))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.error").value("token_expired"))
                .andExpect(jsonPath("$.message").value(
                        containsString("request a new confirmation link")));
        assertThat(jdbcTemplate.queryForObject(
                "SELECT account_status FROM user_accounts", String.class)).isEqualTo("pending");
    }

    @Test
    void verifyRejectsUnknownAndMissingToken() throws Exception {
        mockMvc.perform(post("/internal/auth/verify/email").param("token", "does-not-exist"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid_token"));

        mockMvc.perform(post("/internal/auth/verify/email"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.field_errors.token").value("token is required"));
    }

    @Test
    void resendIssuesNewTokenAndInvalidatesPreviousOne() throws Exception {
        register("resend@example.com", PASSWORD).andExpect(status().isAccepted());
        String firstToken = tokenFrom(lastEmail());
        jdbcTemplate.update("UPDATE user_accounts SET registration_timestamp = DATEADD('SECOND', -61, CURRENT_TIMESTAMP)");

        resend("resend@example.com")
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.message").value("A new confirmation link has been sent to your email address."));
        String secondToken = tokenFrom(lastEmail());
        assertThat(secondToken).isNotEqualTo(firstToken);

        mockMvc.perform(post("/internal/auth/verify/email").param("token", firstToken))
                .andExpect(status().isConflict());
        mockMvc.perform(post("/internal/auth/verify/email").param("token", secondToken))
                .andExpect(status().isOk());
    }

    @Test
    void resendEnforcesCooldownAndUnknownEmail() throws Exception {
        register("cooldown@example.com", PASSWORD).andExpect(status().isAccepted());

        resend("cooldown@example.com")
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.error").value("resend_cooldown"));
        resend("nobody@example.com")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("not_found"));
    }

    @Test
    void resendEnforcesMaximumAttempts() throws Exception {
        register("limit@example.com", PASSWORD).andExpect(status().isAccepted());
        jdbcTemplate.update("UPDATE user_accounts SET resend_count = 5");

        resend("limit@example.com")
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.error").value("resend_limit_exceeded"));
    }

    @Test
    void healthEndpointIsPublic() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void nonAuthEndpointsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/internal/users/me"))
                .andExpect(status().isForbidden());
    }

    private ResultActions register(String email, String password)
            throws Exception {
        return mockMvc.perform(post("/internal/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"));
    }

    private ResultActions resend(String email) throws Exception {
        return mockMvc.perform(post("/internal/auth/register/resend")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\"}"));
    }

    private EmailMessage lastEmail() {
        ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);
        verify(notificationClient, atLeastOnce()).sendEmail(captor.capture());
        List<EmailMessage> all = captor.getAllValues();
        return all.get(all.size() - 1);
    }

    private static String tokenFrom(EmailMessage message) {
        Matcher matcher = TOKEN_IN_LINK.matcher(message.body());
        assertThat(matcher.find()).isTrue();
        return matcher.group(1);
    }
}
