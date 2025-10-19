package com.academia.backend.repo;

import com.academia.backend.domain.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CityRepo extends JpaRepository<City, Long> {

    @Query("SELECT c FROM City c WHERE c.state.id = :stateId ORDER BY c.name")
    List<City> findByStateId(@Param("stateId") Long stateId);
}
