package ru.practicum.compilations;

import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Value
@Builder
@Jacksonized
public class UpdateCompilationRequest implements Requestable {
    List<Long> events;
    Boolean pinned;

    @Size(max = 255, message = "Title size excided 255 characters")
    String title;


    public boolean hasEvents() {
        return events != null;
    }


    public boolean hasPinned() {
        return pinned != null;
    }

    public boolean hasTitle() {
        return title != null && !title.isBlank();
    }

}
