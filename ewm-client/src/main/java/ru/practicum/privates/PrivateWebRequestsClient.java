package ru.practicum.privates;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import ru.practicum.base.BaseWebClient;
import ru.practicum.exception.*;
import ru.practicum.requests.EventRequestStatusUpdateRequest;
import ru.practicum.requests.EventRequestStatusUpdateResult;
import ru.practicum.requests.RequestDto;

import java.util.List;

@Service
@Slf4j
public class PrivateWebRequestsClient extends BaseWebClient {
    private static final String API_PREFIX = "/users";

    public PrivateWebRequestsClient(@Value("${ewm-service.url}") String baseUrl) {
        super(baseUrl, API_PREFIX);
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
        return webClient.get()
                .uri("/" + userId + "/requests")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(status -> status ==  HttpStatus.NOT_FOUND, response -> {
                    throw new NotFoundException("User with id=" + userId + " was not found");
                })
                .bodyToFlux(RequestDto.class)
                .collectList();

    }

    public RequestDto cancelRequest(Long userId, Long requestId) {
        try {
            return webClient.patch()
                    .uri("/" + userId + "/requests/" + requestId + "/cancel")
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


    public Mono<List<RequestDto>> getRequestsByUserEvent(Long userId, Long eventId) {
        log.debug("getRequests({}, {})", userId, eventId);
        return webClient.get()
                .uri("/" + userId + "/events/" + eventId + "/requests")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(status -> status == HttpStatus.NOT_FOUND,
                        response -> {
                            throw new NotFoundException("Event or user not found");
                        })
                .onStatus(status -> status == HttpStatus.FORBIDDEN,
                        response -> {
                            throw new ForbiddenException("You are not allowed to perform this action." +
                                    " You are not initiator of event: " + eventId);
                        })
                .bodyToFlux(RequestDto.class)
                .collectList();
    }

    public EventRequestStatusUpdateResult updateEventRequestsStatus(Long userId,
                                                                    Long eventId,
                                                                    EventRequestStatusUpdateRequest request) {
        log.info("Change status for requests {}, to {}", request.getRequestIds(), request.getStatus());
        log.info("For event {}", eventId);

        try {
            return webClient.patch()
                    .uri("/" + userId + "/events/" + eventId + "/requests")
                    .accept(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(EventRequestStatusUpdateResult.class)
                    .block();
        } catch (WebClientResponseException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new NotFoundException(ex.getResponseBodyAsString());
            } if (ex.getStatusCode() == HttpStatus.CONFLICT) {
                throw new ConflictException(ex.getResponseBodyAsString());
            }
            throw ex;
        }
    }

}
