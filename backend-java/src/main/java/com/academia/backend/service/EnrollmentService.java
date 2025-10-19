package com.academia.backend.service;

import com.academia.backend.domain.*;
import com.academia.backend.dto.EnrollmentDto;
import com.academia.backend.dto.in.CreateEnrollmentIn;
import com.academia.backend.dto.in.UpdateEnrollmentIn;
import com.academia.backend.repo.CourseRepo;
import com.academia.backend.repo.EnrollmentRepo;
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
public class EnrollmentService {

    private final EnrollmentRepo enrollmentRepo;
    private final CourseRepo courseRepo;
    private final UserRepo userRepo;
    private final LogService logService;

    public EnrollmentService(EnrollmentRepo enrollmentRepo, CourseRepo courseRepo,
            UserRepo userRepo, LogService logService) {
        this.enrollmentRepo = enrollmentRepo;
        this.courseRepo = courseRepo;
        this.userRepo = userRepo;
        this.logService = logService;
    }

    // Crear matrícula (estudiante se inscribe en curso)
    public EnrollmentDto createEnrollment(CreateEnrollmentIn input, UUID studentId) {
        logService.logInfo("Enrollment", "create_enrollment",
                "Creando matrícula para curso: " + input.courseId(), studentId);

        UserEntity student = userRepo.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));

        if (student.getRole() != Role.STUDENT && student.getRole() != Role.ADMIN) {
            logService.logError("Enrollment", "create_enrollment",
                    "Solo estudiantes pueden matricularse", studentId);
            throw new RuntimeException("Solo estudiantes pueden matricularse en cursos");
        }

        CourseEntity course = courseRepo.findById(input.courseId())
                .orElseThrow(() -> new RuntimeException("Curso no encontrado"));

        if (course.getStatus() != CourseStatus.PUBLISHED) {
            throw new RuntimeException("El curso no está disponible para matrícula");
        }

        // Verificar si ya está matriculado
        if (enrollmentRepo.existsByStudentAndCourseAndStatus(student, course, EnrollmentStatus.ACTIVE)) {
            throw new RuntimeException("Ya estás matriculado en este curso");
        }

        // Verificar cupo disponible
        if (course.getMaxStudents() != null &&
                course.getCurrentStudents() >= course.getMaxStudents()) {
            throw new RuntimeException("El curso ha alcanzado el máximo de estudiantes");
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setStatus(EnrollmentStatus.PENDING); // Pendiente hasta que se pague
        enrollment.setNotes(input.notes());
        enrollment.setAmountPaid(BigDecimal.ZERO);

        Enrollment saved = enrollmentRepo.save(enrollment);
        logService.logInfo("Enrollment", "create_enrollment",
                "Matrícula creada: " + saved.getId(), studentId);

        return toDto(saved);
    }

    // Actualizar matrícula (progreso, estado, etc.)
    public EnrollmentDto updateEnrollment(UUID enrollmentId, UpdateEnrollmentIn input, UUID userId) {
        logService.logInfo("Enrollment", "update_enrollment",
                "Actualizando matrícula: " + enrollmentId, userId);

        Enrollment enrollment = enrollmentRepo.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Matrícula no encontrada"));

        UserEntity user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Solo el estudiante, profesor del curso o admin pueden actualizar
        boolean isOwner = enrollment.getStudent().getId().equals(userId);
        boolean isTeacher = enrollment.getCourse().getTeacher().getId().equals(userId);
        boolean isAdmin = user.getRole() == Role.ADMIN;

        if (!isOwner && !isTeacher && !isAdmin) {
            logService.logError("Enrollment", "update_enrollment", "No autorizado", userId);
            throw new RuntimeException("No autorizado para actualizar esta matrícula");
        }

        if (input.status() != null) {
            enrollment.setStatus(input.status());

            // Si se completa, registrar fecha y activar certificado
            if (input.status() == EnrollmentStatus.COMPLETED) {
                enrollment.setCompletedAt(Instant.now());
                enrollment.setProgressPercentage(100);
            }
        }

        if (input.progressPercentage() != null) {
            enrollment.setProgressPercentage(input.progressPercentage());

            // Auto-completar si llega al 100%
            if (input.progressPercentage() >= 100 && enrollment.getStatus() == EnrollmentStatus.ACTIVE) {
                enrollment.setStatus(EnrollmentStatus.COMPLETED);
                enrollment.setCompletedAt(Instant.now());
            }
        }

        if (input.notes() != null) {
            enrollment.setNotes(input.notes());
        }

        Enrollment saved = enrollmentRepo.save(enrollment);
        logService.logInfo("Enrollment", "update_enrollment",
                "Matrícula actualizada: " + enrollmentId, userId);

        return toDto(saved);
    }

    // Activar matrícula (después del pago)
    public EnrollmentDto activateEnrollment(UUID enrollmentId, BigDecimal amountPaid) {
        logService.logInfo("Enrollment", "activate_enrollment",
                "Activando matrícula: " + enrollmentId, null);

        Enrollment enrollment = enrollmentRepo.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Matrícula no encontrada"));

        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        enrollment.setAmountPaid(amountPaid);

        // Incrementar contador de estudiantes del curso
        CourseEntity course = enrollment.getCourse();
        course.setCurrentStudents(course.getCurrentStudents() + 1);
        courseRepo.save(course);

        Enrollment saved = enrollmentRepo.save(enrollment);
        logService.logInfo("Enrollment", "activate_enrollment",
                "Matrícula activada: " + enrollmentId, null);

        return toDto(saved);
    }

    // Obtener matrículas de un estudiante
    @Transactional(readOnly = true)
    public Page<EnrollmentDto> getStudentEnrollments(UUID studentId, Pageable pageable) {
        UserEntity student = userRepo.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));

        return enrollmentRepo.findByStudent(student, pageable)
                .map(this::toDto);
    }

    // Obtener matrículas activas de un estudiante
    @Transactional(readOnly = true)
    public List<EnrollmentDto> getActiveEnrollments(UUID studentId) {
        UserEntity student = userRepo.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));

        return enrollmentRepo.findByStudentAndStatus(student, EnrollmentStatus.ACTIVE)
                .stream()
                .map(this::toDto)
                .toList();
    }

    // Obtener estudiantes de un curso
    @Transactional(readOnly = true)
    public Page<EnrollmentDto> getCourseEnrollments(UUID courseId, Pageable pageable) {
        CourseEntity course = courseRepo.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Curso no encontrado"));

        return enrollmentRepo.findByCourse(course, pageable)
                .map(this::toDto);
    }

    // Obtener matrícula específica
    @Transactional(readOnly = true)
    public EnrollmentDto getEnrollmentById(UUID enrollmentId) {
        Enrollment enrollment = enrollmentRepo.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Matrícula no encontrada"));

        return toDto(enrollment);
    }

    // Cancelar matrícula
    public void cancelEnrollment(UUID enrollmentId, UUID userId) {
        logService.logInfo("Enrollment", "cancel_enrollment",
                "Cancelando matrícula: " + enrollmentId, userId);

        Enrollment enrollment = enrollmentRepo.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Matrícula no encontrada"));

        UserEntity user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        boolean isOwner = enrollment.getStudent().getId().equals(userId);
        boolean isAdmin = user.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            logService.logError("Enrollment", "cancel_enrollment", "No autorizado", userId);
            throw new RuntimeException("No autorizado");
        }

        enrollment.setStatus(EnrollmentStatus.CANCELLED);

        // Decrementar contador si estaba activo
        if (enrollment.getStatus() == EnrollmentStatus.ACTIVE) {
            CourseEntity course = enrollment.getCourse();
            course.setCurrentStudents(Math.max(0, course.getCurrentStudents() - 1));
            courseRepo.save(course);
        }

        enrollmentRepo.save(enrollment);
        logService.logInfo("Enrollment", "cancel_enrollment",
                "Matrícula cancelada: " + enrollmentId, userId);
    }

    // Convertir a DTO
    private EnrollmentDto toDto(Enrollment enrollment) {
        return new EnrollmentDto(
                enrollment.getId(),
                enrollment.getStudent().getId(),
                enrollment.getStudent().getNombre() + " " +
                        (enrollment.getStudent().getApellido() != null ? enrollment.getStudent().getApellido() : ""),
                enrollment.getStudent().getEmail(),
                enrollment.getCourse().getId(),
                enrollment.getCourse().getTitle(),
                enrollment.getStatus(),
                enrollment.getEnrolledAt() != null ? enrollment.getEnrolledAt().toString() : null,
                enrollment.getCompletedAt() != null ? enrollment.getCompletedAt().toString() : null,
                enrollment.getProgressPercentage(),
                enrollment.getAmountPaid(),
                enrollment.getNotes(),
                enrollment.isCertificateIssued(),
                enrollment.getCertificateUrl(),
                enrollment.getUpdatedAt() != null ? enrollment.getUpdatedAt().toString() : null);
    }
}
