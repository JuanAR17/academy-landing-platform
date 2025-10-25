package com.academia.backend.repo;

import com.academia.backend.domain.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepo extends JpaRepository<UserEntity, UUID> {

  // EXISTENTES (se mantienen)
  Optional<UserEntity> findByEmail(String email);
  Optional<UserEntity> findByUsername(String username);

  @Query("SELECT u FROM UserEntity u WHERE u.email = :identifier OR u.username = :identifier")
  Optional<UserEntity> findByEmailOrUsername(@Param("identifier") String identifier);

  // NUEVOS: case-insensitive (para CheckoutUserService.ensureUserForCheckout)
  Optional<UserEntity> findByEmailIgnoreCase(String email);
  Optional<UserEntity> findByUsernameIgnoreCase(String username);

  // (Opcional) versión ignore-case del OR si alguna vez la necesitas:
  // @Query("SELECT u FROM UserEntity u WHERE lower(u.email) = lower(:identifier) OR lower(u.username) = lower(:identifier)")
  // Optional<UserEntity> findByEmailOrUsernameIgnoreCase(@Param("identifier") String identifier);
}
