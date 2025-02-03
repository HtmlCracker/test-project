package org.example.api.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "file")
public class FileEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(nullable = false)
    String fileName;

    @Column(nullable = false)
    String originalFileName;

    @Column(nullable = false)
    Long originalFileSizeByte;

    Long compressedFileSizeByte;

    @Builder.Default
    Instant uploadDate = Instant.now(Clock.system(ZoneId.of("Europe/Moscow")));

    @Column(nullable = false)
    String path;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id")
    FolderEntity folder;
}
