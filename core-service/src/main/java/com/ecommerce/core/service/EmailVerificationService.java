package com.ecommerce.core.service;

import com.ecommerce.core.domain.dto.response.SessionResponse;
import com.ecommerce.core.domain.entity.UserAccount;
import com.ecommerce.core.domain.entity.VerificationToken;
import com.ecommerce.core.exception.AuthException;
import com.ecommerce.core.repository.VerificationTokenRepository;
import java.time.Clock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final VerificationTokenRepository tokenRepository;
    private final SessionService sessionService;
    private final Clock clock;

    @Transactional
    public SessionResponse verify(String rawToken) {
        VerificationToken token = tokenRepository.findByTokenValueForUpdate(VerificationTokenService.hash(rawToken))
                .orElseThrow(AuthException::invalidToken);
        if (token.isConsumed()) {
            throw AuthException.tokenConsumed();
        }
        if (token.isExpiredAt(clock.instant())) {
            throw AuthException.tokenExpired();
        }
        UserAccount account = token.getUserAccount();
        if (!account.isPending()) {
            throw AuthException.accountNotPending();
        }

        token.consume();
        account.activate();
        SessionResponse session = sessionService.openSession(account);
        log.info("Verified email and activated account id={}", account.getId());
        return session;
    }
}
