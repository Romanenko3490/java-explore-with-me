package ru.practicum.admin;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.categories.*;
import ru.practicum.compilations.*;
import ru.practicum.events.*;
import ru.practicum.exception.CategoryConflictException;
import ru.practicum.exception.DataConflictException;
import ru.practicum.exception.EventDataException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class AdminService {
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final CompilationsRepository compilationsRepository;

    private final UserMapper userMapper;
    private final CategoryMapper categoryMapper;
    private  final EventMapper eventMapper;
    private final CompilationsMapper compilationsMapper;

    //Users
    public UserDto addUser(UserRequest request) {
        log.debug("Adding user {}", request);

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DataConflictException(
                    "could not execute statement; SQL [n/a];" +
                            " constraint " + request.getEmail() + ";" +
                            " nested exception is org.hibernate.exception." +
                            "ConstraintViolationException: could not execute statement");
        }

        User user = userRepository.save(User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .build());
        log.debug("Saved user: {}", user);

        return userMapper.userToUserDto(user);
    }

    @Transactional(readOnly = true)
    public List<UserDto> findAllUsersByIds(List<Long> ids, Integer from, Integer size) {
        log.info("Finding all users by ids {}", ids);
        Pageable pageable = PageRequest.of(from / size, size);

        if (ids != null && !ids.isEmpty()) {
            List<User> users = userRepository.findUsersByIds(ids, pageable);
            log.info("Found {} users by ids {}", users.size(), users);

            return users.stream()
                    .map(userMapper::userToUserDto)
                    .collect(Collectors.toList());
        }
        List<User> users = userRepository.findAllBy(pageable);
        log.info("Found {} users by ids {}", users.size(), users);

        return users.stream()
                .map(userMapper::userToUserDto)
                .collect(Collectors.toList());
    }

    public void deleteUser(Long id) {
        log.info("Deleting user {}", id);
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("User with id=" + id + " was not found");
        }

        userRepository.deleteById(id);
    }

    //Categories
    public CategoryDto saveCategory(NewCategoryRequest request) {
        log.info("Saving category {}", request);

        if (categoryRepository.existsByName((request.getName()))) {
            throw new DataConflictException("could not execute statement; SQL [n/a];" +
                    " constraint " + request.getName() + "; " +
                    "nested exception is org.hibernate.exception" +
                    ".ConstraintViolationException: could not execute statement");
        }

        Category category = categoryRepository.save(Category.builder()
                .name(request.getName())
                .build()
        );

        return categoryMapper.categoryToCategoryDto(category);
    }

    public void deleteCategory(Long id) {
        log.info("Deleting category {}", id);

        if (!categoryRepository.existsById(id)) {
            throw new NotFoundException("Category with id=" + id + " was not found");
        }
        if (eventRepository.existsByCategory_Id(id)) {
            throw new CategoryConflictException("The category is not empty");
        }

        categoryRepository.deleteById(id);
    }

    public CategoryDto updateCategory(Long id, UpdateCategoryRequest request) {
        log.info("Updating category {}", id);

        Category category = categoryRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Category with id=" + id + " was not found")
        );

        if (!request.getName().equals(category.getName()) && categoryRepository.existsByName(request.getName())) {
            throw new DataConflictException("could not execute statement; SQL [n/a];" +
                    " constraint " + request.getName() + "; " +
                    "nested exception is org.hibernate.exception" +
                    ".ConstraintViolationException: could not execute statement");
        }

        category.setName(request.getName());
        return categoryMapper.categoryToCategoryDto(categoryRepository.save(category));
    }

    //события
    @Transactional(readOnly = true)
    public List<EventDto> getEvents(
            List<Long> users,
            List<String> states,
            List<Long> categories,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            Integer from,
            Integer size
    ) {
        log.info("--------------------------------------------------------------------");
        log.info("Get by filters users: {}, states: {}, categorise: {}," +
                " rangeStart: {}, rangeEnd: {}, from: {}, size: {}", users, states, categories, rangeStart, rangeEnd, from, size);

        List<EventState> eventStates = parseStates(states);

        Pageable pageable = PageRequest.of(from / size, size);

        List<Event> eventList = eventRepository.findByAdminFilters(users,
                eventStates,
                categories,
                rangeStart,
                rangeEnd,
                pageable);

        return eventList.stream()
                .map(eventMapper::toDto)
                .collect(Collectors.toList());

    }

    private List<EventState> parseStates(List<String> states) {
        if (states != null && !states.isEmpty()) {
            return states.stream()
                    .map(state -> {
                        try {
                            return EventState.valueOf(state.toUpperCase());
                        } catch (IllegalArgumentException e) {
                            throw new ValidationException("Invalid event state: " + state);
                        }

                    })
                    .collect(Collectors.toList());
        }
        return null;
    }

    public EventDto updateEvent(Long eventId, UpdateEventRequest request) {
        log.info("Updating event {} , by request : {}", eventId, request);

        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (request.hasAnnotation()) {
            log.info("set annotation {}", request.getAnnotation());
            event.setAnnotation(request.getAnnotation());
        }
        if (request.hasCategory()) {
            log.info("set category {}", request.getCategory());
            event.setCategory(categoryRepository.findById(request.getCategory()).orElseThrow(
                    () -> new NotFoundException("Category with id=" + request.getCategory() + " was not found")
            ));
        }
        if (request.hasDescription()) {
            log.info("set description {}", request.getDescription());
            event.setDescription(request.getDescription());
        }
        if (request.hasLocation()) {
            Location location = new Location(request.getLocation().getLat(), request.getLocation().getLon());
            log.info("set location {}", location);
            event.setLocation(location);
        }
        if (request.hasParticipantLimit()) {
            log.info("set participant limit {}", request.getParticipantLimit());
            event.setParticipantLimit(request.getParticipantLimit());
        }
        if (request.hasRequestModeration()) {
            log.info("set request moderation {}", request.getRequestModeration());
            event.setRequestModeration(request.getRequestModeration());
        }
        if (request.hasTitle()) {
            log.info("set title {}", request.getTitle());
            event.setTitle(request.getTitle());
        }
        if (request.hasPaid()) {
            log.info("set paid {}", request.getPaid());
            event.setPaid(request.getPaid());
        }
        if (request.hasEventDate()) {
            log.info("set event date {}", request.getEventDate());
            if (request.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
                throw new EventDataException("Incorrect event date: " +
                        request.getEventDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) +
                        ". Event date must be at least 1 hour in the future");
            }
            event.setEventDate(request.getEventDate());
        }

        if (request.getStateAction() != null) {
            log.info("set state action {}", request.getStateAction());
            switch (request.getStateAction()) {
                case PUBLISH_EVENT -> {
                    if (!event.getState().equals(EventState.PENDING)) {
                        throw new EventDataException("Cannot publish the event because it's " +
                                "not in the right state: " + event.getState());
                    }
                    event.setState(EventState.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                    log.info("published event {}", event);
                }
                case REJECT_EVENT -> {
                    if (!event.getState().equals(EventState.PENDING)) {
                        throw new EventDataException("Cannot reject the event because it is not " +
                                "in the right state: " + event.getState());
                    }
                    event.setState(EventState.CANCELED);
                    log.info("rejected event {}", event);
                }
            }
        }

        event = eventRepository.save(event);
        return eventMapper.toDto(event);
    }

    //подобрки

    public CompilationDto addCompilation(NewCompilationRequest request) {
        log.info("Adding compilation {}", request);

        if (compilationsRepository.existsByTitle(request.getTitle())) {
            throw new DataConflictException("could not execute statement; SQL [n/a];" +
                    " constraint " + request.getTitle() +
                    "; nested exception is org.hibernate.exception.ConstraintViolationException:" +
                    " could not execute statement");
        }

        List<Event> events;

        if (request.getEvents() != null) {
            events = eventRepository.findByIdIn(request.getEvents());
            log.info("Found {} events", events.size());

            logMissedIds(events, request);

        } else {
            events = Collections.emptyList();
        }



        Compilation compilation = compilationsRepository.save(Compilation.builder()
                .events(events)
                .title(request.getTitle())
                .pinned(request.getPinned())
                .build());

        log.info("Compilation created with id: {}", compilation.getId());

        return compilationsMapper.toDto(compilation);
    }

    private void logMissedIds(List<Event> events, Requestable request) {
        if (events.size() != request.getEvents().size()) {
            Set<Long> foundEventIds = events.stream()
                    .map(Event::getId)
                    .collect(Collectors.toSet());
            Set<Long> missingEventIds = request.getEvents().stream()
                    .filter(id -> !foundEventIds.contains(id))
                    .collect(Collectors.toSet());
            log.info("Some events not found: {}", missingEventIds);
        }
    }


    public void deleteCompilation(Long compId) {

        if (!compilationsRepository.existsById(compId)) {
            throw new NotFoundException("Compilation with id=" + compId + " was not found");
        }

        compilationsRepository.deleteById(compId);
    }

    public CompilationDto getCompilation(Long compId) {
        Compilation compilation = compilationsRepository.findById(compId).orElseThrow(
                () -> new NotFoundException("Compilation with id=" + compId + " was not found")
        );
        return compilationsMapper.toDto(compilation);
    }

    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest request) {
        log.info("update compilation {}", request);

        if (compilationsRepository.existsByTitle(request.getTitle())) {
            throw new DataConflictException("could not execute statement; " +
                    "SQL [n/a]; constraint" + request.getTitle() +
                    "; nested exception is org.hibernate.exception.ConstraintViolationException:" +
                    " could not execute statement");
        }

        Compilation compilation = compilationsRepository.findById(compId).orElseThrow(
                () -> new NotFoundException("Compilation with id=" + compId + " was not found")
        );

        if (request.hasEvents()) {
            List<Event> events = eventRepository.findByIdIn(request.getEvents());
            logMissedIds(events, request);
            events = sortEventsByRequestOrder(events, request.getEvents());
            compilation.setEvents(events);
        } else {
            compilation.setEvents(Collections.emptyList());
        }

        if (request.hasPinned()) {
            compilation.setPinned(request.getPinned());
        }

        if (request.hasTitle()) {
            compilation.setTitle(request.getTitle());
        }

        compilation = compilationsRepository.save(compilation);
        return compilationsMapper.toDto(compilation);
    }


    private List<Event> sortEventsByRequestOrder(List<Event> events, List<Long> requestedOrder) {

        Map<Long, Event> eventMap = events.stream()
                .collect(Collectors.toMap(Event::getId, Function.identity()));

        return requestedOrder.stream()
                .map(eventMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
