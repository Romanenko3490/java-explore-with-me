package ru.practicum.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class ServiceExceptionHandler {

    @ExceptionHandler(DataConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleDataConflictException(DataConflictException ex) {
        log.warn("Data conflict in service: {}", ex.getMessage());
        return ex.getMessage();
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFoundException(NotFoundException ex) {
        log.warn("Not found in service: {}", ex.getMessage());
        return ex.getMessage();
    }

    @ExceptionHandler(EventDataException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleEventDataException(EventDataException ex) {
        log.warn("Forbidden in service: {}", ex.getMessage());
        return ex.getMessage();
    }

    @ExceptionHandler(CategoryConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public  String handleCategoryConflictException(CategoryConflictException ex) {
        log.warn("Category conflict in service: {}", ex.getMessage());
        return ex.getMessage();
    }

    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleEventUpdateForbiddenException(ForbiddenException ex) {
        log.warn("Forbidden in service: {}", ex.getMessage());
        return ex.getMessage();
    }

}
