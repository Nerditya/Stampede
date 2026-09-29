package com.stampede.exception;

/** Thrown when registering with an email that is already taken. */
public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}
