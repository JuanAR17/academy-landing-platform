package com.academia.backend.web;

import com.academia.backend.dto.CourseDto;
import com.academia.backend.dto.in.CreateCourseIn;
import com.academia.backend.dto.in.UpdateCourseIn;
import com.academia.backend.service.CourseService;
import com.academia.backend.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/courses")
@Tag(name = "Courses", description = "Gestión de cursos")
public class CourseController {
  
  private final CourseService courseService;
  private final JwtService jwtService;
  
  public CourseController(CourseService courseService, JwtService jwtService) {
    this.courseService = courseService;
    this.jwtService = jwtService;
  }
  
  @PostMapping
  @SecurityRequirement(name = "bearerAuth")
  @Operation(summary = "Crear nuevo curso", description = "Solo profesores y administradores pueden crear cursos")
  public ResponseEntity<CourseDto> createCourse(
      @Valid @RequestBody CreateCourseIn input,
      @RequestHeader("Authorization") String authHeader) {
    
    UUID userId = jwtService.extractUserIdFromHeader(authHeader);
    CourseDto course = courseService.createCourse(input, userId);
    return ResponseEntity.status(HttpStatus.CREATED).body(course);
  }
  
  @GetMapping("/{id}")
  @Operation(summary = "Obtener curso por ID")
  public ResponseEntity<CourseDto> getCourseById(@PathVariable UUID id) {
    CourseDto course = courseService.getCourseById(id);
    return ResponseEntity.ok(course);
  }
  
  @GetMapping
  @Operation(summary = "Listar cursos publicados")
  public ResponseEntity<Page<CourseDto>> getPublishedCourses(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    
    Pageable pageable = PageRequest.of(page, size);
    Page<CourseDto> courses = courseService.getPublishedCourses(pageable);
    return ResponseEntity.ok(courses);
  }
  
  @GetMapping("/category/{category}")
  @Operation(summary = "Listar cursos por categoría")
  public ResponseEntity<Page<CourseDto>> getCoursesByCategory(
      @PathVariable String category,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    
    Pageable pageable = PageRequest.of(page, size);
    Page<CourseDto> courses = courseService.getCoursesByCategory(category, pageable);
    return ResponseEntity.ok(courses);
  }
  
  @GetMapping("/teacher/{teacherId}")
  @Operation(summary = "Listar cursos de un profesor")
  public ResponseEntity<List<CourseDto>> getCoursesByTeacher(@PathVariable UUID teacherId) {
    List<CourseDto> courses = courseService.getCoursesByTeacher(teacherId);
    return ResponseEntity.ok(courses);
  }
  
  @GetMapping("/search")
  @Operation(summary = "Buscar cursos por palabra clave")
  public ResponseEntity<Page<CourseDto>> searchCourses(
      @RequestParam String keyword,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    
    Pageable pageable = PageRequest.of(page, size);
    Page<CourseDto> courses = courseService.searchCourses(keyword, pageable);
    return ResponseEntity.ok(courses);
  }
  
  @GetMapping("/categories")
  @Operation(summary = "Obtener lista de categorías disponibles")
  public ResponseEntity<List<String>> getCategories() {
    List<String> categories = courseService.getCategories();
    return ResponseEntity.ok(categories);
  }
  
  @PutMapping("/{id}")
  @SecurityRequirement(name = "bearerAuth")
  @Operation(summary = "Actualizar curso", description = "Solo el profesor del curso o administradores")
  public ResponseEntity<CourseDto> updateCourse(
      @PathVariable UUID id,
      @Valid @RequestBody UpdateCourseIn input,
      @RequestHeader("Authorization") String authHeader) {
    
    UUID userId = jwtService.extractUserIdFromHeader(authHeader);
    CourseDto course = courseService.updateCourse(id, input, userId);
    return ResponseEntity.ok(course);
  }
  
  @PostMapping("/{id}/publish")
  @SecurityRequirement(name = "bearerAuth")
  @Operation(summary = "Publicar curso", description = "Cambiar estado del curso a publicado")
  public ResponseEntity<CourseDto> publishCourse(
      @PathVariable UUID id,
      @RequestHeader("Authorization") String authHeader) {
    
    UUID userId = jwtService.extractUserIdFromHeader(authHeader);
    CourseDto course = courseService.publishCourse(id, userId);
    return ResponseEntity.ok(course);
  }
  
  @DeleteMapping("/{id}")
  @SecurityRequirement(name = "bearerAuth")
  @Operation(summary = "Eliminar curso", description = "Archiva el curso (soft delete)")
  public ResponseEntity<Void> deleteCourse(
      @PathVariable UUID id,
      @RequestHeader("Authorization") String authHeader) {
    
    UUID userId = jwtService.extractUserIdFromHeader(authHeader);
    courseService.deleteCourse(id, userId);
    return ResponseEntity.noContent().build();
  }
}
