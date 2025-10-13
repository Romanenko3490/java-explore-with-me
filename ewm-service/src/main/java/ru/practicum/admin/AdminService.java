package ru.practicum.admin;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.categories.*;
import ru.practicum.events.*;
import ru.practicum.exception.CategoryConflictException;
import ru.practicum.exception.DataConflictException;
import ru.practicum.exception.EventDataException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class AdminService {
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    private final UserMapper userMapper;
    private final CategoryMapper categoryMapper;
    private  final EventMapper eventMapper;

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
        log.debug("Finding all users by ids {}", ids);
        Pageable pageable = PageRequest.of(from / size, size);

        if (ids != null && !ids.isEmpty()) {
            List<User> users = userRepository.findUsersByIds(ids, pageable);
            log.debug("Found {} users by ids {}", users.size(), users);

            return users.stream()
                    .map(userMapper::userToUserDto)
                    .collect(Collectors.toList());
        }
        List<User> users = userRepository.findAllBy(pageable);
        log.debug("Found {} users by ids {}", users.size(), users);

        return users.stream()
                .map(userMapper::userToUserDto)
                .collect(Collectors.toList());
    }

    public void deleteUser(Long id) {

        if (!userRepository.existsById(id)) {
            throw new NotFoundException("User with id=" + id + " was not found");
        }

        userRepository.deleteById(id);
    }

    //Categories
    public CategoryDto saveCategory(NewCategoryRequest request) {
        log.debug("Saving category {}", request);

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
        log.debug("Deleting category {}", id);

        if (!categoryRepository.existsById(id)) {
            throw new NotFoundException("Category with id=" + id + " was not found");
        }
        if (eventRepository.existsByCategory_Id(id)) {
            throw new CategoryConflictException("The category is not empty");
        }

        categoryRepository.deleteById(id);
    }

    public CategoryDto updateCategory(Long id, UpdateCategoryRequest request) {
        log.debug("Updating category {}", id);

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

    public List<EventDto> getEvents(
            List<Long> users,
            List<String> states,
            List<Long> categories,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            Integer from,
            Integer size
    ) {

        List<EventState> eventStates = parseStates(states);

        Pageable pageable = PageRequest.of(from / size, size);

        List<Event> eventList = eventRepository.findByFilters(users,
                eventStates,
                categories,
                rangeStart,
                rangeEnd,
                pageable);

        return null;

    }

    private List<EventState> parseStates(List<String> states) {
        if (states != null && !states.isEmpty()) {
            return null;
        }

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

    public EventDto updateEvent(Long eventId, UpdateEventRequest request) {
        log.debug("Updating event {} , by request : {}", eventId, request);

        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Event with id=" + eventId + " was not found"));

        boolean isPublished = event.getState().equals(EventState.PUBLISHED);

        if (request.hasAnnotation()) {
            event.setAnnotation(request.getAnnotation());
        }
        if (request.hasCategory()) {
            event.setCategory(categoryRepository.findById(request.getCategory()).orElseThrow(
                    () -> new NotFoundException("Category with id=" + request.getCategory() + " was not found")
            ));
        }
        if (request.hasDescription()) {
            event.setDescription(request.getDescription());
        }
        if (request.hasLocation()) {
            Location location = new Location(request.getLocation().getLat(), request.getLocation().getLon());
            event.setLocation(location);
        }
        if (request.hasParticipantLimit()) {
            event.setParticipantLimit(request.getParticipantLimit());
        }
        if (request.hasRequestModeration()) {
            event.setRequestModeration(request.getRequestModeration());
        }
        if (request.hasTitle()) {
            event.setTitle(request.getTitle());
        }
        if (request.hasPaid()) {
            event.setPaid(request.getPaid());
        }
        if (request.hasEventDate()) {
            if (request.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
                throw new EventDataException("Event date must be at least 1 hour in the future");
            }
            event.setEventDate(request.getEventDate());
        }

        if (request.getStateAction() != null) {
            switch (request.getStateAction()) {
                case PUBLISH_EVENT -> {
                    if (!event.getState().equals(EventState.PENDING)) {
                        throw new EventDataException("Cannot publish the event because it's " +
                                "not in the right state: " + event.getState());
                    }
                    event.setState(EventState.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                }
                case REJECT_EVENT -> {
                    if (!event.getState().equals(EventState.PENDING)) {
                        throw new EventDataException("Cannot reject the event because it is not " +
                                "in the right state: " + event.getState());
                    }
                    event.setState(EventState.CANCELED);
                }
            }
        }

        event = eventRepository.save(event);
        return eventMapper.toDto(event);
    }

}
