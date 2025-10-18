package ru.practicum.stats;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriBuilder;
import ru.practicum.base.BaseWebClient;
import ru.practicum.hit.NewHitRequest;
import ru.practicum.hit.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class StatsWebClient extends BaseWebClient {
    private static final String API_PREFIX = "";

    public StatsWebClient(@Value("${stats-server.url}") String serverUrl) {
        super(serverUrl, API_PREFIX);
    }

    public ResponseEntity<Void> addHit(NewHitRequest request) {
        return webClient.post()
                .uri("/" + "hit")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .toBodilessEntity()
                .doOnSuccess(response -> {
                    log.info("Hit saved successfully");
                })
                .doOnError(throwable -> {
                    log.error("Hit save failed : {}", throwable.getMessage());
                })
                .block();
    }

    public List<ViewStatsDto> getStats(LocalDateTime start,
                                       LocalDateTime end,
                                       List<String> uris,
                                       boolean unique) {
        return webClient.get()
                .uri(uriBuilder -> {
                    UriBuilder builder = uriBuilder
                            .path("/stats")
                            .queryParam("start", start)
                            .queryParam("end", end)
                            .queryParam("unique", unique);

                    // Передаем каждый URI как отдельный параметр
                    if (uris != null && !uris.isEmpty()) {
                        uris.forEach(uri -> builder.queryParam("uris", uri));
                    }
                    return builder.build();
                })
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .bodyToFlux(ViewStatsDto.class)
                .collectList()
                .block();
    }
}
