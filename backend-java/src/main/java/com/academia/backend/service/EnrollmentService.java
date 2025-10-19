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

    // Constants for logging
    private static final String MODULE_ENROLLMENT = "Enrollment";
    private static final String ACTION_CREATE_ENROLLMENT = "create_enrollment";
    private static final String ACTION_UPDATE_ENROLLMENT = "update_enrollment";
    private static final String ACTION_CANCEL_ENROLLMENT = "cancel_enrollment";
    private static final String ACTION_ACTIVATE_ENROLLMENT = "activate_enrollment";

    // Constants for error messages
    private static final String ERROR_STUDENT_NOT_FOUND = "Estudiante no encontrado";
    private static final String ERROR_COURSE_NOT_FOUND = "Curso no encontrado";
    private static final String ERROR_ENROLLMENT_NOT_FOUND = "Matrícula no encontrada";
    private static final String ERROR_ONLY_STUDENTS_ENROLL = "Solo estudiantes pueden matricularse en cursos";
    private static final String ERROR_COURSE_NOT_AVAILABLE = "El curso no está disponible para matrícula";
    private static final String ERROR_ALREADY_ENROLLED = "Ya estás matriculado en este curso";
    private static final String ERROR_COURSE_FULL = "El curso ha alcanzado el máximo de estudiantes";
    private static final String ERROR_NOT_AUTHORIZED_UPDATE = "No autorizado para actualizar esta matrícula";
    private static final String ERROR_NOT_AUTHORIZED = "No autorizado";

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
        logService.logInfo(MODULE_ENROLLMENT, ACTION_CREATE_ENROLLMENT,
                "Creando matrícula para curso: " + input.courseId(), studentId);

        UserEntity student = userRepo.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException(ERROR_STUDENT_NOT_FOUND));

        if (student.getRole() != Role.STUDENT && student.getRole() != Role.ADMIN) {
            logService.logError(MODULE_ENROLLMENT, ACTION_CREATE_ENROLLMENT,
                    "Solo estudiantes pueden matricularse", studentId);
            throw new IllegalArgumentException(ERROR_ONLY_STUDENTS_ENROLL);
        }

        CourseEntity course = courseRepo.findById(input.courseId())
                .orElseThrow(() -> new IllegalArgumentException(ERROR_COURSE_NOT_FOUND));

        if (course.getStatus() != CourseStatus.PUBLISHED) {
            throw new IllegalArgumentException(ERROR_COURSE_NOT_AVAILABLE);
        }

        // Verificar si ya está matriculado
        if (enrollmentRepo.existsByStudentAndCourseAndStatus(student, course, EnrollmentStatus.ACTIVE)) {
            throw new IllegalArgumentException(ERROR_ALREADY_ENROLLED);
        }

        // Verificar cupo disponible
        if (course.getMaxStudents() != null &&
                course.getCurrentStudents() >= course.getMaxStudents()) {
            throw new IllegalArgumentException(ERROR_COURSE_FULL);
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setStatus(EnrollmentStatus.PENDING); // Pendiente hasta que se pague
        enrollment.setNotes(input.notes());
        enrollment.setAmountPaid(BigDecimal.ZERO);

        Enrollment saved = enrollmentRepo.save(enrollment);
        logService.logInfo(MODULE_ENROLLMENT, ACTION_CREATE_ENROLLMENT,
                "Matrícula creada: " + saved.getId(), studentId);

        return toDto(saved);
    }

    // Actualizar matrícula (progreso, estado, etc.)
    public EnrollmentDto updateEnrollment(UUID enrollmentId, UpdateEnrollmentIn input, UUID userId) {
        logService.logInfo(MODULE_ENROLLMENT, ACTION_UPDATE_ENROLLMENT,
                "Actualizando matrícula: " + enrollmentId, userId);

        Enrollment enrollment = enrollmentRepo.findById(enrollmentId)
                .orElseThrow(() -> new IllegalArgumentException(ERROR_ENROLLMENT_NOT_FOUND));

        UserEntity user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(ERROR_STUDENT_NOT_FOUND));

        // Solo el estudiante, profesor del curso o admin pueden actualizar
        boolean isOwner = enrollment.getStudent().getId().equals(userId);
        boolean isTeacher = enrollment.getCourse().getTeacher().getId().equals(userId);
        boolean isAdmin = user.getRole() == Role.ADMIN;

        if (!isOwner && !isTeacher && !isAdmin) {
            logService.logError(MODULE_ENROLLMENT, ACTION_UPDATE_ENROLLMENT, ERROR_NOT_AUTHORIZED, userId);
            throw new IllegalArgumentException(ERROR_NOT_AUTHORIZED_UPDATE);
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
        logService.logInfo(MODULE_ENROLLMENT, ACTION_UPDATE_ENROLLMENT,
                "Matrícula actualizada: " + enrollmentId, userId);

        return toDto(saved);
    }

    // Activar matrícula (después del pago)
    public EnrollmentDto activateEnrollment(UUID enrollmentId, BigDecimal amountPaid) {
        logService.logInfo(MODULE_ENROLLMENT, ACTION_ACTIVATE_ENROLLMENT,
                "Activando matrícula: " + enrollmentId, null);

        Enrollment enrollment = enrollmentRepo.findById(enrollmentId)
                .orElseThrow(() -> new IllegalArgumentException(ERROR_ENROLLMENT_NOT_FOUND));

        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        enrollment.setAmountPaid(amountPaid);

        // Incrementar contador de estudiantes del curso
        CourseEntity course = enrollment.getCourse();
        course.setCurrentStudents(course.getCurrentStudents() + 1);
        courseRepo.save(course);

        Enrollment saved = enrollmentRepo.save(enrollment);
        logService.logInfo(MODULE_ENROLLMENT, ACTION_ACTIVATE_ENROLLMENT,
                "Matrícula activada: " + enrollmentId, null);

        return toDto(saved);
    }

    // Obtener matrículas de un estudiante
    @Transactional(readOnly = true)
    public Page<EnrollmentDto> getStudentEnrollments(UUID studentId, Pageable pageable) {
        UserEntity student = userRepo.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado"));

        return enrollmentRepo.findByStudent(student, pageable)
                .map(this::toDto);
    }

    // Obtener matrículas activas de un estudiante
    @Transactional(readOnly = true)
    public List<EnrollmentDto> getActiveEnrollments(UUID studentId) {
        UserEntity student = userRepo.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado"));

        return enrollmentRepo.findByStudentAndStatus(student, EnrollmentStatus.ACTIVE)
                .stream()
                .map(this::toDto)
                .toList();
    }

    // Obtener estudiantes de un curso
    @Transactional(readOnly = true)
    public Page<EnrollmentDto> getCourseEnrollments(UUID courseId, Pageable pageable) {
        CourseEntity course = courseRepo.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Curso no encontrado"));

        return enrollmentRepo.findByCourse(course, pageable)
                .map(this::toDto);
    }

    // Obtener matrícula específica
    @Transactional(readOnly = true)
    public EnrollmentDto getEnrollmentById(UUID enrollmentId) {
        Enrollment enrollment = enrollmentRepo.findById(enrollmentId)
                .orElseThrow(() -> new IllegalArgumentException(ERROR_ENROLLMENT_NOT_FOUND));

        return toDto(enrollment);
    }

    // Cancelar matrícula
    public void cancelEnrollment(UUID enrollmentId, UUID userId) {
        logService.logInfo(MODULE_ENROLLMENT, ACTION_CANCEL_ENROLLMENT,
                "Cancelando matrícula: " + enrollmentId, userId);

        Enrollment enrollment = enrollmentRepo.findById(enrollmentId)
                .orElseThrow(() -> new IllegalArgumentException(ERROR_ENROLLMENT_NOT_FOUND));

        UserEntity user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(ERROR_STUDENT_NOT_FOUND));

        boolean isOwner = enrollment.getStudent().getId().equals(userId);
        boolean isAdmin = user.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            logService.logError(MODULE_ENROLLMENT, ACTION_CANCEL_ENROLLMENT, ERROR_NOT_AUTHORIZED, userId);
            throw new IllegalArgumentException(ERROR_NOT_AUTHORIZED);
        }

        enrollment.setStatus(EnrollmentStatus.CANCELLED);

        // Decrementar contador si estaba activo
        if (enrollment.getStatus() == EnrollmentStatus.ACTIVE) {
            CourseEntity course = enrollment.getCourse();
            course.setCurrentStudents(Math.max(0, course.getCurrentStudents() - 1));
            courseRepo.save(course);
        }

        enrollmentRepo.save(enrollment);
        logService.logInfo(MODULE_ENROLLMENT, ACTION_CANCEL_ENROLLMENT,
                "Matrícula cancelada: " + enrollmentId, userId);
    }

    // Convertir a DTO
    private EnrollmentDto toDto(Enrollment enrollment) {
        return new EnrollmentDto(
                enrollment.getId(),
                enrollment.getStudent().getId(),
                enrollment.getStudent().getFirstName() + " " +
                        (enrollment.getStudent().getLastName() != null ? enrollment.getStudent().getLastName() : ""),
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
