package com.academia.backend.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "epayco_cards")
public class EpaycoCardEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "card_token_id", nullable = false, unique = true, length = 40)
  private String cardTokenId;

  @Column(length = 20)
  private String brand;

  @Column(length = 4)
  private String last4;

  @Column(name = "exp_year", length = 4)
  private String expYear;

  @Column(name = "exp_month", length = 2)
  private String expMonth;

  @Column(name = "holder_name", length = 150)
  private String holderName;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();

  // getters/setters
  public Long getId() { return id; }
  public UUID getUserId() { return userId; }
  public void setUserId(UUID userId) { this.userId = userId; }
  public String getCardTokenId() { return cardTokenId; }
  public void setCardTokenId(String cardTokenId) { this.cardTokenId = cardTokenId; }
  public String getBrand() { return brand; }
  public void setBrand(String brand) { this.brand = brand; }
  public String getLast4() { return last4; }
  public void setLast4(String last4) { this.last4 = last4; }
  public String getExpYear() { return expYear; }
  public void setExpYear(String expYear) { this.expYear = expYear; }
  public String getExpMonth() { return expMonth; }
  public void setExpMonth(String expMonth) { this.expMonth = expMonth; }
  public String getHolderName() { return holderName; }
  public void setHolderName(String holderName) { this.holderName = holderName; }
  public Instant getCreatedAt() { return createdAt; }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
