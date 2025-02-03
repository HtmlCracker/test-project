package org.example.api.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "folder")
public class FolderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(nullable = false)
    String folderName;

    @Builder.Default
    int fileCount = 0;

    @Builder.Default
    Boolean isLast = true;

    @Builder.Default
    Long usedStorageByte = 0L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    FolderEntity parent;

    @Column(nullable = false)
    String path;

    @Builder.Default
    Instant createdDate = Instant.now(Clock.system(ZoneId.of("Europe/Moscow")));

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    List<FolderEntity> childrens = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "folder", cascade = CascadeType.ALL, orphanRemoval = true)
    List<FileEntity> files = new ArrayList<>();
}
