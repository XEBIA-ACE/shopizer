package com.ecommerce.core.repository;

import com.ecommerce.core.domain.entity.UserAccount;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAccountRepository extends JpaRepository<UserAccount, UUID> {

    boolean existsByEmailAddress(String emailAddress);

    Optional<UserAccount> findByEmailAddress(String emailAddress);
}
