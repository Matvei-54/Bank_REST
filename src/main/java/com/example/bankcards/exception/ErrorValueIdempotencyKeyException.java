package com.example.bankcards.exception;

public class ErrorValueIdempotencyKeyException extends RuntimeException {

    public ErrorValueIdempotencyKeyException() {
        super("Incorrect idempotency key");
    }
}
