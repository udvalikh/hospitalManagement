package com.techelevator.exception;

/** Mapped to HTTP 409, e.g. a username that's taken or a slot that's already booked. */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
