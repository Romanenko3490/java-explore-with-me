package ru.practicum.admin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import ru.practicum.base.BaseWebClient;
import ru.practicum.events.EventDto;
import ru.practicum.events.UpdateEventRequest;
import ru.practicum.exception.EventDataException;
import ru.practicum.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class AdminWebEventClient extends BaseWebClient {
    public static final String API_PREFIX_EVENT = "/admin/events";

    public AdminWebEventClient(@Value("${ewm-service.url}") String baseUrl) {
        super(baseUrl, API_PREFIX_EVENT);
    }

    public Mono<List<EventDto>> getEvents(
            List<Long> users,
            List<String> states,
            List<Long> categories,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            Integer from,
            Integer size
    ) {
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParamIfPresent("users", Optional.ofNullable(users))
                            .queryParamIfPresent("states", Optional.ofNullable(states))
                            .queryParamIfPresent("categories", Optional.ofNullable(categories))
                            .queryParamIfPresent("rangeStart", Optional.ofNullable(rangeStart))
                            .queryParamIfPresent("rangeEnd", Optional.ofNullable(rangeEnd))
                            .queryParam("from", from)
                            .queryParam("size", size)
                            .build())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToFlux(EventDto.class)
                    .collectList();
    }

    public EventDto updateEvent(Long eventId, UpdateEventRequest request) {
        try {
            return webClient.patch()
                    .uri("/" + eventId)
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(EventDto.class)
                    .block();
        }catch (WebClientResponseException ex){
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new NotFoundException(ex.getResponseBodyAsString());
            } else if (ex.getStatusCode() == HttpStatus.CONFLICT) {
                throw new EventDataException(ex.getResponseBodyAsString());
            }
            throw ex;
        }
    }
}
