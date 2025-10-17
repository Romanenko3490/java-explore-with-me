package ru.practicum.publics;


import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.practicum.categories.CategoryDto;
import ru.practicum.compilations.CompilationDto;
import ru.practicum.events.EventDto;
import ru.practicum.events.EventSortType;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@AllArgsConstructor
@Validated
public class PublicController {
    private final PublicWebClientCategories publicWebClientCategories;
    private final PublicWebClientCompilations publicWebClientCompilations;
    private final PublicWebClientEvents publicWebClientEvents;

    //Public: Категории
    //Публичный API для работы с категориями

    @GetMapping("/categories")
    public Mono<List<CategoryDto>> getCategories(
            @RequestParam(defaultValue = "0") @Min(0) Integer from,
            @RequestParam(defaultValue = "10") @Min(1) Integer size
    ) {
        return publicWebClientCategories.getCategories(from, size);
    }

    @GetMapping("/categories/{catId}")
    public CategoryDto getCategory(
            @PathVariable @Min(1) Long catId
    ) {
        return publicWebClientCategories.getCategory(catId);
    }

    //Public: Подборки событий
    //Публичный API для работы с подборками событий
    @GetMapping("/compilations")
    public Mono<List<CompilationDto>> getCompilations(
            @RequestParam(defaultValue = "0") @Min(0) Integer from,
            @RequestParam(defaultValue = "10") @Min(1) Integer size
    ) {
        return publicWebClientCompilations.getCompilations(from, size);
    }

    @GetMapping("/compilations/{compId}")
    public CompilationDto getCompilationById(
            @PathVariable @Min(1) Long compId
    ) {
        return publicWebClientCompilations.getCompilationById(compId);
    }

    //Public: События
    //Публичный API для работы с событиями

    @GetMapping("/events")
    public Mono<List<EventDto>> getEvents(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) LocalDateTime rangeStart,
            @RequestParam(required = false) LocalDateTime rangeEnd,
            @RequestParam(required = false, defaultValue = "false") Boolean onlyAvailable,
            @RequestParam(required = false) EventSortType sort,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size

    ) {
        return publicWebClientEvents.getEvents(text,
                categories,
                paid,
                rangeStart,
                rangeEnd,
                onlyAvailable,
                sort,
                from,
                size);
    }


    @GetMapping("/events/{id}")
    public EventDto getEvent(@PathVariable @Min(1) Long id) {
        return publicWebClientEvents.getEvent(id);
    }


}
