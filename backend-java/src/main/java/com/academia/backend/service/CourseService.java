package com.academia.backend.service;

import com.academia.backend.domain.*;
import com.academia.backend.dto.CourseDto;
import com.academia.backend.dto.in.CreateCourseIn;
import com.academia.backend.dto.in.UpdateCourseIn;
import com.academia.backend.repo.CourseRepo;
import com.academia.backend.repo.UserRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CourseService {

    private final CourseRepo courseRepo;
    private final UserRepo userRepo;
    private final LogService logService;

    public CourseService(CourseRepo courseRepo, UserRepo userRepo, LogService logService) {
        this.courseRepo = courseRepo;
        this.userRepo = userRepo;
        this.logService = logService;
    }

    // Crear curso (solo profesores)
    public CourseDto createCourse(CreateCourseIn input, UUID teacherId) {
        logService.logInfo("Course", "create_course", "Creando nuevo curso: " + input.title(), teacherId);

        UserEntity teacher = userRepo.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Profesor no encontrado"));

        if (teacher.getRole() != Role.TEACHER && teacher.getRole() != Role.ADMIN) {
            logService.logError("Course", "create_course", "Usuario no autorizado para crear cursos", teacherId);
            throw new RuntimeException("Solo profesores pueden crear cursos");
        }

        CourseEntity course = new CourseEntity();
        course.setTitle(input.title());
        course.setDescription(input.description());
        course.setShortDescription(input.shortDescription());
        course.setThumbnailUrl(input.thumbnailUrl());
        course.setVideoPreviewUrl(input.videoPreviewUrl());
        course.setTeacher(teacher);
        course.setPrice(input.price());
        course.setDurationHours(input.durationHours());
        course.setDifficultyLevel(input.difficultyLevel());
        course.setMaxStudents(input.maxStudents());
        course.setCategory(input.category());

        if (input.tags() != null) {
            course.setTags(input.tags());
        }
        if (input.requirements() != null) {
            course.setRequirements(input.requirements());
        }
        if (input.learningOutcomes() != null) {
            course.setLearningOutcomes(input.learningOutcomes());
        }

        CourseEntity saved = courseRepo.save(course);
        logService.logInfo("Course", "create_course", "Curso creado exitosamente: " + saved.getId(), teacherId);

        return toDto(saved);
    }

    // Actualizar curso
    public CourseDto updateCourse(UUID courseId, UpdateCourseIn input, UUID userId) {
        logService.logInfo("Course", "update_course", "Actualizando curso: " + courseId, userId);

        CourseEntity course = courseRepo.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Curso no encontrado"));

        UserEntity user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Solo el profesor del curso o admin pueden actualizar
        if (!course.getTeacher().getId().equals(userId) && user.getRole() != Role.ADMIN) {
            logService.logError("Course", "update_course", "Usuario no autorizado para actualizar curso", userId);
            throw new RuntimeException("No autorizado para actualizar este curso");
        }

        if (input.title() != null)
            course.setTitle(input.title());
        if (input.description() != null)
            course.setDescription(input.description());
        if (input.shortDescription() != null)
            course.setShortDescription(input.shortDescription());
        if (input.thumbnailUrl() != null)
            course.setThumbnailUrl(input.thumbnailUrl());
        if (input.videoPreviewUrl() != null)
            course.setVideoPreviewUrl(input.videoPreviewUrl());
        if (input.price() != null)
            course.setPrice(input.price());
        if (input.durationHours() != null)
            course.setDurationHours(input.durationHours());
        if (input.difficultyLevel() != null)
            course.setDifficultyLevel(input.difficultyLevel());
        if (input.maxStudents() != null)
            course.setMaxStudents(input.maxStudents());
        if (input.category() != null)
            course.setCategory(input.category());
        if (input.tags() != null)
            course.setTags(input.tags());
        if (input.requirements() != null)
            course.setRequirements(input.requirements());
        if (input.learningOutcomes() != null)
            course.setLearningOutcomes(input.learningOutcomes());

        CourseEntity saved = courseRepo.save(course);
        logService.logInfo("Course", "update_course", "Curso actualizado: " + courseId, userId);

        return toDto(saved);
    }

    // Publicar curso
    public CourseDto publishCourse(UUID courseId, UUID userId) {
        logService.logInfo("Course", "publish_course", "Publicando curso: " + courseId, userId);

        CourseEntity course = courseRepo.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Curso no encontrado"));

        UserEntity user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!course.getTeacher().getId().equals(userId) && user.getRole() != Role.ADMIN) {
            logService.logError("Course", "publish_course", "Usuario no autorizado", userId);
            throw new RuntimeException("No autorizado");
        }

        course.setStatus(CourseStatus.PUBLISHED);
        course.setPublishedAt(Instant.now());

        CourseEntity saved = courseRepo.save(course);
        logService.logInfo("Course", "publish_course", "Curso publicado: " + courseId, userId);

        return toDto(saved);
    }

    // Obtener curso por ID
    @Transactional(readOnly = true)
    public CourseDto getCourseById(UUID courseId) {
        CourseEntity course = courseRepo.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Curso no encontrado"));
        return toDto(course);
    }

    // Listar cursos publicados
    @Transactional(readOnly = true)
    public Page<CourseDto> getPublishedCourses(Pageable pageable) {
        return courseRepo.findByStatusOrderByCreatedAtDesc(CourseStatus.PUBLISHED, pageable)
                .map(this::toDto);
    }

    // Listar cursos por categoría
    @Transactional(readOnly = true)
    public Page<CourseDto> getCoursesByCategory(String category, Pageable pageable) {
        return courseRepo.findByCategoryAndStatus(category, CourseStatus.PUBLISHED, pageable)
                .map(this::toDto);
    }

    // Listar cursos de un profesor
    @Transactional(readOnly = true)
    public List<CourseDto> getCoursesByTeacher(UUID teacherId) {
        UserEntity teacher = userRepo.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Profesor no encontrado"));

        return courseRepo.findByTeacher(teacher).stream()
                .map(this::toDto)
                .toList();
    }

    // Buscar cursos
    @Transactional(readOnly = true)
    public Page<CourseDto> searchCourses(String keyword, Pageable pageable) {
        return courseRepo.searchByTitleAndStatus(keyword, CourseStatus.PUBLISHED, pageable)
                .map(this::toDto);
    }

    // Obtener categorías
    @Transactional(readOnly = true)
    public List<String> getCategories() {
        return courseRepo.findDistinctCategories(CourseStatus.PUBLISHED);
    }

    // Eliminar curso (soft delete -> archived)
    public void deleteCourse(UUID courseId, UUID userId) {
        logService.logInfo("Course", "delete_course", "Eliminando curso: " + courseId, userId);

        CourseEntity course = courseRepo.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Curso no encontrado"));

        UserEntity user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!course.getTeacher().getId().equals(userId) && user.getRole() != Role.ADMIN) {
            logService.logError("Course", "delete_course", "Usuario no autorizado", userId);
            throw new RuntimeException("No autorizado");
        }

        course.setStatus(CourseStatus.ARCHIVED);
        courseRepo.save(course);

        logService.logInfo("Course", "delete_course", "Curso archivado: " + courseId, userId);
    }

    // Convertir a DTO
    private CourseDto toDto(CourseEntity course) {
        return new CourseDto(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getShortDescription(),
                course.getThumbnailUrl(),
                course.getVideoPreviewUrl(),
                course.getTeacher().getId(),
                course.getTeacher().getNombre() + " "
                        + (course.getTeacher().getApellido() != null ? course.getTeacher().getApellido() : ""),
                course.getPrice(),
                course.getDurationHours(),
                course.getDifficultyLevel(),
                course.getStatus(),
                course.getMaxStudents(),
                course.getCurrentStudents(),
                course.getCategory(),
                course.getTags(),
                course.getRequirements(),
                course.getLearningOutcomes(),
                course.getCreatedAt() != null ? course.getCreatedAt().toString() : null,
                course.getUpdatedAt() != null ? course.getUpdatedAt().toString() : null,
                course.getPublishedAt() != null ? course.getPublishedAt().toString() : null);
    }
}
