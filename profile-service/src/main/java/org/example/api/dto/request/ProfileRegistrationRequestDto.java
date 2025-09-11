package org.example.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProfileRegistrationRequestDto {
    @NotBlank(message = "Name can't be empty")
    @NotNull
    String name;

    @NotBlank(message = "Surname can't be empty")
    @NotNull
    String surname;

    @NotBlank(message = "Roles can't be empty")
    @NotNull
    String roles;
}
