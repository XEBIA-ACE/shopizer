package com.salesmanager.core.business.repositories.customer;

import org.springframework.data.jpa.repository.JpaRepository;
import com.salesmanager.core.model.customer.CustomerSession;

public interface CustomerSessionRepository extends JpaRepository<CustomerSession, String> {
}
