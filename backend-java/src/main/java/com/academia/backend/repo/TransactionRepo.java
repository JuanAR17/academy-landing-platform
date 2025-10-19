package com.academia.backend.repo;

import com.academia.backend.domain.Transaction;
import com.academia.backend.domain.TransactionStatus;
import com.academia.backend.domain.UserEntity;
import com.academia.backend.domain.CourseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepo extends JpaRepository<Transaction, UUID> {

    // Buscar por referencia de transacción
    Optional<Transaction> findByTransactionReference(String transactionReference);

    // Buscar por ID externo
    Optional<Transaction> findByExternalTransactionId(String externalTransactionId);

    // Buscar transacciones de un usuario
    Page<Transaction> findByUserOrderByCreatedAtDesc(UserEntity user, Pageable pageable);

    // Buscar transacciones de un usuario por estado
    List<Transaction> findByUserAndStatus(UserEntity user, TransactionStatus status);

    // Buscar transacciones de un curso
    Page<Transaction> findByCourseOrderByCreatedAtDesc(CourseEntity course, Pageable pageable);

    // Buscar transacciones por estado
    Page<Transaction> findByStatusOrderByCreatedAtDesc(TransactionStatus status, Pageable pageable);

    // Buscar transacciones completadas en un rango de fechas
    @Query("SELECT t FROM Transaction t WHERE t.status = 'COMPLETED' AND t.completedAt BETWEEN :startDate AND :endDate")
    List<Transaction> findCompletedTransactionsBetween(@Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);

    // Calcular ingresos totales
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.status = 'COMPLETED'")
    BigDecimal calculateTotalRevenue();

    // Calcular ingresos por curso
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.course = :course AND t.status = 'COMPLETED'")
    BigDecimal calculateRevenueForCourse(@Param("course") CourseEntity course);

    // Calcular ingresos en un rango de fechas
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.status = 'COMPLETED' AND t.completedAt BETWEEN :startDate AND :endDate")
    BigDecimal calculateRevenueBetween(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    // Contar transacciones fallidas recientes
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.status = 'FAILED' AND t.createdAt > :since")
    long countFailedTransactionsSince(@Param("since") Instant since);

    // Buscar transacciones pendientes antiguas
    @Query("SELECT t FROM Transaction t WHERE t.status = 'PENDING' AND t.createdAt < :threshold")
    List<Transaction> findStalePendingTransactions(@Param("threshold") Instant threshold);
}
