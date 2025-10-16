package ru.practicum.publics;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import reactor.core.publisher.Mono;
import ru.practicum.base.BaseWebClient;
import ru.practicum.compilations.CompilationDto;
import ru.practicum.exception.NotFoundException;

import java.util.List;

@Service
@Slf4j
public class PublicWebClientCompilations extends BaseWebClient {
    private final static String COMP_API = "/compilations";

    public PublicWebClientCompilations(@Value("${ewm-service.url}") String baseUrl) {
        super(baseUrl, COMP_API);
    }

    public Mono<List<CompilationDto>> getCompilations(Integer from, Integer size) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("from", from)
                        .queryParam("size", size)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(CompilationDto.class)
                .collectList();
    }

    public CompilationDto getCompilationById(Integer compId) {
        return webClient.get()
                .uri("/" + compId)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(status ->  status == HttpStatus.NOT_FOUND,
                        response -> {
                    throw new NotFoundException("Compilation with id " + compId + " not found");
                        })
                .bodyToMono(CompilationDto.class)
                .block();
    }

}
