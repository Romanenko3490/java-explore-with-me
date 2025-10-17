package ru.practicum.admin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import ru.practicum.base.BaseWebClient;
import ru.practicum.compilations.CompilationDto;
import ru.practicum.compilations.NewCompilationRequest;
import ru.practicum.compilations.UpdateCompilationRequest;
import ru.practicum.exception.DataConflictException;
import ru.practicum.exception.NotFoundException;


@Service
@Slf4j
public class AdminCompilationClient extends BaseWebClient {
    private static final String API_PREFIX = "/admin/compilations";

    public AdminCompilationClient(@Value("${ewm-service.url}") String baseUrl) {
        super(baseUrl, API_PREFIX);
    }


    public CompilationDto addCompilation(NewCompilationRequest request) {
        log.info("Post request to compilation: {}", request);
        try {
            return webClient.post()
                    .accept(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(CompilationDto.class)
                    .doOnSuccess(response -> {
                        log.info("Added compilation: {}", response);
                    })
                    .block();
        } catch (WebClientResponseException ex) {
            if (ex.getStatusCode() == HttpStatus.CONFLICT) {
                throw new DataConflictException(ex.getResponseBodyAsString());
            }
            throw ex;
        }
    }

    public void deleteCompilation(Long compId) {
        webClient.delete()
                .uri("/" + compId)
                .retrieve()
                .onStatus(status -> status == HttpStatus.NOT_FOUND,
                        response -> {
                            throw new NotFoundException("Compilation with id=" + compId + " was not found");
                        })
                .toBodilessEntity()
                .doOnSuccess(response -> {
                    log.info("Deleted compilation: {}", response);
                })
                .block();

    }

    public CompilationDto getCompilation(Long compId) {
        return webClient.get()
                .uri("/" + compId)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(status -> status == HttpStatus.NOT_FOUND,
                        response -> {
                            throw new NotFoundException("Compilation with id=" + compId + " was not found");
                        })
                .bodyToMono(CompilationDto.class)
                .block();
    }

    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest request) {
        return webClient.patch()
                .uri("/" + compId)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .onStatus(status -> status == HttpStatus.NOT_FOUND,
                        response -> {
                            throw new NotFoundException("Compilation with id=" + compId + " was not found");
                        })
                .onStatus(status -> status == HttpStatus.CONFLICT,
                        response -> {
                            throw new DataConflictException("could not execute statement; " +
                                    "SQL [n/a]; constraint" + request.getTitle() +
                                    "; nested exception is org.hibernate.exception.ConstraintViolationException:" +
                                    " could not execute statement");
                        })
                .bodyToMono(CompilationDto.class)
                .doOnSuccess(response -> {
                    log.info("Updated compilation: {}", response);
                })
                .block();
    }
}
