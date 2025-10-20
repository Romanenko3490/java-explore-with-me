package ru.practicum.comments;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;


@Value
@Builder
@Jacksonized
public class UpdateCommentRequest {

    @NotBlank
    private String text;

}
