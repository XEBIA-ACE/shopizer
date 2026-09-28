package com.ecommerce.core.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ecommerce.core.domain.dto.response.SessionResponse;
import com.ecommerce.core.domain.entity.AccountStatus;
import com.ecommerce.core.domain.entity.UserAccount;
import com.ecommerce.core.domain.entity.VerificationToken;
import com.ecommerce.core.exception.AuthException;
import com.ecommerce.core.exception.ErrorCode;
import com.ecommerce.core.repository.VerificationTokenRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    private static final Instant NOW = Instant.parse("2026-09-28T10:00:00Z");
    private static final String RAW = "raw-token";

    @Mock
    private VerificationTokenRepository tokenRepository;
    @Mock
    private SessionService sessionService;

    private EmailVerificationService service;
    private UserAccount account;

    @BeforeEach
    void setUp() {
        service = new EmailVerificationService(tokenRepository, sessionService, Clock.fixed(NOW, ZoneOffset.UTC));
        account = UserAccount.pending("u@example.com", "h", NOW.minusSeconds(60));
    }

    @Test
    void verifyActivatesAccountConsumesTokenAndOpensSession() {
        VerificationToken token = VerificationToken.issue(account, "hash", NOW.minusSeconds(60), NOW.plusSeconds(60));
        when(tokenRepository.findByTokenValueForUpdate(VerificationTokenService.hash(RAW)))
                .thenReturn(Optional.of(token));
        SessionResponse expected = new SessionResponse("jwt", NOW.plusSeconds(3600));
        when(sessionService.openSession(account)).thenReturn(expected);

        SessionResponse session = service.verify(RAW);

        assertThat(session).isEqualTo(expected);
        assertThat(token.isConsumed()).isTrue();
        assertThat(account.getAccountStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(account.isEmailVerified()).isTrue();
    }

    @Test
    void verifyRejectsUnknownToken() {
        when(tokenRepository.findByTokenValueForUpdate(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.verify(RAW)).isInstanceOfSatisfying(AuthException.class,
                ex -> assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INVALID_TOKEN));
    }

    @Test
    void verifyRejectsExpiredTokenWith410() {
        VerificationToken token = VerificationToken.issue(account, "hash", NOW.minusSeconds(90000), NOW);
        when(tokenRepository.findByTokenValueForUpdate(any())).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.verify(RAW)).isInstanceOfSatisfying(AuthException.class, ex -> {
            assertThat(ex.getStatus()).isEqualTo(HttpStatus.GONE);
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.TOKEN_EXPIRED);
        });
        assertThat(token.isConsumed()).isFalse();
        verify(sessionService, never()).openSession(any());
    }

    @Test
    void verifyRejectsConsumedTokenWith409() {
        VerificationToken token = VerificationToken.issue(account, "hash", NOW.minusSeconds(60), NOW.plusSeconds(60));
        token.consume();
        when(tokenRepository.findByTokenValueForUpdate(any())).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.verify(RAW)).isInstanceOfSatisfying(AuthException.class, ex -> {
            assertThat(ex.getStatus()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.TOKEN_CONSUMED);
        });
    }

    @Test
    void verifyRejectsTokenForNonPendingAccount() {
        account.activate();
        VerificationToken token = VerificationToken.issue(account, "hash", NOW.minusSeconds(60), NOW.plusSeconds(60));
        when(tokenRepository.findByTokenValueForUpdate(any())).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.verify(RAW)).isInstanceOfSatisfying(AuthException.class,
                ex -> assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.ACCOUNT_NOT_PENDING));
    }
}
