package com.academia.backend.repo;

import com.academia.backend.domain.CourseEntity;
import com.academia.backend.domain.Enrollment;
import com.academia.backend.domain.EnrollmentStatus;
import com.academia.backend.domain.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EnrollmentRepo extends JpaRepository<Enrollment, UUID> {

    // Buscar matrículas de un estudiante
    Page<Enrollment> findByStudent(UserEntity student, Pageable pageable);

    // Buscar matrículas de un estudiante por estado
    List<Enrollment> findByStudentAndStatus(UserEntity student, EnrollmentStatus status);

    // Buscar matrículas de un curso
    Page<Enrollment> findByCourse(CourseEntity course, Pageable pageable);

    // Buscar matrículas de un curso por estado
    List<Enrollment> findByCourseAndStatus(CourseEntity course, EnrollmentStatus status);

    // Verificar si un estudiante está matriculado en un curso
    Optional<Enrollment> findByStudentAndCourse(UserEntity student, CourseEntity course);

    // Verificar si existe matrícula activa
    boolean existsByStudentAndCourseAndStatus(UserEntity student, CourseEntity course, EnrollmentStatus status);

    // Contar matrículas activas de un curso
    long countByCourseAndStatus(CourseEntity course, EnrollmentStatus status);

    // Contar matrículas de un estudiante
    long countByStudentAndStatus(UserEntity student, EnrollmentStatus status);

    // Obtener matrículas completadas de un estudiante
    @Query("SELECT e FROM Enrollment e WHERE e.student = :student AND e.status = 'COMPLETED' ORDER BY e.completedAt DESC")
    Page<Enrollment> findCompletedEnrollments(@Param("student") UserEntity student, Pageable pageable);

    // Obtener matrículas activas con bajo progreso
    @Query("SELECT e FROM Enrollment e WHERE e.status = 'ACTIVE' AND e.progressPercentage < :threshold")
    List<Enrollment> findActiveEnrollmentsWithLowProgress(@Param("threshold") Integer threshold);
}
