package ru.practicum.categories;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class CategoryDto {
    Long id;
    String name;
}
