package ru.practicum.publics;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.categories.CategoryDto;
import ru.practicum.compilations.CompilationDto;
import ru.practicum.events.EventDto;
import ru.practicum.events.EventShortDto;
import ru.practicum.events.EventSortType;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@Slf4j
@AllArgsConstructor
public class PublicInternalController {
    private final PublicService publicService;


    @GetMapping("/categories")
    public List<CategoryDto> getCategories(
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        return publicService.getCategories(from, size);
    }

    @GetMapping("/categories/{catId}")
    public CategoryDto getCategory(
            @PathVariable Long catId
    ) {
        return publicService.getCategory(catId);
    }

    //Public: Подборки событий
    //Публичный API для работы с подборками событий
    @GetMapping("/compilations")
    public List<CompilationDto> getCompilations(
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        return publicService.getCompilations(from, size);
    }

    @GetMapping("/compilations/{compId}")
    public CompilationDto getCompilationById(
            @PathVariable Long compId
    ) {
        return publicService.getCompilationById(compId);
    }

    //Public: События
    //Публичный API для работы с событиями

    @GetMapping("/events")
    public List<EventShortDto> getEvents(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "false") Boolean onlyAvailable,
            @RequestParam(required = false) EventSortType sort,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestHeader("X-Client-IP") String clientIp

    ) {
        return publicService.getEvents(text,
                categories,
                paid,
                rangeStart,
                rangeEnd,
                onlyAvailable,
                sort,
                from,
                size,
                clientIp);
    }


    @GetMapping("/events/{id}")
    public EventDto getEvent(@PathVariable Long id,
                             @RequestHeader("X-Client-IP") String clientIp) {
        return publicService.getEvent(id, clientIp);
    }


}
