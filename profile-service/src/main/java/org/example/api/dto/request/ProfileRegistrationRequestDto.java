package org.example.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProfileRegistrationRequestDto {
    @Email(message = "Email should be valid")
    String email;

    @NotNull(message = "Name can't be empty")
    String name;

    String surname;

    String description;

    List<String> roles;
}
