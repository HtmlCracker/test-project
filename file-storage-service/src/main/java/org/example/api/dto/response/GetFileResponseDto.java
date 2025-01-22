package org.example.api.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GetFileResponseDto {
    byte[] byteData;
    String fileName;
    Long fileSize;
}
