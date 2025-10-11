package ru.practicum.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Value
@RequiredArgsConstructor
@Jacksonized
@Builder
public class Violation {
    HttpStatus status;
    String reason;
    String message;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime timestamp =  LocalDateTime.now();
}
