package org.example.api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DeleteProjectProfileRequestDto {
    @NotNull(message = "userId can't be null")
    UUID userId;
    @NotNull(message = "projectProfileId can't be null")
    UUID projectProfileId;
}
