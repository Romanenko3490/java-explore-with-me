package ru.practicum.hit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatsService {
    private final HitRepository hitRepository;

    public void addHit(NewHitRequest request) {
        log.debug("Adding a new hit to stats service");
        Hit hit = Hit.builder()
                .app(request.getApp())
                .uri(request.getUri())
                .ip(request.getIp())
                .timestamp(request.getTimestamp())
                .build();
        log.debug("Hit to save : {}", hit);
        hitRepository.save(hit);
    }

    public List<ViewStatsDto> getStats(LocalDateTime start,
                                       LocalDateTime end,
                                       List<String> uris,
                                       boolean unique) {
        log.debug("Getting stats for start: {}, end: {}, uris: {}, unique: {}", start, end, uris, unique);

        List<Object[]> results;

        if (uris == null || uris.isEmpty()) {
            if (unique) {
                results = hitRepository.findUniqueStatsWithoutUris(start, end);
            } else {
                results = hitRepository.findAllStatsWithoutUris(start, end);
            }
        } else {
            if (unique) {
                results = hitRepository.findUniqueStatsWithUris(start, end, uris);
            } else {
                results = hitRepository.findAllStatsWithUris(start, end, uris);
            }
        }
        log.debug("results: {}", results);

        return results.stream()
                .map(StatsMapper::objToViewStats)
                .collect(Collectors.toList());
    }


}
