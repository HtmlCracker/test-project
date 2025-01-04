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
@Table(name = "file_info")
public class FileInfoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    String originalName;

    Long fileSizeByte;

    @Builder.Default
    Instant uploadDate = Instant.now(Clock.system(ZoneId.of("Europe/Moscow")));

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "storage_id")
    StorageEntity storage;
}
