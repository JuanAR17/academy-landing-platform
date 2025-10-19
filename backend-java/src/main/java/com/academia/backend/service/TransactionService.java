package com.academia.backend.service;

import com.academia.backend.domain.*;
import com.academia.backend.dto.EnrollmentDto;
import com.academia.backend.dto.TransactionDto;
import com.academia.backend.dto.in.CreateEnrollmentIn;
import com.academia.backend.dto.in.CreateTransactionIn;
import com.academia.backend.dto.in.UpdateTransactionIn;
import com.academia.backend.repo.CourseRepo;
import com.academia.backend.repo.EnrollmentRepo;
import com.academia.backend.repo.TransactionRepo;
import com.academia.backend.repo.UserRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class TransactionService {

    private final TransactionRepo transactionRepo;
    private final UserRepo userRepo;
    private final CourseRepo courseRepo;
    private final EnrollmentRepo enrollmentRepo;
    private final EnrollmentService enrollmentService;
    private final LogService logService;

    public TransactionService(TransactionRepo transactionRepo, UserRepo userRepo,
            CourseRepo courseRepo, EnrollmentRepo enrollmentRepo,
            EnrollmentService enrollmentService, LogService logService) {
        this.transactionRepo = transactionRepo;
        this.userRepo = userRepo;
        this.courseRepo = courseRepo;
        this.enrollmentRepo = enrollmentRepo;
        this.enrollmentService = enrollmentService;
        this.logService = logService;
    }

    // Crear transacción
    public TransactionDto createTransaction(CreateTransactionIn input, UUID userId) {
        logService.logInfo("Transaction", "create_transaction",
                "Creando transacción para curso: " + input.courseId(), userId);

        UserEntity user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        CourseEntity course = courseRepo.findById(input.courseId())
                .orElseThrow(() -> new RuntimeException("Curso no encontrado"));

        // Validar que el monto coincida con el precio del curso
        if (course.getPrice().compareTo(input.amount()) != 0) {
            logService.logWarn("Transaction", "create_transaction",
                    "El monto no coincide con el precio del curso", userId);
        }

        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setCourse(course);
        transaction.setAmount(input.amount());
        transaction.setCurrency(input.currency());
        transaction.setPaymentMethod(input.paymentMethod());
        transaction.setPaymentGateway(input.paymentGateway());
        transaction.setDescription(
                input.description() != null ? input.description() : "Pago por curso: " + course.getTitle());
        transaction.setMetadata(input.metadata());

        Transaction saved = transactionRepo.save(transaction);
        logService.logInfo("Transaction", "create_transaction",
                "Transacción creada: " + saved.getTransactionReference(), userId);

        return toDto(saved);
    }

    // Actualizar transacción (generalmente desde webhook de pasarela)
    public TransactionDto updateTransaction(UUID transactionId, UpdateTransactionIn input) {
        logService.logInfo("Transaction", "update_transaction",
                "Actualizando transacción: " + transactionId, null);

        Transaction transaction = transactionRepo.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transacción no encontrada"));

        if (input.externalTransactionId() != null) {
            transaction.setExternalTransactionId(input.externalTransactionId());
        }

        if (input.status() != null) {
            TransactionStatus oldStatus = transaction.getStatus();
            transaction.setStatus(input.status());

            // Si la transacción se completa, activar la matrícula
            if (input.status() == TransactionStatus.COMPLETED &&
                    oldStatus != TransactionStatus.COMPLETED) {
                transaction.setCompletedAt(Instant.now());
                handleSuccessfulPayment(transaction);
            }

            // Si falla, registrar error
            if (input.status() == TransactionStatus.FAILED) {
                transaction.setErrorMessage(input.errorMessage());
                logService.logError("Transaction", "payment_failed",
                        "Pago fallido: " + transaction.getTransactionReference() +
                                " - " + input.errorMessage(),
                        transaction.getUser().getId());
            }

            // Si se reembolsa
            if (input.status() == TransactionStatus.REFUNDED &&
                    oldStatus != TransactionStatus.REFUNDED) {
                transaction.setRefundedAt(Instant.now());
                handleRefund(transaction);
            }
        }

        if (input.metadata() != null) {
            transaction.setMetadata(input.metadata());
        }

        Transaction saved = transactionRepo.save(transaction);
        logService.logInfo("Transaction", "update_transaction",
                "Transacción actualizada: " + saved.getTransactionReference(), null);

        return toDto(saved);
    }

    // Manejar pago exitoso
    private void handleSuccessfulPayment(Transaction transaction) {
        logService.logInfo("Transaction", "payment_success",
                "Procesando pago exitoso: " + transaction.getTransactionReference(),
                transaction.getUser().getId());

        try {
            // Buscar o crear matrícula
            UserEntity student = transaction.getUser();
            CourseEntity course = transaction.getCourse();

            var enrollmentOpt = enrollmentRepo.findByStudentAndCourse(student, course);

            Enrollment enrollment;
            if (enrollmentOpt.isPresent()) {
                enrollment = enrollmentOpt.get();
                // Actualizar matrícula existente
                enrollmentService.activateEnrollment(enrollment.getId(), transaction.getAmount());
            } else {
                // Crear nueva matrícula
                CreateEnrollmentIn createEnrollment = new CreateEnrollmentIn(
                        course.getId(),
                        "Matrícula creada automáticamente por pago");
                EnrollmentDto newEnrollment = enrollmentService.createEnrollment(
                        createEnrollment, student.getId());
                enrollmentService.activateEnrollment(
                        newEnrollment.id(), transaction.getAmount());
                enrollment = enrollmentRepo.findById(newEnrollment.id()).orElseThrow();
            }

            // Vincular transacción con matrícula
            transaction.setEnrollment(enrollment);
            transactionRepo.save(transaction);

            logService.logInfo("Transaction", "payment_success",
                    "Matrícula activada exitosamente", student.getId());

        } catch (Exception e) {
            logService.logError("Transaction", "payment_success",
                    "Error al activar matrícula: " + e.getMessage(),
                    transaction.getUser().getId(), e);
            throw new RuntimeException("Error al procesar pago exitoso", e);
        }
    }

    // Manejar reembolso
    private void handleRefund(Transaction transaction) {
        logService.logInfo("Transaction", "refund",
                "Procesando reembolso: " + transaction.getTransactionReference(),
                transaction.getUser().getId());

        // Si hay matrícula asociada, cancelarla
        if (transaction.getEnrollment() != null) {
            try {
                enrollmentService.cancelEnrollment(
                        transaction.getEnrollment().getId(),
                        transaction.getUser().getId());
            } catch (Exception e) {
                logService.logError("Transaction", "refund",
                        "Error al cancelar matrícula en reembolso: " + e.getMessage(),
                        transaction.getUser().getId(), e);
            }
        }
    }

    // Obtener transacción por referencia
    @Transactional(readOnly = true)
    public TransactionDto getTransactionByReference(String reference) {
        Transaction transaction = transactionRepo.findByTransactionReference(reference)
                .orElseThrow(() -> new RuntimeException("Transacción no encontrada"));

        return toDto(transaction);
    }

    // Obtener transacciones de un usuario
    @Transactional(readOnly = true)
    public Page<TransactionDto> getUserTransactions(UUID userId, Pageable pageable) {
        UserEntity user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return transactionRepo.findByUserOrderByCreatedAtDesc(user, pageable)
                .map(this::toDto);
    }

    // Obtener transacciones de un curso
    @Transactional(readOnly = true)
    public Page<TransactionDto> getCourseTransactions(UUID courseId, Pageable pageable) {
        CourseEntity course = courseRepo.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Curso no encontrado"));

        return transactionRepo.findByCourseOrderByCreatedAtDesc(course, pageable)
                .map(this::toDto);
    }

    // Obtener todas las transacciones (admin)
    @Transactional(readOnly = true)
    public Page<TransactionDto> getAllTransactions(Pageable pageable) {
        return transactionRepo.findAll(pageable).map(this::toDto);
    }

    // Obtener transacciones por estado
    @Transactional(readOnly = true)
    public Page<TransactionDto> getTransactionsByStatus(TransactionStatus status, Pageable pageable) {
        return transactionRepo.findByStatusOrderByCreatedAtDesc(status, pageable)
                .map(this::toDto);
    }

    // Calcular ingresos totales
    @Transactional(readOnly = true)
    public BigDecimal getTotalRevenue() {
        return transactionRepo.calculateTotalRevenue();
    }

    // Calcular ingresos de un curso
    @Transactional(readOnly = true)
    public BigDecimal getCourseRevenue(UUID courseId) {
        CourseEntity course = courseRepo.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Curso no encontrado"));

        return transactionRepo.calculateRevenueForCourse(course);
    }

    // Calcular ingresos en rango de fechas
    @Transactional(readOnly = true)
    public BigDecimal getRevenueBetween(Instant startDate, Instant endDate) {
        return transactionRepo.calculateRevenueBetween(startDate, endDate);
    }

    // Obtener transacciones completadas en rango de fechas
    @Transactional(readOnly = true)
    public List<TransactionDto> getCompletedTransactionsBetween(Instant startDate, Instant endDate) {
        return transactionRepo.findCompletedTransactionsBetween(startDate, endDate)
                .stream()
                .map(this::toDto)
                .toList();
    }

    // Convertir a DTO
    private TransactionDto toDto(Transaction transaction) {
        return new TransactionDto(
                transaction.getId(),
                transaction.getUser().getId(),
                transaction.getUser().getUsername(),
                transaction.getCourse() != null ? transaction.getCourse().getId() : null,
                transaction.getCourse() != null ? transaction.getCourse().getTitle() : null,
                transaction.getEnrollment() != null ? transaction.getEnrollment().getId() : null,
                transaction.getTransactionReference(),
                transaction.getExternalTransactionId(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getPaymentMethod(),
                transaction.getStatus(),
                transaction.getPaymentGateway(),
                transaction.getDescription(),
                transaction.getErrorMessage(),
                transaction.getCreatedAt() != null ? transaction.getCreatedAt().toString() : null,
                transaction.getUpdatedAt() != null ? transaction.getUpdatedAt().toString() : null,
                transaction.getCompletedAt() != null ? transaction.getCompletedAt().toString() : null,
                transaction.getRefundedAt() != null ? transaction.getRefundedAt().toString() : null,
                transaction.getRefundReason());
    }
}
