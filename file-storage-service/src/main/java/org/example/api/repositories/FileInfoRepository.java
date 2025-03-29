package org.example.api.repositories;

import org.example.api.entities.FileInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FileInfoRepository extends JpaRepository<FileInfoEntity, UUID> {

}
