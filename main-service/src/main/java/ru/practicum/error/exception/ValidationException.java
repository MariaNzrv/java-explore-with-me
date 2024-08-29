package ru.practicum.error.exception;

public class ValidationException extends RuntimeException {
    public ValidationException(String s) {
        super(s);
    }
}
