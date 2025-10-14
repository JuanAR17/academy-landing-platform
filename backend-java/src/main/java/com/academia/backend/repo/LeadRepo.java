package com.academia.backend.repo;

import com.academia.backend.domain.LeadEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeadRepo extends JpaRepository<LeadEntity, Long> {}

