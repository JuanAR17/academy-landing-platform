package com.academia.backend.repo;

import com.academia.backend.domain.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CountryRepo extends JpaRepository<Country, Long> {

    @Query("SELECT c FROM Country c LEFT JOIN FETCH c.states s LEFT JOIN FETCH s.cities")
    List<Country> findAllWithStatesAndCities();
}