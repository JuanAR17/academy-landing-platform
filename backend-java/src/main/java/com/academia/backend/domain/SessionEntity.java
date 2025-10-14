package com.academia.backend.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="sessions")
public class SessionEntity {
  @Id
  @Column(columnDefinition="uuid")
  private UUID id;

  @Column(name="user_id", nullable=false, columnDefinition="uuid")
  private UUID userId;

  @Lob
  @Column(name="refresh_token_hash", nullable=false, unique=true)
  private byte[] refreshTokenHash;

  @Column(name="created_at", nullable=false)  private Instant createdAt;
  @Column(name="last_used_at")               private Instant lastUsedAt;
  @Column(name="expires_at", nullable=false) private Instant expiresAt;
  @Column(name="is_revoked", nullable=false) private boolean revoked = false;
  @Column(name="ip")                         private String ip;
  @Column(name="user_agent")                 private String userAgent;

  @PrePersist void pre() {
    if (id == null) id = UUID.randomUUID();
    if (createdAt == null) createdAt = Instant.now();
  }

  // getters/setters ...
}

