package ru.practicum.admin;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.practicum.base.BaseWebClient;
import ru.practicum.exception.DataConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.UserDto;
import ru.practicum.user.UserRequest;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class AdminWebUserClient extends BaseWebClient {
    private static final String API_PREFIX_USERS = "/admin/users";

    public AdminWebUserClient(@Value("${ewm-service.url}") String serverUrl) {
        super(serverUrl, API_PREFIX_USERS);
    }

    //users

    public UserDto addUser(UserRequest request) {
        return webClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .onStatus(status -> status == HttpStatus.CONFLICT,
                        response -> response.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new DataConflictException(
                                        "Conflict from service: " + errorBody
                                ))))
                .bodyToMono(UserDto.class)
                .block();
    }

    public Mono<List<UserDto>> getUsers(List<Long> ids, Integer from, Integer size) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParamIfPresent("ids", Optional.ofNullable(ids))
                        .queryParam("from", from)
                        .queryParam("size", size)
                        .build())
                .retrieve()
                .bodyToFlux(UserDto.class)
                .collectList();
    }

    public ResponseEntity<Void> deleteUser(Long id) {
        return webClient.delete()
                .uri("/" + id)
                .retrieve()
                .toBodilessEntity()
                .doOnSuccess(response -> {
                    log.info("Delete user: " + id);
                })
                .doOnError(error -> {
                    log.error("Delete user: " + id, error);
                    throw new NotFoundException("User with id=" + id + " was not found");
                })
                .block();
    }
}
