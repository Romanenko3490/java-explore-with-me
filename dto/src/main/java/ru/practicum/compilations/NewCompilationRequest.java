package ru.practicum.compilations;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.Set;

@Value
@Builder
@Jacksonized
public class NewCompilationRequest {

    @NonNull
    Set<Long> events;

    @NonNull
    Boolean pinned;

    @NotBlank
    String title;
}
