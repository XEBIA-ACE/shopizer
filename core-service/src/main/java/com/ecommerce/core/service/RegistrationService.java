package com.ecommerce.core.service;

import com.ecommerce.core.config.AppProperties;
import com.ecommerce.core.domain.entity.UserAccount;
import com.ecommerce.core.exception.AuthException;
import com.ecommerce.core.exception.ResourceNotFoundException;
import com.ecommerce.core.repository.UserAccountRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final UserAccountRepository accountRepository;
    private final VerificationTokenService tokenService;
    private final ConfirmationEmailService confirmationEmailService;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties properties;
    private final Clock clock;

    @Transactional
    public UserAccount register(String email, String password) {
        String normalizedEmail = normalize(email);
        if (accountRepository.existsByEmailAddress(normalizedEmail)) {
            throw AuthException.emailAlreadyInUse();
        }
        UserAccount account;
        try {
            account = accountRepository.saveAndFlush(
                    UserAccount.pending(normalizedEmail, passwordEncoder.encode(password), clock.instant()));
        } catch (DataIntegrityViolationException ex) {
            throw AuthException.emailAlreadyInUse();
        }
        String rawToken = tokenService.issueFor(account);
        confirmationEmailService.send(normalizedEmail, rawToken);
        log.info("Registered pending account id={}", account.getId());
        return account;
    }

    @Transactional
    public void resendConfirmation(String email) {
        UserAccount account = accountRepository.findByEmailAddress(normalize(email))
                .filter(UserAccount::isPending)
                .orElseThrow(() -> new ResourceNotFoundException("No pending account exists for this email address."));

        AppProperties.Resend resend = properties.resend();
        if (account.getResendCount() >= resend.maxAttempts()) {
            throw AuthException.resendLimitExceeded();
        }
        Instant now = clock.instant();
        Instant lastSentAt = account.getLastResendAt() != null
                ? account.getLastResendAt()
                : account.getRegistrationTimestamp();
        Instant nextAllowedAt = lastSentAt.plus(resend.cooldown());
        if (now.isBefore(nextAllowedAt)) {
            long retryAfter = Math.max(1, Duration.between(now, nextAllowedAt).toSeconds());
            throw AuthException.resendCooldown(retryAfter);
        }

        String rawToken = tokenService.issueFor(account);
        account.recordResend(now);
        confirmationEmailService.send(account.getEmailAddress(), rawToken);
        log.info("Resent confirmation email for account id={} attempt={}", account.getId(), account.getResendCount());
    }

    static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
