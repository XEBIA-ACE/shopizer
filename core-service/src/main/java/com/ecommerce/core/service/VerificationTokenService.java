package com.ecommerce.core.service;

import com.ecommerce.core.config.AppProperties;
import com.ecommerce.core.domain.entity.UserAccount;
import com.ecommerce.core.domain.entity.VerificationToken;
import com.ecommerce.core.repository.VerificationTokenRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VerificationTokenService {

    private static final int TOKEN_BYTES = 32;

    private final SecureRandom secureRandom = new SecureRandom();
    private final VerificationTokenRepository tokenRepository;
    private final AppProperties properties;
    private final Clock clock;

    /**
     * Invalidates any outstanding tokens for the account and issues a new one.
     *
     * @return the raw token value to embed in the confirmation link; only its hash is persisted
     */
    @Transactional
    public String issueFor(UserAccount account) {
        if (account.getId() != null) {
            tokenRepository.consumeAllActiveForAccount(account.getId());
        }
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        Instant now = clock.instant();
        Instant expiresAt = now.plus(properties.verification().tokenTtl());
        tokenRepository.save(VerificationToken.issue(account, hash(rawToken), now, expiresAt));
        return rawToken;
    }

    public static String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(rawToken.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }
}
