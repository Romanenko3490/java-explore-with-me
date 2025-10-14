package ru.practicum.privates;


import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.events.EventDto;
import ru.practicum.events.NewEventRequest;
import ru.practicum.events.UpdateEventRequest;
import ru.practicum.requests.EventRequestStatusUpdateRequest;
import ru.practicum.requests.EventRequestStatusUpdateResult;
import ru.practicum.requests.RequestDto;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}")
@RequiredArgsConstructor
public class PrivateInternalController {
    private final PrivateService privateService;

    //Private: события
    @PostMapping("/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventDto addEvent(
            @PathVariable Long userId,
            @RequestBody NewEventRequest request) {
        return privateService.addEvent(userId, request);
    }

    @GetMapping("/events/{eventId}")
    public EventDto getEvent(
            @PathVariable Long userId,
            @PathVariable Long eventId
    ) {
        return privateService.getEvent(userId, eventId);
    }

    @GetMapping("/events")
    public List<EventDto> getEvents(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        return privateService.getUserEvents(userId, from, size);
    }

    @PatchMapping("/events/{eventId}")
    public EventDto updateEvent(
            @PathVariable @Min(1) Long userId,
            @PathVariable @Min(1) Long eventId,
            @RequestBody @Valid UpdateEventRequest request
    ) {
        return privateService.updateEvent(userId, eventId, request);
    }

    @GetMapping("events/{eventId}/requests")
    public List<RequestDto> getRequests(
            @PathVariable Long userId,
            @PathVariable Long eventId
    ) {
        return privateService.getRequestsByUserEvent(userId, eventId);
    }

    @PatchMapping("events/{eventId}/requests")
    public EventRequestStatusUpdateResult updateEventRequestsStatus (
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @RequestBody EventRequestStatusUpdateRequest request
    ) {
        return privateService.updateEventRequestsStatus(userId, eventId, request);
    }



    //Private: Запросы на участие
    //Закрытый API для работы с запросами текущего пользователя на участие в событиях
    @PostMapping("/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public RequestDto addRequest(
            @PathVariable Long userId,
            @RequestParam Long eventId
    ) {
        return privateService.addRequest(userId, eventId);
    }

    @GetMapping("/requests")
    public List<RequestDto> getUserRequests(
            @PathVariable Long userId
    ) {
        return privateService.getUserRequests(userId);
    }

    @PatchMapping("/requests/{requestId}/cancel")
    public RequestDto cancelRequest(
            @PathVariable Long userId,
            @PathVariable Long requestId
    ) {
        return privateService.cancelRequest(userId, requestId);
    }

}
