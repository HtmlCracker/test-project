package org.example.api.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.UUID;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name="email_quene")
public class EmailQueneEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    String sendTo;

    String htmlTemplateName;

    String subject;

    HashMap<String, Object> variables;

    int priority;

    @Builder.Default
    Instant addDate = Instant.now(Clock.system(ZoneId.of("Europe/Moscow")));
}
