package com.ecommerce.core.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "verification_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_account_id", nullable = false)
    private UserAccount userAccount;

    @Column(name = "token_value", nullable = false, unique = true, length = 128)
    private String tokenValue;

    @Column(name = "expiry_timestamp", nullable = false)
    private Instant expiryTimestamp;

    @Column(name = "consumed", nullable = false)
    private boolean consumed;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public static VerificationToken issue(UserAccount account, String tokenValue, Instant createdAt, Instant expiresAt) {
        VerificationToken token = new VerificationToken();
        token.userAccount = account;
        token.tokenValue = tokenValue;
        token.createdAt = createdAt;
        token.expiryTimestamp = expiresAt;
        token.consumed = false;
        return token;
    }

    public boolean isExpiredAt(Instant now) {
        return !expiryTimestamp.isAfter(now);
    }

    public void consume() {
        this.consumed = true;
    }
}
