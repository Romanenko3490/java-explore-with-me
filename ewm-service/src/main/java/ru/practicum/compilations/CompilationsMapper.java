package ru.practicum.compilations;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CompilationsMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "pinned", target = "pinned")
    @Mapping(source = "title", target = "title")
    CompilationDto toDto(Compilation compilation);
}
