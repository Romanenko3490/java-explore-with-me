package ru.practicum.events;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import ru.practicum.categories.CategoryDto;
import ru.practicum.user.SimpleUserDto;

import java.time.LocalDateTime;

@Value
@Builder
@Jacksonized
public class EventShortDto {
    String annotation;
    CategoryDto category;
    Integer confirmedRequests;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime eventDate;

    Long id;
    SimpleUserDto initiator;
    Boolean paid;
    String title;
    Long views;


}
