package ru.practicum.privates;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import ru.practicum.categories.Category;
import ru.practicum.categories.CategoryRepository;
import ru.practicum.comments.*;
import ru.practicum.events.*;
import ru.practicum.exception.*;
import ru.practicum.requests.*;
import ru.practicum.user.User;
import ru.practicum.user.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
@Transactional
public class PrivateService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final RequestsRepository requestsRepository;
    private final CommentRepository commentRepository;

    private final EventMapper eventMapper;
    private final RequestMapper requestMapper;
    private final CommentMapper commentMapper;

    public EventDto addEvent(Long userId, NewEventRequest request) {
        log.info("Adding event {}", request);
        User initiator = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("User with id=" + userId + "  was not found")
        );

        Category category = categoryRepository.findById(request.getCategory()).orElseThrow(
                () -> new NotFoundException("Category with id=" + request.getCategory() + " was not found")
        );

        Location location = Location.builder()
                .lat(request.getLocation().getLat())
                .lon(request.getLocation().getLon())
                .build();


        Event event = Event.builder()
                .annotation(request.getAnnotation())
                .category(category)
                .createdOn(LocalDateTime.now())
                .description(request.getDescription())
                .eventDate(request.getEventDate())
                .location(location)
                .paid(request.getPaid())
                .participantLimit(request.getParticipantLimit())
                .requestModeration(request.getRequestModeration())
                .title(request.getTitle())
                .initiator(initiator)
                .commentDisabled(request.getCommentDisabled())
                .build();

        Event savedEvent = eventRepository.save(event);
        log.info("Added event {}", savedEvent);
        return eventMapper.toDto(savedEvent);

    }

    @Transactional(readOnly = true)
    public EventDto getEvent(Long userId, Long eventId) {
        log.info("Getting event {}", eventId);
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " was not found");
        }

        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Event with id=" + eventId + " was not found")
        );
        log.info("Getting event {}", event);
        return eventMapper.toDto(event);
    }

    @Transactional(readOnly = true)
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
        log.info("Updating event {}, as request: {}", eventId, request);
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " was not found");
        }

        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Event with id=" + eventId + " was not found")
        );

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ForbiddenException(
                    String.format("User with id=%d is not allowed to update event with id=%d", userId, eventId));
        }

        if (event.getState() != EventState.PENDING && event.getState() != EventState.CANCELED) {
            throw new EventDataException("Only pending or canceled events can be changed");
        }

        if (request.hasAnnotation()) {
            log.info("Set annotation {}", request.hasAnnotation());
            event.setAnnotation(request.getAnnotation());
        }
        if (request.hasCategory()) {
            event.setCategory(categoryRepository.findById(request.getCategory()).orElseThrow(
                    () -> new NotFoundException("Category with id=" + request.getCategory() + " was not found")
            ));
            log.info("Set category {}", request.getCategory());
        }
        if (request.hasDescription()) {
            log.info("Set event description {}", request.hasDescription());
            event.setDescription(request.getDescription());
        }
        if (request.hasLocation()) {
            Location location = new Location(request.getLocation().getLat(), request.getLocation().getLon());
            log.info("Set location {}", location);
            event.setLocation(location);
        }
        if (request.hasParticipantLimit()) {
            log.info("Set participant limit {}", request.hasParticipantLimit());
            event.setParticipantLimit(request.getParticipantLimit());
        }
        if (request.hasRequestModeration()) {
            log.info("Set request moderation {}", request.hasRequestModeration());
            event.setRequestModeration(request.getRequestModeration());
        }
        if (request.hasTitle()) {
            log.info("Set title {}", request.hasTitle());
            event.setTitle(request.getTitle());
        }
        if (request.hasPaid()) {
            log.info("Set paid {}", request.hasPaid());
            event.setPaid(request.getPaid());
        }
        if (request.hasEventDate()) {
            log.info("Set event date {}", request.hasEventDate());
            event.setEventDate(request.getEventDate());
        }
        if (request.hasCommentDisabled()) {
            log.info("Set comment disabled {}", request.hasCommentDisabled());
            event.setCommentDisabled(request.getCommentDisabled());
        }

        if (request.getStateAction() != null) {
            log.info("State action {}", request.getStateAction());
            switch (request.getStateAction()) {
                case SEND_TO_REVIEW -> event.setState(EventState.PENDING);
                case CANCEL_REVIEW -> event.setState(EventState.CANCELED);
                default -> throw new EventDataException("User cannot perform action: " + request.getStateAction());
            }
            log.info("State  {}", event.getState());
        }

        event = eventRepository.save(event);
        return eventMapper.toDto(event);
    }

    @Transactional(readOnly = true)
    public List<RequestDto> getRequestsByUserEvent(Long userId, Long eventId) {
        log.info("Get requests for events from {} to user {}", eventId, userId);
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " was not found");
        }

        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Event with id=" + eventId + " was not found")
        );


        log.info("Event initiator: {}, userId {}", event.getInitiator().getId(), userId);
        if (!event.getInitiator().getId().equals(userId)) {
            log.info("user: {} not allowed perfom request for event: {}", userId, eventId);
            throw new ForbiddenException("You are not allowed to perform this action." +
                    " You are not initiator of event: " + eventId);
        }

        List<Request> requests = requestsRepository.findByEvent_Id(eventId);
        return requests.stream()
                .map(requestMapper::requestToRequestDto)
                .collect(Collectors.toList());
    }


    public EventRequestStatusUpdateResult updateEventRequestsStatus(
            Long userId,
            Long eventId,
            EventRequestStatusUpdateRequest request
    ) {
        log.info("Patch request for requests: {} to status: {}", request.getRequestIds(), request.getStatus());
        log.info("For event: {}", eventId);

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " was not found");
        }

        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Event with id=" + eventId + " was not found")
        );

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ForbiddenException("You are not allowed to perform this action.");
        }

        if (!event.getRequestModeration() || event.getParticipantLimit().equals(0)) {
            throw new ConflictException("Request moderation switched off or Participant limit = 0. Event have " +
                    event.getConfirmedRequests() + " confirmed requests.");
        }

        if (event.getConfirmedRequests() >= event.getParticipantLimit()) {
            throw new ConflictException("Participant limit exceeded");
        }

        List<Request> requests = requestsRepository.findByIdIn(request.getRequestIds());


        if (requests.stream().anyMatch(req -> !req.getStatus().equals(RequestStatus.PENDING))) {
            throw new ConflictException("All requests must have status PENDING");
        }

        List<RequestDto> confirmed = new ArrayList<>();
        List<RequestDto> rejected = new ArrayList<>();

        if (request.getStatus().equals(RequestStatus.CONFIRMED)) {
            for (Request req : requests) {
                if (event.getConfirmedRequests() < event.getParticipantLimit()) {
                    req.setStatus(RequestStatus.CONFIRMED);
                    event.setConfirmedRequests(event.getConfirmedRequests() + 1);
                    confirmed.add(requestMapper.requestToRequestDto(req));
                } else {
                    req.setStatus(RequestStatus.REJECTED);
                    rejected.add(requestMapper.requestToRequestDto(req));
                }
            }
        } else {
            requests.forEach(req -> req.setStatus(RequestStatus.REJECTED));
            rejected = requests.stream()
                    .map(requestMapper::requestToRequestDto)
                    .collect(Collectors.toList());
        }

        requestsRepository.saveAll(requests);
        eventRepository.save(event);

        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(confirmed)
                .rejectedRequests(rejected)
                .build();
    }

    public RequestDto addRequest(Long userId, Long eventId) {
        log.info("Adding request from user {}, to event {}", userId, eventId);

        User user = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("User with id=" + userId + " was not found"));

        if (requestsRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            throw new DataConflictException("Event: " + eventId + " already contain request from user: " + userId);
        }

        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Event with id=" + eventId + " was not found")
        );

        //409 инициатор события не может добавить запрос на участие в своем событии
        if (event.getInitiator().getId().equals(userId)) {
            throw new DataConflictException("Owner of event can not perform request");
        }
        //409 нельзя учавствовать в неопубликованном событии
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new DataConflictException("Event is not published");
        }
        //409 если у события достигнут лимит запросов
        if (event.getParticipantLimit() != 0 && event.getConfirmedRequests() >= event.getParticipantLimit()) {
            throw new DataConflictException("Participant limit exceeded");
        }
        // если отключена предмодерация запросов на участие, запрос автоматически становется accepted
        Request request = Request.builder()
                .created(LocalDateTime.now())
                .requester(user)
                .event(event)
                .status(RequestStatus.PENDING)
                .build();

        if (!event.getRequestModeration() || event.getParticipantLimit().equals(0)) {
            request.setStatus(RequestStatus.CONFIRMED);
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
        }

        event = eventRepository.save(event);
        request = requestsRepository.save(request);
        log.info("Request {} added to the event {}", request, event.getId());
        return requestMapper.requestToRequestDto(request);
    }

    @Transactional(readOnly = true)
    public List<RequestDto> getUserRequests(Long userId) {
        log.info("Get requests for events from user {}", userId);
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " was not found");
        }

        List<Request> requests = requestsRepository.findByRequester_Id(userId);
        log.info("Requests found {}", requests.size());
        return requests.stream()
                .map(requestMapper::requestToRequestDto)
                .collect(Collectors.toList());
    }

    public RequestDto cancelRequest(Long userId, Long requestId) {
        log.info("Canceling request from user {}, to request {}", userId, requestId);

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " was not found");
        }

        Request request = requestsRepository.findById(requestId).orElseThrow(
                () -> new NotFoundException("Request with id=" + requestId + " was not found")
        );

        if (!request.getRequester().getId().equals(userId)) {
            throw new ForbiddenException("User " + userId + " cannot perform request "
                    + request.getRequester().getId());
        }

        Event event = eventRepository.findById(request.getEvent().getId()).orElseThrow(
                () -> new NotFoundException("Event for request with id=" + requestId + " was not found")
        );

        if (request.getStatus() == RequestStatus.CONFIRMED) {
            log.info("Event {}, had confirmed requests {}",
                    event.getId(),
                    event.getConfirmedRequests());
            event.setConfirmedRequests(event.getConfirmedRequests() - 1);
            log.info("After 1 request cancellation event have confirmed requests {}",
                    event.getConfirmedRequests());
            eventRepository.save(event);
        }

        request.setStatus(RequestStatus.CANCELED);

        request = requestsRepository.save(request);
        log.info("request has been cancelled: {} ", request.getId());
        return requestMapper.requestToRequestDto(request);
    }

    //Comments

    public CommentDto addComment(Long userId, Long eventId, NewCommentRequest request) {
        log.info("Adding comment from user {}, to event {}", userId, eventId);

        User user = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("User with id=" + userId + " was not found")
        );

        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Event with id=" + eventId + " was not found")
        );

        if (event.getCommentDisabled()) {
            throw new ConflictException("Comments are disabled for event " +  eventId);
        }

        Comment comment = Comment.builder()
                .text(request.getText())
                .author(user)
                .event(event)
                .build();

        return commentMapper.commentToCommentDto(commentRepository.save(comment));

    }

    public CommentDto updateComment(Long userId, Long eventId, Long commentId, UpdateCommentRequest request) {

        checkCommentConditions(userId, eventId);

        Comment comment = commentRepository.findById(commentId).orElseThrow(
                () -> new NotFoundException("Comment with id=" + commentId + " was not found")
        );

        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ForbiddenException("User " + userId + " cannot perform request ");
        }

        if (!comment.getEvent().getId().equals(eventId)) {
            throw new ForbiddenException("Comment with id=" + commentId + " is not for event=" + eventId);
        }

        if (comment.getText().trim().equalsIgnoreCase(request.getText().trim())) {
            throw new ConflictException("Nothing to change");
        }

        comment.setText(request.getText());
        comment.setEdited(true);

        return commentMapper.commentToCommentDto(commentRepository.save(comment));
    }


    public CommentDto updateCommentStatus(Long userId, Long eventId, Long commentId, CommentCommand command) {
        log.info("Deleting comment from user {}, to event {}", userId, eventId);

        checkCommentConditions(userId, eventId);

        Comment comment = commentRepository.findById(commentId).orElseThrow(
                () -> new NotFoundException("Comment with id=" + commentId + " was not found")
        );

        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ForbiddenException("User " + userId + " cannot perform request." +
                    " Not Author of comment with id=" + commentId);
        }

        if (comment.getParentComment() != null) {
            Boolean isParentDeleted = comment.getParentComment().isDeleted();

            if (isParentDeleted && command == CommentCommand.RESTORE) {
                throw new ConflictException("Comment with id=" + commentId + " can not be restored. Reason:" +
                        "This comment is reply to deleted comment");
            }

        }

        switch (command) {
            case DELETE -> {
                markCommentAndRepliesAsDeleted(comment);// рекурсивно удаляем ответы к удаленному комментарию, при его удалини
                log.info("comment has been marked as deleted: {} ", comment.getId());
            }
            case RESTORE -> {
                markCommentAndRepliesAsRestored(comment);
                log.info("comment has been marked as restored: {} ", comment.getId());
            }
        }
        return commentMapper.commentToCommentDto(commentRepository.save(comment));
    }


    public CommentDto replyToComment(Long userId, Long eventId, Long commentId, NewCommentRequest request) {
        log.info("Replying user {}, event {}, to comment {}", userId, eventId, commentId);
        log.info("Replying request {}", request);

        User author = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("User with id=" + userId + " was not found")
        );

        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Event with id=" + eventId + " was not found")
        );

        Comment parentComment = commentRepository.findById(commentId).orElseThrow(
                () -> new NotFoundException("Parent comment with id=" + commentId + " was not found")
        );

        if (event.getCommentDisabled()) {
            throw new ConflictException("Comments for event with id=" + eventId + " are disabled");
        }

        Comment reply = Comment.builder()
                .text(request.getText())
                .author(author)
                .event(event)
                .parentComment(parentComment)
                .build();

        reply = commentRepository.save(reply);
        log.info("Reply saved: {} ", reply);


//        parentComment.getReplies().add(reply); здесь не нужно обновлять и сохранять родителя(cascade = CascadeType.ALL),
//        commentRepository.save(parentComment); связь устанавливается через через parent_comment_id
//        log.info("Parent comment saved: {} ", parentComment); коллекция replies - это просто "вид" из родителя
//        При следующем чтении родителя, Hibernate автоматически заполнит replies

        return commentMapper.commentToCommentDto(reply);
    }

    @Transactional(readOnly = true)
    public Flux<CommentDto> getComments(Long userId, Long eventId, Integer from, Integer size) {
        log.info("Getting comments from user {}, to event {}", userId, eventId);
        checkCommentConditions(userId, eventId);

        Pageable pageable = PageRequest.of(from / size, size, Sort.by("creationDate").descending());

        Page<Comment> commentPage = commentRepository.findByEvent_IdAndDeleted(eventId, false, pageable);

        return Flux.fromIterable(commentPage.getContent())
                .map(commentMapper::commentToCommentDto);
    }


    private void markCommentAndRepliesAsDeleted(Comment comment) {
        comment.setDeleted(true);

        if (!comment.getReplies().isEmpty()) {
            for (Comment reply : comment.getReplies()) {
                if (!reply.isDeleted()) {
                    reply.setDeleted(true);
                    markCommentAndRepliesAsDeleted(reply);
                }
            }
        }
    }

    private void markCommentAndRepliesAsRestored(Comment comment) {
        comment.setDeleted(false);

        if (!comment.getReplies().isEmpty()) {
            for (Comment reply : comment.getReplies()) {
                if (reply.isDeleted()) {
                    reply.setDeleted(false);
                    markCommentAndRepliesAsRestored(reply);
                }
            }
        }
    }

    @Transactional(readOnly = true)
    public Flux<CommentDto> getUserComments(Long userId, CommentsShowingParam param, Integer from, Integer size) {
        log.info("Getting comments from user {}, to comment {}", userId, param);

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " was not found");
        }

        Pageable pageable = PageRequest.of(from / size, size, Sort.by("creationDate").descending());

        Page<Comment> comments = switch (param) {
            case SHOW_ALL -> commentRepository.findByAuthor_Id(userId, pageable);
            case SHOW_ACTIVE -> commentRepository
                    .findByAuthor_IdAndDeleted(userId, false, pageable);
            case SHOW_DELETED -> commentRepository
                    .findByAuthor_IdAndDeleted(userId, true, pageable);
        };

        return Flux.fromIterable(comments.getContent())
                .map(commentMapper::commentToCommentDto);

    }


    public SimpleEventDto updateCommentsSetting(Long userId, Long eventId, CommentsSetting command) {
        log.info("Updating comment settings from user {}, to event {}", userId, eventId);

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " was not found");
        }

        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Event with id=" + eventId + " was not found")
        );

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ForbiddenException("User with id=" + userId + " not allowed to update comment settings");
        }

        if (command == CommentsSetting.DISABLE_COMMENTS && event.getCommentDisabled()) {
            throw new ConflictException("Comments are already disabled");
        }


        if (command == CommentsSetting.ENABLE_COMMENTS && !event.getCommentDisabled()) {
            throw new ConflictException("Comments are already enabled");
        }

        switch(command) {
            case DISABLE_COMMENTS -> event.setCommentDisabled(true);
            case ENABLE_COMMENTS -> event.setCommentDisabled(false);
        }
        log.info("Comments setting updated. Setting {} for event {}", command, eventId );

        return eventMapper.toSimpleDto(eventRepository.save(event));
    }


    private void checkCommentConditions(Long userId, Long eventId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " was not found");
        }
        if (!eventRepository.existsById(eventId)) {
            throw new NotFoundException("Event with id=" + eventId + " was not found");
        }
    }

}
