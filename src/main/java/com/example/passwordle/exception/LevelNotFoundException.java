package com.example.passwordle.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a requested level cannot be found.
 * The @ResponseStatus automatically translates this to a 404 Not Found HTTP
 * response.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class LevelNotFoundException extends RuntimeException {

    public LevelNotFoundException(String message) {
        super(message);
    }

    public LevelNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
