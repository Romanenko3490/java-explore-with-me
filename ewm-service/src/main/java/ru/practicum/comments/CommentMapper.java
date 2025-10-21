package ru.practicum.comments;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "text", target = "text")
    @Mapping(source = "parentComment.id", target = "parentComment")
    @Mapping(source = "author.name", target = "author")
    @Mapping(source = "creationDate", target = "creationDate")
    CommentDto commentToCommentDto(Comment comment);
}
