package org.example.api.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.example.api.statemachine.state.upload.UploadFileState;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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
    String mimeType;

    @Column(nullable = false)
    String fileExt;

    @Column(nullable = true)
    String encryptionKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    UploadFileState fileState;

    @Column(nullable = false)
    @Builder.Default
    Boolean isCompressed = false;

    @Column(nullable = false)
    String fileHash;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    LocalDateTime createdAt;

    @UpdateTimestamp
    LocalDateTime updatedAt;

    Long currentSize;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id")
    FolderEntity folder;
}
