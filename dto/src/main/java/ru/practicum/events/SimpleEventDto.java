package ru.practicum.events;


import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class SimpleEventDto {
    Long id;
    String title;
    Boolean commentDisabled;
}
