package com.academia.backend.repo;

import com.academia.backend.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LeadRepo extends JpaRepository<LeadEntity, Long> {}

public interface UserRepo extends JpaRepository<UserEntity, UUID> {
  Optional<UserEntity> findByEmail(String email);
}

public interface SessionRepo extends JpaRepository<SessionEntity, UUID> {
  Optional<SessionEntity> findByRefreshTokenHash(byte[] hash);
}

