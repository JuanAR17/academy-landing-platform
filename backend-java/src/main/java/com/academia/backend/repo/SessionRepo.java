package com.academia.backend.repo;

import com.academia.backend.domain.SessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SessionRepo extends JpaRepository<SessionEntity, UUID> {
  Optional<SessionEntity> findByRefreshTokenHash(byte[] hash);
}

