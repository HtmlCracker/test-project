package com.example.securityservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "email_verify")
public class EmailVerifyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    String email;

    UUID token;
}
