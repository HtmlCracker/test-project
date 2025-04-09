package org.example.api.services;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.api.dto.service.CompressedFileDto;
import org.example.api.entities.FileInfoEntity;
import org.example.api.exceptions.NotFoundException;
import org.example.api.repositories.FileInfoRepository;
import org.example.api.services.compression.CompressorService;
import org.example.api.services.encryption.EncryptorService;
import org.example.api.utils.FileUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UploadProcessor {
    FileInfoRepository fileInfoRepository;
    CompressorService compressorService;
    EncryptorService encryptorService;
    FileUtils fileUtils;

    public FileInfoEntity compress(String path) {
        CompressedFileDto dto = compressorService.compressFileAndWrite(path);
        String newPath = dto.getPath();
        deleteSourceAfterProcessing(path);

        return updateFileInfoEntity(path, newPath, dto.getCompressedSize());
    }

    private FileInfoEntity updateFileInfoEntity(String oldPath,
                                                String newPath,
                                                Long newSize) {
        FileInfoEntity fileInfoEntity = fileInfoRepository.findByFilePath(oldPath)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Entity with path: %s is not exists.", oldPath)
                ));
        fileInfoEntity.setFilePath(newPath);
        fileInfoEntity.setCurrentSize(newSize);
        return fileInfoRepository.save(fileInfoEntity);
    }

    private void deleteSourceAfterProcessing(String path) {
        fileUtils.deleteFile(path);
    }
}
