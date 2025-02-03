package org.example.api.factories;

import org.example.api.dto.response.UploadFileResponseDto;
import org.example.api.entities.FileEntity;
import org.springframework.stereotype.Component;

@Component
public class UploadFileResponseDtoFactory {
    public UploadFileResponseDto makeUploadFileResponseDto(FileEntity entity) {
        return UploadFileResponseDto.builder()
                .id(entity.getId())
                .fileSize(entity.getOriginalFileSizeByte())
                .fileName(entity.getOriginalFileName())
                .build();
    }
}
