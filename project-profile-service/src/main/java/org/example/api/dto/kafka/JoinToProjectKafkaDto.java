package org.example.api.dto.kafka;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JoinToProjectKafkaDto {
    UUID adminId;
    UUID userId;
    String message;
}
