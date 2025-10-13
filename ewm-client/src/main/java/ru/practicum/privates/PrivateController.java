package ru.practicum.privates;


import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.practicum.events.Dateable;
import ru.practicum.events.EventDto;
import ru.practicum.events.NewEventRequest;
import ru.practicum.events.UpdateEventRequest;
import ru.practicum.exception.EventDataException;
import ru.practicum.requests.RequestDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/users/{userId}")
@RequiredArgsConstructor
@Slf4j
@Validated
public class PrivateController {
    private final PrivateWebEventsClient privateWebEventsClient;


    //Private: События
    //Закрытый API для работы с событиями
    @PostMapping("/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventDto addEvent(
            @PathVariable("userId") @Min(1) Long userId,
            @RequestBody @Valid NewEventRequest request) {

        dateValidation(request);

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
    ) {
        dateValidation(request);

        return privateWebEventsClient.updateEvent(userId, eventId, request);
    }



    //Private: Запросы на участие
    //Закрытый API для работы с запросами текущего пользователя на участие в событиях
    @PostMapping("/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public RequestDto addRequest(
            @PathVariable @Min(1) Long userId,
            @RequestParam @Min(1) Long eventId
    ) {
        return privateWebEventsClient.addRequest(userId, eventId);
    }

    @GetMapping("/requests")
    public Mono<List<RequestDto>> getUserRequests(
            @PathVariable @Min(1) Long userId
    ) {
        return privateWebEventsClient.getUserRequests(userId);
    }

    @PatchMapping("/requests/{requestId}/cancel")
    public RequestDto cancelRequest(
            @PathVariable @Min(1) Long userId,
            @PathVariable @Min(1) Long requestId
    ) {
        return privateWebEventsClient.cancelRequest(userId, requestId);
    }





    private void dateValidation(Dateable request) {

        if (request.getEventDate() == null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        if (request.getEventDate().isBefore(now)) {
            throw new EventDataException("Field: eventDate. Error: должно содержать дату," +
                    " которая еще не наступила. Value: " + request.getEventDate().format(formatter));
        } else if (!request.getEventDate().isAfter(now.plusHours(2))) {
            throw new EventDataException("Field: eventDate. Error: " +
                    "Event date must be at least 2 hours from now. Value: " + request.getEventDate().format(formatter));
        }
    }
}
