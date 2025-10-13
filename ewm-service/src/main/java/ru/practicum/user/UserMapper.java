package ru.practicum.user;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto userToUserDto(User user);
    SimpleUserDto userToSimpleUserDto(User user);
}
