package com.academia.backend.repo;

import com.academia.backend.domain.PaymentEntity;
import com.academia.backend.domain.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepo extends JpaRepository<PaymentEntity, UUID> {

    Optional<PaymentEntity> findByTransactionId(String transactionId);

    Optional<PaymentEntity> findByEpaycoRef(String epaycoRef);

    List<PaymentEntity> findByUserId(UUID userId);

    List<PaymentEntity> findByStatus(PaymentStatus status);
}