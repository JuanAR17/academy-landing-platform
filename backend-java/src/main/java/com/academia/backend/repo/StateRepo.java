package com.academia.backend.repo;

import com.academia.backend.domain.State;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StateRepo extends JpaRepository<State, Long> {

    @Query("SELECT s FROM State s WHERE s.country.id = :countryId ORDER BY s.name")
    List<State> findByCountryId(@Param("countryId") Long countryId);
}
