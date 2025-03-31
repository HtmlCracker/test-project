package org.example.api.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.api.enums.FileStates;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UploadFileResponseDto {
    UUID fileId;
    Long originalFileSize;
    FileStates fileState;
    String originalFileName;
}
