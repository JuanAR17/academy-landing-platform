package com.academia.backend.repo;

import com.academia.backend.domain.EpaycoCardEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EpaycoCardRepo extends JpaRepository<EpaycoCardEntity, Long> {
  Optional<EpaycoCardEntity> findByCardTokenId(String cardTokenId);
  Optional<EpaycoCardEntity> findByUserIdAndCardTokenId(UUID userId, String cardTokenId);
}
