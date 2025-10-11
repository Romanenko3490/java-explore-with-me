package ru.practicum.categories;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class NewCategoryRequest {

    @NotBlank
    String name;
}
