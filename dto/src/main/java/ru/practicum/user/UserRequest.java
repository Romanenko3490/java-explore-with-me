package ru.practicum.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Jacksonized
@Builder
public class UserRequest {

    @NotBlank
    @Email
    String email;

    @NotBlank
    String name;
}
