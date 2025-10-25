package ru.practicum.events;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EventMapper {
    EventDto toDto(Event event);

    EventShortDto toShortDto(Event event);

    SimpleEventDto toSimpleDto(Event event);
}
