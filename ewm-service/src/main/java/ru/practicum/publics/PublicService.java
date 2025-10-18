package ru.practicum.publics;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.categories.Category;
import ru.practicum.categories.CategoryDto;
import ru.practicum.categories.CategoryMapper;
import ru.practicum.categories.CategoryRepository;
import ru.practicum.clints.StatsClient;
import ru.practicum.compilations.Compilation;
import ru.practicum.compilations.CompilationDto;
import ru.practicum.compilations.CompilationsMapper;
import ru.practicum.compilations.CompilationsRepository;
import ru.practicum.events.*;
import ru.practicum.exception.NotFoundException;
import ru.practicum.hit.NewHitRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
@Transactional(readOnly = true)
public class PublicService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final CompilationsRepository cCompilationsRepository;


    private final EventMapper eventMapper;
    private final CategoryMapper categoryMapper;
    private final CompilationsMapper compilationsMapper;

    private final StatsClient statsClient;

    public List<CategoryDto> getCategories(Integer from, Integer size) {
        log.info("Getting categories from {} size {}", from, size);
        Pageable pageable = PageRequest.of(from / size, size);

        List<Category> categories = categoryRepository.findAll(pageable).getContent();

        return categories.stream()
                .map(categoryMapper::categoryToCategoryDto)
                .collect(Collectors.toList());
    }

    public CategoryDto getCategory(Long id) {
        log.info("Getting category with id {}", id);

        Category category = categoryRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Category with id=" + id + " was not found")
        );

        return categoryMapper.categoryToCategoryDto(category);
    }

    public List<CompilationDto> getCompilations(Integer from, Integer size) {
        log.info("Get compilations from {} size {}", from, size);

        Pageable pageable = PageRequest.of(from / size, size);

        List<Compilation> compilations = cCompilationsRepository.findAll(pageable).getContent();

        return compilations.stream()
                .map(compilationsMapper::toDto)
                .collect(Collectors.toList());
    }


    public CompilationDto getCompilationById(Long compId) {
        log.info("Getting compilation with id {}", compId);

        Compilation compilation = cCompilationsRepository.findById(compId).orElseThrow(
                () -> new NotFoundException("Compilation with id=" + compId + " was not found")
        );

        return compilationsMapper.toDto(compilation);
    }

    public EventDto getEvent(Long id, String clientIp) {
        log.info("Getting event with id {}", id);

        Event event = eventRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Event with id=" + id + " was not found")
        );

        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Event with id=" + id + " was not found");
        }

        event.setViews(event.getViews() + 1);
        eventRepository.save(event);
        sendHitToStats(clientIp, "/events/" + id);


        return eventMapper.toDto(event);
    }


    public List<EventShortDto> getEvents(String text,
                                    List<Long> categories,
                                    Boolean paid,
                                    LocalDateTime rangeStart,
                                    LocalDateTime rangeEnd,
                                    Boolean onlyAvailable,
                                    EventSortType sort,
                                    Integer from,
                                    Integer size, String clientIp
    ) {
        List<Event> events = eventRepository.findByPublicFilters(
                text,
                categories,
                paid,
                rangeStart,
                rangeEnd,
                onlyAvailable,
                sort,
                from,
                size);

        if (!events.isEmpty()) {
            events.stream().forEach(event -> {
                sendHitToStats(clientIp, "/events");
                event.setViews(event.getViews() + 1);
                eventRepository.save(event);
            });
        }

        return events.stream()
                .map(eventMapper::toShortDto)
                .collect(Collectors.toList());

    }

    private void sendHitToStats(String clientIp, String uri) {
        NewHitRequest hitRequest = NewHitRequest.builder()
                .app("ewm-main-service")
                .uri(uri)
                .ip(clientIp)
                .timestamp(LocalDateTime.now())
                .build();

        statsClient.addHit(hitRequest);
    }


}
