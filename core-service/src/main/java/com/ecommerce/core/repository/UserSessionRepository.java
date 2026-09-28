package com.ecommerce.core.repository;

import com.ecommerce.core.domain.entity.UserSession;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {
}
