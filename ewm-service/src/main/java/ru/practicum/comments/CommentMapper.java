package ru.practicum.comments;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    CommentDto commentToCommentDto(Comment comment);
}
