package ru.practicum.base;

import org.springframework.web.reactive.function.client.WebClient;

public class BaseWebClient {
    protected final WebClient webClient;


    public BaseWebClient(String baseUrl, String apiPrefix) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl + apiPrefix)
                .build();
    }
}
