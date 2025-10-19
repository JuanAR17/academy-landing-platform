package com.academia.backend.repo;

import com.academia.backend.domain.CourseEntity;
import com.academia.backend.domain.CourseStatus;
import com.academia.backend.domain.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CourseRepo extends JpaRepository<CourseEntity, UUID> {

    // Buscar cursos por profesor
    List<CourseEntity> findByTeacher(UserEntity teacher);

    // Buscar cursos por estado
    Page<CourseEntity> findByStatus(CourseStatus status, Pageable pageable);

    // Buscar cursos publicados
    Page<CourseEntity> findByStatusOrderByCreatedAtDesc(CourseStatus status, Pageable pageable);

    // Buscar cursos por categoría
    Page<CourseEntity> findByCategoryAndStatus(String category, CourseStatus status, Pageable pageable);

    // Buscar cursos por profesor y estado
    List<CourseEntity> findByTeacherAndStatus(UserEntity teacher, CourseStatus status);

    // Buscar cursos por título (búsqueda parcial)
    @Query("SELECT c FROM CourseEntity c WHERE LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) AND c.status = :status")
    Page<CourseEntity> searchByTitleAndStatus(@Param("keyword") String keyword, @Param("status") CourseStatus status,
            Pageable pageable);

    // Buscar cursos por categoría o tag
    @Query("SELECT DISTINCT c FROM CourseEntity c LEFT JOIN c.tags t WHERE (c.category = :category OR :tag MEMBER OF c.tags) AND c.status = :status")
    Page<CourseEntity> findByCategoryOrTag(@Param("category") String category, @Param("tag") String tag,
            @Param("status") CourseStatus status, Pageable pageable);

    // Contar cursos por profesor
    long countByTeacher(UserEntity teacher);

    // Obtener categorías únicas
    @Query("SELECT DISTINCT c.category FROM CourseEntity c WHERE c.status = :status")
    List<String> findDistinctCategories(@Param("status") CourseStatus status);
}
