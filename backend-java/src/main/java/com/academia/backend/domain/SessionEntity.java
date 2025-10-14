package com.academia.backend.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="sessions")
public class SessionEntity {
  @Id @Column(columnDefinition="uuid")
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

  @PrePersist
  void pre() {
    if (id == null) id = UUID.randomUUID();
    if (createdAt == null) createdAt = Instant.now();
  }

  // --- getters/setters ---
  public UUID getId() { return id; }
  public void setId(UUID id) { this.id = id; }

  public UUID getUserId() { return userId; }
  public void setUserId(UUID userId) { this.userId = userId; }

  public byte[] getRefreshTokenHash() { return refreshTokenHash; }
  public void setRefreshTokenHash(byte[] refreshTokenHash) { this.refreshTokenHash = refreshTokenHash; }

  public Instant getCreatedAt() { return createdAt; }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

  public Instant getLastUsedAt() { return lastUsedAt; }
  public void setLastUsedAt(Instant lastUsedAt) { this.lastUsedAt = lastUsedAt; }

  public Instant getExpiresAt() { return expiresAt; }
  public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }

  public boolean isRevoked() { return revoked; }
  public void setRevoked(boolean revoked) { this.revoked = revoked; }

  public String getIp() { return ip; }
  public void setIp(String ip) { this.ip = ip; }

  public String getUserAgent() { return userAgent; }
  public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
}

