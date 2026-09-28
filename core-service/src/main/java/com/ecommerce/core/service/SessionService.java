package com.ecommerce.core.service;

import com.ecommerce.core.config.AppProperties;
import com.ecommerce.core.domain.dto.response.SessionResponse;
import com.ecommerce.core.domain.entity.UserAccount;
import com.ecommerce.core.domain.entity.UserSession;
import com.ecommerce.core.repository.UserSessionRepository;
import io.jsonwebtoken.Jwts;
import java.security.KeyPair;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final UserSessionRepository sessionRepository;
    private final KeyPair jwtSigningKeyPair;
    private final AppProperties properties;
    private final Clock clock;

    @Transactional
    public SessionResponse openSession(UserAccount account) {
        Instant issuedAt = clock.instant();
        Instant expiresAt = issuedAt.plus(properties.session().ttl());
        String sessionIdentifier = UUID.randomUUID().toString();
        sessionRepository.save(UserSession.open(account, sessionIdentifier, issuedAt, expiresAt));

        String token = Jwts.builder()
                .issuer(properties.jwt().issuer())
                .subject(account.getId().toString())
                .id(sessionIdentifier)
                .claim("email", account.getEmailAddress())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(jwtSigningKeyPair.getPrivate(), Jwts.SIG.RS256)
                .compact();
        return new SessionResponse(token, expiresAt);
    }
}
