package ru.practicum.categories;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class NewCategoryRequest {

    @NotBlank
    @Size(min = 1, max = 50)
    String name;
}
