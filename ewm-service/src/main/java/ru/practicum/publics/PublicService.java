package ru.practicum.publics;

import com.querydsl.core.types.OrderSpecifier;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.w3c.dom.events.EventException;
import ru.practicum.categories.Category;
import ru.practicum.categories.CategoryDto;
import ru.practicum.categories.CategoryMapper;
import ru.practicum.categories.CategoryRepository;
import ru.practicum.compilations.Compilation;
import ru.practicum.compilations.CompilationDto;
import ru.practicum.compilations.CompilationsMapper;
import ru.practicum.compilations.CompilationsRepository;
import ru.practicum.events.*;
import ru.practicum.exception.EventDataException;
import ru.practicum.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class PublicService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final CompilationsRepository cCompilationsRepository;


    private final EventMapper eventMapper;
    private final CategoryMapper categoryMapper;
    private final CompilationsMapper compilationsMapper;

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

    public EventDto getEvent(Long compId) {
        log.info("Getting event with id {}", compId);

        Event event = eventRepository.findById(compId).orElseThrow(
                () -> new NotFoundException("Event with id=" + compId + " was not found")
        );

        if (event.getState() != EventState.PUBLISHED) {
            throw new EventDataException("Event with id=" + compId + " was not published");
        }

        return eventMapper.toDto(event);
    }


    public List<EventDto> getEvents(String text,
                                    List<Long> categories,
                                    Boolean paid,
                                    LocalDateTime rangeStart,
                                    LocalDateTime rangeEnd,
                                    Boolean onlyAvailable,
                                    EventSortType sort,
                                    Integer from,
                                    Integer size
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

        return events.stream()
                .map(eventMapper::toDto)

                .collect(Collectors.toList());

    }





}
