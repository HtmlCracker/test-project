package org.example.api.factories;

import org.example.api.dto.response.UploadFileResponseDto;
import org.example.api.entities.FileInfoEntity;
import org.springframework.stereotype.Component;

@Component
public class UploadFileResponseDtoFactory {
    public UploadFileResponseDto makeUploadFileResponseDto(FileInfoEntity entity) {
        return UploadFileResponseDto.builder()
                .id(entity.getId())
                .fileSize(entity.getFileSizeByte())
                .fileName(entity.getOriginalName())
                .build();
    }
}
