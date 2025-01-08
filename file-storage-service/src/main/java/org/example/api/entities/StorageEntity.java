package org.example.api.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

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
@Table(name = "storage")
public class StorageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(nullable = false)
    UUID ownerId;

    @Builder.Default
    Long usedStorageByte = 0L;

    @OneToMany(mappedBy = "storage", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    List<FileInfoEntity> files = new ArrayList<>();
}
