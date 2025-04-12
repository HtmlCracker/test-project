package org.example.api.dto.service;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.api.entities.FolderEntity;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StoredFileDto {
    String path;
    Long fileSize;
    FolderEntity folderEntity;
}