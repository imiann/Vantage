package com.vantage.api.dto;

import java.time.LocalDateTime;

/**
 * Standard error response body returned by the global exception handler.
 */
public record ErrorResponse(
        int status,
        String message,
        LocalDateTime timestamp) {
    public static ErrorResponse of(int status, String message) {
        return new ErrorResponse(status, message, LocalDateTime.now());
    }
}
