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
import ru.practicum.exception.DataConflictException;


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
}
