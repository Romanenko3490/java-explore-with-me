package ru.practicum.events;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;

@Value
@Jacksonized
@Builder
@Getter
@Setter
public class NewEventRequest implements Dateable {

    @NotBlank
    @Size(min = 20, max = 2000)
    String annotation;

    @NotNull
    Long category;

    @NotBlank
    @Size(min = 20, max = 7000)
    String description;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime eventDate;

    @NotNull
    @Valid
    LocationDto location;

    @Builder.Default
    Boolean paid = false;

    @PositiveOrZero
    @Builder.Default
    Integer participantLimit = 0;

    @Builder.Default
    Boolean requestModeration = true;

    @NotBlank
    @Size(min = 3, max = 120)
    String title;

    @Builder.Default
    Boolean commentDisabled = false;


}
