package com.vantage.api.exception;

/**
 * Thrown when a requested entity cannot be found.
 * Mapped to 404 Not Found by the global exception handler.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, Object id) {
        super(resourceName + " not found with id " + id);
    }
}
