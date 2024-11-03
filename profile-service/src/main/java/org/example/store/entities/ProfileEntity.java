package org.example.store.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
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
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    String email;

    String password;

    String name;

    @Builder.Default
    String surname = null;

    @Builder.Default
    String description = null;

    ArrayList<String> roles;

    @Builder.Default
    Instant registrationDate = Instant.now(Clock.system(ZoneId.of("Europe/Moscow")));
}
