package org.example.api.repositories;

import org.example.api.entities.ProjectProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ProjectProfileRepository extends JpaRepository<ProjectProfileEntity, UUID> {
    Optional<ProjectProfileEntity> findById(UUID uuid);
}
