package ru.practicum.exception;

public class EntityNotFoundException extends RuntimeException {
  public EntityNotFoundException(String s) {
    super(s);
  }
}
