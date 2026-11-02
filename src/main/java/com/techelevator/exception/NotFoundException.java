package com.techelevator.exception;

/** Mapped to HTTP 404 by {@link com.techelevator.controller.GlobalExceptionHandler}. */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
