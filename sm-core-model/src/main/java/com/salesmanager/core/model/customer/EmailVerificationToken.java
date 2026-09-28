package com.salesmanager.core.model.customer;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "CUSTOMER_EMAIL_VERIFICATION")
public class EmailVerificationToken {

    @Id
    @Column(name = "ID")
    @TableGenerator(name = "EMAIL_VERIFICATION_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT", pkColumnValue = "EMAIL_VERIFICATION_SEQ_NEXT_VAL")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "EMAIL_VERIFICATION_GEN")
    private Long id;

    @Column(name = "TOKEN_HASH", length = 64, nullable = false, unique = true)
    private String tokenHash;

    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "CUSTOMER_ID", nullable = false, unique = true)
    private Customer customer;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "EXPIRES_AT", nullable = false)
    private Date expiresAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CONSUMED_AT")
    private Date consumedAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "LAST_SENT_AT", nullable = false)
    private Date lastSentAt;

    @Column(name = "RESEND_COUNT", nullable = false)
    private int resendCount;

    public String getTokenHash() { return tokenHash; }
    public void setTokenHash(String tokenHash) { this.tokenHash = tokenHash; }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    public Date getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Date expiresAt) { this.expiresAt = expiresAt; }
    public Date getConsumedAt() { return consumedAt; }
    public void setConsumedAt(Date consumedAt) { this.consumedAt = consumedAt; }
    public Date getLastSentAt() { return lastSentAt; }
    public void setLastSentAt(Date lastSentAt) { this.lastSentAt = lastSentAt; }
    public int getResendCount() { return resendCount; }
    public void setResendCount(int resendCount) { this.resendCount = resendCount; }
}
