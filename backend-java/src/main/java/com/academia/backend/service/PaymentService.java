package com.academia.backend.service;

import com.academia.backend.domain.*;
import com.academia.backend.dto.in.CreateEnrollmentIn;
import com.academia.backend.repo.PaymentRepo;
import com.academia.backend.repo.UserRepo;
import com.academia.backend.repo.CourseRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepo paymentRepo;
    private final UserRepo userRepo;
    private final CourseRepo courseRepo;
    private final LogService logService;
    private final EnrollmentService enrollmentService;

    public PaymentService(PaymentRepo paymentRepo, UserRepo userRepo, CourseRepo courseRepo,
            LogService logService, EnrollmentService enrollmentService) {
        this.paymentRepo = paymentRepo;
        this.userRepo = userRepo;
        this.courseRepo = courseRepo;
        this.logService = logService;
        this.enrollmentService = enrollmentService;
    }

    @Transactional
    public PaymentEntity createPayment(UUID userId, UUID courseId, BigDecimal amount, String description) {
        logService.logInfo("PAYMENT", "CREATE_PAYMENT",
                String.format("Creando pago - Usuario: %s, Curso: %s, Monto: %s", userId, courseId, amount), userId);

        UserEntity user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        CourseEntity course = courseRepo.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Curso no encontrado"));

        PaymentEntity payment = new PaymentEntity();
        payment.setUser(user);
        payment.setCourse(course);
        payment.setAmount(amount);
        payment.setDescription(description);
        payment.setStatus(PaymentStatus.PENDING);

        PaymentEntity saved = paymentRepo.save(payment);

        logService.logInfo("PAYMENT", "PAYMENT_CREATED",
                "Pago creado con ID: " + saved.getId(), userId);

        return saved;
    }

    @Transactional
    public void processPaymentConfirmation(Map<String, Object> paymentData) {
        String transactionId = (String) paymentData.get("x_ref_payco");
        String status = (String) paymentData.get("x_transaction_state");
        String epaycoRef = (String) paymentData.get("x_transaction_id");
        String amount = (String) paymentData.get("x_amount");
        String userId = (String) paymentData.get("x_extra1");
        String courseId = (String) paymentData.get("x_extra2");

        logService.logInfo("PAYMENT", "PROCESS_CONFIRMATION",
                String.format("Procesando confirmación - Transacción: %s, Estado: %s", transactionId, status), null);

        // Buscar el pago por referencia de ePayco
        PaymentEntity payment = paymentRepo.findByEpaycoRef(epaycoRef).orElse(null);

        if (payment == null) {
            // Si no existe, intentar crear uno nuevo basado en los datos
            if (userId != null && courseId != null) {
                try {
                    UUID userUUID = UUID.fromString(userId);
                    UUID courseUUID = UUID.fromString(courseId);
                    BigDecimal paymentAmount = new BigDecimal(amount);

                    payment = createPayment(userUUID, courseUUID, paymentAmount, "Pago desde ePayco");
                    payment.setEpaycoRef(epaycoRef);
                } catch (Exception e) {
                    logService.logError("PAYMENT", "CONFIRMATION_ERROR",
                            "Error creando pago desde confirmación: " + e.getMessage(), (UUID) null);
                    return;
                }
            } else {
                logService.logError("PAYMENT", "CONFIRMATION_ERROR",
                        "No se pudo encontrar o crear el pago para la confirmación", (UUID) null);
                return;
            }
        }

        // Actualizar el estado del pago
        PaymentStatus newStatus;
        switch (status.toLowerCase()) {
            case "aceptada":
            case "approved":
                newStatus = PaymentStatus.APPROVED;
                // Si el pago fue aprobado, matricular al estudiante
                enrollStudentOnPaymentApproval(payment);
                break;
            case "rechazada":
            case "rejected":
                newStatus = PaymentStatus.REJECTED;
                break;
            case "cancelled":
                newStatus = PaymentStatus.CANCELLED;
                break;
            default:
                newStatus = PaymentStatus.PENDING;
                break;
        }

        payment.setStatus(newStatus);
        payment.setTransactionId(transactionId);
        paymentRepo.save(payment);

        logService.logInfo("PAYMENT", "PAYMENT_UPDATED",
                String.format("Pago actualizado - ID: %s, Nuevo estado: %s", payment.getId(), newStatus), null);
    }

    private void enrollStudentOnPaymentApproval(PaymentEntity payment) {
        try {
            // Crear la matrícula usando el EnrollmentService
            CreateEnrollmentIn enrollmentData = new CreateEnrollmentIn(payment.getCourse().getId(),
                    "Matrícula automática por pago aprobado");
            enrollmentService.createEnrollment(enrollmentData, payment.getUser().getId());

            logService.logInfo("PAYMENT", "AUTO_ENROLLMENT",
                    String.format("Estudiante matriculado automáticamente - Usuario: %s, Curso: %s",
                            payment.getUser().getId(), payment.getCourse().getId()),
                    null);
        } catch (Exception e) {
            logService.logError("PAYMENT", "AUTO_ENROLLMENT_ERROR",
                    "Error matriculando estudiante automáticamente: " + e.getMessage(), (UUID) null);
        }
    }

    public List<PaymentEntity> getPaymentsByUser(UUID userId) {
        return paymentRepo.findByUserId(userId);
    }

    public PaymentEntity getPaymentById(UUID paymentId) {
        return paymentRepo.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Pago no encontrado"));
    }

    public PaymentEntity getPaymentByTransactionId(String transactionId) {
        return paymentRepo.findByTransactionId(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Pago no encontrado"));
    }
}