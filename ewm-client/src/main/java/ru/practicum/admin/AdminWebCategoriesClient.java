package ru.practicum.admin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import ru.practicum.base.BaseWebClient;
import ru.practicum.categories.CategoryDto;
import ru.practicum.categories.NewCategoryRequest;
import ru.practicum.categories.UpdateCategoryRequest;
import ru.practicum.exception.CategoryConflictException;
import ru.practicum.exception.DataConflictException;
import ru.practicum.exception.NotFoundException;

@Service
@Slf4j
public class AdminWebCategoriesClient extends BaseWebClient {
    private static final String API_PREFIX = "/admin/categories";

    public AdminWebCategoriesClient(@Value("${ewm-service.url}") String baseUrl) {
        super(baseUrl, API_PREFIX);
    }

    public CategoryDto addCategory(NewCategoryRequest request) {
        log.debug("Adding new category: " + request);
        return webClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .onStatus(status -> status == HttpStatus.CONFLICT,
                        response -> response.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new DataConflictException(
                                        "Conflict from service: " + errorBody
                                ))))
                .bodyToMono(CategoryDto.class)
                .doOnSuccess(response -> {
                    log.info("Successfully added a new category: " + request);
                })
                .block();
    }

    public void deleteCategory(Long id) {
        log.debug("Deleting category: " + id);
        try {
            webClient.delete()
                    .uri("/" + id)
                    .retrieve()
                    .toBodilessEntity()
                    .doOnSuccess(response -> {
                        log.info("Category deleted successfully: " + id);
                    })
                    .block();
        } catch (WebClientResponseException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new NotFoundException("Category with id=" + id + " was not found");
            } else if (ex.getStatusCode() == HttpStatus.CONFLICT) {
                throw new CategoryConflictException("The category is not empty");
            }
            throw ex;
        }
    }

    public CategoryDto updateCategory(Long id, UpdateCategoryRequest request) {
        log.debug("Updating category: " + id);
        try {
            return webClient.patch()
                    .uri("/" + id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(CategoryDto.class)
                    .doOnSuccess(response -> {
                        log.info("Category updated successfully: " + id);
                    })
                    .block();
        } catch (WebClientResponseException ex) {
            if (ex.getStatusCode() == HttpStatus.CONFLICT) {
                throw new DataConflictException(ex.getResponseBodyAsString());
            } else if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new NotFoundException(ex.getResponseBodyAsString());
            }
            throw ex;
        }
    }
}
