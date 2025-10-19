package com.academia.backend.web;

import com.academia.backend.dto.EnrollmentDto;
import com.academia.backend.dto.in.CreateEnrollmentIn;
import com.academia.backend.dto.in.UpdateEnrollmentIn;
import com.academia.backend.service.EnrollmentService;
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
@RequestMapping("/api/enrollments")
@Tag(name = "Enrollments", description = "Gestión de matrículas")
@SecurityRequirement(name = "bearerAuth")
public class EnrollmentController {
  
  private final EnrollmentService enrollmentService;
  private final JwtService jwtService;
  
  public EnrollmentController(EnrollmentService enrollmentService, JwtService jwtService) {
    this.enrollmentService = enrollmentService;
    this.jwtService = jwtService;
  }
  
  @PostMapping
  @Operation(summary = "Crear matrícula", description = "Un estudiante se inscribe en un curso")
  public ResponseEntity<EnrollmentDto> createEnrollment(
      @Valid @RequestBody CreateEnrollmentIn input,
      @RequestHeader("Authorization") String authHeader) {
    
    UUID userId = jwtService.extractUserIdFromHeader(authHeader);
    EnrollmentDto enrollment = enrollmentService.createEnrollment(input, userId);
    return ResponseEntity.status(HttpStatus.CREATED).body(enrollment);
  }
  
  @GetMapping("/{id}")
  @Operation(summary = "Obtener matrícula por ID")
  public ResponseEntity<EnrollmentDto> getEnrollmentById(@PathVariable UUID id) {
    EnrollmentDto enrollment = enrollmentService.getEnrollmentById(id);
    return ResponseEntity.ok(enrollment);
  }
  
  @GetMapping("/my-enrollments")
  @Operation(summary = "Obtener mis matrículas", description = "Lista de matrículas del usuario autenticado")
  public ResponseEntity<Page<EnrollmentDto>> getMyEnrollments(
      @RequestHeader("Authorization") String authHeader,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    
    UUID userId = jwtService.extractUserIdFromHeader(authHeader);
    Pageable pageable = PageRequest.of(page, size);
    Page<EnrollmentDto> enrollments = enrollmentService.getStudentEnrollments(userId, pageable);
    return ResponseEntity.ok(enrollments);
  }
  
  @GetMapping("/my-active-courses")
  @Operation(summary = "Obtener mis cursos activos", description = "Lista de cursos en los que estoy activamente matriculado")
  public ResponseEntity<List<EnrollmentDto>> getMyActiveCourses(
      @RequestHeader("Authorization") String authHeader) {
    
    UUID userId = jwtService.extractUserIdFromHeader(authHeader);
    List<EnrollmentDto> enrollments = enrollmentService.getActiveEnrollments(userId);
    return ResponseEntity.ok(enrollments);
  }
  
  @GetMapping("/student/{studentId}")
  @Operation(summary = "Obtener matrículas de un estudiante", description = "Solo admin o el propio estudiante")
  public ResponseEntity<Page<EnrollmentDto>> getStudentEnrollments(
      @PathVariable UUID studentId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    
    Pageable pageable = PageRequest.of(page, size);
    Page<EnrollmentDto> enrollments = enrollmentService.getStudentEnrollments(studentId, pageable);
    return ResponseEntity.ok(enrollments);
  }
  
  @GetMapping("/course/{courseId}")
  @Operation(summary = "Obtener estudiantes de un curso", description = "Lista de matrículas de un curso")
  public ResponseEntity<Page<EnrollmentDto>> getCourseEnrollments(
      @PathVariable UUID courseId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    
    Pageable pageable = PageRequest.of(page, size);
    Page<EnrollmentDto> enrollments = enrollmentService.getCourseEnrollments(courseId, pageable);
    return ResponseEntity.ok(enrollments);
  }
  
  @PutMapping("/{id}")
  @Operation(summary = "Actualizar matrícula", description = "Actualizar progreso, estado, etc.")
  public ResponseEntity<EnrollmentDto> updateEnrollment(
      @PathVariable UUID id,
      @Valid @RequestBody UpdateEnrollmentIn input,
      @RequestHeader("Authorization") String authHeader) {
    
    UUID userId = jwtService.extractUserIdFromHeader(authHeader);
    EnrollmentDto enrollment = enrollmentService.updateEnrollment(id, input, userId);
    return ResponseEntity.ok(enrollment);
  }
  
  @DeleteMapping("/{id}")
  @Operation(summary = "Cancelar matrícula")
  public ResponseEntity<Void> cancelEnrollment(
      @PathVariable UUID id,
      @RequestHeader("Authorization") String authHeader) {
    
    UUID userId = jwtService.extractUserIdFromHeader(authHeader);
    enrollmentService.cancelEnrollment(id, userId);
    return ResponseEntity.noContent().build();
  }
}
