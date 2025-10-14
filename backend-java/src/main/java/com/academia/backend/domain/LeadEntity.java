package com.academia.backend.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "leads")
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

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }

  public Instant getTsUtc() { return tsUtc; }
  public void setTsUtc(Instant tsUtc) { this.tsUtc = tsUtc; }

  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  public String getCoursesJson() { return coursesJson; }
  public void setCoursesJson(String coursesJson) { this.coursesJson = coursesJson; }
}

