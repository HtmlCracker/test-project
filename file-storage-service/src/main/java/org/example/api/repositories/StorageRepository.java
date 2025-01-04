package org.example.api.repositories;

import org.example.api.entities.StorageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface StorageRepository extends JpaRepository<StorageEntity, UUID> {
}
