package org.example.api.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.api.enums.JoinRequestStatus;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "join_request")
public class JoinRequestEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    UUID id;

    @Column(updatable = false, nullable = false)
    UUID userId;

    String message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_profile_id", nullable = false)
    ProjectProfileEntity projectProfile;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    JoinRequestStatus status;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    LocalDateTime createdAt;

    @UpdateTimestamp
    LocalDateTime updatedAt;
}
