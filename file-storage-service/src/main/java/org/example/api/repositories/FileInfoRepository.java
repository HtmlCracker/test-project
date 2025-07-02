package org.example.api.repositories;

import org.example.api.entities.FileInfoEntity;
import org.example.api.statemachine.state.upload.UploadFileState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FileInfoRepository extends JpaRepository<FileInfoEntity, UUID> {
    Optional<FileInfoEntity> findById(UUID id);
    Optional<FileInfoEntity> findByFilePath(String filePath);
    List<FileInfoEntity> findByFileStateIn(Collection<UploadFileState> states);
    List<FileInfoEntity> findByFileState(UploadFileState fileState);
}
