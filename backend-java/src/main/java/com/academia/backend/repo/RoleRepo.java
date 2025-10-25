package com.academia.backend.repo;

import com.academia.backend.domain.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepo extends JpaRepository<RoleEntity, Long> {
  Optional<RoleEntity> findByName(String name);
  Optional<RoleEntity> findByNameIgnoreCase(String name); // nuevo
}
