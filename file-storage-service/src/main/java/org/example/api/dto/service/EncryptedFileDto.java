package org.example.api.dto.service;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.File;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EncryptedFileDto {
    String path;
    String encryptionKey;
    Long encryptedSize;
}
