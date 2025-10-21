package ru.practicum.privates;


import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.comments.*;
import ru.practicum.events.*;
import ru.practicum.requests.EventRequestStatusUpdateRequest;
import ru.practicum.requests.EventRequestStatusUpdateResult;
import ru.practicum.requests.RequestDto;
import ru.practicum.util.RequestsValidator;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}")
@RequiredArgsConstructor
@Validated
public class PrivateController {
    private final PrivateWebEventsClient privateWebEventsClient;
    private final PrivateWebRequestsClient privateWebRequestsClient;
    private final PrivateWebCommentsClient privateWebCommentsClient;


    //Private: События
    //Закрытый API для работы с событиями
    @PostMapping("/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventDto addEvent(
            @PathVariable("userId") @Min(1) Long userId,
            @RequestBody @Valid NewEventRequest request) throws BadRequestException {

        RequestsValidator.dateValidation(request);

        return privateWebEventsClient.addEvent(userId, request);
    }

    //Получение полной информации о событии
    @GetMapping("/events/{eventId}")
    public EventDto getEvent(
            @PathVariable @Min(1) Long userId,
            @PathVariable @Min(1) Long eventId
    ) {
        return privateWebEventsClient.getEvent(userId, eventId);
    }

    //получение событий добавленых пользователем
    @GetMapping("/events")
    public Mono<List<EventDto>> getEvents(
            @PathVariable @Min(1) Long userId,
            @RequestParam(defaultValue = "0") @Min(0) Integer from,
            @RequestParam(defaultValue = "10") @Min(1) Integer size
    ) {
        return privateWebEventsClient.getUserEvents(userId, from, size);
    }

    //Изменение события
    @PatchMapping("/events/{eventId}")
    public EventDto updateEvent(
            @PathVariable @Min(1) Long userId,
            @PathVariable @Min(1) Long eventId,
            @RequestBody @Valid UpdateEventRequest request
    ) throws BadRequestException {

        RequestsValidator.dateValidation(request);

        return privateWebEventsClient.updateEvent(userId, eventId, request);
    }

    //получение информации о запросах на участие в событии текущего пользователя
    @GetMapping("/events/{eventId}/requests")
    public Mono<List<RequestDto>> getRequests(
            @PathVariable @Min(1) Long userId,
            @PathVariable @Min(1) Long eventId
    ) {
        return privateWebRequestsClient.getRequestsByUserEvent(userId, eventId);
    }

    //Изменение статуса(подтверждена, отменена) заявок на участие в событии текущего пользователя
    @PatchMapping("events/{eventId}/requests")
    public EventRequestStatusUpdateResult updateEventRequestsStatus(
            @PathVariable @Min(1) Long userId,
            @PathVariable @Min(1) Long eventId,
            @RequestBody @Valid EventRequestStatusUpdateRequest request
    ) {
        return privateWebRequestsClient.updateEventRequestsStatus(userId, eventId, request);
    }


    //Private: Запросы на участие
    //Закрытый API для работы с запросами текущего пользователя на участие в событиях
    @PostMapping("/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public RequestDto addRequest(
            @PathVariable @Min(1) Long userId,
            @RequestParam @Min(1) Long eventId
    ) {
        return privateWebRequestsClient.addRequest(userId, eventId);
    }

    @GetMapping("/requests")
    public Mono<List<RequestDto>> getUserRequests(
            @PathVariable @Min(1) Long userId
    ) {
        return privateWebRequestsClient.getUserRequests(userId);
    }

    @PatchMapping("/requests/{requestId}/cancel")
    public RequestDto cancelRequest(
            @PathVariable @Min(1) Long userId,
            @PathVariable @Min(1) Long requestId
    ) {
        return privateWebRequestsClient.cancelRequest(userId, requestId);
    }

    //comments

    @PostMapping("/events/{eventId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto addComment(
            @PathVariable @Min(1) Long userId,
            @PathVariable @Min(1) Long eventId,
            @RequestBody @Valid NewCommentRequest request
    ) {
        return privateWebCommentsClient.addComment(userId, eventId, request);
    }

    @GetMapping("events/{eventId}/comments")
    public Flux<CommentDto> getComments(
            @PathVariable @Min(1) Long userId,
            @PathVariable @Min(1) Long eventId,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        return privateWebCommentsClient.getComments(userId, eventId, from, size);
    }

    @PatchMapping("events/{eventId}/comments/{commentId}")
    public CommentDto updateComment(
            @PathVariable @Min(1) Long userId,
            @PathVariable @Min(1) Long eventId,
            @PathVariable @Min(1) Long commentId,
            @RequestBody @Valid UpdateCommentRequest request
    ) {
        return privateWebCommentsClient.updateComment(userId, eventId, commentId, request);
    }

    @PatchMapping("events/{eventId}/comments/{commentId}/status")
    public CommentDto updateCommentStatus(
            @PathVariable @Min(1) Long userId,
            @PathVariable @Min(1) Long eventId,
            @PathVariable @Min(1) Long commentId,
            @RequestParam CommentCommand command
    ) {
        return privateWebCommentsClient.updateCommentStatus(userId, eventId, commentId, command);
    }

    @PostMapping("events/{eventId}/comments/{commentId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto replyToComment(
            @PathVariable @Min(1) Long userId,
            @PathVariable @Min(1) Long eventId,
            @PathVariable @Min(1) Long commentId,
            @RequestBody @Valid NewCommentRequest request
    ) {
        return privateWebCommentsClient.replyToComment(userId, eventId, commentId, request);
    }

    @GetMapping("/comments")
    public Flux<CommentDto> getUserComments(
            @PathVariable @Min(1) Long userId,
            @RequestParam(defaultValue = "SHOW_ACTIVE") CommentsShowingParam param,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        return privateWebCommentsClient.getUserComments(userId, param, from, size);
    }

    @PatchMapping("/events/{eventId}/comments")
    public SimpleEventDto updateCommentsSetting(
            @PathVariable @Min(1) Long userId,
            @PathVariable @Min(1) Long eventId,
            @RequestParam CommentsSetting command
    ) {
        return privateWebEventsClient.updateCommentsSetting(userId, eventId, command);
    }


}
