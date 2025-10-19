package ru.practicum.hit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class StatsInternalController {
    private final StatsService statsService;

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public void addHit(@RequestBody NewHitRequest request) {
        statsService.addHit(request);
    }

    @GetMapping("/stats")
    public List<ViewStatsDto> stats(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
            @RequestParam(required = false) List<String> uris,
            @RequestParam(defaultValue = "false") boolean unique
    ) throws BadRequestException {

        log.info("URIs parameter: {}", uris);           // ← что здесь?
        log.info("URIs class: {}", uris != null ? uris.getClass() : "null");
        log.info("URIs size: {}", uris != null ? uris.size() : 0);

        if (end != null && end.isBefore(start)) {
            throw new BadRequestException("rangeEnd is before rangeStart");
        }

        return statsService.getStats(start, end, uris, unique);
    }

}
