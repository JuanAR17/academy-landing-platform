package com.academia.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

import java.util.Arrays;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
  @Value("${app.corsAllowed:}")
  private String corsAllowed;

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    if (!corsAllowed.isBlank()) {
      String[] origins = Arrays.stream(corsAllowed.split(","))
          .map(String::trim).filter(s -> !s.isEmpty()).toArray(String[]::new);
      registry.addMapping("/**")
          .allowedOrigins(origins).allowedMethods("*").allowedHeaders("*").allowCredentials(true);
    }
  }
}

