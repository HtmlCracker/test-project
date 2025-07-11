package org.example.api.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.api.statemachine.state.upload.UploadFileState;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UploadFileResponseDto {
    UUID fileId;
    Long originalFileSize;
    UploadFileState fileState;
    String originalFileName;
}
