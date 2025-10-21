package ru.practicum.privates;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import ru.practicum.base.BaseWebClient;
import ru.practicum.comments.*;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.ForbiddenException;
import ru.practicum.exception.NotFoundException;

@Service
public class PrivateWebCommentsClient extends BaseWebClient {
    private static final String API_COMMENTS = "/users";


    public PrivateWebCommentsClient(@Value("${ewm-service.url}") String baseUrl) {
        super(baseUrl, API_COMMENTS);
    }

    public CommentDto addComment(Long userId, Long eventId, NewCommentRequest request) {
        return webClient.post()
                .uri("/" + userId + "/events/" + eventId + "/comments")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .onStatus(status -> status == HttpStatus.NOT_FOUND, response ->
                        response.bodyToMono(String.class)
                                .handle((errorBody, sink) -> {
                                    sink.error(new NotFoundException(errorBody));
                                }))
                .onStatus(status -> status == HttpStatus.CONFLICT, response ->
                        response.bodyToMono(String.class)
                                .handle((errorBody, sink) -> {
                                    sink.error(new ConflictException(errorBody));
                                }))
                .bodyToMono(CommentDto.class)
                .block();
    }


    public Flux<CommentDto> getComments(Long userId, Long eventId, Integer from, Integer size) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/" + userId + "/events/" + eventId + "/comments")
                        .queryParam("from", from)
                        .queryParam("size", size)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(status -> status == HttpStatus.NOT_FOUND, response ->
                        response.bodyToMono(String.class)
                                .handle((errorBody, sink) -> {
                                    sink.error(new NotFoundException(errorBody));
                                }))
                .bodyToFlux(CommentDto.class);
    }

    public CommentDto updateCommentStatus(Long userId, Long eventId, Long commentId, CommentCommand command) {
            return webClient.patch()
                    .uri(uriBuilder -> uriBuilder
                                    .path("/" + userId + "/events/" + eventId + "/comments/" + commentId + "/status")
                                    .queryParam("command", command)
                                    .build()
                            )
                    .accept(MediaType.APPLICATION_JSON)
                    .bodyValue(command)
                    .retrieve()
                    .onStatus(status -> status == HttpStatus.NOT_FOUND, response ->
                            response.bodyToMono(String.class)
                                    .handle((errorBody, sink) -> {
                                        sink.error(new NotFoundException(errorBody));
                                    }))
                    .onStatus(status -> status == HttpStatus.CONFLICT, response ->
                            response.bodyToMono(String.class)
                                    .handle((errorBody, sink) -> {
                                        sink.error(new ConflictException(errorBody));
                                    }))
                    .onStatus(status -> status == HttpStatus.FORBIDDEN, response ->
                            response.bodyToMono(String.class)
                                    .handle((errorBody, sink) -> {
                                        sink.error(new ForbiddenException(errorBody));
                                    }))
                    .bodyToMono(CommentDto.class)
                    .block();
    }


    public CommentDto updateComment(Long userId, Long eventId, Long commentId, UpdateCommentRequest request) {
        return webClient.patch()
                .uri("/" + userId + "/events/" + eventId + "/comments/" + commentId)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .onStatus(status -> status == HttpStatus.NOT_FOUND, response ->
                        response.bodyToMono(String.class)
                                .handle((errorBody, sink) -> {
                                    sink.error(new NotFoundException(errorBody));
                                }))
                .onStatus(status -> status == HttpStatus.FORBIDDEN, response ->
                        response.bodyToMono(String.class)
                                .handle((errorBody, sink) -> {
                                    sink.error(new ForbiddenException(errorBody));
                                }))
                .onStatus(status -> status == HttpStatus.CONFLICT, response ->
                        response.bodyToMono(String.class)
                                .handle((errorBody, sink) -> {
                                    sink.error(new ConflictException(errorBody));
                                }))
                .bodyToMono(CommentDto.class)
                .block();
    }


    public CommentDto replyToComment(Long userId, Long eventId, Long commentId, NewCommentRequest request) {
        return webClient.post()
                .uri("/" + userId + "/events/" + eventId + "/comments/" + commentId)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .onStatus(status -> status == HttpStatus.NOT_FOUND, response ->
                        response.bodyToMono(String.class)
                                .handle((errorBody, sink) -> {
                                    sink.error(new NotFoundException(errorBody));
                                }))
                .onStatus(statuse -> statuse == HttpStatus.CONFLICT, response ->
                        response.bodyToMono(String.class)
                                .handle((errorBody, sink) -> {
                                    sink.error(new ForbiddenException(errorBody));
                                }))
                .bodyToMono(CommentDto.class)
                .block();
    }

    public Flux<CommentDto> getUserComments(Long userId, CommentsShowingParam param, Integer from, Integer size) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/" + userId +"/comments")
                        .queryParam("param", param)
                        .queryParam("from", from)
                        .queryParam("size", size)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(status -> status == HttpStatus.NOT_FOUND, response ->
                        response.bodyToMono(String.class)
                                .handle((errorBody, sink) -> {
                                    sink.error(new NotFoundException(errorBody));
                                }))
                .bodyToFlux(CommentDto.class);
    }


}
