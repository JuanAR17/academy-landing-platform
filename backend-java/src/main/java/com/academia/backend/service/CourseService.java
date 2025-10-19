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

    private static final String MODULE_COURSE = "Course";
    private static final String ACTION_CREATE_COURSE = "create_course";
    private static final String ACTION_UPDATE_COURSE = "update_course";
    private static final String ACTION_PUBLISH_COURSE = "publish_course";
    private static final String ACTION_DELETE_COURSE = "delete_course";

    private static final String ERROR_COURSE_NOT_FOUND = "Curso no encontrado";
    private static final String ERROR_USER_NOT_FOUND = "Usuario no encontrado";
    private static final String ERROR_NOT_AUTHORIZED_UPDATE = "No autorizado para actualizar este curso";
    private static final String ERROR_NOT_AUTHORIZED = "No autorizado";
    private static final String ERROR_ONLY_TEACHERS_CREATE = "Solo profesores pueden crear cursos";

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
        logService.logInfo(MODULE_COURSE, ACTION_CREATE_COURSE, "Creando nuevo curso: " + input.title(), teacherId);

        UserEntity teacher = userRepo.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException(ERROR_USER_NOT_FOUND));

        if (teacher.getRole() != Role.TEACHER && teacher.getRole() != Role.ADMIN) {
            logService.logError(MODULE_COURSE, ACTION_CREATE_COURSE, "Usuario no autorizado para crear cursos",
                    teacherId);
            throw new IllegalArgumentException(ERROR_ONLY_TEACHERS_CREATE);
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
        logService.logInfo(MODULE_COURSE, ACTION_CREATE_COURSE, "Curso creado exitosamente: " + saved.getId(),
                teacherId);

        return toDto(saved);
    }

    // Actualizar curso
    public CourseDto updateCourse(UUID courseId, UpdateCourseIn input, UUID userId) {
        logService.logInfo(MODULE_COURSE, ACTION_UPDATE_COURSE, "Actualizando curso: " + courseId, userId);

        CourseEntity course = courseRepo.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException(ERROR_COURSE_NOT_FOUND));

        UserEntity user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(ERROR_USER_NOT_FOUND));

        // Solo el profesor del curso o admin pueden actualizar
        if (!course.getTeacher().getId().equals(userId) && user.getRole() != Role.ADMIN) {
            logService.logError(MODULE_COURSE, ACTION_UPDATE_COURSE, "Usuario no autorizado para actualizar curso",
                    userId);
            throw new IllegalArgumentException(ERROR_NOT_AUTHORIZED_UPDATE);
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
        logService.logInfo(MODULE_COURSE, ACTION_UPDATE_COURSE, "Curso actualizado: " + courseId, userId);

        return toDto(saved);
    }

    // Publicar curso
    public CourseDto publishCourse(UUID courseId, UUID userId) {
        logService.logInfo(MODULE_COURSE, ACTION_PUBLISH_COURSE, "Publicando curso: " + courseId, userId);

        CourseEntity course = courseRepo.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException(ERROR_COURSE_NOT_FOUND));

        UserEntity user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(ERROR_USER_NOT_FOUND));

        if (!course.getTeacher().getId().equals(userId) && user.getRole() != Role.ADMIN) {
            logService.logError(MODULE_COURSE, ACTION_PUBLISH_COURSE, "Usuario no autorizado", userId);
            throw new IllegalArgumentException(ERROR_NOT_AUTHORIZED);
        }

        course.setStatus(CourseStatus.PUBLISHED);
        course.setPublishedAt(Instant.now());

        CourseEntity saved = courseRepo.save(course);
        logService.logInfo(MODULE_COURSE, ACTION_PUBLISH_COURSE, "Curso publicado: " + courseId, userId);

        return toDto(saved);
    }

    // Obtener curso por ID
    @Transactional(readOnly = true)
    public CourseDto getCourseById(UUID courseId) {
        CourseEntity course = courseRepo.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException(ERROR_COURSE_NOT_FOUND));
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
                .orElseThrow(() -> new IllegalArgumentException("Profesor no encontrado"));

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
        logService.logInfo(MODULE_COURSE, ACTION_DELETE_COURSE, "Eliminando curso: " + courseId, userId);

        CourseEntity course = courseRepo.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException(ERROR_COURSE_NOT_FOUND));

        UserEntity user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(ERROR_USER_NOT_FOUND));

        if (!course.getTeacher().getId().equals(userId) && user.getRole() != Role.ADMIN) {
            logService.logError(MODULE_COURSE, ACTION_DELETE_COURSE, "Usuario no autorizado", userId);
            throw new IllegalArgumentException(ERROR_NOT_AUTHORIZED);
        }

        course.setStatus(CourseStatus.ARCHIVED);
        courseRepo.save(course);

        logService.logInfo(MODULE_COURSE, ACTION_DELETE_COURSE, "Curso archivado: " + courseId, userId);
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
                course.getTeacher().getFirstName() + " "
                        + (course.getTeacher().getLastName() != null ? course.getTeacher().getLastName() : ""),
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
