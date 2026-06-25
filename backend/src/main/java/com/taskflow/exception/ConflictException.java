package com.taskflow.exception;

import java.util.Map;

/**
 * Thrown when a request conflicts with the current state of a resource.
 * Examples: duplicate email registration, optimistic locking version mismatch.
 * Carries an arbitrary body so each call site can include extra fields
 * (e.g. {@code currentVersion} for the optimistic lock response).
 */
public class ConflictException extends RuntimeException {

    private final Map<String, Object> body;

    public ConflictException(String message) {
        super(message);
        this.body = Map.of("error", message);
    }

    public ConflictException(String message, Map<String, Object> body) {
        super(message);
        this.body = body;
    }

    public Map<String, Object> getBody() {
        return body;
    }
}
