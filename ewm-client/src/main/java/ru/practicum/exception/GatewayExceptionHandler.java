package ru.practicum.exception;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.format.DateTimeFormatter;

@RestControllerAdvice
@Slf4j
public class GatewayExceptionHandler {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Violation handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        String field = ex.getBindingResult().getFieldError().getField();
        String message = ex.getBindingResult().getFieldError().getDefaultMessage();
        Object rejectedValue = ex.getBindingResult().getFieldError().getRejectedValue();

        String errorMessage = String.format("Field: %s. Error: %s. Value: %s",
                field, message, rejectedValue == null ? "null" : rejectedValue.toString());

        log.warn("Validation error: {} ", errorMessage);

        return new Violation(
                HttpStatus.BAD_REQUEST,
                "Incorrectly made request.",
                errorMessage
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Violation handleConstraintViolationException(ConstraintViolationException ex) {
        var violation = ex.getConstraintViolations().iterator().next();

        String field = violation.getPropertyPath().toString();
        String message = violation.getMessage();
        Object invalidValue = violation.getInvalidValue();

        String errorMessage = String.format("Field: %s. Error: %s. Value: %s",
                field, message, invalidValue == null ? "null" : invalidValue.toString());

        return new Violation(
                HttpStatus.BAD_REQUEST,
                "Incorrectly made request.",
                errorMessage
        );

    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Violation handleValidationException(ValidationException ex) {
        return new Violation(
                HttpStatus.BAD_REQUEST,
                "Incorrectly made request.",
                ex.getMessage()
        );
    }

    @ExceptionHandler(DataConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Violation handleDataConflictException(DataConflictException ex) {
        log.warn("Data conflict: {}", ex.getMessage());

        return new Violation(
                HttpStatus.CONFLICT,
                "Integrity constraint has been violated.",
                ex.getMessage()
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Violation handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        log.warn("Method argument type mismatch: {}", ex.getMessage());

        return new Violation(
                HttpStatus.BAD_REQUEST,
                "Incorrectly made request.",
                ex.getMessage()
        );
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Violation handleNotFoundException(NotFoundException ex) {
        log.warn("Not found: {}", ex.getMessage());

        return new Violation(
                HttpStatus.NOT_FOUND,
                "The required object was not found.",
                ex.getMessage()
        );
    }

    @ExceptionHandler(EventDataException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Violation handleEventDataException(EventDataException ex) {
        log.warn("Forbidden: {}", ex.getMessage());

        return new Violation(
                HttpStatus.FORBIDDEN,
                "For the requested operation the conditions are not met.",
                ex.getMessage()
        );
    }

    @ExceptionHandler(CategoryConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Violation handleCategoryConflictException(CategoryConflictException ex) {
        log.warn("Category conflict: {}", ex.getMessage());

        return new Violation(
                HttpStatus.CONFLICT,
                "For the requested operation the conditions are not met.",
                ex.getMessage()
        );
    }

    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Violation handleForbiddenException(ForbiddenException ex) {
        log.warn("Forbidden: {}", ex.getMessage());

        return new Violation(
                HttpStatus.FORBIDDEN,
                "For the requested operation the conditions are not met.",
                ex.getMessage()
        );
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Violation handleConflictException(ConflictException ex) {
        log.warn("Conflict: {}", ex.getMessage());

        return new Violation(
                HttpStatus.CONFLICT,
                "For the requested operation the conditions are not met.",
                ex.getMessage()
        );
    }

}
