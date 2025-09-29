package org.example.api.repositories;

import org.example.api.entities.JoinRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JoinRequestRepository extends JpaRepository<JoinRequestEntity, UUID> {
    Optional<JoinRequestEntity> findById(UUID uuid);
    boolean existsByUserIdAndProjectProfileId(
            UUID userId,
            UUID projectProfileId
    );
}
