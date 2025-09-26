package org.example.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProjectProfileRequestDto {
    @NotBlank(message = "Project name can't be empty")
    @NotNull
    String projectName;

    String description;

    @NotNull(message = "Field \"isPublic\" can't be empty")
    Boolean isPublic;

    @NotEmpty(message = "Field \"tags\" cannot be empty")
    List<String> tags;

    UUID chatId;

    UUID avatarId;

    @NotEmpty(message = "Field \"adminIds\" cannot be empty")
    List<UUID> adminIds;

    @NotEmpty(message = "Field \"memberIds\" cannot be empty")
    List<UUID> memberIds;
}
