package org.example.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProfileDto {
    @NonNull
    UUID id;

    @NonNull
    String email;

    @NonNull
    String password;

    @NonNull
    String name;

    String surname;

    String description;

    @NonNull
    ArrayList<String> roles;

    @JsonProperty("registration_date")
    @NonNull
    Instant registrationDate;
}
