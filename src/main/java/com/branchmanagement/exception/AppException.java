package com.branchmanagement.exception;

import org.springframework.http.HttpStatus;
import lombok.Getter;

/**
 * Custom exception for application-level business errors.
 */
@Getter
public class AppException extends RuntimeException {
    private final HttpStatus status;

    public AppException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }

    public AppException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
