package org.example.api.services;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.api.dto.request.DeleteProjectProfileRequestDto;
import org.example.api.dto.request.JoinToProjectRequestDto;
import org.example.api.dto.request.ProjectProfileRequestDto;
import org.example.api.dto.response.DeleteProjectProfileResponseDto;
import org.example.api.entities.JoinRequestEntity;
import org.example.api.entities.ProjectProfileEntity;
import org.example.api.enums.JoinRequestStatus;
import org.example.api.exceptions.BadRequestException;
import org.example.api.exceptions.NotFoundException;
import org.example.api.repositories.JoinRequestRepository;
import org.example.api.repositories.ProjectProfileRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProjectProfileService {
    ProjectProfileRepository projectProfileRepository;
    JoinRequestRepository joinRequestRepository;

    public void sendJoinToProjectRequest(JoinToProjectRequestDto dto) {
        boolean existingRequest = joinRequestRepository.existsByUserIdAndProjectProfileId(
                dto.getUserId(),
                dto.getProjectProfileId()
        );
        if (existingRequest) {
            throw new BadRequestException("You already have a pending join request for this project");
        }
        ProjectProfileEntity projectProfileEntity = projectProfileRepository.findById(dto.getProjectProfileId())
                .orElseThrow(() -> new NotFoundException(
                        "Project profile not found with id: " + dto.getProjectProfileId()
                ));
        JoinRequestEntity joinRequestEntity = joinRequestEntityBuilder(
                dto.getUserId(),
                projectProfileEntity,
                dto.getMessage()
        );
        joinRequestRepository.save(joinRequestEntity);
        log.error(joinRequestEntity.getId().toString() + " " + joinRequestEntity.getMessage());
    }

    public ProjectProfileEntity getProjectProfile(UUID projectProfileId) {
        return projectProfileRepository.findById(projectProfileId)
                .orElseThrow(() -> new NotFoundException(
                        "Project profile not found with id: " + projectProfileId
                ));
    }

    public DeleteProjectProfileResponseDto deleteProjectProfile(DeleteProjectProfileRequestDto dto) {
        ProjectProfileEntity entity = projectProfileRepository.findById(dto.getProjectProfileId())
                .orElseThrow(() -> new NotFoundException(
                        "Project profile not found with id: " + dto.getProjectProfileId()
                ));
        if (!entity.getAdminIds().contains(dto.getUserId())) {
            throw new BadRequestException("Project profile can delete only admin");
        }
        projectProfileRepository.delete(entity);
        log.info("Profile deleted successfully. Profile ID: {}, user ID: {}",
                dto.getProjectProfileId(),
                dto.getUserId()
        );

        return DeleteProjectProfileResponseDto.builder()
                .status("success")
                .build();
    }

    public ProjectProfileEntity saveProjectProfileEntity(ProjectProfileRequestDto dto) {
        ProjectProfileEntity entity = projectProfileEntityBuilder(dto);
        return projectProfileRepository.save(entity);
    }

    public ProjectProfileEntity updateProjectProfile(UUID projectProfileId,
                                                          ProjectProfileRequestDto dto) {
        ProjectProfileEntity existingEntity = projectProfileRepository.findById(projectProfileId)
                .orElseThrow(() -> new NotFoundException(
                        "Project profile not found with id: " + projectProfileId
                ));

        updateEntityFromDto(existingEntity, dto);

        return projectProfileRepository.save(existingEntity);
    }

    private void updateEntityFromDto(ProjectProfileEntity entity,
                                     ProjectProfileRequestDto dto) {
        entity.setProjectName(dto.getProjectName());
        entity.setDescription(dto.getDescription());
        entity.setIsPublic(dto.getIsPublic());
        entity.setTags(dto.getTags());
        entity.setChatId(dto.getChatId());
        entity.setAvatarId(dto.getAvatarId());
        entity.setAdminIds(dto.getAdminIds());
        entity.setMemberIds(dto.getMemberIds());
    }

    private ProjectProfileEntity projectProfileEntityBuilder(ProjectProfileRequestDto dto) {
        return ProjectProfileEntity.builder()
                .projectName(dto.getProjectName())
                .description(dto.getDescription())
                .isPublic(dto.getIsPublic())
                .tags(dto.getTags())
                .adminIds(dto.getAdminIds())
                .memberIds(dto.getMemberIds())
                .build();
    }

    private JoinRequestEntity joinRequestEntityBuilder(
            UUID userId,
            ProjectProfileEntity projectProfileEntity,
            String message
    ) {
        return JoinRequestEntity.builder()
                .userId(userId)
                .projectProfile(projectProfileEntity)
                .message(message)
                .build();
    }
}
