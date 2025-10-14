package com.academia.backend.repo;

import com.academia.backend.domain.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepo extends JpaRepository<UserEntity, UUID> {
  Optional<UserEntity> findByEmail(String email);
}

