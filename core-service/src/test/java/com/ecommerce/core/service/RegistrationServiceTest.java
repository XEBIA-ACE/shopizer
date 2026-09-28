package com.ecommerce.core.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ecommerce.core.config.AppProperties;
import com.ecommerce.core.domain.entity.AccountStatus;
import com.ecommerce.core.domain.entity.UserAccount;
import com.ecommerce.core.exception.AuthException;
import com.ecommerce.core.exception.ErrorCode;
import com.ecommerce.core.exception.ResourceNotFoundException;
import com.ecommerce.core.repository.UserAccountRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    private static final Instant NOW = Instant.parse("2026-09-28T10:00:00Z");

    @Mock
    private UserAccountRepository accountRepository;
    @Mock
    private VerificationTokenService tokenService;
    @Mock
    private ConfirmationEmailService confirmationEmailService;
    @Mock
    private PasswordEncoder passwordEncoder;

    private RegistrationService service;

    @BeforeEach
    void setUp() {
        AppProperties properties = new AppProperties(null,
                new AppProperties.Resend(Duration.ofSeconds(60), 5), null, null, null, null);
        service = new RegistrationService(accountRepository, tokenService, confirmationEmailService,
                passwordEncoder, properties, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void registerCreatesPendingAccountAndSendsConfirmation() {
        when(accountRepository.existsByEmailAddress("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Str0ngPass")).thenReturn("hashed");
        when(accountRepository.saveAndFlush(any(UserAccount.class))).thenAnswer(inv -> inv.getArgument(0));
        when(tokenService.issueFor(any())).thenReturn("raw-token");

        UserAccount account = service.register("  New@Example.com ", "Str0ngPass");

        assertThat(account.getEmailAddress()).isEqualTo("new@example.com");
        assertThat(account.getAccountStatus()).isEqualTo(AccountStatus.PENDING);
        assertThat(account.isEmailVerified()).isFalse();
        assertThat(account.getPasswordHash()).isEqualTo("hashed");
        assertThat(account.getRegistrationTimestamp()).isEqualTo(NOW);
        verify(confirmationEmailService).send("new@example.com", "raw-token");
    }

    @Test
    void registerRejectsDuplicateEmail() {
        when(accountRepository.existsByEmailAddress("dup@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.register("dup@example.com", "Str0ngPass"))
                .isInstanceOfSatisfying(AuthException.class, ex -> {
                    assertThat(ex.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.EMAIL_ALREADY_IN_USE);
                });
        verify(accountRepository, never()).saveAndFlush(any());
    }

    @Test
    void registerMapsUniqueConstraintRaceToDuplicateEmail() {
        when(accountRepository.existsByEmailAddress("race@example.com")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(accountRepository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("unique"));

        assertThatThrownBy(() -> service.register("race@example.com", "Str0ngPass"))
                .isInstanceOfSatisfying(AuthException.class,
                        ex -> assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.EMAIL_ALREADY_IN_USE));
        verify(confirmationEmailService, never()).send(any(), any());
    }

    @Test
    void resendRejectsActiveAccount() {
        UserAccount active = UserAccount.pending("a@example.com", "h", NOW.minusSeconds(3600));
        active.activate();
        when(accountRepository.findByEmailAddress("a@example.com")).thenReturn(Optional.of(active));

        assertThatThrownBy(() -> service.resendConfirmation("a@example.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void resendUsesLastResendTimestampForCooldown() {
        UserAccount account = UserAccount.pending("p@example.com", "h", NOW.minusSeconds(3600));
        account.recordResend(NOW.minusSeconds(30));
        when(accountRepository.findByEmailAddress("p@example.com")).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> service.resendConfirmation("p@example.com"))
                .isInstanceOfSatisfying(AuthException.class, ex -> {
                    assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.RESEND_COOLDOWN);
                    assertThat(ex.getMessage()).contains("30 seconds");
                });
    }

    @Test
    void resendIssuesNewTokenAndRecordsAttempt() {
        UserAccount account = UserAccount.pending("p@example.com", "h", NOW.minusSeconds(3600));
        when(accountRepository.findByEmailAddress("p@example.com")).thenReturn(Optional.of(account));
        when(tokenService.issueFor(account)).thenReturn("fresh");

        service.resendConfirmation("P@example.com");

        assertThat(account.getResendCount()).isEqualTo((short) 1);
        assertThat(account.getLastResendAt()).isEqualTo(NOW);
        verify(confirmationEmailService).send("p@example.com", "fresh");
    }
}
