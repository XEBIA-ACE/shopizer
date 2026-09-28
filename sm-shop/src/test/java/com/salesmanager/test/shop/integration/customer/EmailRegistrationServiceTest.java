package com.salesmanager.test.shop.integration.customer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import com.salesmanager.core.business.modules.email.Email;
import com.salesmanager.core.business.repositories.customer.CustomerSessionRepository;
import com.salesmanager.core.business.repositories.customer.EmailVerificationTokenRepository;
import com.salesmanager.core.business.services.customer.CustomerService;
import com.salesmanager.core.business.services.system.EmailService;
import com.salesmanager.core.model.customer.Customer;
import com.salesmanager.core.model.customer.CustomerSession;
import com.salesmanager.core.model.customer.EmailVerificationToken;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.shop.model.customer.PersistableCustomer;
import com.salesmanager.shop.store.api.v1.customer.EmailRegistrationService;
import com.salesmanager.shop.store.controller.customer.facade.CustomerFacade;
import com.salesmanager.shop.store.security.JWTTokenUtil;

@ExtendWith(MockitoExtension.class)
class EmailRegistrationServiceTest {

    @Mock private CustomerFacade customerFacade;
    @Mock private CustomerService customerService;
    @Mock private EmailVerificationTokenRepository tokens;
    @Mock private CustomerSessionRepository sessions;
    @Mock private EmailService emailService;
    @Mock private JWTTokenUtil jwtTokenUtil;
    @Mock private UserDetailsService jwtCustomerDetailsService;
    @Mock private UserDetails userDetails;
    @InjectMocks private EmailRegistrationService registration;

    private final Instant now = Instant.parse("2026-01-01T00:00:00Z");
    private final MerchantStore store = new MerchantStore();
    private final Language language = new Language();

    @BeforeEach
    void setup() {
        store.setId(1);
        store.setStorename("Store");
        store.setStoreEmailAddress("support@example.com");
        ReflectionTestUtils.setField(registration, "baseUrl", "https://shop.example.com/");
        ReflectionTestUtils.setField(registration, "clock", Clock.fixed(now, ZoneOffset.UTC));
    }

    @Test
    void registersPendingCustomerAndSendsHashedExpiringLink() throws Exception {
        Customer customer = pendingCustomer();
        when(customerService.getById(5L)).thenReturn(customer);
        doAnswer(invocation -> {
            ((PersistableCustomer) invocation.getArgument(0)).setId(5L);
            return invocation.getArgument(0);
        }).when(customerFacade).registerPendingCustomer(any(), any(), any());

        registration.register("  Shopper@Example.com  ", "Secure123", store, language);

        ArgumentCaptor<PersistableCustomer> input = ArgumentCaptor.forClass(PersistableCustomer.class);
        verify(customerFacade).registerPendingCustomer(input.capture(), any(), any());
        assertEquals("shopper@example.com", input.getValue().getEmailAddress());
        ArgumentCaptor<EmailVerificationToken> saved = ArgumentCaptor.forClass(EmailVerificationToken.class);
        verify(tokens).save(saved.capture());
        assertEquals(now.plusSeconds(24 * 3600), saved.getValue().getExpiresAt().toInstant());
        ArgumentCaptor<Email> message = ArgumentCaptor.forClass(Email.class);
        verify(emailService).sendHtmlEmail(any(), message.capture());
        String link = message.getValue().getTemplateTokens().get("CONFIRMATION_URL");
        assertTrue(link.startsWith("https://shop.example.com/verify-email.html?token="));
        assertNotEquals(saved.getValue().getTokenHash(), link.substring(link.indexOf("token=") + 6));
        assertEquals("shopper@example.com", message.getValue().getTo());
    }

    @Test
    void rejectsDuplicateAndWeakPasswordBeforeSaving() throws Exception {
        when(customerService.getByNickForRegistration("shopper@example.com", 1)).thenReturn(pendingCustomer());
        ResponseStatusException duplicate = assertThrows(ResponseStatusException.class,
                () -> registration.register("shopper@example.com", "Secure123", store, language));
        assertEquals(HttpStatus.CONFLICT, duplicate.getStatus());

        ResponseStatusException weak = assertThrows(ResponseStatusException.class,
                () -> registration.register("another@example.com", "short", store, language));
        assertEquals(HttpStatus.BAD_REQUEST, weak.getStatus());
        verify(tokens, never()).save(any());
    }

    @Test
    void verifiesOnceAndCreatesSession() throws Exception {
        String raw = "a".repeat(64);
        EmailVerificationToken link = link(now.plusSeconds(3600));
        when(tokens.findByTokenHash(anyString())).thenReturn(Optional.of(link));
        when(jwtCustomerDetailsService.loadUserByUsername("shopper@example.com")).thenReturn(userDetails);
        when(jwtTokenUtil.generateToken(userDetails)).thenReturn("jwt");
        when(jwtTokenUtil.getExpirationDateFromToken("jwt")).thenReturn(Date.from(now.plusSeconds(3600)));

        assertEquals("jwt", registration.verify(raw));
        assertTrue(link.getCustomer().getEmailVerified());
        assertNotNull(link.getConsumedAt());
        ArgumentCaptor<CustomerSession> saved = ArgumentCaptor.forClass(CustomerSession.class);
        verify(sessions).save(saved.capture());
        assertNotEquals("jwt", saved.getValue().getTokenHash());
        assertEquals(now.plusSeconds(3600), saved.getValue().getExpiresAt().toInstant());

        ResponseStatusException replay = assertThrows(ResponseStatusException.class, () -> registration.verify(raw));
        assertEquals(HttpStatus.GONE, replay.getStatus());
    }

    @Test
    void rejectsExpiredLinkAndPromptsForAnother() {
        EmailVerificationToken link = link(now.minusSeconds(1));
        when(tokens.findByTokenHash(anyString())).thenReturn(Optional.of(link));
        ResponseStatusException expired = assertThrows(ResponseStatusException.class,
                () -> registration.verify("b".repeat(64)));
        assertEquals(HttpStatus.GONE, expired.getStatus());
        assertTrue(expired.getReason().contains("request a new link"));
        assertFalse(link.getCustomer().getEmailVerified());
        verify(sessions, never()).save(any());
    }

    @Test
    void enforcesResendCooldownAndLimit() throws Exception {
        Customer customer = pendingCustomer();
        EmailVerificationToken link = link(now.plusSeconds(3600));
        link.setLastSentAt(Date.from(now.minusSeconds(30)));
        when(customerService.getByNickForRegistration("shopper@example.com", 1)).thenReturn(customer);
        when(tokens.findByCustomerId(5L)).thenReturn(Optional.of(link));

        ResponseStatusException cooldown = assertThrows(ResponseStatusException.class,
                () -> registration.resend("shopper@example.com", store));
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, cooldown.getStatus());

        link.setLastSentAt(Date.from(now.minusSeconds(61)));
        link.setResendCount(5);
        ResponseStatusException limit = assertThrows(ResponseStatusException.class,
                () -> registration.resend("shopper@example.com", store));
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, limit.getStatus());
        verify(emailService, never()).sendHtmlEmail(any(), any());
    }

    @Test
    void failedDeliveryDoesNotReportSuccess() throws Exception {
        when(customerService.getById(5L)).thenReturn(pendingCustomer());
        doAnswer(invocation -> {
            ((PersistableCustomer) invocation.getArgument(0)).setId(5L);
            return invocation.getArgument(0);
        }).when(customerFacade).registerPendingCustomer(any(), any(), any());
        doThrow(new IllegalStateException("mail unavailable")).when(emailService).sendHtmlEmail(any(), any());
        assertThrows(IllegalStateException.class,
                () -> registration.register("shopper@example.com", "Secure123", store, language));
    }

    private Customer pendingCustomer() {
        Customer customer = new Customer();
        customer.setId(5L);
        customer.setEmailAddress("shopper@example.com");
        customer.setNick("shopper@example.com");
        customer.setEmailVerified(false);
        customer.setMerchantStore(store);
        return customer;
    }

    private EmailVerificationToken link(Instant expiry) {
        EmailVerificationToken link = new EmailVerificationToken();
        link.setCustomer(pendingCustomer());
        link.setExpiresAt(Date.from(expiry));
        return link;
    }
}
