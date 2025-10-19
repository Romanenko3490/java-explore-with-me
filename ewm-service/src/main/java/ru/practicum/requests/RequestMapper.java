package ru.practicum.requests;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RequestMapper {

    @Mapping(source = "created", target = "created")
    @Mapping(source = "event.id", target = "event")
    @Mapping(source = "id", target = "id")
    @Mapping(source = "requester.id", target = "requester")
    @Mapping(source = "status", target = "status")
    RequestDto requestToRequestDto(Request request);
}
