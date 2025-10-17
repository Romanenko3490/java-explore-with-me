package ru.practicum.compilations;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Value
@Builder
@Jacksonized
public class NewCompilationRequest implements Requestable {

    List<Long> events;

    @Builder.Default
    Boolean pinned = false;

    @NotBlank
    @Size(min = 1, max = 50, message = "Title size excided 50 characters")
    String title;
}
