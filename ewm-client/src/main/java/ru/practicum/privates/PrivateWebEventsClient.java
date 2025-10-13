package ru.practicum.privates;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import ru.practicum.base.BaseWebClient;
import ru.practicum.events.EventDto;
import ru.practicum.events.NewEventRequest;
import ru.practicum.events.UpdateEventRequest;
import ru.practicum.exception.DataConflictException;
import ru.practicum.exception.EventDataException;
import ru.practicum.exception.ForbiddenException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.requests.RequestDto;

import java.util.List;

@Service
@Slf4j
public class PrivateWebEventsClient extends BaseWebClient {
    private static final String API_PREFIX = "/users";

    public PrivateWebEventsClient(@Value("${ewm-service.url}") String baseUrl) {
        super(baseUrl, API_PREFIX);
    }


    //Private: События
    public EventDto addEvent(Long userId, NewEventRequest request) {
        try {

            return webClient.post()
                    .uri("/" + userId + "/events")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(EventDto.class)
                    .block();
        } catch (WebClientResponseException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new NotFoundException(ex.getResponseBodyAsString());
            }
            throw ex;
        }
    }

    public EventDto getEvent(Long userId, Long eventId) {
        try {
            return webClient.get()
                    .uri("/" + userId + "/events/" + eventId)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(EventDto.class)
                    .block();
        } catch (WebClientResponseException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new NotFoundException(ex.getResponseBodyAsString());
            }
            throw ex;
        }

    }

    public Mono<List<EventDto>> getUserEvents(Long userId, Integer from, Integer size) {
        try {
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/" + userId + "/events")
                            .queryParam("from", from)
                            .queryParam("size", size)
                            .build())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToFlux(EventDto.class)
                    .collectList();
        } catch (WebClientResponseException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new NotFoundException(ex.getResponseBodyAsString());
            }
            throw ex;
        }
    }

    public EventDto updateEvent(Long userId, Long eventId, UpdateEventRequest request) {
        try {
            return webClient.patch()
                    .uri("/" + userId + "/events/" + eventId)
                    .accept(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(EventDto.class)
                    .block();

        } catch (WebClientResponseException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new NotFoundException(ex.getResponseBodyAsString());
            }
            if (ex.getStatusCode() == HttpStatus.CONFLICT) {
                throw new EventDataException(ex.getResponseBodyAsString());
            } else if (ex.getStatusCode() == HttpStatus.FORBIDDEN) {
                throw new ForbiddenException(ex.getResponseBodyAsString());
            }
            throw ex;
        }
    }

    //Private: Запросы на участие
    //Закрытый API для работы с запросами текущего пользователя на участие в событиях

    public RequestDto addRequest(Long userId, Long eventId) {
        try {
            return webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/" + userId + "/requests")
                            .queryParam("eventId", eventId)
                            .build())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(RequestDto.class)
                    .block();
        } catch (WebClientResponseException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new NotFoundException(ex.getResponseBodyAsString());
            } else if (ex.getStatusCode() == HttpStatus.CONFLICT) {
                throw new DataConflictException(ex.getResponseBodyAsString());
            }
            throw ex;
        }
    }

    public Mono<List<RequestDto>> getUserRequests(Long userId) {
        try {
            return webClient.get()
                    .uri("/" + userId + "/requests")
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToFlux(RequestDto.class)
                    .collectList();
        } catch (WebClientResponseException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new NotFoundException(ex.getResponseBodyAsString());
            }
            throw ex;
        }
    }

    public RequestDto cancelRequest(Long userId, Long requestId) {
        try {
            return webClient.patch()
                    .uri("/" + userId + "/requests/" + requestId)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(RequestDto.class)
                    .block();
        } catch (WebClientResponseException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new NotFoundException(ex.getResponseBodyAsString());
            }
            throw ex;
        }
    }

}
