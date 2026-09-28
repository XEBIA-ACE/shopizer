package com.ecommerce.core.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ecommerce.core.config.AppProperties;
import com.ecommerce.core.domain.entity.UserAccount;
import com.ecommerce.core.domain.entity.VerificationToken;
import com.ecommerce.core.repository.VerificationTokenRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VerificationTokenServiceTest {

    private static final Instant NOW = Instant.parse("2026-09-28T10:00:00Z");

    @Mock
    private VerificationTokenRepository tokenRepository;

    @Test
    void issueForGeneratesSingleUseTokenWith24HourExpiryAndStoresOnlyItsHash() {
        AppProperties properties = new AppProperties(
                new AppProperties.Verification(Duration.ofHours(24), "http://localhost/verify"),
                null, null, null, null, null);
        VerificationTokenService service = new VerificationTokenService(
                tokenRepository, properties, Clock.fixed(NOW, ZoneOffset.UTC));
        when(tokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        UserAccount account = UserAccount.pending("u@example.com", "h", NOW);

        String raw = service.issueFor(account);

        ArgumentCaptor<VerificationToken> captor = ArgumentCaptor.forClass(VerificationToken.class);
        verify(tokenRepository).save(captor.capture());
        VerificationToken saved = captor.getValue();
        assertThat(raw).hasSize(43).matches("[A-Za-z0-9_-]+");
        assertThat(saved.getTokenValue()).isEqualTo(VerificationTokenService.hash(raw)).isNotEqualTo(raw);
        assertThat(saved.getExpiryTimestamp()).isEqualTo(NOW.plus(Duration.ofHours(24)));
        assertThat(saved.isConsumed()).isFalse();
        assertThat(saved.getUserAccount()).isSameAs(account);
        assertThat(service.issueFor(account)).isNotEqualTo(raw);
    }
}
