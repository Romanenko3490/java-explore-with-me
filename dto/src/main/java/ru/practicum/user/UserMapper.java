package ru.practicum.user;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto userToUserDto(UserProjection userDtoData);
}
