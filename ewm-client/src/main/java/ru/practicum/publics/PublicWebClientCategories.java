package ru.practicum.publics;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.practicum.base.BaseWebClient;
import ru.practicum.categories.CategoryDto;
import ru.practicum.exception.NotFoundException;

import java.util.List;

@Service
@Slf4j
public class PublicWebClientCategories extends BaseWebClient {
    private final static String CATEGORIES_API = "/categories";

    public PublicWebClientCategories(@Value("${ewm-service.url}") String baseUrl) {
        super(baseUrl, CATEGORIES_API);
    }

    public Mono<List<CategoryDto>> getCategories(Integer from, Integer size) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("from", from)
                        .queryParam("size", size)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(CategoryDto.class)
                .collectList();

    }

    public CategoryDto getCategory(Long catId) {
        return webClient.get()
                .uri("/" + catId)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(status -> status == HttpStatus.NOT_FOUND,
                        response -> {
                    throw  new NotFoundException("Category with id=" + catId + " was not found");
                        })
                .bodyToMono(CategoryDto.class)
                .block();
    }


}
