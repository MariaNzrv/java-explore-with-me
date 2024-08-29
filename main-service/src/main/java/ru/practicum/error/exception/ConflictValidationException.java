package ru.practicum.error.exception;

public class ConflictValidationException extends RuntimeException {
    public ConflictValidationException(String message) {
        super(message);
    }
}
