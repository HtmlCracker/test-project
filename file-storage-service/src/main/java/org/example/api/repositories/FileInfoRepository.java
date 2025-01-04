package org.example.api.repositories;

import org.example.api.entities.FileInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FileInfoRepository extends JpaRepository<FileInfoEntity, UUID> {
}
