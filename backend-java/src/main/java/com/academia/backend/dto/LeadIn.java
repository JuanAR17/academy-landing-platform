package com.academia.backend.dto;

import com.academia.backend.domain.Course;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public class LeadIn {
  @Email public String email;
  @NotBlank public String name;
  @NotEmpty public Set<Course> courses;
}

