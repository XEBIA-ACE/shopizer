package com.ecommerce.core.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_accounts")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "email_address", nullable = false, unique = true, length = 320)
    private String emailAddress;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "account_status", nullable = false, length = 16)
    private AccountStatus accountStatus;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified;

    @Column(name = "registration_timestamp", nullable = false, updatable = false)
    private Instant registrationTimestamp;

    @Column(name = "resend_count", nullable = false)
    private short resendCount;

    @Column(name = "last_resend_at")
    private Instant lastResendAt;

    public static UserAccount pending(String emailAddress, String passwordHash, Instant registeredAt) {
        UserAccount account = new UserAccount();
        account.emailAddress = emailAddress;
        account.passwordHash = passwordHash;
        account.accountStatus = AccountStatus.PENDING;
        account.emailVerified = false;
        account.registrationTimestamp = registeredAt;
        account.resendCount = 0;
        return account;
    }

    public boolean isPending() {
        return accountStatus == AccountStatus.PENDING;
    }

    public void activate() {
        this.accountStatus = AccountStatus.ACTIVE;
        this.emailVerified = true;
    }

    public void recordResend(Instant at) {
        this.resendCount++;
        this.lastResendAt = at;
    }
}
