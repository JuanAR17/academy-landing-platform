package com.academia.backend.service;

import com.academia.backend.domain.*;
import com.academia.backend.dto.CourseDto;
import com.academia.backend.dto.in.CreateCourseIn;
import com.academia.backend.dto.in.UpdateCourseIn;
import com.academia.backend.repo.CourseRepo;
import com.academia.backend.repo.UserRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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

    // Crear curso (profesores y administradores)
    public CourseDto createCourse(CreateCourseIn input, UUID creatorId) {
        logService.logInfo(MODULE_COURSE, ACTION_CREATE_COURSE, "Creando nuevo curso: " + input.title(), creatorId);

        UserEntity creator = userRepo.findById(creatorId)
                .orElseThrow(() -> new IllegalArgumentException(ERROR_USER_NOT_FOUND));

        if (creator.getRole() != Role.TEACHER && creator.getRole() != Role.ADMIN
                && creator.getRole() != Role.SUPER_ADMIN) {
            logService.logError(MODULE_COURSE, ACTION_CREATE_COURSE, "Usuario no autorizado para crear cursos",
                    creatorId);
            throw new IllegalArgumentException(ERROR_ONLY_TEACHERS_CREATE);
        }

        UserEntity instructor = null;
        if (input.instructorId() != null) {
            instructor = userRepo.findById(input.instructorId())
                    .orElseThrow(() -> new IllegalArgumentException(ERROR_USER_NOT_FOUND));
            // Verificar que el instructor sea profesor
            if (instructor.getRole() != Role.TEACHER && instructor.getRole() != Role.ADMIN
                    && instructor.getRole() != Role.SUPER_ADMIN) {
                throw new IllegalArgumentException("El instructor debe ser un profesor o administrador");
            }
        } else {
            if (creator.getRole() != Role.ADMIN && creator.getRole() != Role.SUPER_ADMIN) {
                instructor = creator;
            }
        }

        CourseEntity course = new CourseEntity();
        course.setTitle(input.title());
        course.setDescription(input.description());
        course.setShortDescription(input.shortDescription());
        course.setThumbnailUrl(input.thumbnailUrl());
        course.setVideoPreviewUrl(input.videoPreviewUrl());
        course.setTeacher(instructor);
        course.setPrice(input.price());
        course.setDurationHours(input.durationHours());
        course.setDifficultyLevel(input.difficultyLevel());
        course.setMaxStudents(input.maxStudents());
        course.setCategory(input.category());

        if (input.tags() != null) {
            course.setTags(input.tags().toArray(new String[0]));
        }
        if (input.requirements() != null) {
            course.setRequirements(input.requirements().toArray(new String[0]));
        }
        if (input.learningOutcomes() != null) {
            course.setLearningOutcomes(input.learningOutcomes().toArray(new String[0]));
        }

        CourseEntity saved = courseRepo.save(course);
        logService.logInfo(MODULE_COURSE, ACTION_CREATE_COURSE, "Curso creado exitosamente: " + saved.getId(),
                creatorId);

        return toDto(saved);
    }

    // Actualizar curso
    public CourseDto updateCourse(UUID courseId, UpdateCourseIn input, UUID userId) {
        logService.logInfo(MODULE_COURSE, ACTION_UPDATE_COURSE, "Actualizando curso: " + courseId, userId);

        CourseEntity course = courseRepo.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ERROR_COURSE_NOT_FOUND));

        UserEntity user = userRepo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ERROR_USER_NOT_FOUND));

        // Solo el profesor del curso o admin pueden actualizar
        if (course.getTeacher() != null && !course.getTeacher().getId().equals(userId)
                && user.getRole() != Role.ADMIN) {
            logService.logError(MODULE_COURSE, ACTION_UPDATE_COURSE, "Usuario no autorizado para actualizar curso",
                    userId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, ERROR_NOT_AUTHORIZED_UPDATE);
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
        if (input.duration() != null)
            course.setDurationHours(input.duration());
        if (input.level() != null)
            course.setDifficultyLevel(input.level());
        if (input.maxStudents() != null)
            course.setMaxStudents(input.maxStudents());
        if (input.category() != null)
            course.setCategory(input.category());
        if (input.tags() != null)
            course.setTags(input.tags().toArray(new String[0]));
        if (input.requirements() != null)
            course.setRequirements(input.requirements().toArray(new String[0]));
        if (input.learningOutcomes() != null)
            course.setLearningOutcomes(input.learningOutcomes().toArray(new String[0]));
        if (input.instructorId() != null) {
            UserEntity instructor = userRepo.findById(input.instructorId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ERROR_USER_NOT_FOUND));
            // Verificar que el instructor sea profesor o admin
            if (instructor.getRole() != Role.TEACHER && instructor.getRole() != Role.ADMIN
                    && instructor.getRole() != Role.SUPER_ADMIN) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "El instructor debe ser un profesor o administrador");
            }
            course.setTeacher(instructor);
            // Publicar automáticamente cuando se asigna instructor
            course.setStatus(CourseStatus.PUBLISHED);
        }

        if (input.status() != null) {
            CourseStatus newStatus = CourseStatus.valueOf(input.status().toUpperCase());
            if (newStatus == CourseStatus.PUBLISHED && course.getTeacher() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Debe asignar un instructor antes de publicar el curso");
            }
            course.setStatus(newStatus);
        }

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

        // Validar que el curso tenga un instructor asignado antes de publicar
        if (course.getTeacher() == null) {
            logService.logError(MODULE_COURSE, ACTION_PUBLISH_COURSE,
                    "No se puede publicar un curso sin instructor asignado", userId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Debe asignar un instructor antes de publicar el curso");
        }

        // Verificar autorización: el instructor del curso o admin
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

    // Listar todos los cursos (solo para admins)
    @Transactional(readOnly = true)
    public Page<CourseDto> getAllCourses(Pageable pageable) {
        return courseRepo.findAll(pageable)
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

        // Solo administradores pueden eliminar cursos
        if (user.getRole() != Role.ADMIN && user.getRole() != Role.SUPER_ADMIN) {
            logService.logError(MODULE_COURSE, ACTION_DELETE_COURSE, "Solo administradores pueden eliminar cursos",
                    userId);
            throw new IllegalArgumentException("Solo administradores pueden eliminar cursos");
        }

        course.setStatus(CourseStatus.ARCHIVED);
        courseRepo.save(course);

        logService.logInfo(MODULE_COURSE, ACTION_DELETE_COURSE, "Curso archivado: " + courseId, userId);
    }

    // Convertir a DTO
    private CourseDto toDto(CourseEntity course) {
        UUID teacherId = course.getTeacher() != null ? course.getTeacher().getId() : null;
        String teacherName = course.getTeacher() != null ? course.getTeacher().getFirstName() + " " +
                (course.getTeacher().getLastName() != null ? course.getTeacher().getLastName() : "") : null;
        return new CourseDto(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getShortDescription(),
                course.getThumbnailUrl(),
                course.getVideoPreviewUrl(),
                teacherId,
                teacherName,
                course.getPrice(),
                course.getDurationHours(),
                course.getDifficultyLevel(),
                course.getStatus(),
                course.getMaxStudents(),
                course.getCurrentStudents(),
                course.getCategory(),
                course.getTags() != null ? List.of(course.getTags()) : List.of(),
                course.getRequirements() != null ? List.of(course.getRequirements()) : List.of(),
                course.getLearningOutcomes() != null ? List.of(course.getLearningOutcomes()) : List.of(),
                course.getCreatedAt() != null ? course.getCreatedAt().toString() : null,
                course.getUpdatedAt() != null ? course.getUpdatedAt().toString() : null,
                course.getPublishedAt() != null ? course.getPublishedAt().toString() : null);
    }
}
