package com.techelevator.exception;

/** Mapped to HTTP 400 for business-rule violations that bean validation can't express. */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
