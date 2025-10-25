package ru.practicum.exception;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ErrorMessageExtractor {

    public static String extractErrorMessage(String errorBody) {
        if (errorBody != null && !errorBody.trim().isEmpty()) {
            return errorBody;
        }
        return "Unknown error occurred";
    }
}
