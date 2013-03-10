package com.jetbrains.crucible.connection.exceptions;

/**
 * User : ktisha
 */
public class RemoteApiException extends Exception {

  public RemoteApiException(String message) {
    super(message);
  }

  public RemoteApiException(String message, Throwable throwable) {
    super(message, throwable);
  }
}