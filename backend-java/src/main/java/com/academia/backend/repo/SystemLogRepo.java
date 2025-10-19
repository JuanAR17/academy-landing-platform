package com.academia.backend.repo;

import com.academia.backend.domain.SystemLog;
import com.academia.backend.domain.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface SystemLogRepo extends JpaRepository<SystemLog, UUID> {
  
  // Buscar logs por nivel
  Page<SystemLog> findByLogLevelOrderByCreatedAtDesc(String logLevel, Pageable pageable);
  
  // Buscar logs por módulo
  Page<SystemLog> findByModuleOrderByCreatedAtDesc(String module, Pageable pageable);
  
  // Buscar logs por usuario
  Page<SystemLog> findByUserOrderByCreatedAtDesc(UserEntity user, Pageable pageable);
  
  // Buscar logs por nivel y módulo
  Page<SystemLog> findByLogLevelAndModuleOrderByCreatedAtDesc(String logLevel, String module, Pageable pageable);
  
  // Buscar logs en un rango de fechas
  @Query("SELECT l FROM SystemLog l WHERE l.createdAt BETWEEN :startDate AND :endDate ORDER BY l.createdAt DESC")
  Page<SystemLog> findLogsBetween(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate, Pageable pageable);
  
  // Buscar errores recientes
  @Query("SELECT l FROM SystemLog l WHERE l.logLevel = 'ERROR' AND l.createdAt > :since ORDER BY l.createdAt DESC")
  List<SystemLog> findRecentErrors(@Param("since") Instant since);
  
  // Contar errores por módulo
  @Query("SELECT l.module, COUNT(l) FROM SystemLog l WHERE l.logLevel = 'ERROR' AND l.createdAt > :since GROUP BY l.module")
  List<Object[]> countErrorsByModule(@Param("since") Instant since);
  
  // Buscar logs por ruta de request
  Page<SystemLog> findByRequestPathContainingOrderByCreatedAtDesc(String path, Pageable pageable);
  
  // Buscar logs con stack trace (errores)
  @Query("SELECT l FROM SystemLog l WHERE l.stackTrace IS NOT NULL ORDER BY l.createdAt DESC")
  Page<SystemLog> findLogsWithStackTrace(Pageable pageable);
}
