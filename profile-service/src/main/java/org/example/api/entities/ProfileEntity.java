package org.example.api.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "profile")
public class ProfileEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID) //todo взять из jwt
    UUID id;

    @Column(nullable = false)
    String email;

    @Column(nullable = false)
    String name;

    @Column(nullable = false)
    String surname;

    @Builder.Default
    String phoneNumber = null;

    @Column(name = "birth_date")
    @Builder.Default
    private LocalDate birthDate = null;

    @Builder.Default
    String description = null;

    @Column(nullable = false)
    List<String> roles;

    @Builder.Default
    Instant registrationDate = Instant.now(Clock.system(ZoneId.of("Europe/Moscow")));
}
