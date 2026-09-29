package com.stampede.exception;

/** Thrown when a requested product has insufficient stock or does not exist. */
public class OutOfStockException extends RuntimeException {
    public OutOfStockException(String message) {
        super(message);
    }
}
