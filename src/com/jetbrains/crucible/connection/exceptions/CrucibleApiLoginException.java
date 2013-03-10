package com.jetbrains.crucible.connection.exceptions;

/**
 * User : ktisha
 */
public class CrucibleApiLoginException extends CrucibleApiException {

  public CrucibleApiLoginException(String message) {
    super(message);
  }

  public CrucibleApiLoginException(String message, Throwable throwable) {
    super(message, throwable);
  }
}