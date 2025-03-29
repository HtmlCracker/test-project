package com.example.securityservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "password_reset")
public class PasswordResetEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    UUID token;

    @Column(unique = true)
    String email;
}



