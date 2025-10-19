package ru.practicum.clints;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import ru.practicum.hit.NewHitRequest;


@Slf4j
@Component
public class StatsClient {
    protected final WebClient webClient;

    public StatsClient(@Value("${stats-server.url}") String statsUrl) {
        this.webClient = WebClient.create(statsUrl);
    }

    public void addHit(NewHitRequest request) {
        try {
            webClient.post()
                    .uri("/hit")
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
        } catch (Exception e) {
            log.error("Failed to send hit to stats service", e);
        }
    }
}
