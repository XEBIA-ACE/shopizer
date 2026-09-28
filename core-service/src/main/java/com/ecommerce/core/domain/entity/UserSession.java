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
@Table(name = "user_sessions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_account_id", nullable = false)
    private UserAccount userAccount;

    @Column(name = "session_identifier", nullable = false, unique = true)
    private String sessionIdentifier;

    @Column(name = "issued_at", nullable = false, updatable = false)
    private Instant issuedAt;

    @Column(name = "expiry_timestamp", nullable = false)
    private Instant expiryTimestamp;

    public static UserSession open(UserAccount account, String sessionIdentifier, Instant issuedAt, Instant expiresAt) {
        UserSession session = new UserSession();
        session.userAccount = account;
        session.sessionIdentifier = sessionIdentifier;
        session.issuedAt = issuedAt;
        session.expiryTimestamp = expiresAt;
        return session;
    }
}
