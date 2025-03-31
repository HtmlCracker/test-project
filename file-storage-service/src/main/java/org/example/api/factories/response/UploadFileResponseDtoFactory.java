package org.example.api.factories.response;

import org.example.api.dto.response.UploadFileResponseDto;
import org.example.api.entities.FileInfoEntity;
import org.springframework.stereotype.Component;

@Component
public class UploadFileResponseDtoFactory {
    public UploadFileResponseDto makeUploadFileResponseDto(FileInfoEntity entity) {
        return UploadFileResponseDto.builder()
                .fileId(entity.getId())
                .originalFileSize(entity.getOriginalFileSize())
                .fileState(entity.getFileState())
                .originalFileName(entity.getOriginalFileName())
                .build();
    }
}
