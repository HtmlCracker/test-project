package org.example.api.repositories;

import org.example.api.entities.ProjectProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProjectProfileRepository extends JpaRepository<ProjectProfileEntity, UUID> {
    Optional<ProjectProfileEntity> findById(UUID uuid);
}
