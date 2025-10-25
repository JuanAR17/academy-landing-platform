package com.academia.backend.repo;

import com.academia.backend.domain.EpaycoCustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EpaycoCustomerRepo extends JpaRepository<EpaycoCustomerEntity, Long> {
  Optional<EpaycoCustomerEntity> findByUserId(UUID userId);
  Optional<EpaycoCustomerEntity> findByCustomerId(String customerId);
}
