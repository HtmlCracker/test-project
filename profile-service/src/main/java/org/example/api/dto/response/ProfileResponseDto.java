package org.example.api.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProfileResponseDto {
    @NonNull
    UUID id;

    @NonNull
    String email;

    @NonNull
    String name;

    String surname;

    String description;

    @NonNull
    List<String> roles;

    @JsonProperty("registration_date")
    @NonNull
    Instant registrationDate;
}
