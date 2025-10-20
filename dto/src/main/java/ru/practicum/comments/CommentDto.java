package ru.practicum.comments;

import ru.practicum.user.UserShortDto;

import java.time.LocalDateTime;

public class CommentDto {
    private Long id;
    private String text;
    private Long parentCommentId;
    private UserShortDto author;
    private LocalDateTime createdDate;

}
