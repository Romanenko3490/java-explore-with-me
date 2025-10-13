package ru.practicum.privates;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.categories.Category;
import ru.practicum.categories.CategoryRepository;
import ru.practicum.events.*;
import ru.practicum.exception.DataConflictException;
import ru.practicum.exception.EventDataException;
import ru.practicum.exception.ForbiddenException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.requests.*;
import ru.practicum.user.User;
import ru.practicum.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class PrivateService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final RequestsRepository requestsRepository;

    private final EventMapper eventMapper;
    private final RequestMapper requestMapper;

    public EventDto addEvent(Long userId, NewEventRequest request) {

        User initiator = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("User not found with id=" + userId)
        );

        Category category = categoryRepository.findById(request.getCategory()).orElseThrow(
                () -> new NotFoundException("Category not found with id=" + request.getCategory())
        );

        Location location = Location.builder()
                .lat(request.getLocation().getLat())
                .lon(request.getLocation().getLon())
                .build();


        Event event = Event.builder()
                .annotation(request.getAnnotation())
                .category(category)
                .confirmedRequests(0L)
                .createdOn(LocalDateTime.now())
                .description(request.getDescription())
                .eventDate(request.getEventDate())
                .location(location)
                .paid(request.getPaid())
                .publishedOn(null)
                .participantLimit(request.getParticipantLimit())
                .requestModeration(request.getRequestModeration())
                .title(request.getTitle())
                .state(EventState.PENDING)
                .initiator(initiator)
                .views(0L)
                .build();

        Event savedEvent = eventRepository.save(event);
        return eventMapper.toDto(savedEvent);

    }

    public EventDto getEvent(Long userId, Long eventId) {

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found with id=" + userId);
        }

        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Event with id=" + eventId + " was not found")
        );

        return eventMapper.toDto(event);
    }

    public List<EventDto> getUserEvents(Long userId, Integer from, Integer size) {
        log.info("Getting user events from {} to {}", from, size);

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " was not found");
        }

        Pageable pageable = PageRequest.of(from / size, size);

        List<Event> event = eventRepository.findByInitiatorId(userId, pageable);
        return event.stream()
                .map(eventMapper::toDto)
                .collect(Collectors.toList());
    }

    public EventDto updateEvent(Long userId, Long eventId, UpdateEventRequest request) {

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId +  " was not found");
        }

        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Event with id=" + eventId +  " was not found")
        );

        if (event.getInitiator().getId() != userId) {
            throw new ForbiddenException(
                    String.format("User with id=%d is not allowed to update event with id=%d", userId, eventId));
        }

        if (event.getState() != EventState.PENDING && event.getState() != EventState.CANCELED) {
            throw new EventDataException("Only pending or canceled events can be changed");
        }

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
            event.setEventDate(request.getEventDate());
        }

        if (request.getStateAction() != null) {
            switch (request.getStateAction()) {
                case SEND_TO_REVIEW -> event.setState(EventState.PENDING);
                case CANCEL_REVIEW -> event.setState(EventState.CANCELED);
                default -> throw new EventDataException("User cannot perform action: " + request.getStateAction());
            }
        }

        event = eventRepository.save(event);
        return eventMapper.toDto(event);
    }

    public RequestDto addRequest(Long userId, Long eventId) {
        log.debug("Adding request from user {}, to event {}", userId,eventId);

        User user = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("User with id=" + userId + " not found"));

        if (requestsRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            throw new DataConflictException("could not execute statement; SQL [n/a]; " +
                    "constraint userID:" + userId + ", eventId:" + eventId + "; nested exception " +
                    "is org.hibernate.exception.ConstraintViolationException: could not execute statement");
        }

        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Event with id=" + eventId + " not found")
        );

        //409 инициатор события не может добавить запрос на участие в своем событии
        if (event.getInitiator().getId().equals(userId)) {
            throw new DataConflictException("Owner of event can not perform request");
        }
        //409 нельзя учавствовать в неопубликованном событии
        if (event.getState() != EventState.PUBLISHED) {
            throw new DataConflictException("Event is not published");
        }
        //409 если у события достигнут лимит запросов
        if (event.getParticipantLimit() < event.getConfirmedRequests() + 1) {
            throw new DataConflictException("Participant limit exceeded");
        }
        // если отключена предмодерация запросов на участие, запрос автоматически становется accepted
        Request request = Request.builder()
                .created(LocalDateTime.now())
                .requester(user)
                .event(event)
                .status(RequestStatus.PENDING)
                .build();

        if (!event.getRequestModeration()) {
            request.setStatus(RequestStatus.ACCEPTED);
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
        }

        event = eventRepository.save(event);
        request = requestsRepository.save(request);
        return requestMapper.requestToRequestDto(request);
    }

    public List<RequestDto> getUserRequests(Long userId) {

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " not found");
        }

        List<Request> requests = requestsRepository.findByRequester_Id(userId);

        return requests.stream()
                .map(requestMapper::requestToRequestDto)
                .collect(Collectors.toList());
    }

    public RequestDto cancelRequest(Long userId, Long requestId) {
        log.debug("Canceling request from user {}, to request {}", userId, requestId);

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " not found");
        }

        Request request = requestsRepository.findById(requestId).orElseThrow(
                () -> new NotFoundException("Request with id=" + requestId + " not found")
        );

        if (!request.getRequester().getId().equals(userId)) {
            throw new ForbiddenException("User " + userId +" cannot perform request "
                    + request.getRequester().getId());
        }

        Event event = eventRepository.findById(request.getEvent().getId()).orElseThrow(
                () -> new NotFoundException("Event for request with id=" + requestId + " not found")
        );

        if (request.getStatus() == RequestStatus.ACCEPTED) {
            log.debug("Event {}, had confirmed requests {}" ,
                    event.getId(),
                    event.getConfirmedRequests());
            event.setConfirmedRequests(event.getConfirmedRequests() - 1);
            log.debug("After 1 request cancellation event hav confirmed requests {}",
                    event.getConfirmedRequests());
            eventRepository.save(event);
        }

        request.setStatus(RequestStatus.REJECTED);

        request = requestsRepository.save(request);
        log.debug("request has been cancelled: {} ", request.getId());
        return requestMapper.requestToRequestDto(request);
    }

}
