package org.example.api.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.api.enums.FileStates;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "file_info")
public class FileInfoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    UUID id;

    @Column(nullable = false)
    String originalFileName;

    @Column(nullable = false)
    Long originalFileSize;

    @Column(nullable = false)
    String filePath;

    @Column(nullable = false)
    FileStates fileState;

    @Column(nullable = false)
    Boolean isCompressed = false;

    @Column(nullable = false)
    String fileHash;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    LocalDateTime createdAt;

    @UpdateTimestamp
    LocalDateTime updatedAt;

    Long compressedSize;
}
