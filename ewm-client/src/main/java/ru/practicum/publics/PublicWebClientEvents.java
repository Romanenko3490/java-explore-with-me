package ru.practicum.publics;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.practicum.base.BaseWebClient;
import ru.practicum.events.EventDto;
import ru.practicum.events.EventSortType;
import ru.practicum.exception.EventDataException;
import ru.practicum.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class PublicWebClientEvents extends BaseWebClient {
    private final static String EVENT_API = "/events";

    public PublicWebClientEvents(@Value("${ewm-service.url}") String baseUrl) {
        super(baseUrl, EVENT_API);
    }

    public Mono<List<EventDto>> getEvents(
            String text,
            List<Long> categories,
            Boolean paid,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            Boolean onlyAvailable,
            EventSortType sort,
            Integer from,
            Integer size

    ) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParamIfPresent("text", Optional.ofNullable(text))
                        .queryParamIfPresent("categories", Optional.ofNullable(categories))
                        .queryParamIfPresent("paid", Optional.ofNullable(paid))
                        .queryParamIfPresent("rangeStart", Optional.ofNullable(rangeStart))
                        .queryParamIfPresent("rangeEnd", Optional.ofNullable(rangeEnd))
                        .queryParamIfPresent("onlyAvailable", Optional.ofNullable(onlyAvailable))
                        .queryParamIfPresent("sort", Optional.ofNullable(sort))
                        .queryParam("from", from)
                        .queryParam("size", size)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(EventDto.class)
                .collectList();
    }


    public EventDto getEvent(Long id) {
        return webClient.get()
                .uri("/" + id)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(status -> status == HttpStatus.NOT_FOUND,
                        response -> {
                            throw new NotFoundException("Event with id=" + id + " was not found");
                        })
                .onStatus(status -> status == HttpStatus.CONFLICT,
                        response -> {
                            throw new EventDataException("Event with id=" + id + " was not published");
                        })
                .bodyToMono(EventDto.class)
                .block();
    }
}
