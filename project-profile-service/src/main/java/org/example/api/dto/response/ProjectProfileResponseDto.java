package org.example.api.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProjectProfileResponseDto {
    UUID id;

    String projectName;

    String description;

    Boolean isPublic;

    List<String> tags;

    UUID chatId;

    UUID avatarId;

    List<UUID> adminIds;

    List<UUID> memberIds;

    LocalDateTime createdAt;

    LocalDateTime updatedAt;
}
