package com.academia.backend.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name = "leads")
public class LeadEntity {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name="ts_utc", nullable=false)
  private Instant tsUtc;

  @Column(nullable=false, length=320)
  private String email;

  @Column(nullable=false, length=255)
  private String name;

  @Column(name="courses_json", nullable=false, columnDefinition="text")
  private String coursesJson;

  // getters/setters ...
}

