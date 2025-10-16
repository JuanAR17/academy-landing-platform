package com.academia.backend.repo;

import com.academia.backend.domain.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepo extends JpaRepository<UserEntity, UUID> {
  Optional<UserEntity> findByEmail(String email);
  Optional<UserEntity> findByUsername(String username);
  
  @Query("SELECT u FROM UserEntity u WHERE u.email = :identifier OR u.username = :identifier")
  Optional<UserEntity> findByEmailOrUsername(@Param("identifier") String identifier);
}

