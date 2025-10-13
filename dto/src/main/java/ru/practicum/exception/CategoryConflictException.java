package ru.practicum.exception;

public class CategoryConflictException extends RuntimeException {
    public CategoryConflictException(String message) {
        super(message);
    }
}
