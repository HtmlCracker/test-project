package org.example.api.factories;

import org.example.api.dto.response.ProjectProfileResponseDto;
import org.example.api.entities.ProjectProfileEntity;
import org.springframework.stereotype.Component;

@Component
public class ProjectProfileResponseDtoFactory {
    public ProjectProfileResponseDto makeDto(ProjectProfileEntity entity) {
        return ProjectProfileResponseDto.builder()
                .id(entity.getId())
                .projectName(entity.getProjectName())
                .description(entity.getDescription())
                .tags(entity.getTags())
                .chatId(entity.getChatId())
                .avatarId(entity.getAvatarId())
                .adminIds(entity.getAdminIds())
                .memberIds(entity.getMemberIds())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
