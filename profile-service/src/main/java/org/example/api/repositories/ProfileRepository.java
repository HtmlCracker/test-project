package org.example.api.repositories;

import org.example.api.entities.ProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProfileRepository extends JpaRepository<ProfileEntity, UUID> {
    Optional<ProfileEntity> findByEmail(String email);
}
