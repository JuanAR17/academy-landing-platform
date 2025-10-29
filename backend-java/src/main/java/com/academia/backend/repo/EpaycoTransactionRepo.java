package com.academia.backend.repo;

import com.academia.backend.domain.EpaycoTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface EpaycoTransactionRepo extends JpaRepository<EpaycoTransactionEntity, Long> {
  Optional<EpaycoTransactionEntity> findByRefPayco(Long refPayco);
  List<EpaycoTransactionEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);
  // NUEVO: usado por confirmPse(...) como fallback si no hay ref_payco
  Optional<EpaycoTransactionEntity> findFirstByInvoiceOrderByCreatedAtDesc(String invoice);
}