package ru.practicum.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class ViolationsErrorResponse {
    private final List<Violation> error;
}
