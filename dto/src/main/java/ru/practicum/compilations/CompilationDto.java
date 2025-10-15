package ru.practicum.compilations;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import ru.practicum.events.EventDto;

import java.util.Set;

@Value
@Jacksonized
@Builder
public class CompilationDto {
    Set<EventDto> events;
    Long id;
    Boolean pinned;
    String title;
}
