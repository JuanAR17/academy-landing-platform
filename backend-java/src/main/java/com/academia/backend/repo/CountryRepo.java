package com.academia.backend.repo;

import com.academia.backend.domain.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CountryRepo extends JpaRepository<Country, Long> {
    boolean existsByName(String name);
}