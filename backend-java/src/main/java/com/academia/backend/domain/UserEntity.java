package com.academia.backend.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="users")
public class UserEntity {
  @Id
  @Column(columnDefinition="uuid")
  private UUID id;

  @Column(nullable=false, unique=true)
  private String email;

  @Column(name="password_hash", nullable=false, columnDefinition="text")
  private String passwordHash;

  @Column(name="created_at", nullable=false)
  private Instant createdAt;

  @PrePersist void pre() {
    if (id == null) id = UUID.randomUUID();
    if (createdAt == null) createdAt = Instant.now();
  }

  // getters/setters ...
}

