package ru.practicum.comments;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class NewCommentRequest {

    @NotBlank
    @Size(max = 1000)
    private String text;

    private Long parentCommentId;

}
