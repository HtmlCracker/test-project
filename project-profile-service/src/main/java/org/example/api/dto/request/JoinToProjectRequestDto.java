package org.example.api.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JoinToProjectRequestDto {
    UUID projectProfileId;
    UUID userId;
    String message;
}
