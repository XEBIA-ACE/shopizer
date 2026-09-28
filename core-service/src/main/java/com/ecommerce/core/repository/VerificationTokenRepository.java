package com.ecommerce.core.repository;

import com.ecommerce.core.domain.entity.VerificationToken;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from VerificationToken t join fetch t.userAccount where t.tokenValue = :tokenValue")
    Optional<VerificationToken> findByTokenValueForUpdate(@Param("tokenValue") String tokenValue);

    @Modifying
    @Query("update VerificationToken t set t.consumed = true where t.userAccount.id = :accountId and t.consumed = false")
    int consumeAllActiveForAccount(@Param("accountId") UUID accountId);
}
