package ru.practicum.user;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class SimpleUserDto {
    Long id;
    String name;
}
