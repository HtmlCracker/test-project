package org.example.api.repositories;

import org.example.api.entities.FolderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FolderRepository extends JpaRepository<FolderEntity, UUID> {
    Optional<FolderEntity> findById(UUID id);

    Optional<ArrayList<FolderEntity>> findByIsLast(Boolean isLast);

    FolderEntity findByFolderName(String folderName);

    @Query("SELECT f FROM FolderEntity f ORDER BY f.fileCount ASC")
    Optional<ArrayList<FolderEntity>> findWithMinFileCount();
}
