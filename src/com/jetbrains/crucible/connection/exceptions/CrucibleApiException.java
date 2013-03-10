package com.jetbrains.crucible.connection.exceptions;

/**
 * User : ktisha
 */
public class CrucibleApiException extends Exception {

  public CrucibleApiException(String message) {
    super(message);
  }

  public CrucibleApiException(String message, Throwable throwable) {
    super(message, throwable);
  }
}