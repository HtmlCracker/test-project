package org.example.api.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "project_profile")
public class ProjectProfileEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    UUID id;

    @Column(nullable = false)
    String projectName;

    String description;

    @Column(nullable = false)
    Boolean isPublic;

    @Column(nullable = false)
    List<String> tags;

    UUID chatId;

    UUID avatarId;

    @Column(nullable = false)
    List<UUID> adminIds;

    @Column(nullable = false)
    List<UUID> memberIds;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    LocalDateTime createdAt;

    @UpdateTimestamp
    LocalDateTime updatedAt;
}
